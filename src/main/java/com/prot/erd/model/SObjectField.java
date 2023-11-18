package com.prot.erd.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

/**
 * @Author: <a href="mailto: pengcheng.zhou@gmail.com">PengCheng Zhou</a>
 * @Created: 2023-11-16T20:45 Thursday
 */
@Data
@Accessors(chain = true)
public class SObjectField {
    private static final String RELA_MASTER_DETAIL_DATA_TYPE_PREFIX = "Master-Detail(";
    private static final String RELA_LOOKUP_DATA_TYPE_PREFIX = "Lookup(";

    private String  objectApiName;
    private String  objectLabel;
    private String  fieldApiName;
    private String  fieldLabel;
    private String  fieldDataType;
    private boolean isNillable;
    private boolean isIndexed;
    private String  description;

    // ------- transient -- runtime properties
    @JsonIgnore
    private String objectCode;
    @JsonIgnore
    private String relatedObjectLabel;
    @JsonIgnore
    private boolean relatedObjectLabelResolved = false;

    @JsonIgnore
    public String getObjectCode() {
        if (objectCode == null) {
            objectCode = StringUtils.deleteWhitespace(objectLabel);
            if (objectCode.startsWith("-D")) {
                objectCode = "D_" + objectCode.substring(2);
            }
        }
        return objectCode;
    }
    @JsonIgnore
    public boolean isMasterDetail() {
        return fieldDataType.startsWith(RELA_MASTER_DETAIL_DATA_TYPE_PREFIX);
    }
    @JsonIgnore
    public boolean isLookup() {
        return fieldDataType.startsWith(RELA_LOOKUP_DATA_TYPE_PREFIX);
    }

    @JsonIgnore
    public String getRelatedObjectLabel() {
        if (!relatedObjectLabelResolved) {
            relatedObjectLabelResolved = true;
            if (isMasterDetail()) {
                relatedObjectLabel = fieldDataType.substring(
                        RELA_MASTER_DETAIL_DATA_TYPE_PREFIX.length(), fieldDataType.length() - 1);
            } else if (isLookup()) {
                relatedObjectLabel = fieldDataType.substring(
                        RELA_LOOKUP_DATA_TYPE_PREFIX.length(), fieldDataType.length() - 1);
            }
        }
        return relatedObjectLabel;
    }
}
