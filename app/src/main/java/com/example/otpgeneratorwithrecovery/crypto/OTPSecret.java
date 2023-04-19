package com.example.otpgeneratorwithrecovery.crypto;

import org.apache.commons.codec.binary.Base32;

import java.nio.charset.StandardCharsets;

/**
 * This class is a wrapper for the shared OTP secret from a service provider.
 * Validation of the received QR code, encoding/decoding for saving in the storage, and OTP generation will be done from this class.
 */
public class OTPSecret {
    private OTPURIFormat format;
    private String type;
    private String secret;
    private String issuer;
    private String accountName;

    // ignored for now, value = SHA1
    private String algorithm;

    // ignored for now, value = 6
    private String digits;

    // ignored for now, value = 30
    private String period;

    // ignored for now, because we only try with TOTP
    private String counter;

    public OTPSecret(String otpURIString) throws Exception {
        OTPURIFormat format = new OTPURIFormat(otpURIString);
        this.format = format;

        if (format.getPrefix() == null || !format.getPrefix().equals("otpauth")) {
            throw new Exception("inputted string not in otp secret format.");
        }

        this.type = format.getType();
        if (type == null) {
            throw new Exception("inputted string not in otp secret format.");
        }
        if (!type.equals("totp")) {
            // TODO: add support for HOTP
            throw new Exception("current supported type: totp");
        }

        this.secret = format.getParameter("secret");
        if (secret == null) {
            throw new Exception("secret is required.");
        }
        if ((new Base32()).decode(secret.getBytes(StandardCharsets.UTF_8)) == null) {
            throw new Exception("secret is not in Base32 format.");
        }

        if (format.getParameter("issuer") != null) {
            this.issuer = format.getParameter("issuer");
        }

        if (format.getLabel() == null) {
            throw new Exception("inputted string not in otp secret format.");
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

        this.algorithm = "SHA1";
        this.digits = "6";
        this.period = "30";
    }

    public String getSecret() {
        return this.secret;
    }

    public String getIssuer() {
        return this.issuer;
    }

    public String getAccountName() {
        return this.accountName;
    }

    public String getIdentifier() {
        return String.format("%s:%s", this.issuer, this.accountName);
    }

    public String getOTP() {
        // TODO: add support for HOTP
        if (this.type.equals("hotp")) {
            return "";
        }

        // totp
        long now = System.currentTimeMillis() / 1000L;
        long x = Long.parseLong(period); // assume period is always more than 0, for now it is always 30 since the custom period is ignored.
        long time = now / x;
        String steps = Long.toHexString(time).toUpperCase();
        while (steps.length() < 16) {
            steps = "0" + steps;
        }
        return TOTP.generateTOTP(this.secret, steps, this.digits, String.format("Hmac%s", this.algorithm));
    }
}
