package com.prot.poc.fraud.model;

import jakarta.validation.constraints.NotNull;

import java.util.Date;

/**
 * @Author: <a href="mailto: pengcheng.zhou@gmail.com">PengCheng Zhou</a>
 * @Created: 2023-10-26T15:22 Thursday
 */
public record CreditData(@NotNull String creditScore, @NotNull Date creditDate) {
}
