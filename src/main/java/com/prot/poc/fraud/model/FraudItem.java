package com.prot.poc.fraud.model;

import jakarta.validation.constraints.NotNull;

import java.util.Date;

/**
 * @Author: <a href="mailto: pengcheng.zhou@gmail.com">PengCheng Zhou</a>
 * @Created: 2023-10-26T14:38 Thursday
 */
public record FraudItem(@NotNull Date txDate, @NotNull String merchant, @NotNull String cardNumber, @NotNull double amount) {
}
