package com.prot.poc.fraud.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * @Author: <a href="mailto: pengcheng.zhou@gmail.com">PengCheng Zhou</a>
 * @Created: 2023-10-26T14:48 Thursday
 */
public record Client(
        @NotNull @Schema(description = "Client Id - Salesforce Account Id") String id,
        @NotNull @Schema(description = "Client Full Name - Salesforce Account Name") String fullName,
        @NotNull String email,
        String phone) {
}
