package com.prot.poc.fraud.model;

import jakarta.validation.constraints.NotNull;

/**
 * @Author: <a href="mailto: pengcheng.zhou@gmail.com">PengCheng Zhou</a>
 * @Created: 2023-10-26T14:49 Thursday
 */
public record FraudAttestation(@NotNull Client signer, @NotNull Fraud fraud, @NotNull CallbackInfo callback) {
}
