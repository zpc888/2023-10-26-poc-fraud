package com.prot.erd.service;

import com.prot.poc.common.AppException;
import org.apache.commons.lang3.tuple.Pair;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: <a href="mailto: pengcheng.zhou@gmail.com">PengCheng Zhou</a>
 * @Created: 2023-11-16T18:08 Thursday
 */
public class SOQLGen {
    private static final String SOQL_PREFIX = """
            SELECT EntityDefinition.QualifiedApiName, EntityDefinition.Label,
                QualifiedApiName, Label, DataType, isNillable, isIndexed, Description
            FROM FieldDefinition
            WHERE EntityDefinition.QualifiedApiName in (
            """;

    public void genSOQLWithAspectObjects(List<Pair<String, String>> aspectObjects, String outFile) {
        Map<String, List<String>> byAspects = new LinkedHashMap<>(32);
        Set<String> allObjApis = new HashSet<>();
        for (Pair<String, String> p: aspectObjects) {
            allObjApis.add(p.getValue());
            List<String> objAPIs = byAspects.computeIfAbsent(p.getKey(), k -> new ArrayList<>(32));
            objAPIs.add(p.getValue());
        }
        try (OutputStream os = new FileOutputStream(outFile);
             OutputStreamWriter osw = new OutputStreamWriter(os, StandardCharsets.UTF_8);
             PrintWriter pw = new PrintWriter(osw)) {
             printSOQL(pw, "all", allObjApis);
             for (String aspect: byAspects.keySet()) {
                 printSOQL(pw, aspect, byAspects.get(aspect));
             }
        } catch (Exception ex) {
            throw new AppException("Fail to generate SOQL to file: " + outFile, ex);
        }
    }

    private void printSOQL(PrintWriter pw, String aspect, Collection<String> objApis) {
        pw.printf("%s :%d%n", aspect, objApis.size());
        String where = objApis.stream().map(s -> "'" + s + "'").collect(Collectors.joining(","));
        pw.println(SOQL_PREFIX + where + ")");
        pw.println();
    }
}
