package com.prot.erd.model;

import org.apache.commons.lang3.StringUtils;

/**
 * @Author: <a href="mailto: pengcheng.zhou@gmail.com">PengCheng Zhou</a>
 * @Created: 2023-11-15T20:53 Wednesday
 */
public record SObject(String code, String apiName, String displayName, String description) {
    public String getNormalizedDisplayName() {
        return StringUtils.firstNonBlank(displayName, insertSpaceBeforeUppercase(code));
    }

    public static String insertSpaceBeforeUppercase(String s) {
        StringBuilder sb = new StringBuilder(s.length() * 2);
        sb.append(s.charAt(0));
        for (int i = 1; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= 'A' && c <= 'Z') {
                sb.append(' ');
            }
            sb.append(c);
        }
        return sb.toString();
    }
}
