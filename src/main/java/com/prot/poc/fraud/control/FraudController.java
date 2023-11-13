package com.prot.poc.fraud.control;

import com.prot.poc.esign.ESignService;
import com.prot.poc.esign.vo.*;
import com.prot.poc.fraud.model.Client;
import com.prot.poc.fraud.model.DocumentResult;
import com.prot.poc.fraud.model.FraudAttestation;
import com.prot.poc.fraud.service.FraudService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Base64;
import java.util.Collections;
import java.util.List;

/**
 * @Author: <a href="mailto: pengcheng.zhou@gmail.com">PengCheng Zhou</a>
 * @Created: 2023-10-26T12:18 Thursday
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class FraudController {
    @Value("${app.esign-method:docusign}")
    private String esignMethod;

    private final FraudService fraudGenAndSign;
    private final ESignService eSignService;

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
            // send to docusign for sign
            if ("docusign".equalsIgnoreCase(esignMethod)) {
                SignPackageResult sendResult = eSignService.sendForSign(buildSignPackage(fraudAttestation, result));
                result = new DocumentResult(result.documentId(), result.status(), result.base64Content(), sendResult);
            }
            return Mono.just(result);
        } catch (Exception ex) {
            throw new RuntimeException("Fail to generate attestation document", ex);
        }
    }

    private SignPackage buildSignPackage(FraudAttestation fraudAttestation, DocumentResult docResult) {
        final String role = "Signer";
        final Client signer = fraudAttestation.signer();
        SignPackage ret = new SignPackage();
        SignPackage.SignDocument signDoc = new SignPackage.SignDocument();
        signDoc.setId(docResult.documentId()).setName("fraud-attestation").setContentSupplier(() -> {
            return Base64.getDecoder().decode(docResult.base64Content());
        });
        signDoc.setTouches(List.of(
                new TouchType.SignatureType().setToucherRole(role)
                        .setLocation(new TouchLocation.ViaAnchor("_signature_", 0, 0)),
                new TouchType.TextType().setValue(signer.fullName()).setToucherRole(role)
                        .setLocation(new TouchLocation.ViaAnchor("_printed_name_", 0, 0)),
                new TouchType.DateSignedType().setToucherRole(role)
                        .setLocation(new TouchLocation.ViaAnchor("_signed_date_", 0, 0))
        ));
        SignPackage.Recipient recipient = new SignPackage.Recipient();
        recipient.setId(signer.id()).setName(signer.fullName())
                .setEmail(signer.email()).setClientUserId(null)
                .setSignAuth(signer.phone() == null ? SignAuth.NONE : new SignAuth.SMS(signer.phone()));
        ret.setEmailSubject("For Fraud POC Docusign")
                .setEmailBody("After DocuSign, Salesforce status should be auto updated");
        ret.setRecipients(List.of(recipient)).setSignedDocuments(List.of(signDoc))
                .setRoleToRecipientIds(Collections.singletonMap(role, signer.id()));
        return ret;
    }
}
