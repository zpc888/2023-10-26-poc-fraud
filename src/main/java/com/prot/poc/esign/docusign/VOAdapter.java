package com.prot.poc.esign.docusign;

import com.docusign.esign.model.*;
import com.prot.poc.esign.vo.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @Author: <a href="mailto: pengcheng.zhou@gmail.com">PengCheng Zhou</a>
 * @Created: 2023-11-06T00:49 Monday
 */
public class VOAdapter {
    public static EnvelopeDefinition prepareEnvelopeRequest(SignPackage pkg) {
        EnvelopeDefinition envelopeDef = new EnvelopeDefinition();
        envelopeDef.setEmailSubject(pkg.getEmailSubject());
        envelopeDef.setEmailBlurb(pkg.getEmailBody());
        if (StringUtils.isNotBlank(pkg.getBrandId())) {
            envelopeDef.setBrandId(pkg.getBrandId());
        }
        List<Document> docs = pkg.getSignedDocuments().stream()
                .map(VOAdapter::mapDocuments).collect(Collectors.toList());
        envelopeDef.setDocuments(docs);

        List<Signer> signers = buildSigners(pkg);
        Recipients recipients = new Recipients();
        recipients.setSigners(signers);
        envelopeDef.setRecipients(recipients);

        if (pkg.hasESignEventListeners()) {
            envelopeDef.setEventNotification(buildEventNotification(pkg));
        }

        return envelopeDef;
    }

    private static EventNotification buildEventNotification(SignPackage pkg) {
        EventNotification ret = new EventNotification();
        ret.setUrl(pkg.getEventListenerUrl());
        if (!CollectionUtils.isEmpty(pkg.getPackageEventListeners())) {
            pkg.getPackageEventListeners().stream().map(VOAdapter::buildEnvelopeEvent).forEach(ret::addEnvelopeEventsItem);
        }
        if (!CollectionUtils.isEmpty(pkg.getRecipientEventListeners())) {
            pkg.getRecipientEventListeners().stream().map(VOAdapter::buildRecipientEvent).forEach(ret::addRecipientEventsItem);
        }
        return ret;
    }

    private static EnvelopeEvent buildEnvelopeEvent(ESignEventListener.PackageEventListener event) {
        EnvelopeEvent ret = new EnvelopeEvent();
        ret.setEnvelopeEventStatusCode(event.getEventStatus().name());
        ret.setIncludeDocuments("" + event.isDocumentsIncluded());
        return ret;
    }


    private static RecipientEvent buildRecipientEvent(ESignEventListener.RecipientEventListener event) {
        RecipientEvent ret = new RecipientEvent();
        ret.setRecipientEventStatusCode(event.getEventStatus().name());
        ret.setIncludeDocuments("" + event.isDocumentsIncluded());
        return ret;
    }

    private static List<Signer> buildSigners(SignPackage pkg) {
        List<Signer> ret = new ArrayList<>(48);
        Map<SignPackage.Recipient, List<SignPackage.RecipientTouch>> touchesByRecipient = pkg.groupTouchesByRecipient();
        for (SignPackage.Recipient recipient: touchesByRecipient.keySet()) {
            final Signer signer = buildSigner(recipient);
            attachActionsToSigner(signer, touchesByRecipient.get(recipient));
            ret.add(signer);
        }
        return ret;
    }

    private static void attachActionsToSigner(Signer signer, List<SignPackage.RecipientTouch> recipientTouches) {
        final Tabs tabs = new Tabs();
        final AtomicBoolean actionPerformed = new AtomicBoolean(false);
        Map<Class<? extends TouchType>, Consumer<SignPackage.RecipientTouch>> handlers = new HashMap<>(8);
        handlers.put(TouchType.SignatureType.class, (SignPackage.RecipientTouch t) -> handleSignature(t, tabs, actionPerformed));
        handlers.put(TouchType.InitialType.class, (SignPackage.RecipientTouch t) -> handleInitial(t, tabs, actionPerformed));
        handlers.put(TouchType.DateSignedType.class, (SignPackage.RecipientTouch t) -> handleDateSigned(t, tabs, actionPerformed));
        handlers.put(TouchType.SignerAttachmentType.class, (SignPackage.RecipientTouch t) -> handleSignerAttachment(t, tabs, actionPerformed));
        handlers.put(TouchType.TextType.class, (SignPackage.RecipientTouch t) -> handleText(t, tabs, actionPerformed));
        handlers.put(TouchType.FullnameType.class, (SignPackage.RecipientTouch t) -> handleFullname(t, tabs, actionPerformed));
        handlers.put(TouchType.BeingSentType.class, (SignPackage.RecipientTouch t) -> {});     // no actions
        for (SignPackage.RecipientTouch recipientTouch: recipientTouches) {
            handlers.get(recipientTouch.touch().getClass()).accept(recipientTouch);
        }
        if (actionPerformed.get()) {
            signer.setTabs(tabs);
        }
    }

