package com.prot.poc.fraud.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * @Author: <a href="mailto: pengcheng.zhou@gmail.com">PengCheng Zhou</a>
 * @Created: 2023-10-26T14:55 Thursday
 */
@Schema(description = """
OAuth information and callback URL to salesforce for sign status change notification after mock-sign api is called.
The callback payload will contains documentId, signedDocumentId and signedBase64Content.
""")
public record CallbackInfo(
    @NotNull @Schema(description = "salesforce url to receive the sign status change event") String callbackUrl,
    @NotNull @Schema(description = "salesforce oauth token url to generate jwt") String oauthUrl,
    @NotNull @Schema(description = "only support client_credentials type for now") String grantType,
    @NotNull @Schema(description = "salesforce connector application client id") String clientId,
    @NotNull @Schema(description = "salesforce connector application client secret") String clientSecret) {
}

//        final String callbackOauthUrl = "https://ability-business-3077-dev-ed.scratch.my.salesforce.com/services/oauth2/token";
//        final String clientId = "3MVG99nUjAVk2edwYTM8ZHhUfVBKY.in7kFNX7ortXNlaHZ2LFiqiOASh8znJ7WtA7ftBnOGogyVcS26.MKIa";
//        final String clientSecret = "ECAB64013CEC0A2DB944975E3E87228ACEDFDEBDC3382DC2B6723F7D07724950";
//        final String grantType = "client_credentials";
//        final String callbackUrl = "https://ability-business-3077-dev-ed.scratch.my.salesforce.com/services/apexrest/fraud/v1/esign/status/callback/after-esigned";
