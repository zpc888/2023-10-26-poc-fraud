package com.prot.erd.service;

import com.prot.erd.model.AllDefinition;
import com.prot.erd.model.AspectDefinition;
import com.prot.erd.model.Relationship;
import com.prot.erd.model.SObject;
import com.prot.poc.common.AppException;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @Author: <a href="mailto: pengcheng.zhou@gmail.com">PengCheng Zhou</a>
 * @Created: 2023-11-15T17:44 Wednesday
 */
public class PlantUMLGen {
    private String outFilePrefix;
    private String outFileSuffix;
    private String niceTitlePrefix;
    private String niceTitleSuffix;

    public void setOutputFileFormatter(String prefix, String suffix) {
        this.outFilePrefix = prefix;
        this.outFileSuffix = suffix;
    }

    public void setNiceTitleFormatter(String prefix, String suffix) {
        this.niceTitlePrefix = prefix;
        this.niceTitleSuffix = suffix;
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
            if (StringUtils.isNotBlank(outFilePrefix)) {
                outFileName = outFilePrefix + outFileName;
            }
            if (StringUtils.isNotBlank(outFileSuffix)) {
                outFileName = outFileName + outFileSuffix;
            }
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

    private String niceTitle(String outFileName, String titlePrefix, String titleSuffix) {
        String ret = Arrays.stream(outFileName.split("-"))
                .map(StringUtils::capitalize)
                .collect(Collectors.joining(" "));
        if (StringUtils.isNotBlank(titlePrefix)) {
            ret = titlePrefix + ret;
        }
        if (StringUtils.isNotBlank(titleSuffix)) {
            ret = ret + titleSuffix;
        }
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
