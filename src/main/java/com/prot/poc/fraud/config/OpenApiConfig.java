package com.prot.poc.fraud.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @Author: <a href="mailto: pengcheng.zhou@gmail.com">PengCheng Zhou</a>
 * @Created: 2023-10-26T13:43 Thursday
 */
@Configuration
public class OpenApiConfig {
    private static Set<String> notToAddHeadersForOperations = Set.of("viewDocument", "signEventListener", "updateSignStatus");

    @Bean
    public OpenAPI pocFraudOpenAPI(@Autowired VendorsConfig vendorsConfig) {
        OpenAPI ret = new OpenAPI()
                .info(new Info()
                        .title("Fraud POC API")
                        .description("Gen PDF, retrive PDF and mimic PDF sign")
                        .version("1.0")
                );
        Schema stringType = new Schema().type("string");
        ret.components(new Components()
                .addParameters("vendor-client-id", new HeaderParameter().required(true)
                        .name(vendorsConfig.getAuth().getClientIdHeaderName())
                        .description("Vendor Client Id").schema(stringType))
                .addParameters("vendor-client-secret", new HeaderParameter().required(true)
                        .name(vendorsConfig.getAuth().getClientSecretHeaderName())
                        .description("Vendor Client Secret").schema(stringType))
        );
        return ret;
    }

    @Bean
    public OpenApiCustomizer addGloablVendorAuthHeaders(@Autowired VendorsConfig vendorsConfig) {
        return api -> api.getPaths().values().stream()
                .flatMap(pathItem -> pathItem.readOperations().stream())
                .filter(operation -> !notToAddHeadersForOperations.contains(operation.getOperationId())
                        && notToAddHeadersForOperations.stream().map(op -> op + "_")   // when supporting multiple http method, it will add _1, _2, etc for operationId
                        .noneMatch(op -> operation.getOperationId().startsWith(op)))
                .forEach(operation -> {
                    // remove first if global headers are referred by operations already
                    List<Parameter> params = operation.getParameters() == null ? Collections.emptyList() : operation.getParameters();
                    params.removeIf(param -> {
                        String name = param.getName();
                        return name.equals(vendorsConfig.getAuth().getClientIdHeaderName())
                                || name.equals(vendorsConfig.getAuth().getClientSecretHeaderName());
                    });
                    // add global ones
                    operation.addParametersItem(new HeaderParameter().$ref("#/components/parameters/vendor-client-id"));
                    operation.addParametersItem(new HeaderParameter().$ref("#/components/parameters/vendor-client-secret"));
                });
    }

//    @Bean
//    public GroupedOpenApi vendorAuthHeaders() {
//        return GroupedOpenApi.builder()
//                .group("vendor-auth-header")
//                .addOperationCustomizer(((operation, handlerMethod) -> {
//                    operation.getParameters().add(new HeaderParameter()
//                            .name(httpHeaderNameForVendorClientId).description("Vendor Client Id"));
//                    operation.getParameters().add(new HeaderParameter()
//                            .name(httpHeaderNameForVendorClientSecret).description("Vendor Client Secret"));
//                    return operation;
//                }))
//                .build();
//    }
}
