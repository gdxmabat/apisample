package com.sabadellzurich.olimpo.renewal.dto;

import com.sabadellzurich.olimpo.renewal.common.lib.dto.PacketStatus;

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
public class UpdatePacketRequestDTO {
    @ApiModelProperty("${swagger.packetState}")
    private PacketStatus state;
}
