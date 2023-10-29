package com.prot.poc.fraud.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: <a href="mailto: pengcheng.zhou@gmail.com">PengCheng Zhou</a>
 * @Created: 2023-10-28T16:01 Saturday
 */
@Data
public class VendorInfo {
    @NotEmpty
    @NotBlank
    private String clientId;
    @NotEmpty
    @NotBlank
    private String clientSecret;
    private Integer numberOfHourlyRequests;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private AtomicInteger visitCounter = new AtomicInteger(0);

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private int hourLimit = 0;

    public void setDefaultNumberOfHourlyRequests(int defaultLimit) {
        if (numberOfHourlyRequests != null) {
            hourLimit = numberOfHourlyRequests.intValue();
        } else {
            hourLimit = defaultLimit;
        }
    }

    public void resetThreshold() {
        visitCounter.set(0);
    }

    public boolean isUnderThreshold() {
        int count = visitCounter.incrementAndGet();
        return count <= hourLimit;
    }
}
