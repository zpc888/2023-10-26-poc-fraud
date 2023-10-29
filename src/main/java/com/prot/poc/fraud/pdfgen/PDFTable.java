package com.prot.poc.fraud.pdfgen;

import org.apache.pdfbox.pdmodel.font.PDFont;

import java.awt.Color;
import java.util.List;

/**
 * @Author: <a href="mailto: pengcheng.zhou@gmail.com">PengCheng Zhou</a>
 * @Created: 2023-10-26T23:24 Thursday
 */
public record PDFTable(float x, float y, float cellHeight,
                       PDFont headerFont, int headerFontSize, Color headerFontColor,
                       PDFont bodyFont, int bodyFontSize, Color bodyFontColor,
                       Color headerColor, Color bodyColor,
                       PDFTableHeader[] headers,
                       List<String[]> rows, String[] footers) {
}
