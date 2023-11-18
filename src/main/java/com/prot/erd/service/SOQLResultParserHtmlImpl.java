package com.prot.erd.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.prot.erd.model.SObjectField;
import lombok.Data;

import javax.xml.stream.XMLResolver;
import javax.xml.stream.XMLStreamException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;


/**
 * @Author: <a href="mailto: pengcheng.zhou@gmail.com">PengCheng Zhou</a>
 * @Created: 2023-11-16T22:04 Thursday
 */
public class SOQLResultParserHtmlImpl implements SOQLResultParser {

    @Override
    public List<SObjectField> doParse(InputStream inputStream) throws Exception {
        HtmlTable tbl = parseHtmlTable(inputStream);
        return convertFromHtmlTable(tbl);
    }

    HtmlTable parseHtmlTable(InputStream inputStream) throws Exception {
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.getFactory().getXMLInputFactory().setProperty(
                "com.ctc.wstx.undeclaredEntityResolver", new XMLResolver() {
                    @Override
                    public Object resolveEntity(String publicID, String systemID, String baseURI, String namespace) throws XMLStreamException {
                        switch (namespace) {
                            case "nbsp": return "";
                            default:
                                return null;
                        }
                    }
                }

        );
        return xmlMapper.readValue(inputStream, HtmlTable.class);
    }

    private List<SObjectField> convertFromHtmlTable(HtmlTable tbl) {
        return tbl.getTbody().getTr().stream()
                .filter(tr -> tr.td != null && !tr.td.isEmpty())
                .map(this::mapRowToSObjectField)
                .collect(Collectors.toList());
    }

    private SObjectField mapRowToSObjectField(HtmlTr htmlTr) {
        List<String> cols = htmlTr.td.stream().map(t -> t.div).collect(Collectors.toList());
        SObjectField ret = new SObjectField();
        ret.setObjectApiName(cols.get(0).trim());
        ret.setObjectLabel(cols.get(1).trim());
        ret.setFieldApiName(cols.get(2).trim());
        ret.setFieldLabel(cols.get(3).trim());
        ret.setFieldDataType(cols.get(4).trim());
        ret.setNillable("true".equalsIgnoreCase(cols.get(5).trim()));
        ret.setIndexed("true".equalsIgnoreCase(cols.get(6).trim()));
        if (cols.size() > 7) {
            ret.setDescription(cols.get(7).trim());
        }
        return ret;
    }

    @Data
    @JacksonXmlRootElement(localName = "table")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class HtmlTable {
        private HtmlTBody tbody;
    }
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class HtmlTBody {
        @JacksonXmlElementWrapper(useWrapping = false)
        private List<HtmlTr> tr;
    }
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class HtmlTr {
        @JacksonXmlElementWrapper(useWrapping = false)
        private List<HtmlTd> td;
    }
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class HtmlTd {
        private String div;
    }
}
