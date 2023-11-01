package com.prot.poc.fraud.model;

import jakarta.validation.constraints.NotNull;

/**
 * @Author: <a href="mailto: pengcheng.zhou@gmail.com">PengCheng Zhou</a>
 * @Created: 2023-10-26T15:20 Thursday
 */
public record CreditBureauReport(@NotNull CreditData data, @NotNull DocumentResult pdf) {
}
