package com.prot.poc.fraud.control;

import com.prot.poc.fraud.model.Client;
import com.prot.poc.fraud.model.CreditBureauReport;
import com.prot.poc.fraud.pdfgen.CreditReportPDFGen;
import com.prot.poc.fraud.service.CreditBureauService;
import io.swagger.v3.oas.annotations.headers.Header;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * @Author: <a href="mailto: pengcheng.zhou@gmail.com">PengCheng Zhou</a>
 * @Created: 2023-10-26T15:24 Thursday
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CreditBureauController {
    private final CreditBureauService service;

    @PostMapping("/credit-report")
    public Mono<CreditBureauReport> retrieveCreditReport(
            @RequestHeader(value = "x-vendor-client-id", required = false) String vendorClientId,
            @RequestBody Client client) {
        try {
            CreditBureauReport report = service.getCreditReport(vendorClientId, client);
            return Mono.just(report);
        } catch (Exception ex) {
            throw new RuntimeException("Fail to get CBR report for client: " + client.fullName(), ex);
        }
    }
}
