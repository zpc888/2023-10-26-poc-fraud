package com.prot.poc.esign.docusign;

import com.prot.poc.esign.ESignService;
import com.prot.poc.fraud.entity.DocStore;
import com.prot.poc.fraud.repository.DocStoreRepository;
import com.prot.poc.fraud.service.FraudService;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * @Author: <a href="mailto: pengcheng.zhou@gmail.com">PengCheng Zhou</a>
 * @Created: 2023-11-13T09:52 Monday
 */
@Controller
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class DocuSignEventListenerController {
    private final FraudService fraudGenAndSign;
    private final ESignService eSignService;
    private final DocStoreRepository repository;

    @PostMapping(path="/docusign/event/notification", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Mono<Map<String, Object>> updateSignStatus(@RequestBody Map<String, Object> eventInfo) {
        // {data={envelopeId=d63307d8-e78c-4b3f-81fe-561740e64821, userId=5b80600b-9ce3-4945-aec2-9701474c84e7, accountId=a484ba6c-eef7-459d-9872-a494bbe7dd6b},
        // generatedDateTime=2023-11-14T03:15:44.5531192Z, configurationId=10470803, retryCount=0,
        // uri=/restapi/v2.1/accounts/a484ba6c-eef7-459d-9872-a494bbe7dd6b/envelopes/d63307d8-e78c-4b3f-81fe-561740e64821,
        // apiVersion=v2.1, event=envelope-completed}
        log.debug("event = {}", eventInfo);
        String event = (String)eventInfo.get("event");
        if ("envelope-completed".equals(event)) {
            Map<String, Object> data = (Map<String, Object>)eventInfo.get("data");
            String envelopeId = (String)data.get("envelopeId");
            log.debug("try to download signed document for envelopeId = {}", envelopeId);
            downloadSignedPDF(envelopeId);
        }
        return Mono.just(Collections.singletonMap("status", "ok"));
    }

    private void downloadSignedPDF(String pkgId) {
        Optional<DocStore> byPkgId = repository.findDocBySignPackageId(pkgId);
        if (!byPkgId.isPresent()) {
            log.error("cannot find document with sign package id: {}", pkgId);
            return;
        }
        log.debug("Found the document from doc store based on sign package id = {}", pkgId);
        byte[] signedPDF = eSignService.downloadDocument(pkgId, byPkgId.get().getId().toString());
        fraudGenAndSign.saveSignedDocAndNotifySalesforce(byPkgId.get(), signedPDF);
    }

    @RequestMapping("/esign-event-listener")
    @ResponseBody
    public Mono<Void> signEventListener(@Parameter(hidden = true) ServerHttpRequest request) {
        final String requestMethod = request.getMethod().name();
        final String path = request.getURI().getPath();
        final String query = request.getURI().getQuery();
//        log.debug("request method={}; path={}; query={}", requestMethod, path, query);
        if ("POST".equals(requestMethod) || "PUT".equals(requestMethod) || "PATCH".equals(requestMethod)) {
            return Mono.from(request.getBody()).flatMap(buffer -> {
                byte[] bytes = new byte[buffer.readableByteCount()];
                buffer.read(bytes);
                DataBufferUtils.release(buffer);
                String bodyStr = new String(bytes, StandardCharsets.UTF_8);
                if (log.isDebugEnabled()) {
                    String eventRequest = String.format("%n%s %s ? %s%n%s", requestMethod, path, query, bodyStr);
                    log.debug("Received DocuSign Event Notification: " + requestMethod + eventRequest);
                }
                return Mono.empty();
            });
        } else {
            // Mono.from(request.getBody()) will be empty for GET, DELETE, HEAD, OPTIONS
            if (log.isDebugEnabled()) {
                String eventRequest = String.format("%n%s %s ? %s", requestMethod, path, query);
                log.debug("Received DocuSign Event Notification: " + requestMethod + eventRequest);
            }
            return Mono.empty();
        }
        // why this is not working -- because return might happen before subscribe-work is done
//        request.getBody().subscribe(buffer -> {
//           byte[] bytes = new byte[buffer.readableByteCount()];
//           buffer.read(bytes);
//            DataBufferUtils.release(buffer);
//            String bodyStr = new String(bytes, StandardCharsets.UTF_8);
//            log.debug("request body={}", bodyStr);
//        });
//        return Mono.empty();
    }
}
