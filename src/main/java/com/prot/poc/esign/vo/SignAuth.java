package com.prot.poc.esign.vo;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.List;

/**
 * @Author: <a href="mailto: pengcheng.zhou@gmail.com">PengCheng Zhou</a>
 * @Created: 2023-11-06T23:37 Monday
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "__type")
@JsonSubTypes({
        @JsonSubTypes.Type(name="None", value=SignAuth.None.class),
        @JsonSubTypes.Type(name="Phone", value=SignAuth.Phone.class),
        @JsonSubTypes.Type(name="SMS", value=SignAuth.SMS.class)
})
sealed public interface SignAuth permits SignAuth.None, SignAuth.Phone, SignAuth.SMS {
    None NONE = new None();

    record None() implements SignAuth {}
    record Phone(List<String> phoneNumbers) implements SignAuth {
        public Phone(String phoneNumber) {
            this(List.of(phoneNumber));
        }
    }
    record SMS(List<String> phoneNumbers) implements SignAuth {
        public SMS(String phoneNumber) {
            this(List.of(phoneNumber));
        }
    }
}
