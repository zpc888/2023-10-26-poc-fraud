package com.prot.poc.esign.docusign;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * @Author: <a href="mailto: pengcheng.zhou@gmail.com">PengCheng Zhou</a>
 * @Created: 2023-11-13T09:52 Monday
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class DocuSignEventListenerController {
    @RequestMapping("/esign-event-listener")
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
