package com.prot.poc.fraud.service;

import com.prot.poc.common.JSONUtils;
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

    public Mono<SignedAndCallbackResult> mimicSign(Long docId) throws Exception {
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

        return saveSignedDocAndNotifySalesforce(doc, signedPDF);
    }

    public Mono<SignedAndCallbackResult> saveSignedDocAndNotifySalesforce(DocStore unsignedDoc, byte[] signedPDF) {
        Long docId = unsignedDoc.getId();
        DocStore signedDoc = new DocStore();
        signedDoc.setDocName("Fraud Attestation Report - Signed");
        signedDoc.setVendorName(unsignedDoc.getVendorName());
        signedDoc.setStatus(DocStatus.SIGNED);
        signedDoc.setSourceNumber(unsignedDoc.getSourceNumber());
        signedDoc.setContent(signedPDF);
        signedDoc.setSignPackageId(unsignedDoc.getSignPackageId() + "/signed");
        docStoreRepository.save(signedDoc);
        String signedDocId = signedDoc.getId().toString();
        log.debug("Saved the signed doc with new doc-id = {}", signedDocId);

        String base64PDF = Base64.getEncoder().encodeToString(signedPDF);
        SignedResult signedResult = new SignedResult(docId.toString(), signedDocId, base64PDF);
        CallbackInfo cbi = JSONUtils.fromJSONString(unsignedDoc.getCallbackInfo(), CallbackInfo.class);
        Mono<String> accessToken = getAccessToken(cbi.oauthUrl(), cbi.grantType(), cbi.clientId(), cbi.clientSecret());
        final String callbackUrl = cbi.callbackUrl();
        Mono<Map<String, Object>> result = doCallback(accessToken, callbackUrl, signedResult);
        return result.map(m -> {
            log.debug("callback {} response is: {}", callbackUrl, m);
            return new SignedAndCallbackResult(callbackUrl, signedResult, m);
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

    private Mono<Map<String, Object>> doCallback(Mono<String> accessToken, String callbackUrl, SignedResult signedResult) {
        WebClient client = WebClient.create(callbackUrl);
        Map<String, Object> reqBody = new LinkedHashMap<>();
        reqBody.put("documentId", signedResult.documentId());
        reqBody.put("signedDocumentId", signedResult.signedDocumentId());
        reqBody.put("signedBase64Content", signedResult.signedBase64Content());
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
                            Map<String, Object> errorResponse = new LinkedHashMap<>();
                            errorResponse.put("callbackErrorStatusCode", statusCode + "");
                            if (statusCode >= 500) {
                                return clientResponse.bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {
                                }).map(list -> {
                                    errorResponse.put("callbackErrorResponseBody", list);
                                    return errorResponse;
                                });
                            } else {
                                return Mono.just(errorResponse);
                            }
                        }
                        return clientResponse.bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                        });
                    });
        });
        return ret;
    }

    public void insertSignPackage(DocumentResult result) {
        Optional<DocStore> byId = docStoreRepository.findById(Long.parseLong(result.documentId()));
        byId.get().setSignPackageId(result.signPackage().getPackageId());
        docStoreRepository.saveAndFlush(byId.get());
    }
}
