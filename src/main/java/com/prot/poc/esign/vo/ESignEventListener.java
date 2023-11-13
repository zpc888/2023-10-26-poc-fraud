package com.prot.poc.esign.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;

/**
 * @Author: <a href="mailto: pengcheng.zhou@gmail.com">PengCheng Zhou</a>
 * @Created: 2023-11-11T22:14 Saturday
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "__type")
@JsonSubTypes({
        @JsonSubTypes.Type(name = "RecipientCallback", value = ESignEventListener.RecipientEventListener.class),
        @JsonSubTypes.Type(name = "PackageCallback", value = ESignEventListener.PackageEventListener.class)
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
public abstract class ESignEventListener<T extends ESignEventStatus> {
    private String packageCorrelationId;
    private T eventStatus;
    private Date eventAt;
    private boolean documentsIncluded;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Data
    @EqualsAndHashCode(callSuper=true)
    public static class PackageEventListener extends ESignEventListener<ESignEventStatus.PackageStatus> {
        private List<DeclinedRecipient> declinedRecipients;
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Data
    @EqualsAndHashCode(callSuper=true)
    public static class RecipientEventListener extends ESignEventListener<ESignEventStatus.RecipientStatus> {
        private String guid;
        private String email;
    }

    public record DeclinedRecipient(String email, String declinedReason, Date declinedAt) {}
}
