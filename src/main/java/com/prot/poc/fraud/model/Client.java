package com.prot.poc.fraud.model;

import jakarta.validation.constraints.NotNull;

/**
 * @Author: <a href="mailto: pengcheng.zhou@gmail.com">PengCheng Zhou</a>
 * @Created: 2023-10-26T14:48 Thursday
 */
public record Client(@NotNull String id, @NotNull String fullName, String email, String phone) {
}
