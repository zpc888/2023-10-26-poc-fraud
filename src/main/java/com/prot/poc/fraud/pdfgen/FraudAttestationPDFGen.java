package com.prot.poc.fraud.pdfgen;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.EAN13Writer;
import com.google.zxing.qrcode.QRCodeWriter;
import com.prot.poc.fraud.model.FraudAttestation;
import com.prot.poc.fraud.model.FraudItem;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.util.Matrix;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: <a href="mailto: pengcheng.zhou@gmail.com">PengCheng Zhou</a>
 * @Created: 2023-10-26T21:43 Thursday
 */
@Slf4j
@Service
public class FraudAttestationPDFGen {
    public byte[] addWatermarkSign(byte[] pdf, String clientName) throws Exception {
        PDDocument doc = Loader.loadPDF(pdf);
        String signInfo = "Signed by " + clientName + " at "
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        PDFont font = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        float fontSize = 16.0f;
        for (PDPage page: doc.getPages()) {
            PDPageContentStream stream = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true, true);
//            PDResources resources = page.getResources();
            PDExtendedGraphicsState r0 = new PDExtendedGraphicsState();
            r0.setNonStrokingAlphaConstant(0.5f);
            stream.setGraphicsStateParameters(r0);
            stream.setNonStrokingColor(Color.RED);    //Red
            stream.beginText();
            stream.setFont(font, fontSize);
            stream.setTextMatrix(Matrix.getTranslateInstance(0f,0f));
            stream.newLineAtOffset(80, page.getMediaBox().getHeight() / 3);
            stream.showText(signInfo);
            stream.endText();
            stream.close();
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        doc.save(baos);
        doc.close();
        return baos.toByteArray();
    }

    public byte[] genAttestationReport(FraudAttestation vo) throws Exception {
        PDDocument doc = new PDDocument();
        PDPage page = new PDPage();
        doc.addPage(page);
        PDRectangle pageRect = page.getMediaBox();
        float pageHeight = pageRect.getHeight();
        float pageWidth = pageRect.getWidth();
        PDPageContentStream stream = new PDPageContentStream(doc, page);

        final int brandHeight = 30;
        stream.setNonStrokingColor(PDFGenUtils.BG_COLOR);
        stream.addRect(0, pageHeight - brandHeight, pageWidth, brandHeight);
        stream.fill();

        // title
        PDFont font = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        int fontSize = 24;
        float fontHeight = PDFGenUtils.getFontHeight(font, fontSize);
        String text = vo.fraud().fraudNumber() + " Fraud Report";
        float textWidth = PDFGenUtils.getTextFontWidth(text, font, fontSize);
        float titleOffsetY = (float) (pageHeight - brandHeight - fontHeight * 1.6);
        PDFGenUtils.showSingleLine(stream, text, (pageWidth - textWidth)/2, titleOffsetY, font, fontSize, Color.BLACK);

        // line separator
        stream.setNonStrokingColor(Color.LIGHT_GRAY);
        stream.addRect(0, titleOffsetY - 6, pageWidth, 1);
        stream.fill();

        // content
        final float lineSpan = 1.5f;
        final float leading = 18;
        PDFont contentFont = new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN);
        int contentFontSize = 16;
        float contentFontHeight = PDFGenUtils.getFontHeight(contentFont, contentFontSize);

        stream.beginText();
        stream.setNonStrokingColor(Color.BLACK);
        stream.setFont(contentFont, contentFontSize);

        // --- to who
        String toWho = "Dear " + vo.fraud().clientName() + ":";
        stream.newLineAtOffset(leading, titleOffsetY - 2 * contentFontHeight * 1.6f);
        stream.showText(toWho);

        // fraud reason
        continueToRenderFraudReason(vo, stream, contentFontHeight, lineSpan);

        // signature
        stream.newLineAtOffset(200, (float)(-12 * contentFontHeight * lineSpan));             // offset x and y
        stream.showText("Signature: ");
        stream.newLineAtOffset(0, (float)(-2 * contentFontHeight * lineSpan));                // offset x and y
        stream.showText("Printed Name: ");
        stream.newLineAtOffset(0, (float)(-2 * contentFontHeight * lineSpan));                // offset x and y
        stream.showText("Signed Date: ");

        // esign anchor
        stream.setNonStrokingColor(Color.WHITE);
        stream.newLineAtOffset(110, (float)(4 * contentFontHeight * lineSpan));             // offset x and y
        stream.showText("_signature_");
        stream.newLineAtOffset(0, (float)(-2 * contentFontHeight * lineSpan));              // offset x and y
        stream.showText("_printed_name_");
        stream.newLineAtOffset(0, (float)(-2 * contentFontHeight * lineSpan));              // offset x and y
        stream.showText("_signed_date_");
        // temporarily test duplicated anchors
        // if 1 anchor was duplicated 3 times, it will ask to sign 3 times.
        // if anchor was added twice, it will ask to sign 3 * 2 times, i.e. each anchor will be asked 3 times.
        /*
        stream.newLineAtOffset(0, (float)(-2 * contentFontHeight * lineSpan));
        stream.showText("_signature_");
        stream.newLineAtOffset(0, (float)(-2 * contentFontHeight * lineSpan));
        stream.showText("_signed_date_");
         */

