package com.prot.erd.service;

import com.prot.erd.model.Relationship;
import com.prot.erd.model.SObjectField;
import com.prot.poc.common.AppException;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author: <a href="mailto: pengcheng.zhou@gmail.com">PengCheng Zhou</a>
 * @Created: 2023-11-17T09:52 Friday
 */
public class SOQLResultToERDGen {
    public void gen(PrintWriter pw, String title, List<String> objectApis,
                    Map<String, List<SObjectField>> fieldsByObjMap) {
        if (objectApis == null || objectApis.isEmpty()) {
            throw new AppException(title + " has no sObject, which is impossible");
        }
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
        Map<String, List<SObjectField>> onlyRelaFields = filterOnlyRelatedFields(objectApis, fieldsByObjMap);
        objectApis.forEach(e -> printEntity(e, fieldsByObjMap.get(e).get(0), onlyRelaFields.get(e), pw));
        pw.println();
        Map<String, SObjectField> objLabelToFirstFields = new HashMap<>(32);
        for (String objApiName : fieldsByObjMap.keySet()) {
            SObjectField first = fieldsByObjMap.get(objApiName).get(0);
            objLabelToFirstFields.put(first.getObjectLabel(), first);
        }
        printRelationships(pw, objLabelToFirstFields, onlyRelaFields);
        pw.println();
        pw.println("@enduml");
    }

    private void printRelationships(PrintWriter pw, Map<String, SObjectField> objLabelToFirstFields, Map<String, List<SObjectField>> onlyRelaFields) {
        onlyRelaFields.values().stream().flatMap(List::stream).forEach(f -> {
            String fromEntity = f.getObjectCode();
            String toEntity = objLabelToFirstFields.get(f.getRelatedObjectLabel()).getObjectCode();
            Relationship.Cardinality fromCardinality = Relationship.Cardinality.ZERO_OR_MANY;
            Relationship.Cardinality toCardinality = f.isNillable() ? Relationship.Cardinality.ZERO_OR_ONE
                    : Relationship.Cardinality.EXACTLY_ONE;
            pw.printf("%s %s--%s %s%n",
                    fromEntity, fromCardinality.plantumlAtFromSide(),
                    toCardinality.plantumlAtToSide(), toEntity);
        });
    }


    // relatedFields will be empty if this object is orphan in the aspect, that is why it needs first field
    private void printEntity(String objApi, SObjectField first, List<SObjectField> relatedFields, PrintWriter pw) {
        String backgroundColor = first.getObjectApiName().indexOf("__") > 0 ? "" : "#lightblue";
        pw.printf("entity %s as \"<b>%s</b> \\n%s\" %s {%n",
                first.getObjectCode(), first.getObjectLabel(), objApi, backgroundColor);
        relatedFields.forEach(f -> {
            String out = String.format("%s: %s", f.getFieldApiName(), f.getRelatedObjectLabel());
            if (!f.isNillable()) {
                out = "<b>" + out + "</b>";  // bold
            }
            if (f.isMasterDetail()) {
//                out = "<font color=\"blue\">" + out + "</font>";
//                out = "<u:green>" + out + "</u>";
                out = "<u>" + out + "</u>";
            }
            pw.println(out);
        });
        pw.println("}");

    }

    /**
     * Only look for field data type is Master-Detail or Lookup, and the other object is
     * in these objects (belong to the same aspect), but its fieldApiName is not CreatedById,
     * nor LastModifiedById since they are system auto fields for all objects
     *
     * @param objectApis
     * @param allFields
     * @return
     */
    private Map<String, List<SObjectField>> filterOnlyRelatedFields(List<String> objectApis, Map<String, List<SObjectField>> allFields) {
        Set<String> objLabels = objectApis.stream().map(objApi -> allFields.get(objApi).get(0).getObjectLabel()).collect(Collectors.toSet());
        Map<String, List<SObjectField>> ret = new HashMap<>(allFields.size());
        for (String k: allFields.keySet()) {
            ret.put(k, allFields.get(k).stream()
                    .filter(f -> hasRelationshipWithinAspect(f, objLabels))
                    .collect(Collectors.toList()));
        }
        return ret;
    }

    private boolean hasRelationshipWithinAspect(SObjectField field, Set<String> objLabels) {
        //    Master-Detail(Service Mgmt)
        //    Lookup(Relationship)
        //    fieldApiName=CreatedById
        //    fieldApiName=LastModifiedById
        if (field.getFieldApiName().equals("CreatedById") || field.getFieldApiName().equals("LastModifiedById")) {
            return false;
        }
        if (field.getRelatedObjectLabel() == null) {
            return false;
        }
        return objLabels.contains(field.getRelatedObjectLabel());
    }
}
