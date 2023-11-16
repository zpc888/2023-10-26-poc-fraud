package com.prot.erd.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * @Author: <a href="mailto: pengcheng.zhou@gmail.com">PengCheng Zhou</a>
 * @Created: 2023-11-15T21:14 Wednesday
 */
@Data
@Accessors(chain = true)
public class AllDefinition {
    private final Map<String, SObject> sObjectMap;
    private List<AspectDefinition> allAspects;

    public AllDefinition(Map<String, SObject> sObjectMap) {
        this.sObjectMap = sObjectMap;
    }

    public void validate() {
        allAspects.forEach(aspect -> aspect.validate(sObjectMap));
    }
}
