package com.prot.erd.service;

import com.prot.erd.model.*;
import com.prot.poc.common.AppException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

/**
 * @Author: <a href="mailto: pengcheng.zhou@gmail.com">PengCheng Zhou</a>
 * @Created: 2023-11-15T17:44 Wednesday
 */
public class PlantUMLGen {
    private String outFilePrefix;
    private String outFileSuffix;
    private String niceTitlePrefix;
    private String niceTitleSuffix;

    private boolean birdview;

    public void setBirdview(boolean birdview) {
        this.birdview = birdview;
    }

    public void setOutputFileFormatter(String prefix, String suffix) {
        this.outFilePrefix = prefix;
        this.outFileSuffix = suffix;
    }

    public void setNiceTitleFormatter(String prefix, String suffix) {
        this.niceTitlePrefix = prefix;
        this.niceTitleSuffix = suffix;
    }

    public void genAspectERDAutomatically(String inRootDir, String outRootDir) {
        AspectObjectsLoader loader = new AspectObjectsLoader();
        List<Pair<String, String>> pairs = loader.loadAspectObjects(inRootDir, "aspect-objects.txt");
        Map<String, List<String>> aspectObjects = pairs.stream().collect(Collectors.groupingBy(Pair::getKey,
                mapping(Pair::getValue, toList())));
        for (String aspect: aspectObjects.keySet()) {
            List<String> objectApis = aspectObjects.get(aspect);
            try {
                genOneApsectERD(aspect, objectApis, inRootDir, outRootDir);
            } catch (AppException ae) {
                throw ae;
            } catch (Exception ex) {
                throw new AppException("Fail to generate aspect: " + aspect + " ERDs", ex);
            }
        }
    }

    private void genOneApsectERD(String aspect, List<String> objectApis, String inRoot, String outRoot) throws Exception {
        String noSpace = aspect.replaceAll(" ", "-");
        String fieldFilename = noSpace + ".html";
        File f = new File(inRoot, fieldFilename);
        if (!f.isFile()) {
            throw new AppException("Field file: " + f.getAbsolutePath() + " is not a file");
        }
        if (!f.exists()) {
            throw new AppException("Field file: " + f.getAbsolutePath() + " does not exist");
        }
        String outFile = addPrefixAndSuffix(outFilePrefix, noSpace, outFileSuffix) + ".puml";
        File out = new File(outRoot, outFile);
        try (InputStream is = new FileInputStream(f);
             OutputStream os = new FileOutputStream(out);
             OutputStreamWriter osw = new OutputStreamWriter(os, StandardCharsets.UTF_8);
             PrintWriter pw = new PrintWriter(osw)) {
            Map<String, List<SObjectField>> verifiedMap = new SOQLResultParserHtmlImpl()
                    .parse(aspect, objectApis, is);
            String title = addPrefixAndSuffix(niceTitlePrefix, aspect, niceTitleSuffix);
            SOQLResultToERDGen erdGen = new SOQLResultToERDGen();
            erdGen.setBirdview(birdview);
            erdGen.gen(pw, title, objectApis, verifiedMap);
        }
    }

    public void genUML(String erdDefRootDir, String outRootDir) {
        genUML(new File(erdDefRootDir), new File(outRootDir));
    }

    public void genUML(File defRootDir, File outRootDir) {
        AllDefinition allDefs = new FilesLoader().loadDir(defRootDir);
        allDefs.validate();
        allDefs.getAllAspects().forEach(aspect -> genOneAspectERD(aspect, allDefs,
                outRootDir));
    }

    private void genOneAspectERD(AspectDefinition aspect, AllDefinition allDefs,
                                 File outRootDir) {
        try {
            String outFileName = aspect.getNormalizedName();
            final String title = niceTitle(outFileName, niceTitlePrefix, niceTitleSuffix);
            outFileName = addPrefixAndSuffix(outFilePrefix, outFileName, outFileSuffix);
            outFileName = outFileName + ".puml";
            File f = new File(outRootDir, outFileName);
            try (OutputStream os = new FileOutputStream(f);
                     OutputStreamWriter osw = new OutputStreamWriter(os, StandardCharsets.UTF_8);
                     PrintWriter pw = new PrintWriter(osw)) {
               pw.println("@startuml");
               pw.println("'https://plantuml.com/class-diagram");
               pw.println();
               pw.println("title " + title);
               pw.println();
               pw.println("""
                    'skinparam classBorderThickness 0

                    hide circle
                    hide empty methods
                    hide empty fields

                    """);
               aspect.getTouchedObjects().forEach(o -> printOneTouchedObject(o, pw));
               pw.println();
               aspect.getOnewayList().forEach(r -> printOneRelationship(r, pw));
               pw.println();
               pw.println("@enduml");
            }
        } catch (Exception ex) {
            throw new AppException("Fail to output aspect ERD file for: " + aspect , ex);
        }


    }

    private String addPrefixAndSuffix(String prefix, String value, String suffix) {
        if (StringUtils.isNotBlank(prefix)) {
            value = prefix + value;
        }
        if (StringUtils.isNotBlank(suffix)) {
            value = value + suffix;
        }
        return value;
    }

    private String niceTitle(String outFileName, String titlePrefix, String titleSuffix) {
        String ret = Arrays.stream(outFileName.split("-"))
                .map(StringUtils::capitalize)
                .collect(Collectors.joining(" "));
        ret = addPrefixAndSuffix(titlePrefix, ret, titleSuffix);
        return ret;
    }

    private void printOneRelationship(Relationship r, PrintWriter pw) {
        pw.printf("%s %s--%s %s%n",
                r.getFromObjectCode(), r.getFromObjectCardinality().plantumlAtFromSide(),
                r.getToObjectCardinality().plantumlAtToSide(), r.getToObjectCode());
    }

    private void printOneTouchedObject(SObject o, PrintWriter pw) {
        pw.printf("entity %s as \"<b>%s</b> \\n%s\"%n",
                o.code(), o.getNormalizedDisplayName(), o.apiName());
    }
}
