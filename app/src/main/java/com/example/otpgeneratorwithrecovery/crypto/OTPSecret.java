package com.example.otpgeneratorwithrecovery.crypto;

import com.example.otpgeneratorwithrecovery.util.Base32Wrapper;

/**
 * This class is a wrapper for the shared OTP secret from a service provider.
 * Validation of the received QR code, encoding/decoding for saving in the storage, and OTP generation will be done from this class.
 */
public class OTPSecret {
    public static final String PREFIX_OTP_SECRET = "otpauth";

    private OTPURIFormat format;
    private String type;
    private String base32EncodedSecret;
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
        this(new OTPURIFormat(otpURIString));
    }

    public OTPSecret(OTPURIFormat format) throws Exception {
        this.format = format;
        if (format.getPrefix() == null || !format.getPrefix().equals(OTPSecret.PREFIX_OTP_SECRET)) {
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

        this.base32EncodedSecret = format.getParameter("secret");
        if (this.base32EncodedSecret == null) {
            throw new Exception("secret is required.");
        }
        if (Base32Wrapper.decodeStringToHexString(this.base32EncodedSecret).equals("")) {
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

        if (this.accountName.equals("")) {
            this.accountName = "unknown";
        }
        if (this.issuer.equals("")) {
            this.issuer = "unknown";
        }

        this.algorithm = "SHA1";
        this.digits = "6";

        // TODO: add support for HOTP
        // if (this.type.equals("hotp")) {
        //     this.period = "";
        // }
        this.period = "30";
        this.secret = Base32Wrapper.decodeStringToHexString(this.base32EncodedSecret);
    }

    public String getBase32EncodedSecret() {
        return this.base32EncodedSecret;
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

    public OTPURIFormat getFormat() {
        return this.format;
    }

    public String getPeriod() {
        return this.period;
    }
}
