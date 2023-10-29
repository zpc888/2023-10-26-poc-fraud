package com.prot.poc.fraud.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @Author: <a href="mailto: pengcheng.zhou@gmail.com">PengCheng Zhou</a>
 * @Created: 2023-10-28T14:49 Saturday
 */
public class JSONUtils {
    private JSONUtils() {
    }

    public static <T> T fromJSONString(String str, Class<T> type) {
        if (str == null) {
            return null;
        }
        ObjectMapper om = new ObjectMapper();
        try {
            T t = om.readValue(str, type);
            return t;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String toJSONString(Object o) {
        return toJSONString(o, false);
    }

    public static String toJSONStringNicely(Object o) {
        return toJSONString(o, true);
    }

    private static String toJSONString(Object o, boolean nicely) {
        if (o == null) {
            return null;
        }
        ObjectMapper om = new ObjectMapper();
        try {
            if (nicely) {
                return om.writerWithDefaultPrettyPrinter().writeValueAsString(o);
            } else {
                return om.writeValueAsString(o);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


}
