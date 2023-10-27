package com.prot.poc.fraud.control;

import com.prot.poc.fraud.model.DocumentResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;

/**
 * @Author: <a href="mailto: pengcheng.zhou@gmail.com">PengCheng Zhou</a>
 * @Created: 2023-10-26T15:10 Thursday
 */
@Slf4j
@RestController
public class DocumentController {
    @GetMapping("/api/v1/documents/{docId}")
    public Mono<DocumentResult> getDocumentContentAndStatus(@PathVariable String docId) {
        return null;
    }

    @GetMapping("/api/v1/document-statuses/{docId}")
    public Mono<DocumentResult> getDocumentStatusOnly(@PathVariable String docId) {
        return null;
    }

    @GetMapping("/api/v1/document-signs/{docId}")
    public Mono<DocumentResult> mimicSigned(@PathVariable String docId) {
        final String callbackOauthUrl = "https://ability-business-3077-dev-ed.scratch.my.salesforce.com/services/oauth2/token";
        final String clientId = "3MVG99nUjAVk2edwYTM8ZHhUfVBKY.in7kFNX7ortXNlaHZ2LFiqiOASh8znJ7WtA7ftBnOGogyVcS26.MKIa";
        final String clientSecret = "ECAB64013CEC0A2DB944975E3E87228ACEDFDEBDC3382DC2B6723F7D07724950";
        final String grantType = "client_credentials";

        Mono<String> accessToken = getAccessToken(callbackOauthUrl, grantType, clientId, clientSecret);
        final String callbackUrl = "https://ability-business-3077-dev-ed.scratch.my.salesforce.com/services/apexrest/fraud/v1/esign/status/callback/after-esigned";
        Mono<Map<String, Object>> result = doCallback(accessToken, callbackUrl, docId);
        return result.map(m -> {
            log.debug("callback {} response is: {}", callbackUrl, m);
            DocumentResult finalResult = new DocumentResult(docId, "Completed", null);
            return finalResult;
        });
    }

    private Mono<Map<String, Object>> doCallback(Mono<String> accessToken, String callbackUrl, String docId) {
        WebClient client = WebClient.create(callbackUrl);
        Map<String, Object> reqBody = Collections.singletonMap("documentId", docId);
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
}
