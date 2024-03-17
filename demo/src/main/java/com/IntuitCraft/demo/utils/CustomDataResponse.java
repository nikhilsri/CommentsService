/**
 * Copyright (c) 2022 Drishti-Soft Solutions Pvt. Ltd.
 *
 * @author: praveenkumar
 * Date:  Dec 5, 2022
 */
package com.IntuitCraft.demo.utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.io.Serializable;

/**
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomDataResponse<R> implements Serializable {

    @Schema(description = "HTTP code for the response", example = "200")
    @Builder.Default
    @JsonProperty("http_code")
    Integer httpCode = HttpStatus.OK.value();
    @Schema(description = "Error related information", example = "Invalid input for parameter")
    @JsonProperty("error_data")
    ErrorData errorData;
    @Schema(description = "Response data")
    R data;

}
