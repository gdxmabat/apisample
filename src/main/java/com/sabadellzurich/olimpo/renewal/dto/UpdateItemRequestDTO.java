package com.sabadellzurich.olimpo.renewal.dto;

import com.sabadellzurich.olimpo.renewal.common.lib.dto.ItemStatus;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Update packet request DTO.
 * ==========================
 *
 * Represents the body of the Update Packet request.
 *
 * @author msellers
 */
@Data
public class UpdateItemRequestDTO {
    @ApiModelProperty("${swagger.itemState}")
    private ItemStatus state;
}
