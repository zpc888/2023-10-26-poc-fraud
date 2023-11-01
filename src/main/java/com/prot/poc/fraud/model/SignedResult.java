package com.prot.poc.fraud.model;

import jakarta.validation.constraints.NotNull;

/**
 * @Author: <a href="mailto: pengcheng.zhou@gmail.com">PengCheng Zhou</a>
 * @Created: 2023-11-01T14:57 Wednesday
 */
public record SignedResult(@NotNull String documentId, @NotNull String signedDocumentId, @NotNull String signedBase64Content) {
}
