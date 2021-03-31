package com.sabadellzurich.olimpo.renewal.service;

import com.sabadellzurich.olimpo.renewal.common.lib.dto.*;
import com.sabadellzurich.olimpo.renewal.common.lib.enumstatus.ProcessRecomhFileStatusEnum;
import com.sabadellzurich.olimpo.renewal.common.lib.enumstatus.ProcessRecomhItemStatusEnum;
import com.sabadellzurich.olimpo.renewal.common.lib.model.CommunicationItemLog;
import com.sabadellzurich.olimpo.renewal.common.lib.model.RenewalCommunicationItem;
import com.sabadellzurich.olimpo.renewal.common.lib.model.RenewalCommunicationPacket;
import com.sabadellzurich.olimpo.renewal.common.lib.repository.PacketTransactionRepository;
import com.sabadellzurich.olimpo.renewal.common.lib.repository.RenewalCommunicationItemRepository;
import com.sabadellzurich.olimpo.renewal.common.lib.repository.RenewalCommunicationPacketRepository;
import com.sabadellzurich.olimpo.renewal.common.lib.services.RecomhService;
import com.sabadellzurich.olimpo.renewal.common.lib.services.util.CsvUtils;
import com.sabadellzurich.olimpo.renewal.dto.FullPacketDTO;
import com.sabadellzurich.olimpo.renewal.util.EventUtil;
import com.univocity.parsers.common.processor.BeanWriterProcessor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Packets service. ================
 * <p>
 * Service for the Packets controller.
 *
 * @author msellers
 */
@Service
@Slf4j
public class PacketsService {
    @Autowired
    private RenewalCommunicationPacketRepository repository;

    @Autowired
    private RenewalCommunicationItemRepository itemsRepository;

    @Autowired
    private PacketTransactionRepository transactionRepository;

    @Autowired
    private ModelMapper mapper;

    @Autowired
    private RecomhService recomhService;

    @Autowired
    private PacketStatsService packetStatsService;

    @Autowired
    private FilterService filterService;

    @Autowired
    private EntityManager entityManager;

    /**
     * Finds and returns all packets in the database.
     *
     * @param filter Packet filter.
     * @return All packets in the database.
     */
    public List<PacketDTO> findAll(PacketFilterRequest filter) {
        List<RenewalCommunicationPacket> ret;
        if (!StringUtils.isEmpty(filter.getFileName())) {
            ret = repository.findAllByFileNameLike("%" + filter.getFileName() + "%");
        } else {
            ret = repository.findAll();
        }

        return ret.stream().map(this::mapPacket).filter(p -> filter(p, filter)).collect(Collectors.toList());
    }

    /**
     * Finds and returns a packet by its id.
     *
     * @param id Packet id.
     * @return Packet with the given id.
     */
    public FullPacketDTO findById(long id) {
        return mapFullPacket(repository.findById(id).orElse(null));
    }

    /**
     * Performs the filtering on the given packet.
     *
     * @param p Packet to filter.
     * @param f Filter to pass.
     * @return Whether p passes f or not.
     */
    private boolean filter(PacketDTO p, PacketFilterRequest f) {
        // If packet last update is after f's date.
        if (f.getCreatedAtStart() != null && f.getCreatedAtStart().compareTo(p.getCreatedAt()) > 0) {
            return false;
        }

        // If packet last update is before f's date.
        if (f.getCreatedAtEnd() != null && f.getCreatedAtEnd().compareTo(p.getCreatedAt()) < 0) {
            return false;
        }

        // If packet renovation date is after f's date.
        if (f.getRenovationDateStart() != null && p.getRenovationDate() != null
                && f.getRenovationDateStart().compareTo(p.getRenovationDate()) > 0) {
            return false;
        }

        // If packet last update is before f's date.
        if (f.getRenovationDateEnd() != null && p.getRenovationDate() != null
                && f.getRenovationDateEnd().compareTo(p.getRenovationDate()) < 0) {
            return false;
        }

        // Open f.
        if (f.getOpen() != null) {
            // Open packets.
            if (f.getOpen() && p.getState() == ProcessRecomhFileStatusEnum.CLOSED) {
                return false;
            }

            // Closed packets
            if (!f.getOpen() && p.getState() != ProcessRecomhFileStatusEnum.CLOSED) {
                return false;
            }
        }

        // If packet state is on the selected f states.
        return filterState(f.getState(), p.getState());
    }

