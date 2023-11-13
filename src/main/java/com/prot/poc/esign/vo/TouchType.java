package com.prot.poc.esign.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * Model a recipient (signer is one kind of recipient, some just want to receive a copy of document and don't need to sign)
 * action at one location in documents
 *
 * @Author: <a href="mailto: pengcheng.zhou@gmail.com">PengCheng Zhou</a>
 * @Created: 2023-11-07T23:42 Tuesday
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "__type")
@JsonSubTypes({
        @JsonSubTypes.Type(name = "Signature", value = TouchType.SignatureType.class),
        @JsonSubTypes.Type(name = "Initial", value = TouchType.InitialType.class),
        @JsonSubTypes.Type(name = "DateSigned", value = TouchType.DateSignedType.class),
        @JsonSubTypes.Type(name = "SignerAttachment", value = TouchType.SignerAttachmentType.class),
        @JsonSubTypes.Type(name = "Text", value = TouchType.TextType.class),
        @JsonSubTypes.Type(name = "Fullname", value = TouchType.FullnameType.class),
        @JsonSubTypes.Type(name = "BeingSent", value = TouchType.BeingSentType.class)
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
@Accessors(chain = true)
public abstract class TouchType {
     private String        toucherRole;
     private TouchLocation location;
     private String tooltip;
     private String fontColor;

     @JsonInclude(JsonInclude.Include.NON_DEFAULT)
     private int      scalePercent = 0;     // 0 or 100% = normal size, no scale up/down

     public String resolveScaleValue() {
          if (scalePercent != 100 && scalePercent > 0) {
               double d = (1.0d * scalePercent) / 100;
               return "" + d;
          } else {
               return null;
          }
     }

     /**
      * just send the package to a recipient who doesn't need to sign or initial, etc
      */
     @EqualsAndHashCode(callSuper=true)
     @JsonInclude(JsonInclude.Include.NON_NULL)
     @Data
     public static class BeingSentType extends TouchType {
          @JsonInclude(JsonInclude.Include.NON_DEFAULT)
          private boolean optional = true;
     }

     @EqualsAndHashCode(callSuper=true)
     @JsonInclude(JsonInclude.Include.NON_NULL)
     @Data
     public static class SignerAttachmentType extends TouchType {
          @JsonInclude(JsonInclude.Include.NON_DEFAULT)
          private boolean optional = false;
     }

     @EqualsAndHashCode(callSuper=true)
     @JsonInclude(JsonInclude.Include.NON_NULL)
     @Data
     public static class SignatureType extends TouchType {
          @JsonInclude(JsonInclude.Include.NON_DEFAULT)
          private boolean optional = false;
     }

     @EqualsAndHashCode(callSuper=true)
     @JsonInclude(JsonInclude.Include.NON_NULL)
     @Data
     public static class InitialType extends TouchType {
          @JsonInclude(JsonInclude.Include.NON_DEFAULT)
          private boolean optional = false;
     }

     @EqualsAndHashCode(callSuper=true)
     @JsonInclude(JsonInclude.Include.NON_NULL)
     @Data
     public static class DateSignedType extends TouchType {
     }

     @EqualsAndHashCode(callSuper=true)
     @JsonInclude(JsonInclude.Include.NON_NULL)
     @Data
     public static class TextType extends TouchType {
          @JsonInclude(JsonInclude.Include.NON_DEFAULT)
          private boolean optional = false;
          private String value;
     }

     @EqualsAndHashCode(callSuper=true)
     @JsonInclude(JsonInclude.Include.NON_NULL)
     @Data
     public static class FullnameType extends TouchType {
     }
}
