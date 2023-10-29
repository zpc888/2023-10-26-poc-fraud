package com.prot.poc.fraud.control;

import com.prot.poc.fraud.entity.DocStore;
import com.prot.poc.fraud.model.DocumentResult;
import com.prot.poc.fraud.repository.DocStoreRepository;
import com.prot.poc.fraud.service.FraudService;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.*;
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

    @CrossOrigin
    @GetMapping(value = "document-views/{docId}", produces = {"application/pdf"})
    public Mono<Void> viewDocument(@PathVariable Long docId, @Parameter(hidden = true) ServerHttpResponse response) {
//        DocStore doc = mockDocWithFixedPDFData();

        Optional<DocStore> byId = docStoreRepository.findById(docId);
        if (!byId.isPresent()) {
            return httpError(response, HttpStatus.NOT_FOUND);
        }
        DocStore doc = byId.get();
        response.setStatusCode(HttpStatus.OK);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/pdf");
        response.getHeaders().add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\""
                + doc.getDocName() + ".pdf\"");
        response.getHeaders().add(HttpHeaders.CONTENT_LENGTH, "" + doc.getContent().length);
        DataBuffer buffer = response.bufferFactory().wrap(doc.getContent());
        return response.writeWith(Mono.just(buffer));
    }

    private static DocStore mockDocWithFixedPDFData() {
        String base64PDF = "JVBERi0xLjYKJfbk/N8KMSAwIG9iago8PAovVHlwZSAvQ2F0YWxvZwovVmVyc2lvbiAvMS42Ci9QYWdlcyAyIDAgUgo+PgplbmRvYmoKMTEgMCBvYmoKPDwKL0xlbmd0aCA1OTgKL0ZpbHRlciAvRmxhdGVEZWNvZGUKPj4Kc3RyZWFtDQp4nI2Tz47aMBDG73mKOfSwlZZgO47tcFugrFqp3WrhBdxgSiqIV46p1Lfv2CTb8KcBcTDyeL7ffDOT8dz8rkrz+jyFsklIKoUSGZCUMppLPBnPKIemTAhIwUBQBhkBZ5JNMl0l4wUFxmG1wXD44TsqZZoxTihIKtKMcMJgtU4enqazEcHbhdOHNbyaN+v8R1j9Sj6tYvI+wHNWcDg/j3BKWrFQAn2v4J07XuC1CKVQBSIvUiq4LCJ6brSDL3Zbw3Jf+e0kYgmMMpZSTlR8M9WNWYOtIQZpp4q5Xlc7rFc3GFxYBy9+a1wr0b16BLxsDGhnwj/YBJOHnak9eKfrRpe+snWTdmRWpFyExiH58wb+2ENMra2P6VVd+Up7hNlNq9yTeYS3HZZjoKl+1uAtlLbeVG5/VGckOqMpOmtbv8R32h+cmUBXQJ6luTg6/+6q2qP3b3p//UFIx/hc+y4eRja0KUxhciHiqFhvWTJQJ7uSU5livbgVjEQUIywbUTJiKEgnGT1dEArPQ1zUyJkCiudxRacnC8IxnEVH4wXHV6GSh9W/tkaDHfAGivIjC/f8KouKa7CvxpVbXfs7IZlqDZH/GboGmWmHszzsf7RLepvDb3ByRlIh+BXW094ezu2oXCkG4eRF+IQlfobtdHgxPB3S6ued/uk6sOI+UhhOQA0O54I1s40v7X2EMJloZmgyFwSlpEAhosR9FH6DkqPuJeWDfBRK4rT7kNB80W/+8rz5XJ4L9fNjS0W/pcuLlg4rxJaJvpnzEm4p8BsKoR2XChft+Asq7o9bDQplbmRzdHJlYW0KZW5kb2JqCjEyIDAgb2JqCjw8Ci9MZW5ndGggMjU1Ci9UeXBlIC9PYmpTdG0KL04gOQovRmlsdGVyIC9GbGF0ZURlY29kZQovRmlyc3QgNTIKPj4Kc3RyZWFtDQp4nM1Ry2rDMBD8lfmBVF5ZtmMIgTo0FEqhuIEeQg6OLYIgkYpll/bvu5Kgj1sLPfSgZXd2Z3YHSWTIoSQUSGUoQEWFEpJyVJB1jSXypUQNVRKIB5TCaiV2b88a4qE7aQ9xZwaPfc5KLQ4QGzfbCYT1+vskxL0eTNe4V+yzqwzhlSQ5VnWIhzA2aubKKCVa7d089rxCJWDj7MR9D6IIpA1bRvnwLwCxg0jYSnaRspydpEyxm5QVwdEnL50a5cTjfJxiGUCCaDqvU+dWn1/0ZPpu0bjzAHFjezcYe4J4MvbaevMB/EZ1Zy7aL1p36exfSfI/jEaP//jCn0u+A79EyKINCmVuZHN0cmVhbQplbmRvYmoKMTMgMCBvYmoKPDwKL0xlbmd0aCA0NgovUm9vdCAxIDAgUgovSUQgWzwyNTc0QTM2NzNBOEQxMDcxQjlBMDEyNjJFOTA3OEQwNT4gPDI1NzRBMzY3M0E4RDEwNzFCOUEwMTI2MkU5MDc4RDA1Pl0KL1R5cGUgL1hSZWYKL1NpemUgMTQKL0luZGV4IFswIDEzXQovVyBbMSAyIDFdCi9GaWx0ZXIgL0ZsYXRlRGVjb2RlCj4+CnN0cmVhbQ0KeJwVw8kNACAMBLHZcIQHEp3RfxeUAIslA1csgvnLw4tXb949fYiN4vAARy0C8w0KZW5kc3RyZWFtCmVuZG9iagpzdGFydHhyZWYKMTExMAolJUVPRgo=";
        DocStore doc = new DocStore();
        doc.setDocName("zpc-PDF-View");
        doc.setContent(Base64.getDecoder().decode(base64PDF));
        return doc;
    }

    @GetMapping("/documents/{docId}")
    public Mono<DocumentResult> getDocumentContentAndStatus(@PathVariable Long docId, @Parameter(hidden = true) ServerHttpResponse response) {
        return getDocumentById(docId, response, true);
    }

    private Mono<DocumentResult> getDocumentById(Long docId, ServerHttpResponse response, boolean withPdfData) {
        Optional<DocStore> byId = docStoreRepository.findById(docId);
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
    public Mono<DocumentResult> getDocumentStatusOnly(@PathVariable Long docId, @Parameter(hidden = true) ServerHttpResponse response) {
        return getDocumentById(docId, response, false);
    }

    private <T> Mono<T> httpError(ServerHttpResponse response, HttpStatusCode statusCode) {
        response.setStatusCode(statusCode);
        return Mono.empty();
    }

    @GetMapping("/document-signs/{docId}")
    public Mono<DocumentResult> mimicSigned(@PathVariable Long docId, @Parameter(hidden = true) ServerHttpResponse response) {
        try {
            Mono<DocumentResult> result = fraudGenAndSign.mimicSign(docId);
            return result.switchIfEmpty(httpError(response, HttpStatus.NOT_FOUND));
        } catch (Exception ex) {
            throw new RuntimeException("Fail to sign document: " + docId, ex);
        }
    }
}
