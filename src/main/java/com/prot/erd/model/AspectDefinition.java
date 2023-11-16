package com.prot.erd.model;

import com.prot.poc.common.AppException;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author: <a href="mailto: pengcheng.zhou@gmail.com">PengCheng Zhou</a>
 * @Created: 2023-11-15T21:18 Wednesday
 */
@Data
@Accessors(chain = true)
@RequiredArgsConstructor
public class AspectDefinition {
    public static final String ASPECT_ERD_DEFINITION_SUFFIX = "-erd.txt";
    private String fileName;
    private List<Relationship> relationships;

    // ----------- runtime properties
    @Setter(AccessLevel.NONE)
    private List<Relationship> onewayList;
    @Setter(AccessLevel.NONE)
    private Set<SObject> touchedObjects;

    public String getNormalizedName() {
        return fileName.substring(0, fileName.length() - ASPECT_ERD_DEFINITION_SUFFIX.length());
    }

    public void validate(Map<String, SObject> sObjectMap) {
        if (relationships == null) {
            throw new AppException("No relationship defined for file: " + fileName);
        }
        ensureObjectCodeDefinedInSObject(sObjectMap);
        ensureRelationshipFromToBidirectionDefined();
        touchedObjects = onewayList.stream()
                .flatMap(r -> List.of(sObjectMap.get(r.getFromObjectCode()), sObjectMap.get(r.getToObjectCode())).stream()
        ).collect(Collectors.toSet());
    }

    private void ensureRelationshipFromToBidirectionDefined() {
//        if (relationships.size() / 2 != 0) {
//            throw new AppException("Relationships are not bi-direction defined for double checking in file " + fileName);
//        }
        onewayList = new ArrayList<>(relationships.size() / 2);
        List<Relationship> cloned = new ArrayList<>(relationships);
        while (!cloned.isEmpty()) {
            Relationship r1 = cloned.get(0);
            Relationship r2 = null;
            for (Relationship r: cloned) {
                if (r1.getFromObjectCode().equals(r.getToObjectCode()) && r1.getFromObjectCardinality() == r.getToObjectCardinality()
                  && r1.getToObjectCode().equals(r.getFromObjectCode()) && r1.getToObjectCardinality() == r.getFromObjectCardinality()) {
                    r2 = r;
                    break;
                }
            }
            if (r2 == null) {
                throw new AppException("Relationship: " + r1 + " is not defined from other direction for double checking in file " + fileName);
            }
            onewayList.add(r1);
            cloned.remove(r1);
            cloned.remove(r2);
        }
    }

    private void ensureObjectCodeDefinedInSObject(Map<String, SObject> sObjectMap) {
        relationships.forEach(r -> {
            if (!sObjectMap.containsKey(r.getFromObjectCode())) {
                throw new AppException("From Object Code [" + r.getFromObjectCode() + "] from file " + fileName + " is not defined");
            }
            if (!sObjectMap.containsKey(r.getToObjectCode())) {
                throw new AppException("To Object Code [" + r.getToObjectCode() + "] from file " + fileName + " is not defined");
            }
        });
    }
}
