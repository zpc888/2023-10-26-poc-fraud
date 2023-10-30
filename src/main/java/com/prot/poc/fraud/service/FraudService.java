package com.prot.poc.fraud.service;

import com.prot.poc.fraud.entity.DocStatus;
import com.prot.poc.fraud.entity.DocStore;
import com.prot.poc.fraud.model.*;
import com.prot.poc.fraud.pdfgen.FraudAttestationPDFGen;
import com.prot.poc.fraud.repository.DocStoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;

/**
 * @Author: <a href="mailto: pengcheng.zhou@gmail.com">PengCheng Zhou</a>
 * @Created: 2023-10-28T22:19 Saturday
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FraudService {
    private final FraudAttestationPDFGen attestationPDFGen;
    private final DocStoreRepository docStoreRepository;

    public DocumentResult genFraudAttestationThenSendToSign(String vendorId, FraudAttestation fraudAttestation) throws Exception {
        DocStore docStore = new DocStore();

        docStore.setDocName("Fraud Attestation Report");
        docStore.setVendorName(vendorId);
        docStore.setStatus(DocStatus.SENT);             // mimic generated and then sent to sign
        docStore.setSourceNumber(fraudAttestation.fraud().fraudNumber());

        byte[] bytes = attestationPDFGen.genAttestationReport(fraudAttestation);

        Client signer = fraudAttestation.signer();
        if (signer == null) {
            signer = new Client("000", fraudAttestation.fraud().clientName(), null, null);
        } else if (signer.fullName() == null) {
            signer = new Client(signer.id(), fraudAttestation.fraud().clientName(), signer.email(), signer.phone());
        }
        docStore.setSignerInfo(JSONUtils.toJSONString(signer));
        docStore.setCallbackInfo(JSONUtils.toJSONStringNicely(fraudAttestation.callback()));
        docStore.setContent(bytes);
        docStoreRepository.save(docStore);

        String encoded = Base64.getEncoder().encodeToString(bytes);
        return new DocumentResult(docStore.getId().toString(),
                docStore.getStatus().toString(), encoded);
    }

    public Mono<DocumentResult> mimicSign(Long docId) throws Exception {
//        final String callbackOauthUrl = "https://ability-business-3077-dev-ed.scratch.my.salesforce.com/services/oauth2/token";
//        final String clientId = "3MVG99nUjAVk2edwYTM8ZHhUfVBKY.in7kFNX7ortXNlaHZ2LFiqiOASh8znJ7WtA7ftBnOGogyVcS26.MKIa";
//        final String clientSecret = "ECAB64013CEC0A2DB944975E3E87228ACEDFDEBDC3382DC2B6723F7D07724950";
//        final String grantType = "client_credentials";
//        final String callbackUrl = "https://ability-business-3077-dev-ed.scratch.my.salesforce.com/services/apexrest/fraud/v1/esign/status/callback/after-esigned";
        Optional<DocStore> byId = docStoreRepository.findById(docId);
        if (!byId.isPresent()) {
            log.error("cannot find document id: {}", docId);
            return Mono.empty();
        }
        DocStore doc = byId.get();
        log.debug("Found the document from doc store based on doc id = {}", docId);

        Client signer = JSONUtils.fromJSONString(doc.getSignerInfo(), Client.class);
        byte[] signedPDF = attestationPDFGen.addWatermarkSign(doc.getContent(), signer.fullName());
        log.debug("Signed for doc id = {}", docId);

        DocStore signedDoc = new DocStore();
        signedDoc.setDocName("Fraud Attestation Report - Signed");
        signedDoc.setVendorName(doc.getVendorName());
        signedDoc.setStatus(DocStatus.SIGNED);
        signedDoc.setSourceNumber(doc.getSourceNumber());
        signedDoc.setContent(signedPDF);
        docStoreRepository.save(signedDoc);
        String signedDocId = signedDoc.getId().toString();
        log.debug("Saved the signed doc with new doc-id = {}", signedDocId);

        String base64PDF = Base64.getEncoder().encodeToString(signedPDF);
        CallbackInfo cbi = JSONUtils.fromJSONString(doc.getCallbackInfo(), CallbackInfo.class);
        Mono<String> accessToken = getAccessToken(cbi.oauthUrl(), cbi.grantType(), cbi.clientId(), cbi.clientSecret());
        final String callbackUrl = cbi.callbackUrl();
        Mono<Map<String, Object>> result = doCallback(accessToken, callbackUrl, docId.toString(), signedDocId, base64PDF);
        return result.map(m -> {
            log.debug("callback {} response is: {}", callbackUrl, m);
            DocumentResult finalResult = new DocumentResult(signedDocId, "Completed", base64PDF);
            return finalResult;
        });
    }

    private static Mono<String> getAccessToken(String oauthUrl, String grantType, String clientId, String clientSecret) {
        final String endpoint = String.format("%s?grant_type=%s&client_id=%s&client_secret=%s",
                oauthUrl, grantType, clientId, clientSecret);
        WebClient oauth2Client = WebClient.create(endpoint);
        Mono<String> accessToken = oauth2Client.post()
                .headers(h -> h.set("Accept", "application/json"))
                .exchangeToMono(clientResponse -> {
                    return clientResponse.bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                    }).map(m -> {
                        String jwt = (String)m.get("access_token");
                        if (jwt == null) {
                            log.error("fail to get access token, response = {}", m);
                        }
                        return jwt;
                    });
                });
        return accessToken;
    }

    private Mono<Map<String, Object>> doCallback(Mono<String> accessToken, String callbackUrl,
                                                 String docId, String signedDocId, String base64PDF) {
        WebClient client = WebClient.create(callbackUrl);
        Map<String, Object> reqBody = new LinkedHashMap<>();
        reqBody.put("documentId", docId);
        reqBody.put("signedDocumentId", signedDocId);
        reqBody.put("signedBase64Content", base64PDF);
        Mono<Map<String, Object>> ret = accessToken.flatMap(token -> {
            return client.post()
                    .headers(h -> h.set("Content-Type", "application/json"))
                    .headers(h -> h.set("Accept", "application/json"))
                    .headers(h -> h.setBearerAuth(token))
                    .body(BodyInserters.fromValue(reqBody))
                    .exchangeToMono(clientResponse -> {
                        final int statusCode = clientResponse.statusCode().value();
                        if (statusCode >= 400) {
                            log.error("callback response status: {}", clientResponse.statusCode().value());
                        }
                        return clientResponse.bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                        });
                    });
        });
        return ret;
    }
}