        stream.endText();

        // footer
        renderFooter(doc, vo, stream, contentFontHeight, pageWidth, leading);

        // transaction item tables
        renderTransactionItem(stream, vo, pageHeight);

        stream.close();

        return PDFGenUtils.saveDocToByteArray(doc);
    }


    private void renderTransactionItem(PDPageContentStream stream, FraudAttestation vo, float pageHeight) throws Exception {
        PDFTableHeader txDateHeader = new PDFTableHeader("Transaction Date", 120, Alignment.LEFT);
        PDFTableHeader merchantHeader = new PDFTableHeader("Merchant", 240, Alignment.LEFT);
        PDFTableHeader cardHeader = new PDFTableHeader("Card Number", 100, Alignment.LEFT);
        PDFTableHeader amountHeader = new PDFTableHeader("Amount", 100, Alignment.RIGHT);
        List<String[]> items = vo.fraud().items().stream().map(this::mapTxItemToRow).collect(Collectors.toList());
        double totalAmount = 0;
        for (FraudItem item: vo.fraud().items()) {
            totalAmount += item.amount();
        }
        Color headFillColor = new Color(240, 93, 11);
        PDFTable table = new PDFTable(20, pageHeight * 2 / 3, 30,
                new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN), 12, Color.BLACK,
                new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN), 12, Color.BLACK,
                PDFGenUtils.BG_COLOR, new Color(219, 218, 198),
                new PDFTableHeader[]{txDateHeader, merchantHeader, cardHeader, amountHeader},
                items, new String[]{"", "", "", NumberFormat.getCurrencyInstance().format(totalAmount)});
        PDFGenUtils.showTable(stream, table);
    }

    private String[] mapTxItemToRow(FraudItem item) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String txDate = item.txDate().toInstant().atZone(ZoneId.systemDefault())
                .toLocalDateTime().format(formatter);
        NumberFormat nf = NumberFormat.getCurrencyInstance();
        String amount = nf.format(item.amount());
        return new String[]{ txDate, item.merchant(), item.cardNumber(), amount };
    }

    private static void continueToRenderFraudReason(FraudAttestation vo, PDPageContentStream stream, float contentFontHeight, float lineSpan) throws IOException {
        stream.newLineAtOffset(0, (float)(-contentFontHeight * lineSpan * 1.2));                // offset x and y
        String reason = vo.fraud().otherReasonDetail() == null ? vo.fraud().fraudReason() : vo.fraud().otherReasonDetail();
        stream.showText("Based on ");
        stream.setNonStrokingColor(Color.RED);
        stream.showText(reason);
        stream.setNonStrokingColor(Color.BLACK);
        stream.showText(", these are the fraudulent transactions.");

        stream.newLineAtOffset(0, (float)(-contentFontHeight * lineSpan * 1.1));                // offset x and y
        stream.showText("If you are not the initiator of these transaction, please sign to confirm.");
    }

    private static void renderFooter(PDDocument doc, FraudAttestation vo, PDPageContentStream stream, float contentFontHeight, float pageWidth, float leading) throws IOException {
        // footer line seperator
        stream.setNonStrokingColor(PDFGenUtils.BG_COLOR);
        stream.addRect(0, (float)(contentFontHeight * 1.6), pageWidth, 2);
        stream.fill();

        // footer date
        PDFont footerFont = new PDType1Font(Standard14Fonts.FontName.COURIER);
        int footerFontSize = 8;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String footer = LocalDateTime.now().format(formatter);
        float x = pageWidth - PDFGenUtils.getTextFontWidth(footer, footerFont, footerFontSize) - leading;
        float y = 20;
        PDFGenUtils.showSingleLine(stream, footer, x, y, footerFont, footerFontSize, Color.BLACK);

        // draw barcode
//        int imgWidth = 300;
//        int imgHeight = 100;
//        BufferedImage barcodeImg = genBarcodeBytes(vo, imgWidth, imgHeight);
        try {
            int imgWidth = 200;
            int imgHeight = 200;
            BufferedImage barcodeImg = genQRCode(vo, imgWidth, imgHeight);
            PDImageXObject barcode = LosslessFactory.createFromImage(doc, barcodeImg);
            stream.drawImage(barcode, 0, y + 12);
        } catch (WriterException we) {
            log.error("fail to generate qr code for " + vo.fraud().fraudNumber(), we);
        }
    }

    private static BufferedImage genQRCode(FraudAttestation vo, int width, int height) throws WriterException {
        String barcodeText = vo.fraud().fraudNumber() + " " + vo.fraud().clientName();
        QRCodeWriter barcodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix =
                barcodeWriter.encode(barcodeText, BarcodeFormat.QR_CODE, width, height);
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }

    private static BufferedImage genBarcodeEAN13(FraudAttestation vo, int width, int height) {
        String barcodeText = vo.fraud().fraudNumber() + " " + vo.fraud().clientName();
        if (barcodeText.length() != 12 && barcodeText.length() != 13) {
            // normalize to 12 or 13 digits         // TODO:
        }
        EAN13Writer ean13Writer = new EAN13Writer();
        BitMatrix bitMatrix = ean13Writer.encode(barcodeText, BarcodeFormat.EAN_13, width, height);
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }
}
