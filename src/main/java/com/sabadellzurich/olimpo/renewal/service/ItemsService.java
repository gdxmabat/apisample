package com.sabadellzurich.olimpo.renewal.service;

import com.sabadellzurich.olimpo.renewal.common.lib.dto.*;
import com.sabadellzurich.olimpo.renewal.common.lib.enumstatus.ProcessRecomhItemStatusEnum;
import com.sabadellzurich.olimpo.renewal.common.lib.model.CommunicationItemLog;
import com.sabadellzurich.olimpo.renewal.common.lib.model.RenewalCommunicationItem;
import com.sabadellzurich.olimpo.renewal.common.lib.repository.ItemTransactionRepository;
import com.sabadellzurich.olimpo.renewal.common.lib.repository.RenewalCommunicationItemRepository;
import com.sabadellzurich.olimpo.renewal.common.lib.services.RecomhService;
import com.sabadellzurich.olimpo.renewal.common.lib.services.util.CsvUtils;
import com.sabadellzurich.olimpo.renewal.dto.FullItemDTO;
import com.sabadellzurich.olimpo.renewal.enumstatus.ChannelAssignedEnum;
import com.sabadellzurich.olimpo.renewal.enumstatus.CheckerStatusEnum;
import com.sabadellzurich.olimpo.renewal.util.EventUtil;
import com.univocity.parsers.common.processor.BeanWriterProcessor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Items service.
 * ==============
 * <p>
 * Service for the Items controller.
 *
 * @author msellers
 */
@Service
@Slf4j
public class ItemsService {
	@Autowired
	private RenewalCommunicationItemRepository repository;

	@Autowired
	private ItemTransactionRepository transactionRepository;

	@Autowired
	private ModelMapper mapper;

	@Autowired
	private RecomhService recomhService;
	
	@Autowired
	private FilterService filterService;
	
	@Autowired
	private EntityManager entityManager;

	/**
	 * Finds and returns all items in the database.
	 *
	 * @param filter Item filter.
	 *
	 * @return All items in the database.
	 */
	public Page<ItemDTO> findAll(ItemFilterRequest filter, Pageable page) {
		log.info("findAll");
		
		var pg = filterService.items(filter, page);
		var items = pg.stream()
				.map(this::mapItem)
				.collect(Collectors.toList());
		
		return new PageImpl<>(items, page, pg.getTotalElements());
	}

	/**
	 * Finds and returns all items in the database.
	 *
	 * @param filter Item filter.
	 *
	 * @return All items in the database.
	 */
	public Page<FullItemDTO> findAllAsFullItemDTO(ItemFilterRequest filter, Pageable page) {
		log.info("findAllAsFullItemDTO");
		
		var pg = filterService.items(filter, page);
		var items = pg.stream()
				.map(this::mapFullItem)
				.collect(Collectors.toList());
		
		return new PageImpl<>(items, page, pg.getTotalElements());
	}

	/**
	 * Maps an Item model to its DTO instance.
	 *
	 * @param item Item to map.
	 *
	 * @return Mapped item.
	 */
	private ItemDTO mapItem(RenewalCommunicationItem item) {
		var dto = mapper.map(item, ItemDTO.class);
		dto.setChannelAssignedStr(ChannelAssignedEnum.valueOf(dto.getChannelAssigned()).str());
		dto.setRenovationDate(
				LocalDateTime.of(item.getAnorenovacion(), item.getMesrenovacion(), item.getDiarenovacion(), 0, 0));

		for (var filterState : ItemStatus.values()) {
			if (item.getState().ordinal() >= filterState.from() && item.getState().ordinal() <= filterState.to()) {
				dto.setStateStr(filterState.str());
			}
		}

		if (item.isMuestra()) {
			dto.setValidationStr("Muestra");
		} else if (item.getState().equals(ProcessRecomhItemStatusEnum.BUSINESS_VALIDATION_OK)) {
			dto.setValidationStr("OK");
		} else if (item.getState().equals(ProcessRecomhItemStatusEnum.BUSINESS_VALIDATION_NOK)) {
			dto.setValidationStr("KO");
		} else if (item.getState().ordinal() < ProcessRecomhItemStatusEnum.BUSINESS_VALIDATION_OK.ordinal()) {
			dto.setValidationStr("Sin validar");
		} else if (item.getState().equals(ProcessRecomhItemStatusEnum.COMMUNICATION_SENT)) {
			dto.setValidationStr("Cerrado");
		}

		if (!StringUtils.isEmpty(dto.getPhoneCheckerResult())) {
			dto.setPhoneCheckerResultStr(CheckerStatusEnum.valueOf(dto.getPhoneCheckerResult()).str());
		}

		if (!StringUtils.isEmpty(dto.getEmailCheckerResult())) {
			dto.setEmailCheckerResultStr(CheckerStatusEnum.valueOf(dto.getEmailCheckerResult()).str());
		}

		return dto;
	}

