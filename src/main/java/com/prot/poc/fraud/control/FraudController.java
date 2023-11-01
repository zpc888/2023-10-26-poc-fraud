package com.prot.poc.fraud.control;

import com.prot.poc.fraud.model.DocumentResult;
import com.prot.poc.fraud.model.FraudAttestation;
import com.prot.poc.fraud.service.FraudService;
import io.swagger.v3.oas.annotations.Operation;
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
    @Operation(summary = "Generate a Fraud Attestation PDF, then send user to sign",
            description = "In reality, signer email is mandatory to email client to ask for signing. " +
                    "But for POC, it is optional since sign action is mocked via api. " +
                    "After signed, it will notify salesforce based on the callback information, " +
                    "which is mandatory. It is about salesforce connected app and callback url to handle signed status. ")
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
