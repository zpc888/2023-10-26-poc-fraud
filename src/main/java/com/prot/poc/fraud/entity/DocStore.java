package com.prot.poc.fraud.entity;

import jakarta.persistence.*;

/**
 * @Author: <a href="mailto: pengcheng.zhou@gmail.com">PengCheng Zhou</a>
 * @Created: 2023-10-27T21:42 Friday
 */
@Entity
@Table(name="DOC_STORE", indexes = {
        // @Index(name = "vendorInfoIdx", columnList = "SOURCE_NUMBER,VENDOR_NAME", unique = true)
        @Index(name = "vendorInfoIdx", columnList = "SOURCE_NUMBER"),
        @Index(name = "signPkgIdx", columnList = "SIGN_PKG_ID", unique = true)
})
public class DocStore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "VENDOR_NAME", nullable = true, length = 64)
    private String vendorName = "PengCheng";

    @Column(name = "SOURCE_NUMBER", nullable = false, length = 64, updatable = false)
    private String sourceNumber;

    @Column(name = "DOC_NAME", nullable = true, length = 120)
    private String docName;

    // For simplicity, sign package and doc is 1-to-1 instead of 1-to-many
    @Column(name = "SIGN_PKG_ID", nullable = true, length = 48)
    private String signPackageId;

    @Column(name = "DOC_STATUS", nullable = false, length = 16)
    @Enumerated(EnumType.STRING)
//    @Enumerated(EnumType.ORDINAL)     // will map to Integer
    private DocStatus status;

    @Lob
    @Column(name = "DOC_CONTENT", columnDefinition = "BLOB")
    private byte[] content;

    @Lob
    @Column(name = "SIGNER_INFO", columnDefinition = "CLOB", updatable = false)
    private String signerInfo;

    @Lob
    @Column(name = "CALLBACK_INFO", columnDefinition = "CLOB", updatable = false)
    private String callbackInfo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public String getSourceNumber() {
        return sourceNumber;
    }

    public void setSourceNumber(String sourceNumber) {
        this.sourceNumber = sourceNumber;
    }

    public String getDocName() {
        return docName;
    }

    public void setDocName(String docName) {
        this.docName = docName;
    }

    public DocStatus getStatus() {
        return status;
    }

    public void setStatus(DocStatus status) {
        this.status = status;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public String getSignerInfo() {
        return signerInfo;
    }

    public void setSignerInfo(String signerInfo) {
        this.signerInfo = signerInfo;
    }

    public String getCallbackInfo() {
        return callbackInfo;
    }

    public void setCallbackInfo(String callbackInfo) {
        this.callbackInfo = callbackInfo;
    }

    public String getSignPackageId() {
        return signPackageId;
    }

    public void setSignPackageId(String signPackageId) {
        this.signPackageId = signPackageId;
    }
}
