package com.prot.poc.fraud.model;

/**
 * @Author: <a href="mailto: pengcheng.zhou@gmail.com">PengCheng Zhou</a>
 * @Created: 2023-10-26T14:55 Thursday
 */
public record CallbackInfo(String callbackUrl, String oauthUrl, String grantType, String clientId, String clientSecret) {
}