    private static void handleSignature(SignPackage.RecipientTouch rt, Tabs tabs, AtomicBoolean actionPerformed) {
        SignHere tabItem = new SignHere();
        TouchType.SignatureType touch = (TouchType.SignatureType) rt.touch();
        tabItem.setName(touch.getTooltip());
        tabItem.setOptional("" + touch.isOptional());
        tabItem.setScaleValue(touch.resolveScaleValue());
        tabItem.setDocumentId(rt.docId());
        tabItem.setRecipientId(rt.recipient().getId());
        if (touch.getLocation() instanceof TouchLocation.ViaXY viaXY) {
            tabItem.setPageNumber("" + viaXY.pageNumber());
            tabItem.setXPosition("" + viaXY.xPosition());
            tabItem.setYPosition("" + viaXY.yPosition());
        } else if (touch.getLocation() instanceof TouchLocation.ViaAnchor viaAnchor) {
            tabItem.setAnchorString(viaAnchor.getAnchorName());
            tabItem.setAnchorCaseSensitive("" + viaAnchor.isAnchorCaseSensitive());
            tabItem.setAnchorXOffset("" + viaAnchor.getOffsetX());
            tabItem.setAnchorYOffset("" + viaAnchor.getOffsetY());
            tabItem.setAnchorUnits(viaAnchor.getAnchorUnits());
        } // else impossible
        tabs.addSignHereTabsItem(tabItem);
        actionPerformed.set(true);
    }

    private static void handleInitial(SignPackage.RecipientTouch rt, Tabs tabs, AtomicBoolean actionPerformed) {
        InitialHere tabItem = new InitialHere();
        TouchType.InitialType touch = (TouchType.InitialType) rt.touch();
        tabItem.setName(touch.getTooltip());
        tabItem.setOptional("" + touch.isOptional());
        tabItem.setScaleValue(touch.resolveScaleValue());
        tabItem.setDocumentId(rt.docId());
        tabItem.setRecipientId(rt.recipient().getId());
        if (touch.getLocation() instanceof TouchLocation.ViaXY viaXY) {
            tabItem.setPageNumber("" + viaXY.pageNumber());
            tabItem.setXPosition("" + viaXY.xPosition());
            tabItem.setYPosition("" + viaXY.yPosition());
        } else if (touch.getLocation() instanceof TouchLocation.ViaAnchor viaAnchor) {
            tabItem.setAnchorString(viaAnchor.getAnchorName());
            tabItem.setAnchorCaseSensitive("" + viaAnchor.isAnchorCaseSensitive());
            tabItem.setAnchorXOffset("" + viaAnchor.getOffsetX());
            tabItem.setAnchorYOffset("" + viaAnchor.getOffsetY());
            tabItem.setAnchorUnits(viaAnchor.getAnchorUnits());
        } // else impossible
        tabs.addInitialHereTabsItem(tabItem);
        actionPerformed.set(true);
    }

    private static void handleDateSigned(SignPackage.RecipientTouch rt, Tabs tabs, AtomicBoolean actionPerformed) {
        DateSigned tabItem = new DateSigned();
        TouchType.DateSignedType touch = (TouchType.DateSignedType) rt.touch();
        tabItem.setName(touch.getTooltip());
        tabItem.setDocumentId(rt.docId());
        tabItem.setRecipientId(rt.recipient().getId());
        if (touch.getLocation() instanceof TouchLocation.ViaXY viaXY) {
            tabItem.setPageNumber("" + viaXY.pageNumber());
            tabItem.setXPosition("" + viaXY.xPosition());
            tabItem.setYPosition("" + viaXY.yPosition());
        } else if (touch.getLocation() instanceof TouchLocation.ViaAnchor viaAnchor) {
            tabItem.setAnchorString(viaAnchor.getAnchorName());
            tabItem.setAnchorCaseSensitive("" + viaAnchor.isAnchorCaseSensitive());
            tabItem.setAnchorXOffset("" + viaAnchor.getOffsetX());
            tabItem.setAnchorYOffset("" + viaAnchor.getOffsetY());
            tabItem.setAnchorUnits(viaAnchor.getAnchorUnits());
        } // else impossible
        tabs.addDateSignedTabsItem(tabItem);
        actionPerformed.set(true);
    }

    private static void handleText(SignPackage.RecipientTouch rt, Tabs tabs, AtomicBoolean actionPerformed) {
        Text tabItem = new Text();
        TouchType.TextType touch = (TouchType.TextType) rt.touch();
        tabItem.setName(touch.getTooltip());
        tabItem.setDocumentId(rt.docId());
        tabItem.setValue(touch.getValue());
        tabItem.setRecipientId(rt.recipient().getId());
        if (touch.getLocation() instanceof TouchLocation.ViaXY viaXY) {
            tabItem.setPageNumber("" + viaXY.pageNumber());
            tabItem.setXPosition("" + viaXY.xPosition());
            tabItem.setYPosition("" + viaXY.yPosition());
        } else if (touch.getLocation() instanceof TouchLocation.ViaAnchor viaAnchor) {
            tabItem.setAnchorString(viaAnchor.getAnchorName());
            tabItem.setAnchorCaseSensitive("" + viaAnchor.isAnchorCaseSensitive());
            tabItem.setAnchorXOffset("" + viaAnchor.getOffsetX());
            tabItem.setAnchorYOffset("" + viaAnchor.getOffsetY());
            tabItem.setAnchorUnits(viaAnchor.getAnchorUnits());
        } // else impossible
        tabs.addTextTabsItem(tabItem);
        actionPerformed.set(true);
    }

