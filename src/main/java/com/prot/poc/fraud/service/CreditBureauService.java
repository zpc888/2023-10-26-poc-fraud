package com.prot.poc.fraud.service;

import com.prot.poc.fraud.entity.DocStatus;
import com.prot.poc.fraud.entity.DocStore;
import com.prot.poc.fraud.model.Client;
import com.prot.poc.fraud.model.CreditBureauReport;
import com.prot.poc.fraud.model.CreditData;
import com.prot.poc.fraud.model.DocumentResult;
import com.prot.poc.fraud.pdfgen.CreditReportPDFGen;
import com.prot.poc.fraud.repository.DocStoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.Date;
import java.util.Random;

/**
 * @Author: <a href="mailto: pengcheng.zhou@gmail.com">PengCheng Zhou</a>
 * @Created: 2023-10-29T00:39 Sunday
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CreditBureauService {
    private final CreditReportPDFGen pdfGen;
    private final DocStoreRepository repository;

    private Random random = new Random(System.currentTimeMillis());

    public CreditBureauReport getCreditReport(String vendorId, Client client) throws Exception {
        CreditData fromExternalServiceProvider = getCBRFromProvicer(client);
        byte[] pdfBytes = pdfGen.genCreditReport(client, fromExternalServiceProvider);

        DocStore docStore = new DocStore();
        docStore.setDocName("Client Credit Report");
        docStore.setVendorName(vendorId);
        docStore.setStatus(DocStatus.GENERATED);
        docStore.setSourceNumber(client.id() == null ? client.fullName() : client.id());
        docStore.setContent(pdfBytes);
        repository.save(docStore);

        String encoded = Base64.getEncoder().encodeToString(pdfBytes);
        return new CreditBureauReport(fromExternalServiceProvider,
                new DocumentResult(docStore.getId().toString(), DocStatus.GENERATED.toString(), encoded));
    }

    private CreditData getCBRFromProvicer(Client client) {
        int creditScore = random.nextInt(100);
        return new CreditData("" + creditScore, new Date());
    }
}
