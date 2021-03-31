package com.sabadellzurich.olimpo.renewal.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class ValidateCommunicationsRequestDTO {
    @ApiModelProperty("${swagger.items.validateCommunications.valid}")
    private boolean valid;

    @ApiModelProperty("${swagger.items.validateCommunications.itemIds}")
    private List<Long> itemIds;
}
