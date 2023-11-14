package com.prot.poc.esign.vo;

/**
 * @Author: <a href="mailto: pengcheng.zhou@gmail.com">PengCheng Zhou</a>
 * @Created: 2023-11-11T22:22 Saturday
 */
sealed public interface ESignEventStatus permits ESignEventStatus.RecipientStatus, ESignEventStatus.PackageStatus {
    enum RecipientStatus implements ESignEventStatus {
        Sent,
        AutoResponded,
        Delivered,
        Completed,
        Declined,
        AuthenticationFailed
/*
Sent: This event is sent when an email notification is sent to the recipient signifying that it is their turn to sign an envelope.
Delivery Failed: This event is sent when DocuSign gets notification that an email delivery has failed. The delivery failure could be for a number of reasons, such as a bad email address or that the recipient’s email system auto-responds to the email from DocuSign. This event can only be used if “Send-on-behalf-of” is turned off for the account.
Delivered: This event is sent when the recipient has viewed the documents in an envelope through the DocuSign signing web site. This does not signify an email delivery of an envelope.
Signed/Completed: This event is sent when the recipient has completed (signed) the envelope. If the recipient is not a signer, this is sent when the recipient has completed his or her actions for an envelope. Signed is a temporary state used during processing, after which the recipient is automatically moved to Completed.
Declined: This event is sent when the recipient declines to sign the documents in the envelope.
Authentication Failure: This event is sent when the recipient fails an authentication check. In cases where a recipient has multiple attempts to pass a check, it means that the recipient failed all the attempts.
*/
    }
    enum PackageStatus implements ESignEventStatus {
        sent,
        delivered,
        signed,
        completed,
        declined,
        voided
/*
https://support.docusign.com/guides/dfs-admin-guide-envelope-recipient-events-ref
https://developers.docusign.com/esign-rest-api/guides/status-and-error-codes
Sent:      This event is sent when the email notification, with a link to the envelope, is sent to at least one recipient. The envelope remains in this state until all recipients have viewed the envelope.
Delivered: This event is sent when all recipients have opened the envelope through the DocuSign signing website. This does not signify an email delivery of an envelope.
Signed:    This event is sent when the envelope has been signed by all required recipients. This is a temporary state used during processing, after which the envelope is automatically moved to Completed status.
Completed: The envelope has been completed by all the recipients.
Declined:  The envelope has been declined by one of the recipients.
Voided:    The envelope has been voided by the sender.
*/
    }
}
