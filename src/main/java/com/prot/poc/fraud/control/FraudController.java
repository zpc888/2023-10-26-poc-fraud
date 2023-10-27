package com.prot.poc.fraud.control;

import com.prot.poc.fraud.model.DocumentResult;
import com.prot.poc.fraud.model.FraudAttestation;
import com.prot.poc.fraud.service.FraudAttestationPDFGen;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Base64;

/**
 * @Author: <a href="mailto: pengcheng.zhou@gmail.com">PengCheng Zhou</a>
 * @Created: 2023-10-26T12:18 Thursday
 */
@RestController
public class FraudController {
    private final FraudAttestationPDFGen attestationPDFGen;

    @Autowired
    public FraudController(FraudAttestationPDFGen attestationPDFGen) {
        this.attestationPDFGen = attestationPDFGen;
    }

    @PostMapping("/api/v1/fraud-attestations")
    public Mono<DocumentResult> genFraudAttestationThenSendToSign(@RequestBody FraudAttestation fraudAttestation) {
        try {
            byte[] bytes = attestationPDFGen.genAttestationReport(fraudAttestation);
            String encoded = Base64.getEncoder().encodeToString(bytes);
            DocumentResult result = new DocumentResult(fraudAttestation.fraud().fraudNumber(), "generated", encoded);
            return Mono.just(result);
        } catch (Exception ex) {
            throw new RuntimeException("Fail to generate attestation document", ex);
        }
    }
}
