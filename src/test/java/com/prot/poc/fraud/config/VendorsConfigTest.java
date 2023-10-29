package com.prot.poc.fraud.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @Author: <a href="mailto: pengcheng.zhou@gmail.com">PengCheng Zhou</a>
 * @Created: 2023-10-28T16:20 Saturday
 */
@ExtendWith(SpringExtension.class)
@EnableConfigurationProperties(value = VendorsConfig.class)
@TestPropertySource("classpath:vendors-config-test.properties")
class VendorsConfigTest {
    @Autowired
    private VendorsConfig vendorsConfig;

    @Test
    void testHappyPath() {
        assertEquals(200, vendorsConfig.getDefaultNumberOfHourlyRequests().intValue());
        assertEquals(4, vendorsConfig.getMap().size());
        verify(vendorsConfig.getMap().get("accenture"), "accenture", "1xy", 208, 208);
        verify(vendorsConfig.getMap().get("deloitte"), "deloitte", "2ab", 209, 209);
        verify(vendorsConfig.getMap().get("v1"), "v1", "1234", null, 200);
        verify(vendorsConfig.getMap().get("v2"), "v2", "12345", 0, 0);
        assertEquals(new VendorAuthInfo(), vendorsConfig.getAuth());
    }

    private void verify(VendorInfo v, String clientId, String clientSecret, Integer hourLimit, int allowToVisitCount) {
        assertEquals(clientId, v.getClientId());
        assertEquals(clientSecret, v.getClientSecret());
        assertEquals(hourLimit, v.getNumberOfHourlyRequests());
        for (int i = 0; i < allowToVisitCount; i++) {
            assertTrue(v.isUnderThreshold());
        }
        assertFalse(v.isUnderThreshold());
    }
}