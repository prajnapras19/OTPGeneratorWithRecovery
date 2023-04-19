package com.example.otpgeneratorwithrecovery.crypto;

import android.annotation.SuppressLint;

import java.util.HashMap;
import java.util.Map;

/**
 * This class provide validation for one share of shared secret, and saved in OTPURIFormat
 */
public class SharedSecretToRecover {
    private static final String PREFIX_SHARES = "otpshares";
    private static final String RECIPIENT = "recipient";
    private static final String RECIPIENTS = "recipients";
    private static final String RECIPIENT_NUMBER = "recipientNumber";
    private static final String ENCRYPTED_SECRET = "encryptedSecret";
    private static final String SHARED_ENCRYPTION_KEY = "sharedEncryptionKey";

    private OTPURIFormat format;
    private String recipient;
    private int recipientNumber; // 1-based
    private String[] recipients;
    private String encryptedSecret;
    private String sharedEncryptionKey;

    // TODO: refactor to remove this, because this just become a placeholder for validation
    private String type;
    private String issuer;
    private String accountName;

    /**
     * The constructor for case when the user wants to create the shared secret.
     * @param secret
     * @param recipient
     * @param recipientNumber
     * @param recipients
     * @param encryptedSecret
     * @param sharedEncryptionKey
     */
    public SharedSecretToRecover(OTPSecret secret, String recipient, int recipientNumber, String[] recipients, String encryptedSecret, String sharedEncryptionKey) {
        this.format = secret.getFormat();
        this.recipient = recipient;
        this.recipientNumber = recipientNumber;
        this.recipients = recipients;
        this.encryptedSecret = encryptedSecret;
        this.sharedEncryptionKey = sharedEncryptionKey;
    }

    /**
     * The constructor for case when the user wants to recover the shared secret.
     * @param otpURIString
     */
    public SharedSecretToRecover(String otpURIString) throws Exception {
        OTPURIFormat format = new OTPURIFormat(otpURIString);
        this.format = format;

        // validation, same as OTPSecret, without "secret", and more parameters
        if (format.getPrefix() == null || !format.getPrefix().equals(PREFIX_SHARES)) {
            throw new Exception("inputted string not in shared otp secret format.");
        }

        this.type = format.getType();
        if (type == null) {
            throw new Exception("inputted string not in shared otp secret format.");
        }
        if (!type.equals("totp")) {
            // TODO: add support for HOTP
            throw new Exception("current supported type: totp");
        }

        if (format.getParameter("issuer") != null) {
            this.issuer = format.getParameter("issuer");
        }

        if (format.getLabel() == null) {
            throw new Exception("inputted string not in shared otp secret format.");
        }
        String[] labelParts = format.getLabel().split(":");
        if (labelParts.length == 2) {
            this.accountName = labelParts[1].strip();
            if (this.issuer == null) {
                this.issuer = labelParts[0];
            } else if (!this.issuer.equals(labelParts[0])){
                throw new Exception("issuer in label and parameter not match.");
            }
        } else if (labelParts.length == 1) {
            this.accountName = labelParts[0];
            if (this.issuer == null) {
                // no issuer in parameter and label
                this.issuer = "unknown";
            }
        } else {
            throw new Exception("label format not recognized.");
        }

        this.recipient = this.format.getParameter(SharedSecretToRecover.RECIPIENT);
        String recipientNumberString = this.format.getParameter(SharedSecretToRecover.RECIPIENT_NUMBER);
        String recipientsString = this.format.getParameter(SharedSecretToRecover.RECIPIENTS);
        this.encryptedSecret = this.format.getParameter(SharedSecretToRecover.ENCRYPTED_SECRET);
        this.sharedEncryptionKey = this.format.getParameter(SharedSecretToRecover.SHARED_ENCRYPTION_KEY);

        if (this.recipient == null || recipientNumberString == null || recipientsString == null || this.encryptedSecret == null || this.sharedEncryptionKey == null) {
            throw new Exception("inputted string not in shared otp secret format.");
        }

        this.recipients = recipientsString.split(",");
        this.recipientNumber = Integer.valueOf(recipientNumberString);
        if (!(1 <= this.recipientNumber && this.recipientNumber <= this.recipients.length) || !this.recipient.equals(this.recipients[this.recipientNumber-1])) {
            throw new Exception("inputted string not in shared otp secret format.");
        }
    }

    public boolean hasSameSource(SharedSecretToRecover other) {
        if (!this.format.getLabel().equals(other.format.getLabel())) {
            return false;
        }

        if (!this.encryptedSecret.equals(other.encryptedSecret)) {
            return false;
        }

        if (this.recipients.length != other.recipients.length) {
            return false;
        }

        for (int i = 0; i < this.recipients.length; i++) {
            if (!recipients[i].equals(other.recipients[i])) {
                return false;
            }
        }
        return true;
    }

    @SuppressLint("DefaultLocale")
    public String getIdentifier() {
        return String.format("%s:%s:%d", this.format.getLabel(), this.recipient, this.recipientNumber);
    }

    public String toString() {
        Map<String, String> newParameterMap = new HashMap<>();
        newParameterMap.putAll(this.format.getParameterMap());

        // omit secret
        newParameterMap.remove("secret");

        // insert recipient, recipients, recipientNumber, encryptedSecret, and sharedEncryptionKey
        newParameterMap.put(SharedSecretToRecover.RECIPIENT, this.recipient);
        newParameterMap.put(SharedSecretToRecover.RECIPIENTS, String.join(",", this.recipients));
        newParameterMap.put(SharedSecretToRecover.RECIPIENT_NUMBER, String.valueOf(this.recipientNumber));
        newParameterMap.put(SharedSecretToRecover.ENCRYPTED_SECRET, encryptedSecret);
        newParameterMap.put(SharedSecretToRecover.SHARED_ENCRYPTION_KEY, sharedEncryptionKey);

        return (new OTPURIFormat(PREFIX_SHARES, this.format.getType(), this.format.getLabel(), newParameterMap)).toString();
    }
}