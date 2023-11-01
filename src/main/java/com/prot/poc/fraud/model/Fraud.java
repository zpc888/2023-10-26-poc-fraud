package com.prot.poc.fraud.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * @Author: <a href="mailto: pengcheng.zhou@gmail.com">PengCheng Zhou</a>
 * @Created: 2023-10-26T14:33 Thursday
 */
public record Fraud(@NotNull String fraudNumber, @NotNull String clientName,
                    @NotNull String fraudReason, String otherReasonDetail,
                    @NotNull @NotEmpty List<FraudItem> items) {
}
