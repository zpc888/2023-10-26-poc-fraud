package com.prot.poc.esign.docusign;

import com.prot.poc.common.JSONUtils;
import com.prot.poc.esign.vo.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @Author: <a href="mailto: pengcheng.zhou@gmail.com">PengCheng Zhou</a>
 * @Created: 2023-11-12T08:51 Sunday
 */
@ExtendWith(SpringExtension.class)
@EnableConfigurationProperties(value = DocuSignConfig.class)
@TestPropertySource("classpath:docusign-config-test.properties")
class DocuSignServiceTest {
    private static final String VISIBLE_ANCHOR_PDF = "test-esign-fraud-attestation.pdf";
    private static final String INVISIBLE_ANCHOR_PDF = "test-esign-with-invisible-anchor.pdf";
    private static final String ROLE_NAME = "signer";
    
    @Autowired
    DocuSignConfig docuSignConfig;
    DocuSignService service;
    SignPackage pkgWithVisibleAnchorAndXY;
    SignPackage pkgWithInvisibleAnchorOnly;

    static byte[] loadPDF(String resourcePDF) {
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePDF)) {
            byte[] buf = new byte[1024];
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            for (int cnt = -1; (cnt = is.read(buf)) != -1;) {
                baos.write(buf, 0, cnt);
            }
            return baos.toByteArray();
        } catch (Exception ex) {
            throw new RuntimeException("Fail to load pdf: " + resourcePDF, ex);
        }
    }

    @BeforeEach
    void setup() {
        service = new DocuSignService(docuSignConfig, docuSignConfig.docusignPkiResolver());

        pkgWithVisibleAnchorAndXY = buildPackage(
                buildVisibleAnchorAndXYTouch(ROLE_NAME),
                VISIBLE_ANCHOR_PDF,
                "test (300, 300) and visible anchors to 'initiator' and 'Signature'");
        pkgWithInvisibleAnchorOnly = buildPackage(
                buildInvisibleAnchorTouch(ROLE_NAME),
                INVISIBLE_ANCHOR_PDF,
                "test fraud invisible anchors to '_signature_' and '_signed_date_'");
    }

    private SignPackage buildPackage(List<TouchType> touches, String pdfResource, String subjectPrefix) {
        SignPackage ret = new SignPackage();
        LocalDateTime now = LocalDateTime.now();
        SignPackage.SignDocument signDoc = new SignPackage.SignDocument();
        Supplier<byte[]> fraudAttestationContent = () -> loadPDF(pdfResource);
        signDoc.setId("1").setName("fraud-attestation").setContentSupplier(fraudAttestationContent);
        signDoc.setTouches(touches);
        ret.setEmailSubject(subjectPrefix + " at " + now)
                .setEmailBody("Sent from unit test. Original point is top-left, which is different from PDF box, whose (0, 0) is at bottom-left")
                .setRoleToRecipientIds(Collections.singletonMap(ROLE_NAME, "10080"))
                .setRecipients(List.of(new SignPackage.Recipient().setId("10080")
                        .setName("George Gmail").setEmail("pengcheng.zhou@gmail.com")
                        .setClientUserId(null).setSignAuth(SignAuth.NONE)))
                .setSignedDocuments(List.of(signDoc));
        return ret;
    }

    private static List<TouchType> buildVisibleAnchorAndXYTouch(String roleName) {
        List<TouchType> ret = new ArrayList<>();
        ret.add(new TouchType.SignatureType().setToucherRole(roleName)
                .setLocation(new TouchLocation.ViaXY(1, 300, 300))
        );
        // anchor is just a text rendered in PDF, no special attributes. They should be rendered in background color
        // so that they are invisible, otherwise they will be covered with the signature, date, etc that docusign puts on.
        ret.add(new TouchType.SignatureType().setToucherRole(roleName)
                .setLocation(new TouchLocation.ViaAnchor("initiator", 0, 0))
        );
        ret.add(new TouchType.SignatureType().setToucherRole(roleName)
                .setLocation(new TouchLocation.ViaAnchor("Signature", 100, 0))
        );
        return ret;
    }

    private static List<TouchType> buildInvisibleAnchorTouch(String roleName) {
        List<TouchType> ret = new ArrayList<>();
        // anchor is just a text rendered in PDF, no special attributes. They should be rendered in background color
        // so that they are invisible, otherwise they will be covered with the signature, date, etc that docusign puts on.
        ret.add(new TouchType.SignatureType().setToucherRole(roleName)
                .setLocation(new TouchLocation.ViaAnchor("_signature_", 0, 0))
        );
        ret.add(new TouchType.DateSignedType().setToucherRole(roleName)
                .setLocation(new TouchLocation.ViaAnchor("_signed_date_", 0, 0))
        );
        // Fullname is PengCheng Zhou
//        ret.add(new TouchType.FullnameType().setToucherRole(roleName)
//                .setLocation(new TouchLocation.ViaAnchor("_printed_name_", 0, 0))
//        );
        ret.add(new TouchType.TextType().setValue("George(PengCheng) Zhou").setToucherRole(roleName)
                .setLocation(new TouchLocation.ViaAnchor("_printed_name_", 0, 0))
        );
        return ret;
    }

    @Test
    void sendVisibleAnchorForSign() {
        assertNotNull(docuSignConfig);
        assertNotNull(service);
        sendPkgForSign(pkgWithVisibleAnchorAndXY);
    }

    private void sendPkgForSign(SignPackage signPackage) {
        SignPackageResult result = service.sendForSign(signPackage);
        System.out.println("---------------- REQUEST  ----------------");
        System.out.println(JSONUtils.toJSONStringNicely(signPackage));
        System.out.println("---------------- RESPONSE ----------------");
        System.out.println(JSONUtils.toJSONStringNicely(result));
    }

    @Test
    void sendInvisibleAnchorForSign() {
        sendPkgForSign(pkgWithInvisibleAnchorOnly);
    }
}