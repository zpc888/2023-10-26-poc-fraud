package com.prot.erd.service;

import com.prot.erd.model.SObjectField;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @Author: <a href="mailto: pengcheng.zhou@gmail.com">PengCheng Zhou</a>
 * @Created: 2023-11-16T22:35 Thursday
 */
class SOQLResultParserHtmlImplTest {
    private static final String FEE_SUMMARY_RESOURCE_NAME = "erd/ncino-erds-2023-fall/auto-discover-relationships/Fees-Summary.html";
    private static final int EXPECTED_FIELDS_COUNT_fOR_FEE_SUMMARY = 772;
    private static final String ASPECT = "Fees Summary";
    private SOQLResultParserHtmlImpl soqlHtmlParser;
    private SOQLResultToERDGen erdGen;

    @BeforeEach
    void init() {
        soqlHtmlParser = new SOQLResultParserHtmlImpl();
        erdGen = new SOQLResultToERDGen();
    }

    @Test
    void parseObjectFields() throws Exception {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(FEE_SUMMARY_RESOURCE_NAME);
        List<SObjectField> sObjectFields = soqlHtmlParser.doParse(is);
        is.close();
        assertEquals(EXPECTED_FIELDS_COUNT_fOR_FEE_SUMMARY, sObjectFields.size());
    }

    @Test
    void genERD() throws Exception {
        FileOutputStream fos = new FileOutputStream("build/ncion-2023-fall-fees-summary.puml");
        OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
        PrintWriter pw = new PrintWriter(osw);

        List<Pair<String, String>> pairs = loadAspectObjects();
        Map<String, List<SObjectField>> verifiedMap = parseAndVerifyFeesSummaryAspectFields(pairs);
        List<String> objectApis = pairs.stream().filter(p -> ASPECT.equals(p.getKey())).map(Pair::getValue).collect(Collectors.toList());
        erdGen.gen(pw, ASPECT + " ERDs - nCino 2023 Fall", objectApis, verifiedMap);

        osw.close();
        pw.close();
        fos.close();
    }

    @Test
    void parseAndVerifyObjectFields() throws Exception {
        List<Pair<String, String>> pairs = loadAspectObjects();
        Map<String, List<SObjectField>> verifiedMap = parseAndVerifyFeesSummaryAspectFields(pairs);
        assertEquals(7, verifiedMap.size());
        verifiedMap.keySet().forEach(System.out::println);
        System.out.println("---------------------------------");
        verifiedMap.values().stream().flatMap(List::stream).forEach(System.out::println);
    }

    private Map<String, List<SObjectField>> parseAndVerifyFeesSummaryAspectFields(List<Pair<String, String>> pairs) {
        List<String> objectApis = pairs.stream().filter(p -> ASPECT.equals(p.getKey())).map(Pair::getValue).collect(Collectors.toList());
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(FEE_SUMMARY_RESOURCE_NAME);
        Map<String, List<SObjectField>> verifiedMap = soqlHtmlParser.parse(ASPECT, objectApis, is);
        return verifiedMap;
    }

    private static List<Pair<String, String>> loadAspectObjects() {
        AspectObjectsLoader loader = new AspectObjectsLoader();
        List<Pair<String, String>> pairs = loader.loadAspectObjects(
                "src/test/resources/erd/ncino-erds-2023-fall/auto-discover-relationships",
                "aspect-objects.txt");
        return pairs;
    }

    @Test
    void parseHtmlTable() throws Exception {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(FEE_SUMMARY_RESOURCE_NAME);
        SOQLResultParserHtmlImpl.HtmlTable tbl = soqlHtmlParser.parseHtmlTable(is);
        is.close();
        tbl.getTbody().getTr().forEach(System.out::println);
        assertNotNull(tbl);
        assertEquals(EXPECTED_FIELDS_COUNT_fOR_FEE_SUMMARY + 1, tbl.getTbody().getTr().size());
        // + 1 is for table head titles, which are empty
    }
}