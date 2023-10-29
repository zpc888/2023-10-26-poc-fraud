package com.prot.poc.fraud.util;

import com.prot.poc.fraud.pdfgen.PDFGenUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.Test;

/**
 * @Author: <a href="mailto: pengcheng.zhou@gmail.com">PengCheng Zhou</a>
 * @Created: 2023-10-26T18:11 Thursday
 */
public class PDFGenSizeTest {
    @Test
    void testXYSerial() throws Exception {
        PDDocument doc = new PDDocument();
        for (int i = 0; i < 3; i++) {
            PDPage page = new PDPage();
            PDPageContentStream stream = new PDPageContentStream(doc, page);
            PDFont font = new PDType1Font(i  == 0 ? Standard14Fonts.FontName.TIMES_ROMAN : (
                    i == 1 ? Standard14Fonts.FontName.COURIER : Standard14Fonts.FontName.HELVETICA_BOLD) );
            final int fontSize = 16;
            float fontHeight = PDFGenUtils.getFontHeight(font, fontSize);
            float fontWidth = PDFGenUtils.getTextFontWidth("1234567890", font, fontSize) / 10;
            PDRectangle pageSize = page.getMediaBox();
            float pageHeight = pageSize.getHeight();
            float pageWidth = pageSize.getWidth();
            String str = String.format("Page width = %d, height = %d; font width = %d, height = %d",
                    (int)pageWidth, (int)pageHeight, (int)fontWidth, (int)fontHeight);
            System.out.println(str);

            stream.beginText();
            stream.setFont(font, fontSize);
            stream.newLineAtOffset(10, pageHeight - fontHeight - 8);
            stream.showText(str);
            stream.endText();

            stream.moveTo(0, 0);
            // print diagonally
            float ih = fontHeight;
            float iw = fontWidth;
            int cnt = 1;
            stream.beginText();
            while ((ih * cnt) < pageHeight && (iw * cnt) < pageWidth) {
                stream.newLineAtOffset(iw, ih);
                stream.showText("" + cnt);
                cnt++;
            }
            stream.endText();

            stream.moveTo(0, 0);

            stream.beginText();
            stream.newLineAtOffset(10, pageHeight/2);
            stream.showText("Count ends at: " + cnt);               // 45, 47, 42
            stream.endText();

            stream.close();
            doc.addPage(page);
        }
        doc.save("/tmp/t1.pdf");
        System.out.println("PDF is generated at /tmp/t1.pdf");
        doc.close();
    }
}
