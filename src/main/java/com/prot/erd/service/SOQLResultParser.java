package com.prot.erd.service;

import com.prot.erd.model.SObjectField;
import com.prot.poc.common.AppException;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author: <a href="mailto: pengcheng.zhou@gmail.com">PengCheng Zhou</a>
 * @Created: 2023-11-16T19:34 Thursday
 */
public interface SOQLResultParser {
    // Columns are:
    //      EntityDefinition.QualifiedApiName
    //      EntityDefinition.Label
    //      QualifiedApiName
    //      Label
    //      DataType
    //      IsNillable
    //      IsIndexed
    //      Description

    /**
     * @param aspect
     * @param aspectObjectApiNames
     * @param inputStream
     * @return key - object api name, value - the list of fields for this object
     */
    default Map<String, List<SObjectField>> parse(String aspect, List<String> aspectObjectApiNames, InputStream inputStream) {
        try {
            List<SObjectField> fields = doParse(inputStream);
            Map<String, List<SObjectField>> ret = fields.stream().collect(Collectors.groupingBy(SObjectField::getObjectApiName));
            List<String> noFielObjects = aspectObjectApiNames.stream().filter(oa -> !ret.containsKey(oa)).collect(Collectors.toList());
            if (!noFielObjects.isEmpty()) {
                throw new AppException("Aspect: " + aspect + " objects don't have fields: " + noFielObjects + ", which are wrong");
            }
            return ret;
        } catch (AppException ae) {
            throw ae;
        } catch (Exception ex) {
            throw new AppException("Fail to parse aspect: " + aspect + " SOQL Result", ex);
        }
    }
    List<SObjectField> doParse(InputStream inputStream) throws Exception;
}
