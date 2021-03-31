package com.sabadellzurich.olimpo.renewal.dto;

import java.util.List;
import com.sabadellzurich.olimpo.renewal.common.lib.dto.ItemDTO;
import com.sabadellzurich.olimpo.renewal.common.lib.dto.Timeline;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Full item DTO.
 * ================
 * <p>
 * Item DTO that will be printed in the packet detail screen.
 *
 * @author msellers
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class FullItemDTO extends ItemDTO {
    private List<Timeline> events;
}
