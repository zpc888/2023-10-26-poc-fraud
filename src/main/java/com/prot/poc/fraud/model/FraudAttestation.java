package com.prot.poc.fraud.model;

/**
 * @Author: <a href="mailto: pengcheng.zhou@gmail.com">PengCheng Zhou</a>
 * @Created: 2023-10-26T14:49 Thursday
 */
public record FraudAttestation(Client signer, Fraud fraud, CallbackInfo callback) {
}