    /**
     * Filters the given state.
     *
     * @param filter Filter to pass.
     * @param state  State to filter.
     * @return Whether state passes the filter or not.
     */
    private boolean filterState(List<PacketStatus> filter, ProcessRecomhFileStatusEnum state) {
        if (CollectionUtils.isEmpty(filter)) {
            return true;
        }

        for (var filterState : filter) {
            if (state.ordinal() >= filterState.from() && state.ordinal() <= filterState.to()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Validates communication packets.
     *
     * @param packetIds Packets to validate.
     * @param isValid   Whether the packets are valid or not.
     * @return Validated items.
     */
    public List<RenewalCommunicationItem> validateCommunications(List<Long> packetIds, boolean isValid) {
        var ret = new LinkedList<RenewalCommunicationItem>();

        packetIds.forEach(i -> {
            var packet = this.repository.findById(i).orElse(null);
            if (packet == null) {
                return;
            }

            var items = this.itemsRepository.findAllByRenewalCommunicationPacketId(i);
            var logs = new ArrayList<CommunicationItemLog>(items.size());

            items.forEach(item -> {
                if (item.getState().ordinal() >= ProcessRecomhFileStatusEnum.BUSINESS_VALIDATION_OK.ordinal()) {
                    // Skip already validated items.
                    return;
                }

                item.setState(ProcessRecomhItemStatusEnum.BUSINESS_VALIDATION_NOK);

                if (isValid) {
                    item.setState(ProcessRecomhItemStatusEnum.BUSINESS_VALIDATION_OK);
                }

                logs.add(new CommunicationItemLog(item, item.getState().toString(), ""));
            });

            try {
                recomhService.auditTransaction(packet, ProcessRecomhItemStatusEnum.BUSINESS_VALIDATION_OK.toString());
                recomhService.saveItemsAudit(logs);
            } catch (Exception e) {
                log.warn("Couldn't save audit!", e);
            }

            ret.addAll(items);
        });

        return this.itemsRepository.saveAll(ret);
    }

    /**
     * Maps and returns a packet to its DTO.
     *
     * @param packet Packet to map.
     * @return Packet as a DTO.
     */
    private PacketDTO mapPacket(RenewalCommunicationPacket packet) {
        if (packet == null) {
            return null;
        }

        var dto = mapper.map(packet, PacketDTO.class);
        dto.setOpen(packet.getState() != ProcessRecomhFileStatusEnum.CLOSED);

        var last = itemsRepository.findMaxRenovationByPacket(packet, PageRequest.of(0, 1));
        if (last.size() > 0) {
            dto.setRenovationDate(LocalDateTime.of(last.get(0).getAnorenovacion(), last.get(0).getMesrenovacion(),
                    last.get(0).getDiarenovacion(), 0, 0));
        }

        for (var filterState : PacketStatus.values()) {
            if (packet.getState().ordinal() >= filterState.from() && packet.getState().ordinal() <= filterState.to()) {
                dto.setStateStr(filterState.str());
            }
        }

        // File ends with a line
        dto.setLineCount(dto.getLineCount() - 1);

        return dto;
    }

    /**
     * Maps and returns a packet to its full DTO.
     *
     * @param packet Packet to map.
     * @return Packet as a full DTO.
     */
    private FullPacketDTO mapFullPacket(RenewalCommunicationPacket packet) {
        if (packet == null) {
            return null;
        }

        var dto = mapper.map(mapPacket(packet), FullPacketDTO.class);

        packetStatsService.setStats(dto);

        var events = transactionRepository.findAllByRenewalCommunicationPacketId(packet.getId());
        events.forEach(i -> {
            var event = new Timeline();

            event.setDate(i.getCreatedAt());
            event.setText(EventUtil.getPacketEventUtil().parseEvent(i));
            dto.getEvents().add(event);
        });

        return dto;
    }

    @SneakyThrows
    public FullPacketDTO close(Long packetId) {
        var packet = repository.findById(packetId).orElse(null);
        if (packet == null) {
            return null;
        }

        recomhService.auditTransaction(packet, ProcessRecomhFileStatusEnum.CLOSED.toString());

        return mapFullPacket(packet);
    }

    @SneakyThrows
    @Transactional(readOnly = true)
    public void export(Long id, HttpServletResponse response) {
        var items = filterService.items(id);

        log.info("Writing csv...");
        response.setStatus(200);
        response.setHeader("Content-Type", "text/csv; charset=utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"Proceso_Comunicacion_" + id + ".csv\"");
        var out = response.getWriter();
        out.write(parseCsv(null));

        items.peek(entityManager::detach)
                .map(this::parseCsv)
                .forEach(out::write);

        out.flush();
        out.close();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private String parseCsv(RenewalCommunicationItem item) {
        try {
            if (item == null) {
                return CsvUtils.convertToCsv(
                        new ArrayList(),
                        WhiteCoCSVDTO.FIELDS_ORDER,
                        null,
                        new BeanWriterProcessor<>(WhiteCoCSVDTO.class)
                );
            }

            var arr = new ArrayList<WhiteCoCSVDTO>(1);
            arr.add(new WhiteCoCSVDTO(item));

            return CsvUtils.convertToCsv(
                    arr,
                    WhiteCoCSVDTO.FIELDS_ORDER,
                    null,
                    new BeanWriterProcessor<>(WhiteCoCSVDTO.class),
                    false
            );
        } catch (Exception e) {
            log.warn("Couldn't export item", e);

            return "";
        }
    }
}
