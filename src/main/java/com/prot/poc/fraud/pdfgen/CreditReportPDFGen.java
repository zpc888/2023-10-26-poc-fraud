package com.prot.poc.fraud.pdfgen;

import com.prot.poc.fraud.model.Client;
import com.prot.poc.fraud.model.CreditBureauReport;
import com.prot.poc.fraud.model.CreditData;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * @Author: <a href="mailto: pengcheng.zhou@gmail.com">PengCheng Zhou</a>
 * @Created: 2023-10-26T21:43 Thursday
 */
@Service
public class CreditReportPDFGen {

    public byte[] genCreditReport(Client client, CreditData report) throws Exception {
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
        String text = "Credit Bureau Report";
        float textWidth = PDFGenUtils.getTextFontWidth(text, font, fontSize);
        float titleOffsetY = (float) (pageHeight - brandHeight - fontHeight * 1.6);
        PDFGenUtils.showSingleLine(stream, text, (pageWidth - textWidth)/2, titleOffsetY, font, fontSize, Color.BLACK);

        // line separator
        stream.setNonStrokingColor(Color.LIGHT_GRAY);
        stream.addRect(0, titleOffsetY - 6, pageWidth, 1);
        stream.fill();

        // content
        final float lineSpan = 1.5f;
        final float leading = 100;
        PDFont contentFont = new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN);
        int contentFontSize = 16;
        float contentFontHeight = PDFGenUtils.getFontHeight(contentFont, contentFontSize);

        stream.beginText();
        stream.setNonStrokingColor(Color.BLACK);
        stream.setFont(contentFont, contentFontSize);

        final float col2Leanding = 200;

        // --- client name
        stream.newLineAtOffset(leading, titleOffsetY - 2 * contentFontHeight * 1.6f);
        stream.showText("Client Name");
        stream.newLineAtOffset(col2Leanding, 0);
        stream.showText(": " + client.fullName());

        // --- Credit score
        stream.newLineAtOffset(-col2Leanding, -contentFontHeight * 2);
        stream.showText("Credit Score");
        stream.newLineAtOffset(col2Leanding, 0);
        stream.showText(": " + report.creditScore());

        // --- Credit date
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String creditDate = report.creditDate().toInstant().atZone(ZoneId.systemDefault())
                .toLocalDateTime().format(formatter);

        stream.newLineAtOffset(-col2Leanding, -contentFontHeight * 2);
        stream.showText("Credit Date");
        stream.newLineAtOffset(col2Leanding, 0);
        stream.showText(": " + creditDate);

        // --- etc
        stream.newLineAtOffset(-col2Leanding, -contentFontHeight * 2);
        stream.showText("... ...");
        stream.newLineAtOffset(col2Leanding, 0);
        stream.showText(": ... ...");

        // end text rendering
        stream.endText();

        // footer
        renderFooter(stream, contentFontHeight, pageWidth, leading);

        stream.close();

        return PDFGenUtils.saveDocToByteArray(doc);
    }

    private static void renderFooter(PDPageContentStream stream, float contentFontHeight, float pageWidth, float leading) throws IOException {
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
        PDFGenUtils.showSingleLine(stream, footer, x, 20, footerFont, footerFontSize, Color.BLACK);
    }

}