	/**
	 * Maps an Item model to its full DTO instance.
	 *
	 * @param item Item to map.
	 *
	 * @return Mapped item.
	 */
	public FullItemDTO mapFullItem(RenewalCommunicationItem item) {
		var dto = mapper.map(this.mapItem(item), FullItemDTO.class);

		dto.setEvents(
				transactionRepository.findAllByRenewalCommunicationItemId(item.getId())
					.stream()
					.map(i -> {
						var event = new Timeline();
			
						event.setDate(i.getCreatedAt());
						event.setText(EventUtil.getItemEventUtil().parseEvent(i));
						event.setReason(i.getDescription());
			
						return event;
					})
					.collect(Collectors.toList())
		);
		
		return dto;
	}

	/**
	 * Finds and returns a item by its id.
	 *
	 * @param id Item id.
	 *
	 * @return Item with the given id.
	 */
	public FullItemDTO findById(long id) {
		return repository.findById(id).map(this::mapFullItem).orElse(null);
	}

	/**
	 * Finds and returns a list of item by its packet id.
	 *
	 * @param id Packet id.
	 *
	 * @return Items with the given packet id.
	 */
	public List<ItemDTO> findByPacketId(long id) {
		return repository.findAllByRenewalCommunicationPacketId(id).stream().map(this::mapItem)
				.collect(Collectors.toList());
	}

	/**
	 * Perform the communication validation by setting the item's state to
	 * BUSINESS_VALIDATION_(N)OK.
	 *
	 * @param itemIds Items to validate.
	 * @param isValid Whether the items are valid or not.
	 *
	 * @return Validated items.
	 */
	public List<RenewalCommunicationItem> validateCommunications(List<Long> itemIds, boolean isValid) {
		var items = this.repository.findAllById(itemIds);
		var logs = new ArrayList<CommunicationItemLog>(items.size());

		items.forEach(i -> {
			i.setState(ProcessRecomhItemStatusEnum.BUSINESS_VALIDATION_NOK);

			if (isValid) {
				i.setState(ProcessRecomhItemStatusEnum.BUSINESS_VALIDATION_OK);
			}

			logs.add(new CommunicationItemLog(i, i.getState().toString(), ""));
		});

		try {
			recomhService.saveItemsAudit(logs);
		} catch (Exception e) {
			log.warn("Couldn't save audits!", e);
		}

		return this.repository.saveAll(items);
	}

	@SneakyThrows
	public void export(ItemFilterRequest filter, HttpServletResponse response) {
		var items = filterService.items(filter);

		log.info("Writing csv...");
		response.setStatus(200);
		response.setHeader("Content-Type", "text/csv; charset=utf-8");
		response.setHeader("Content-Disposition", "attachment; filename=\"Comunicaciones_Individuales.csv\"");
		var out = response.getWriter();
		out.write(parseCsv(null));
		items.peek(i -> out.write(parseCsv(i)))
				.forEach(entityManager::detach);
		out.flush();
		out.close();
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
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
		} catch(Exception e) {
			log.warn("Couldn't export item", e);
			
			return "";
		}
	}
}
