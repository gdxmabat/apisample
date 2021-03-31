package com.sabadellzurich.olimpo.renewal;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Swagger Pageable.
 * =================
 *
 * Fix for springfox's implementation of Spring Pageable.
 *
 * @author msellers
 */
@Data
public class SwaggerPageable {
    @ApiModelProperty(value = "Number of records per page", example = "20")
    private int size;

    @ApiModelProperty(value = "Results page you want to retrieve (0..N)", example = "0")
    private int page;

    @ApiModelProperty("Sorting criteria in the format: property(,asc|desc). Default sort order is ascending. Multiple sort criteria are supported.")
    private String sort;
}
