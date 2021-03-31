package com.sabadellzurich.olimpo.renewal.controller;

import java.time.LocalDateTime;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sabadellzurich.olimpo.renewal.common.lib.dto.ItemDTO;
import com.sabadellzurich.olimpo.renewal.common.lib.dto.ItemFilterRequest;
import com.sabadellzurich.olimpo.renewal.common.lib.dto.ItemStatus;
import com.sabadellzurich.olimpo.renewal.common.lib.model.RenewalCommunicationItem;
import com.sabadellzurich.olimpo.renewal.common.lib.model.ValidationOption;
import com.sabadellzurich.olimpo.renewal.dto.FullItemDTO;
import com.sabadellzurich.olimpo.renewal.dto.ValidateCommunicationsRequestDTO;
import com.sabadellzurich.olimpo.renewal.service.ItemsService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

/**
 * Items controller.
 * =================
 * <p>
 * REST Controller for the /items endpoint.
 *
 * @author msellers
 */
@RestController
@RequestMapping("/items")
@Slf4j
@Api(description = "Controller for actions related to items of a packet.")
public class ItemsController {
    @Autowired
    private ItemsService service;

    /**
     * Returns a list of the items.
     *
     * @return List of filtered items.
     */
    @ApiOperation(value = "${swagger.items.listItems.desc}")
    @GetMapping
    public Page<ItemDTO> listItems(
            @ApiParam("${swagger.items.listItems.fileName}")
            @RequestParam(required = false) String fileName,
            @ApiParam("${swagger.items.listItems.policy}")
            @RequestParam(required = false) String policy,
            @ApiParam("${swagger.items.listItems.fileName}")
            @RequestParam(required = false) String channel,
            @ApiParam("${swagger.items.listItems.segment}")
            @RequestParam(required = false) String segment,
            @ApiParam("${swagger.items.listItems.state}")
            @RequestParam(required = false) List<ItemStatus> state,
            @ApiParam("${swagger.items.listItems.validation}")
            @RequestParam(required = false) List<ValidationOption> validation,
            @ApiParam("${swagger.packets.listPackets.renovationDateStart}")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam(required = false) LocalDateTime renovationDateStart,
            @ApiParam("${swagger.packets.listPackets.renovationDateEnd}")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam(required = false) LocalDateTime renovationDateEnd,
            @ApiParam("${swagger.packets.listItems.packetId")
            @RequestParam(required = false) Long packetId,
            Pageable page
    ) {
        log.info("GET /items");
        
        return service.findAll(
                        ItemFilterRequest.builder()
                                .fileName(fileName)
                                .policy(policy)
                                .channel(channel)
                                .renovationDateEnd(renovationDateEnd)
                                .renovationDateStart(renovationDateStart)
                                .segment(segment)
                                .state(state)
                                .validation(validation)
                                .packetId(packetId)
                                .build(),
                        page
                );
    }

    /**
     * Returns a item.
     *
     * @param id Item id.
     *
     * @return Item with the given id.
     */
    @GetMapping("/{id:[0-9]+}")
    @ApiOperation("${swagger.items.getItem.desc}")
    public FullItemDTO getItem(@ApiParam("${swagger.items.getItem.id}") @PathVariable("id") long id) {
        log.info("GET /items/{id}: " + id);

        return service.findById(id);
    }

    /**
     * Validates the communication state for the given items.
     *
     * @param body Items to validate.
     *
     * @return Validated items.
     */
    @PostMapping("/validateCommunications")
    @ApiOperation("${swagger.items.validateCommunications.desc}")
    public List<RenewalCommunicationItem> validateCommunications(
            @ApiParam("${swagger.items.validateCommunications.items}")
            @RequestBody ValidateCommunicationsRequestDTO body
    ) {
        log.info("POST /items/validateCommunications");

        return service.validateCommunications(body.getItemIds(), body.isValid());
    }

    /**
     * Exports the given item.
     *
     * @param id Item to export.
     *
     * @return CSV with the exported packet.
     */
    @GetMapping("/export")
    @ApiOperation("${swagger.items.export.desc}")
    @Transactional(readOnly = true)
    public void export(
    		@ApiParam("${swagger.items.listItems.fileName}")
            @RequestParam(required = false) String fileName,
            @ApiParam("${swagger.items.listItems.policy}")
            @RequestParam(required = false) String policy,
            @ApiParam("${swagger.items.listItems.fileName}")
            @RequestParam(required = false) String channel,
            @ApiParam("${swagger.items.listItems.segment}")
            @RequestParam(required = false) String segment,
            @ApiParam("${swagger.items.listItems.state}")
            @RequestParam(required = false) List<ItemStatus> state,
            @ApiParam("${swagger.items.listItems.validation}")
            @RequestParam(required = false) List<ValidationOption> validation,
            @ApiParam("${swagger.packets.listPackets.renovationDateStart}")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam(required = false) LocalDateTime renovationDateStart,
            @ApiParam("${swagger.packets.listPackets.renovationDateEnd}")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam(required = false) LocalDateTime renovationDateEnd,
            @ApiParam("${swagger.packets.listItems.packetId")
            @RequestParam(required = false) Long packetId,
            HttpServletResponse response
    ) {
        log.info("GET /items/export");
            
        service.export(
        		ItemFilterRequest.builder()
	                .fileName(fileName)
	                .policy(policy)
	                .channel(channel)
	                .renovationDateEnd(renovationDateEnd)
	                .renovationDateStart(renovationDateStart)
	                .segment(segment)
	                .state(state)
	                .validation(validation)
	                .packetId(packetId)
	                .build(),
                response
        );
    }
}
