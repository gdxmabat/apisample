package com.sabadellzurich.olimpo.renewal.controller;

import com.sabadellzurich.olimpo.renewal.common.lib.dto.PacketDTO;
import com.sabadellzurich.olimpo.renewal.common.lib.dto.PacketFilterRequest;
import com.sabadellzurich.olimpo.renewal.common.lib.dto.PacketStatus;
import com.sabadellzurich.olimpo.renewal.common.lib.model.RenewalCommunicationItem;
import com.sabadellzurich.olimpo.renewal.dto.FullPacketDTO;
import com.sabadellzurich.olimpo.renewal.dto.ValidatePacketCommunicationsRequestDTO;
import com.sabadellzurich.olimpo.renewal.service.PacketsService;
import com.sabadellzurich.olimpo.renewal.util.PageUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Packets controller.
 * ===================
 * <p>
 * REST Controller for the /packets endpoint.
 *
 * @author msellers
 */
@RestController
@RequestMapping("/packets")
@Slf4j
@Api(description = "Controller for actions related to a packet of items.")
public class PacketsController {
    @Autowired
    private PacketsService service;

    /**
     * Returns a list of the packets.
     *
     * @return List of filtered packets.
     */
    @GetMapping
    @ApiOperation(value = "${swagger.packets.listPackets.desc}")
    public Page<PacketDTO> listPackets(
            @ApiParam("${swagger.packets.listPackets.state}")
            @RequestParam(required = false) List<PacketStatus> state,
            @ApiParam("${swagger.packets.listPackets.fileName}")
            @RequestParam(required = false) String fileName,
            @ApiParam("${swagger.packets.listPackets.createdAtStart}")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam(required = false) LocalDateTime createdAtStart,
            @ApiParam("${swagger.packets.listPackets.createdAtEnd}")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam(required = false) LocalDateTime createdAtEnd,
            @ApiParam("${swagger.packets.listPackets.renovationDateStart}")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam(required = false) LocalDateTime renovationDateStart,
            @ApiParam("${swagger.packets.listPackets.renovationDateEnd}")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam(required = false) LocalDateTime renovationDateEnd,
            @ApiParam("${swagger.packets.listPackets.open}")
            @RequestParam(required = false) Boolean open,
            Pageable page
    ) {
        log.info("GET /packets");

        return PageUtil.createPageFromList(
                service.findAll(
                        PacketFilterRequest.builder()
                                .fileName(fileName)
                                .state(state)
                                .createdAtEnd(createdAtEnd)
                                .createdAtStart(createdAtStart)
                                .renovationDateEnd(renovationDateEnd)
                                .renovationDateStart(renovationDateStart)
                                .open(open)
                                .build()
                ),
                page
        );
    }

    /**
     * Returns a packet.
     *
     * @param id Packet id.
     * @return Packet with the given id.
     */
    @GetMapping("/{id:[0-9]+}")
    @ApiOperation(value = "${swagger.packets.getPacket.desc}")
    public FullPacketDTO getPacket(@ApiParam("${swagger.packets.getPacket.id}") @PathVariable("id") long id) {
        log.info("GET /packets/{id}: " + id);

        return service.findById(id);
    }

    /**
     * Validates the communication state for the given items.
     *
     * @param body Items to validate.
     * @return Validated items.
     */
    @PostMapping("/validateCommunications")
    @ApiOperation("${swagger.items.validateCommunications.desc}")
    public List<RenewalCommunicationItem> validateCommunications(
            @ApiParam("${swagger.items.validateCommunications.items}")
            @RequestBody ValidatePacketCommunicationsRequestDTO body
    ) {
        log.info("POST /items/validateCommunications");

        return service.validateCommunications(body.getPacketIds(), body.isValid());
    }

    /**
     * Closes the given packet
     *
     * @param id Packet to close.
     * @return Closed packet.
     */
    @PostMapping("/close/{id}")
    @ApiOperation("${swagger.packets.close.desc}")
    public FullPacketDTO close(
            @ApiParam("${swagger.packets.close.id}")
            @PathVariable Long id
    ) {
        log.info("POST /packets/close/{id}: " + id);

        return service.close(id);
    }

    /**
     * Exports the given packet.
     *
     * @param id Packet to export.
     * @return CSV with the exported packet.
     */
    @GetMapping("/export/{id}")
    @ApiOperation("${swagger.packets.export.desc}")
    public void export(
            @ApiParam("${swagger.packets.export.id}")
            @PathVariable Long id,
            HttpServletResponse response
    ) {
        log.info("POST /packets/export/{id}: " + id);

        service.export(id, response);
    }
}
