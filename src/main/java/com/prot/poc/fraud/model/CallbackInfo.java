package com.prot.poc.fraud.model;

import jakarta.validation.constraints.NotNull;

/**
 * @Author: <a href="mailto: pengcheng.zhou@gmail.com">PengCheng Zhou</a>
 * @Created: 2023-10-26T14:55 Thursday
 */
public record CallbackInfo(
    @NotNull String callbackUrl,
    @NotNull String oauthUrl,
    @NotNull String grantType,
    @NotNull String clientId,
    @NotNull String clientSecret) {
}
