package com.prot.poc.fraud.model;

import java.util.List;

/**
 * @Author: <a href="mailto: pengcheng.zhou@gmail.com">PengCheng Zhou</a>
 * @Created: 2023-10-26T14:33 Thursday
 */
public record Fraud(String fraudNumber, String clientName,
                    String fraudReason, String otherReasonDetail,
                    List<FraudItem> items) {
}