    private static void handleFullname(SignPackage.RecipientTouch rt, Tabs tabs, AtomicBoolean actionPerformed) {
        FullName tabItem = new FullName();
        TouchType.FullnameType touch = (TouchType.FullnameType) rt.touch();
        tabItem.setName(touch.getTooltip());
        tabItem.setDocumentId(rt.docId());
        tabItem.setRecipientId(rt.recipient().getId());
        if (touch.getLocation() instanceof TouchLocation.ViaXY viaXY) {
            tabItem.setPageNumber("" + viaXY.pageNumber());
            tabItem.setXPosition("" + viaXY.xPosition());
            tabItem.setYPosition("" + viaXY.yPosition());
        } else if (touch.getLocation() instanceof TouchLocation.ViaAnchor viaAnchor) {
            tabItem.setAnchorString(viaAnchor.getAnchorName());
            tabItem.setAnchorCaseSensitive("" + viaAnchor.isAnchorCaseSensitive());
            tabItem.setAnchorXOffset("" + viaAnchor.getOffsetX());
            tabItem.setAnchorYOffset("" + viaAnchor.getOffsetY());
            tabItem.setAnchorUnits(viaAnchor.getAnchorUnits());
        } // else impossible
        tabs.addFullNameTabsItem(tabItem);
        actionPerformed.set(true);
    }

    private static void handleSignerAttachment(SignPackage.RecipientTouch rt, Tabs tabs, AtomicBoolean actionPerformed) {
        SignerAttachment tabItem = new SignerAttachment();
        TouchType.SignerAttachmentType touch = (TouchType.SignerAttachmentType) rt.touch();
        tabItem.setTooltip(touch.getTooltip());
        tabItem.setName(touch.getTooltip());
        tabItem.setOptional("" + touch.isOptional());
        tabItem.setScaleValue(touch.resolveScaleValue());
        tabItem.setDocumentId(rt.docId());
        tabItem.setRecipientId(rt.recipient().getId());
        if (touch.getLocation() instanceof TouchLocation.ViaXY viaXY) {
            tabItem.setPageNumber("" + viaXY.pageNumber());
            tabItem.setXPosition("" + viaXY.xPosition());
            tabItem.setYPosition("" + viaXY.yPosition());
        } else if (touch.getLocation() instanceof TouchLocation.ViaAnchor viaAnchor) {
            tabItem.setAnchorString(viaAnchor.getAnchorName());
            tabItem.setAnchorCaseSensitive("" + viaAnchor.isAnchorCaseSensitive());
            tabItem.setAnchorXOffset("" + viaAnchor.getOffsetX());
            tabItem.setAnchorYOffset("" + viaAnchor.getOffsetY());
            tabItem.setAnchorUnits(viaAnchor.getAnchorUnits());
        } // else impossible
        tabs.addSignerAttachmentTabsItem(tabItem);
        actionPerformed.set(true);
    }

    private static Signer buildSigner(SignPackage.Recipient recipient) {
        Signer signer = new Signer();
        signer.setName(recipient.getName());
        signer.setEmail(recipient.getEmail());
        signer.setRecipientId(recipient.getId());
        signer.setClientUserId(recipient.getClientUserId());
        if (recipient.getSignAuth() instanceof SignAuth.Phone phone) {
            RecipientPhoneAuthentication phoneAuth = new RecipientPhoneAuthentication();
            phoneAuth.setRecipMayProvideNumber("true");
            phoneAuth.setSenderProvidedNumbers(phone.phoneNumbers());
            signer.setIdCheckConfigurationName("Phone Auth $");
            signer.setPhoneAuthentication(phoneAuth);
        } else if (recipient.getSignAuth() instanceof SignAuth.SMS sms) {
            RecipientSMSAuthentication smsAuth = new RecipientSMSAuthentication();
            smsAuth.setSenderProvidedNumbers(sms.phoneNumbers());
            signer.setIdCheckConfigurationName("SMS Auth $");
            signer.setSmsAuthentication(smsAuth);
        }
        return signer;
    }

    private static Document mapDocuments(SignPackage.SignDocument signDocument) {
        Document document = new Document();
        document.setDocumentId(signDocument.getId());
        document.setName(signDocument.getName());
        document.setDocumentBase64(Base64.getEncoder().encodeToString(signDocument.getContentSupplier().get()));
        return document;
    }

    public static SignPackageResult parseEnvelopeResponse(EnvelopeSummary summary) {
        return new SignPackageResult()
                .setPackageId(summary.getEnvelopeId())
                .setStatus(summary.getStatus())
                .setUri(summary.getUri())
                .setStatusDateTime(summary.getStatusDateTime());
    }
}
