package com.prot.poc.fraud.config;

import jakarta.validation.Valid;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * @Author: <a href="mailto: pengcheng.zhou@gmail.com">PengCheng Zhou</a>
 * @Created: 2023-10-28T15:56 Saturday
 */
@Data
@Slf4j
@Configuration
@ConfigurationProperties(prefix="app.vendors.config")
// @Validated
public class VendorsConfig implements InitializingBean {
    private Integer defaultNumberOfHourlyRequests;

    @Valid
    private Map<String, VendorInfo> map;

    private VendorAuthInfo auth = new VendorAuthInfo();

    @Override
    public void afterPropertiesSet() throws Exception {
        if (map == null || map.isEmpty()) {
            return;
        }
        int defaultLimit = defaultNumberOfHourlyRequests == null ? 200 : defaultNumberOfHourlyRequests.intValue();
        for (VendorInfo vendor: map.values()) {
            vendor.setDefaultNumberOfHourlyRequests(defaultLimit);
        }
    }

    public void resetThreshold() {
        for (VendorInfo v: map.values()) {
            v.resetThreshold();
        }
    }

    public boolean isAuthenticated(String clientId, String clientSecret) {
        VendorInfo vendor = getVendorByClientId(clientId);
        return vendor != null && clientSecret != null && clientSecret.equals(vendor.getClientSecret());
    }

    public boolean isUnderThreshold(String clientId) {
        VendorInfo vendor = getVendorByClientId(clientId);
        return vendor != null && vendor.isUnderThreshold();
    }

    private VendorInfo getVendorByClientId(String clientId) {
        if (clientId == null) {
            return null;
        }
        for (VendorInfo v: map.values()) {
            if (clientId.equals(v.getClientId())) {
                return v;
            }
        }
        log.error("No vendor client id '{}' found!!!", clientId);
        return null;
    }
}
