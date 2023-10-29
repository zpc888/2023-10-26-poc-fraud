package com.prot.poc.fraud.config;

import lombok.Data;

/**
 * @Author: <a href="mailto: pengcheng.zhou@gmail.com">PengCheng Zhou</a>
 * @Created: 2023-10-28T21:59 Saturday
 */
@Data
public class VendorAuthInfo {
    private String clientIdHeaderName = "x-vendor-client-id";
    private String clientSecretHeaderName = "x-vendor-client-secret"; //s
}
