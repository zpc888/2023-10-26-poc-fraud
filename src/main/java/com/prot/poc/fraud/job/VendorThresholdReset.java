package com.prot.poc.fraud.job;

import com.prot.poc.fraud.config.VendorsConfig;
import lombok.extern.slf4j.Slf4j;
//import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

//import java.util.concurrent.TimeUnit;

/**
 * @Author: <a href="mailto: pengcheng.zhou@gmail.com">PengCheng Zhou</a>
 * @Created: 2023-10-28T21:01 Saturday
 */

@Slf4j
@Component
public class VendorThresholdReset {
    private final VendorsConfig vendorsConfig;

    public VendorThresholdReset(VendorsConfig vendorsConfig) {
        this.vendorsConfig = vendorsConfig;
    }

//    @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.MINUTES, initialDelay = 1)
    public void resetAllVendorsThreshold() {
        log.debug("reset all vendors' threshold");
        vendorsConfig.resetThreshold();
    }
}
