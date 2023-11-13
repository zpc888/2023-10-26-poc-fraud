package com.prot.poc.esign.docusign;

import com.prot.poc.esign.PkiResolver;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * @Author: <a href="mailto: pengcheng.zhou@gmail.com">PengCheng Zhou</a>
 * @Created: 2023-11-05T13:37 Sunday
 */
@Data
@Slf4j
@Configuration
@ConfigurationProperties(prefix="app.docusign.config")
public class DocuSignConfig {
    private String clientId;        // integration key
    private String userId;          // api user name (user guid to impersonate)
    private String baseApiUrl;
    private String baseOauthUrl;
    private int oauthTokenExpiryInSeconds;
    private String esignEventListenerUrl;
    private String rsaKeyResourceName;

    @Bean
    public PkiResolver docusignPkiResolver() {
        return new PkiResolver() {
            @Override
            public byte[] resolvePrivateKey() {
                try (InputStream is = openFileThenResource(rsaKeyResourceName)) {
                    return readOutAsBytes(is);
                } catch (Exception ex) {
                    final String errMsg = "fail to read docusign key from: " + rsaKeyResourceName;
                    log.error(errMsg, ex);
                    throw new RuntimeException(errMsg, ex);
                }
            }

            @Override
            public byte[] resolvePublicKey() {
                return new byte[0];
            }
        };
    }

    static InputStream openFileThenResource(String name) throws Exception {
        File f = new File(name);
        if (f.exists()) {
            return new FileInputStream(f);
        } else {
            return Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
        }
    }

    static byte[] readOutAsBytes(InputStream in) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(2048);
        byte[] buf = new byte[1024];
        for (int cnt = -1; (cnt = in.read(buf)) != -1; ) {
            baos.write(buf, 0, cnt);
        }
        return baos.toByteArray();
    }
}
