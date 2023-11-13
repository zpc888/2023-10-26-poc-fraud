package com.prot.poc.esign.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Author: <a href="mailto: pengcheng.zhou@gmail.com">PengCheng Zhou</a>
 * @Created: 2023-11-05T22:30 Sunday
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
@Accessors(chain = true)
public class SignPackage {
    @Size(max = 100)
    private String emailSubject;
    private String emailBody;
    private String brandId;
    private Map<String, String> roleToRecipientIds;
    private List<SignDocument> signedDocuments;
    private List<Recipient> recipients;
    private List<ESignEventListener.PackageEventListener> packageEventListeners = null;
    private List<ESignEventListener.RecipientEventListener> recipientEventListeners = null;
    private String eventListenerUrl;

    public SignPackage addESignEventListener(ESignEventListener<?> callback) {
        if (callback instanceof ESignEventListener.PackageEventListener pc) {
            if (packageEventListeners == null) {
                packageEventListeners = new ArrayList<>(8);
            }
            packageEventListeners.add(pc);
        } else if (callback instanceof ESignEventListener.RecipientEventListener rc) {
            if (recipientEventListeners == null) {
                recipientEventListeners = new ArrayList<>(8);
            }
            recipientEventListeners.add(rc);
        }
        return this;
    }

    public boolean hasESignEventListeners() {
        return (packageEventListeners != null && !packageEventListeners.isEmpty())
                || (recipientEventListeners != null && !recipientEventListeners.isEmpty());
    }

    public record RecipientTouch(Recipient recipient, String docId, TouchType touch) {
    }

    public Stream<RecipientTouch> eachTouch() {
        Map<String, Recipient> recipientMap = recipients.stream().collect(
                Collectors.toMap(Recipient::getId, Function.identity()));
        return signedDocuments.stream().flatMap(
                d -> d.touches.stream().map(t -> {
                    String recipientId = roleToRecipientIds.get(t.getToucherRole());
                    Recipient recipient = recipientMap.get(recipientId);
                    return new RecipientTouch(recipient, d.getId(), t);
                }));
    }

    public Map<Recipient, List<RecipientTouch>> groupTouchesByRecipient() {
        return eachTouch().collect(Collectors.groupingBy(RecipientTouch::recipient));
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Data
    @Accessors(chain = true)
    public static class SignDocument {
        private String id;
        private String name;
        @JsonIgnore
        private Supplier<byte[]> contentSupplier;
        private List<TouchType> touches;
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Data
    @Accessors(chain = true)
    public static class Recipient {
        private String id;
        private String name;
        private String email;
        private String clientUserId;
        private SignAuth signAuth;
    }

}
