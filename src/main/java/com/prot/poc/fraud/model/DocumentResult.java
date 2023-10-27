package com.prot.poc.fraud.model;

/**
 * @Author: <a href="mailto: pengcheng.zhou@gmail.com">PengCheng Zhou</a>
 * @Created: 2023-10-26T15:00 Thursday
 */
public record DocumentResult(String documentId, String status, String base64Content) {
}
