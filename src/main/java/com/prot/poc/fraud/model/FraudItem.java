package com.prot.poc.fraud.model;

import java.util.Date;

/**
 * @Author: <a href="mailto: pengcheng.zhou@gmail.com">PengCheng Zhou</a>
 * @Created: 2023-10-26T14:38 Thursday
 */
public record FraudItem(Date txDate, String merchant, String cardNumber, double amount) {
}
