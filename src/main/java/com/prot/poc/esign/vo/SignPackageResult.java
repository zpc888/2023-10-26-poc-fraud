package com.prot.poc.esign.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: <a href="mailto: pengcheng.zhou@gmail.com">PengCheng Zhou</a>
 * @Created: 2023-11-05T23:54 Sunday
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
@Accessors(chain = true)
public class SignPackageResult {
    private String packageId = null;
    private String status = null;
    private String statusDateTime = null;
    private String uri = null;

    private final List<RecipientStatus> recipientStatuses = new ArrayList<>(4);

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Data
    @Accessors( chain = true )
    public static class RecipientStatus {
        private String email;
        private String status;
        private String reason;
    }

    public SignPackageResult addRecipientStatus(RecipientStatus recipientStatus) {
        this.recipientStatuses.add(recipientStatus);
        return this;
    }
}
