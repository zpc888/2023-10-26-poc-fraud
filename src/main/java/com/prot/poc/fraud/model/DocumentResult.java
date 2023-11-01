package com.prot.poc.fraud.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;

/**
 * @Author: <a href="mailto: pengcheng.zhou@gmail.com">PengCheng Zhou</a>
 * @Created: 2023-10-26T15:00 Thursday
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DocumentResult(@NotNull String documentId, @NotNull String status, String base64Content) {
}
