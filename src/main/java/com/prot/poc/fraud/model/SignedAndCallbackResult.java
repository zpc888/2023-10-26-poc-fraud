package com.prot.poc.fraud.model;

import java.util.Map;

/**
 * @Author: <a href="mailto: pengcheng.zhou@gmail.com">PengCheng Zhou</a>
 * @Created: 2023-11-01T17:42 Wednesday
 */
public record SignedAndCallbackResult(String callbackUrl, SignedResult callbackRequestPaylod, Map<String, Object> callbackResponse) {
}
