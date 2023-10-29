package com.prot.poc.fraud.control;

import com.prot.poc.fraud.model.DocumentResult;
import com.prot.poc.fraud.model.FraudAttestation;
import com.prot.poc.fraud.service.FraudService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * @Author: <a href="mailto: pengcheng.zhou@gmail.com">PengCheng Zhou</a>
 * @Created: 2023-10-26T12:18 Thursday
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class FraudController {
    private final FraudService fraudGenAndSign;

    @PostMapping("/fraud-attestations")
    public Mono<DocumentResult> genFraudAttestationThenSendToSign(
            @RequestHeader(value = "x-vendor-client-id", required = false) String vendorClientId,
            @RequestBody FraudAttestation fraudAttestation) {
        try {
            DocumentResult result = fraudGenAndSign.genFraudAttestationThenSendToSign(vendorClientId, fraudAttestation);
            return Mono.just(result);
        } catch (Exception ex) {
            throw new RuntimeException("Fail to generate attestation document", ex);
        }
    }
}
