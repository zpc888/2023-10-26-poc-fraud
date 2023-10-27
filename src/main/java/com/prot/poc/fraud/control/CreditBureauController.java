package com.prot.poc.fraud.control;

import com.prot.poc.fraud.model.Client;
import com.prot.poc.fraud.model.CreditBureauReport;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * @Author: <a href="mailto: pengcheng.zhou@gmail.com">PengCheng Zhou</a>
 * @Created: 2023-10-26T15:24 Thursday
 */
@RestController
public class CreditBureauController {
    @PostMapping("/api/v1/credit-report")
    public Mono<CreditBureauReport> retrieveCreditReport(@RequestBody Client client) {
        return null;
    }
}
