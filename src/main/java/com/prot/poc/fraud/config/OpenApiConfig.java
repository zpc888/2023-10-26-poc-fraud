package com.prot.poc.fraud.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: <a href="mailto: pengcheng.zhou@gmail.com">PengCheng Zhou</a>
 * @Created: 2023-10-26T13:43 Thursday
 */
@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI pocFraudOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Fraud POC API")
                        .description("Gen PDF, retrive PDF and mimic PDF sign")
                        .version("1.0")
                );
    }
}
