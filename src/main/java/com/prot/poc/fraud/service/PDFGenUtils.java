package com.prot.poc.fraud.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @Author: <a href="mailto: pengcheng.zhou@gmail.com">PengCheng Zhou</a>
 * @Created: 2023-10-26T17:34 Thursday
 */
public class PDFGenUtils {
    public static final Color BG_COLOR = new Color(196, 31, 62);   // #C41F3E

    public static float getFontHeight(PDFont font, int fontSize) {
        return font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * fontSize;
    }

    public static float getTextFontWidth(String text, PDFont font, int fontSize) throws IOException {
        return font.getStringWidth(text) / 1000 * fontSize;
    }

    public static byte[] loadResource(String resourceName) throws IOException {
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName)) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            for (int count = -1; (count = is.read(buf)) != -1;) {
                baos.write(buf, 0, count);
            }
            return baos.toByteArray();
        }
    }

    public static void saveDoc(PDDocument doc, OutputStream out) throws IOException {
        doc.save(out);
        doc.close();
    }

    public static byte[] saveDocToByteArray(PDDocument doc) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        saveDoc(doc, baos);
        return baos.toByteArray();
    }

    public static void drawResourceImage(PDDocument doc, PDPageContentStream stream,
                                         String resourceImage, float x, float y) throws IOException {
        byte[] images = loadResource(resourceImage);
        PDImageXObject image = PDImageXObject.createFromByteArray(doc, images, resourceImage);
//        stream.setNonStrokingColor(nonStrokingColor);
        stream.drawImage(image, x, y);
    }

    public static void showSingleLine(PDPageContentStream stream, String text, float x, float y,
                                      PDFont font, float fontSize, Color color) throws IOException {
        stream.beginText();
        stream.setFont(font, fontSize);
        if (color != null) {
            stream.setNonStrokingColor(color);
        }
        stream.newLineAtOffset(x, y);
        stream.showText(text);
        stream.endText();
        stream.moveTo(0, 0);
    }

    public static void showMultiLines(PDPageContentStream stream, String[] texts,
                                      float leading, float x, float y,
                                      PDFont font, float fontSize, Color color) throws IOException {
        stream.beginText();
        stream.setFont(font, fontSize);
        if (color != null) {
            stream.setNonStrokingColor(color);
        }
        stream.setLeading(leading);
        stream.newLineAtOffset(x, y);
        for (String text: texts) {
            stream.showText(text);
            stream.newLine();
        }
        stream.endText();
        stream.moveTo(0, 0);
    }

    public static void showTable(PDPageContentStream stream, PDFTable table) throws Exception {
        // render table header
        PDFTableHeader[] headers = table.headers();
        renderTableHeaders(table.x(), table.y(), stream, table, headers);
        float y = table.y();
        for (String[] row: table.rows()) {
            y = y - table.cellHeight();
            renderTableRow(table.x(), y, stream, table, headers, row);
        }
        y = y - table.cellHeight();
        renderTableFoot(table.x(), y, stream, table, headers, table.footers());
    }

    private static void renderTableFoot(float x, float y, PDPageContentStream stream, PDFTable table, PDFTableHeader[] headers, String[] row) throws Exception {
        for (int i = 0; i < headers.length; i++) {
            PDFTableHeader header = headers[i];
            String col = row[i];
            stream.setStrokingColor(1f);
            stream.addRect(x, y, header.width(), table.cellHeight());
            stream.stroke();

            stream.beginText();
            stream.setNonStrokingColor(table.bodyFontColor());
            if (header.alignment() == Alignment.LEFT) {
                stream.newLineAtOffset(x + 20, y + 10);
            } else if (header.alignment() == Alignment.RIGHT) {
                float textWidth = getTextFontWidth(col, table.bodyFont(), table.bodyFontSize());
                stream.newLineAtOffset(x + header.width() - 20 - textWidth, y + 10);
            } else {
                // center
                float textWidth = getTextFontWidth(col, table.bodyFont(), table.bodyFontSize());
                stream.newLineAtOffset(x + (header.width() - textWidth) / 2, y + 10);
            }
            stream.setFont(table.bodyFont(), table.bodyFontSize());
            stream.showText(col);
            stream.endText();

            x = x + headers[i].width();
        }
    }

    private static void renderTableRow(float x, float y, PDPageContentStream stream, PDFTable table, PDFTableHeader[] headers, String[] row) throws Exception {
        for (int i = 0; i < headers.length; i++) {
            PDFTableHeader header = headers[i];
            String col = row[i];
            stream.setStrokingColor(1f);
            stream.setNonStrokingColor(table.bodyColor());
            stream.addRect(x, y, header.width(), table.cellHeight());
            stream.fillAndStroke();

            stream.beginText();
            stream.setNonStrokingColor(table.bodyFontColor());
            if (header.alignment() == Alignment.LEFT) {
                stream.newLineAtOffset(x + 20, y + 10);
            } else if (header.alignment() == Alignment.RIGHT) {
                float textWidth = getTextFontWidth(col, table.bodyFont(), table.bodyFontSize());
                stream.newLineAtOffset(x + header.width() - 20 - textWidth, y + 10);
            } else {
                // center
                float textWidth = getTextFontWidth(col, table.bodyFont(), table.bodyFontSize());
                stream.newLineAtOffset(x + (header.width() - textWidth) / 2, y + 10);
            }
            stream.setFont(table.bodyFont(), table.bodyFontSize());
            stream.showText(col);
            stream.endText();

            x = x + headers[i].width();
        }
    }

    private static void renderTableHeaders(float x, float y, PDPageContentStream stream, PDFTable table, PDFTableHeader[] headers) throws Exception {
        for (int i = 0; i < headers.length; i++) {
            stream.setStrokingColor(1f);
            stream.setNonStrokingColor(table.headerColor());
            stream.addRect(x, y, headers[i].width(), table.cellHeight());
            stream.fillAndStroke();

            stream.beginText();
            stream.setNonStrokingColor(table.headerFontColor());
            if (headers[i].alignment() == Alignment.LEFT) {
                stream.newLineAtOffset(x+20, y+10);
            } else if (headers[i].alignment() == Alignment.RIGHT) {
                float textWidth = getTextFontWidth(headers[i].title(), table.headerFont(), table.headerFontSize());
                stream.newLineAtOffset(x + headers[i].width() - 20 - textWidth, y+10);
            } else {
                // center
                float textWidth = getTextFontWidth(headers[i].title(), table.headerFont(), table.headerFontSize());
                stream.newLineAtOffset(x + (headers[i].width() - textWidth)/2, y+10);
            }
            stream.setFont(table.headerFont(), table.headerFontSize());
            stream.showText(headers[i].title());
            stream.endText();

            x = x + headers[i].width();
        }
    }
}
