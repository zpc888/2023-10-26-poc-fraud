package com.prot.poc.fraud.sec;

import com.prot.poc.fraud.config.VendorsConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * @Author: <a href="mailto: pengcheng.zhou@gmail.com">PengCheng Zhou</a>
 * @Created: 2023-10-28T17:50 Saturday
 */
@Component
public class VendorCheckFilter implements WebFilter {
    private String apiPathPrefix = "/api";
    private String excludedPath = "/document-views/";

    private final VendorsConfig vendorsConfig;
    private final String httpHeaderNameForVendorClientId;

    private final String httpHeaderNameForVendorClientSecret;

    @Autowired
    public VendorCheckFilter(VendorsConfig vendorsConfig) {
        this.vendorsConfig = vendorsConfig;
        this.httpHeaderNameForVendorClientId = vendorsConfig.getAuth().getClientIdHeaderName();
        this.httpHeaderNameForVendorClientSecret = vendorsConfig.getAuth().getClientSecretHeaderName();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (path.startsWith(apiPathPrefix) && !path.contains(excludedPath)) {
            HttpHeaders headers = exchange.getRequest().getHeaders();
            String clientId = headers.getFirst(httpHeaderNameForVendorClientId);
            String clientSecret = headers.getFirst(httpHeaderNameForVendorClientSecret);
            if (!vendorsConfig.isAuthenticated(clientId, clientSecret)) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return Mono.empty();
//            } else if (!vendorsConfig.isUnderThreshold(clientId)) {
//                exchange.getResponse().setStatusCode(HttpStatus.BANDWIDTH_LIMIT_EXCEEDED);
//                return Mono.empty();
            }
        }
        return chain.filter(exchange);
    }
}
