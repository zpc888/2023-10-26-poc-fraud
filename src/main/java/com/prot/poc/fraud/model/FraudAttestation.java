package com.prot.poc.fraud.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * @Author: <a href="mailto: pengcheng.zhou@gmail.com">PengCheng Zhou</a>
 * @Created: 2023-10-26T14:49 Thursday
 */
@Schema(description = "Fraud Attestation")
public record FraudAttestation(@NotNull Client signer, @NotNull Fraud fraud, @NotNull CallbackInfo callback) {
}
