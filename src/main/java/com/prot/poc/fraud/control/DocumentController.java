package com.prot.poc.fraud.control;

import com.prot.poc.fraud.entity.DocStore;
import com.prot.poc.fraud.model.DocumentResult;
import com.prot.poc.fraud.repository.DocStoreRepository;
import com.prot.poc.fraud.service.FraudService;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Base64;
import java.util.Optional;

/**
 * @Author: <a href="mailto: pengcheng.zhou@gmail.com">PengCheng Zhou</a>
 * @Created: 2023-10-26T15:10 Thursday
 */
@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class DocumentController {
    private final FraudService fraudGenAndSign;
    private final DocStoreRepository docStoreRepository;

    @GetMapping("/documents/{docId}")
    public Mono<DocumentResult> getDocumentContentAndStatus(@PathVariable String docId, @Parameter(hidden = true) ServerHttpResponse response) {
        return getDocumentById(docId, response, true);
    }

    private Mono<DocumentResult> getDocumentById(String docId, ServerHttpResponse response, boolean withPdfData) {
        long id = Long.parseLong(docId);
        Optional<DocStore> byId = docStoreRepository.findById(id);
        if (byId.isPresent()) {
            DocStore doc = byId.get();
            DocumentResult ret = new DocumentResult(doc.getId().toString(), doc.getStatus().toString(),
                    withPdfData ? Base64.getEncoder().encodeToString(doc.getContent()) : null);
            return Mono.just(ret);
        } else {
            return httpError(response, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/document-statuses/{docId}")
    public Mono<DocumentResult> getDocumentStatusOnly(@PathVariable String docId, @Parameter(hidden = true) ServerHttpResponse response) {
        return getDocumentById(docId, response, false);
    }

    private <T> Mono<T> httpError(ServerHttpResponse response, HttpStatusCode statusCode) {
        response.setStatusCode(statusCode);
        return Mono.empty();
    }

    @GetMapping("/document-signs/{docId}")
    public Mono<DocumentResult> mimicSigned(@PathVariable String docId, @Parameter(hidden = true) ServerHttpResponse response) {
        try {
            Mono<DocumentResult> result = fraudGenAndSign.mimicSign(docId);
            return result.switchIfEmpty(httpError(response, HttpStatus.NOT_FOUND));
        } catch (Exception ex) {
            throw new RuntimeException("Fail to sign document: " + docId, ex);
        }
    }
}
