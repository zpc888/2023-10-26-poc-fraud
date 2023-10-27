package com.prot.poc.fraud.service;

import com.prot.poc.fraud.model.Fraud;
import com.prot.poc.fraud.model.FraudAttestation;
import com.prot.poc.fraud.model.FraudItem;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.util.Matrix;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;


/**
 * @Author: <a href="mailto: pengcheng.zhou@gmail.com">PengCheng Zhou</a>
 * @Created: 2023-10-26T22:45 Thursday
 */

class FraudAttestationPDFGenTest {
    FraudAttestation attestation;
    Fraud fraud;

    @BeforeEach
    void setup() {
        FraudItem item1 = new FraudItem(new Date(), "Walmart", "7376373837", 38.10);
        FraudItem item2 = new FraudItem(new Date(), "Costco", "7376373898", 122.70);
        fraud = new Fraud("FA-0001", "David Smith",
                "Fraud Reason 2", null,
                Arrays.asList(item1, item2));
        attestation = new FraudAttestation(null, fraud, null);
    }

    @Test
    void testGen() throws Exception {
        genTo("/tmp/test-fraud-attestation.pdf");
    }

    private void genTo(String fileName) throws Exception {
        byte[] pdf = new FraudAttestationPDFGen().genAttestationReport(attestation);
        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            fos.write(pdf, 0, pdf.length);
            fos.flush();
        }
    }

    @Test
    void testSignWithWatermark() throws Exception {
        final String fileName = "/tmp/test-fraud-attestation.pdf";
        genTo(fileName);
        File file = new File(fileName);
        PDDocument doc = Loader.loadPDF(file);
        String signInfo = "Signed by " + fraud.clientName() + " at "
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        PDFont font = new PDType1Font(Standard14Fonts.FontName.TIMES_ITALIC);
        float fontSize = 16.0f;
        for (PDPage page: doc.getPages()) {
            PDPageContentStream stream = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true, true);
            PDResources resources = page.getResources();
            PDExtendedGraphicsState r0 = new PDExtendedGraphicsState();
            r0.setNonStrokingAlphaConstant(0.5f);
            stream.setGraphicsStateParameters(r0);
            stream.setNonStrokingColor(Color.RED);    //Red
            stream.beginText();
            stream.setFont(font, fontSize);
            stream.setTextMatrix(Matrix.getTranslateInstance(0f,0f));
            stream.newLineAtOffset(80, 300);
            stream.showText(signInfo);
            stream.endText();
            stream.close();
        }
        doc.save(new File("/tmp/signed-fraud-attestation.pdf"));
        doc.close();
    }

    @Test
    void testAddWatermark() throws Exception {
        FraudAttestationPDFGen pdfGen = new FraudAttestationPDFGen();
        byte[] pdf1 = pdfGen.genAttestationReport(attestation);
        byte[] pdf2 = pdfGen.addWatermarkSign(pdf1, "George Washington");
        try (FileOutputStream fos = new FileOutputStream("/tmp/signed-by-gw.pdf")) {
            fos.write(pdf2, 0, pdf2.length);
            fos.flush();
        }
    }
}