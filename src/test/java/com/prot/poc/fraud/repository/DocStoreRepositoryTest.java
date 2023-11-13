package com.prot.poc.fraud.repository;

import com.prot.poc.fraud.entity.DocStatus;
import com.prot.poc.fraud.entity.DocStore;
import com.prot.poc.fraud.model.CallbackInfo;
import com.prot.poc.fraud.model.Client;
import com.prot.poc.common.JSONUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @Author: <a href="mailto: pengcheng.zhou@gmail.com">PengCheng Zhou</a>
 * @Created: 2023-10-27T23:24 Friday
 */

@DataJpaTest
class DocStoreRepositoryTest {
    @Autowired
    DocStoreRepository repository;

    DocStore doc1, doc2, doc3;
    byte[] pdf1, pdf2, pdf3;

    @BeforeEach
    void init() {
        pdf1 = new byte[]{1, 2, 3, 4};
        pdf2 = new byte[]{5, 6, 7, 8, 9, 10};
        pdf3 = new byte[]{2, 3};

        doc1 = new DocStore();
        doc2 = new DocStore();
        doc3 = new DocStore();
        doc1.setVendorName("gz");
        doc1.setSourceNumber("f-001");
        doc1.setStatus(DocStatus.GENERATED);

        doc2.setVendorName("gz");
        doc2.setSourceNumber("f-002");
        doc2.setStatus(DocStatus.GENERATED);
        doc2.setContent(pdf2);

        doc3.setVendorName("gz");
        doc3.setSourceNumber("f-003");
        doc3.setStatus(DocStatus.SIGNED);
        doc3.setContent(pdf3);

        repository.deleteAll();
    }

    @Test
    void testWithBigData() {
        Client client = new Client("7373874", "George Zhou", "George.Zhou@global.com", "1-658-8272");
        CallbackInfo callbackInfo = new CallbackInfo("https://salesforce.com/apex/services/v1/receiver",
                "https://.../oauth/token", "grant_type", "protTech", "GuessWhat");
        int size = 512*1024;
        byte[] pdf = new byte[size];
        for (int i = 0; i < size; i++) {
            pdf[i] = (byte) i;
        }
        doc1.setContent(pdf);
        doc1.setDocName("Testing Document 01");
        doc1.setSignerInfo(JSONUtils.toJSONString(client));
        doc1.setCallbackInfo(JSONUtils.toJSONStringNicely(callbackInfo));
        repository.save(doc1);
        List<DocStore> all = repository.findAll();
        assertEquals(1, all.size());
        DocStore out = all.get(0);
        assertEquals(pdf.length, out.getContent().length);
        for (int i = 0; i < size; i++) {
            assertEquals(pdf[i], out.getContent()[i]);
        }
        assertEquals("Testing Document 01", out.getDocName());
        assertEquals(doc1.getSignerInfo(), out.getSignerInfo());
        assertEquals(doc1.getCallbackInfo(), out.getCallbackInfo());
        assertEquals(client, JSONUtils.fromJSONString(out.getSignerInfo(), Client.class));
        assertEquals(callbackInfo, JSONUtils.fromJSONString(out.getCallbackInfo(), CallbackInfo.class));
    }

    @Test
    void testRepository() {
        List<DocStore> all = repository.findAll();
        assertEquals(0, all.size());
        repository.save(doc1);
        repository.save(doc2);
        assertNotNull(doc1.getId());
        assertNotNull(doc2.getId());
        assertNull(doc3.getId());
        repository.save(doc3);
        assertNotNull(doc3.getId());

        all = repository.findAll();
        assertEquals(3, all.size());

        assertNull(doc1.getContent());
        assertEquals(pdf2.length, doc2.getContent().length);
        assertEquals(pdf3.length, doc3.getContent().length);

        doc1.setContent(pdf3);
        repository.save(doc1);
        doc3.setContent(pdf1);
        repository.save(doc3);

        all = repository.findAll();
        assertEquals(3, all.size());
        doc1 = all.get(0);
        doc2 = all.get(1);
        doc3 = all.get(2);
        assertEquals(pdf3.length, doc1.getContent().length);
        assertEquals(pdf2.length, doc2.getContent().length);
        assertEquals(pdf1.length, doc3.getContent().length);

        Optional<DocStore> query1 = repository.findDocByVendorInfo("f-012", "gz");
        assertFalse(query1.isPresent());

        Optional<DocStore> query2 = repository.findDocByVendorInfo("f-002", "gz");
        assertTrue(query2.isPresent());
        // assertEquals(3, query2.get().getId().intValue());    // depending on other methods execution order
    }

}