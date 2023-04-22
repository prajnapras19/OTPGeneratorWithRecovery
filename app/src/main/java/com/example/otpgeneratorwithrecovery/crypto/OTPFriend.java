package com.example.otpgeneratorwithrecovery.crypto;

import org.apache.commons.codec.binary.Hex;

import java.util.UUID;

/**
 * This class provides validation for client id and client secret
 */
public class OTPFriend {
    public static final String PREFIX_FRIENDS = "otpfriends";
    public static final String TYPE_FRIEND = "friend";

    private OTPURIFormat format;
    private String clientID;
    private byte[] clientSecret;

    public OTPFriend(String clientID, byte[] clientSecret) {
        this.clientID = clientID;
        this.clientSecret = clientSecret;
        this.format = new OTPURIFormat(OTPFriend.PREFIX_FRIENDS, OTPFriend.TYPE_FRIEND, String.format("%s:%s", clientID, Hex.encodeHexString(clientSecret)), null);
    }

    public OTPFriend(String otpURIString) throws Exception {
        this.format = new OTPURIFormat(otpURIString);

        if (!(this.format.getPrefix().equals(OTPFriend.PREFIX_FRIENDS) && this.format.getType().equals(OTPFriend.TYPE_FRIEND))) {
            throw new Exception("inputted string not in otp friend format.");
        }

        String[] label = this.format.getLabel().split(":");
        if (label.length != 2) {
            throw new Exception("inputted string not in otp friend format.");
        }

        try {
            this.clientID = UUID.fromString(label[0]).toString();
            this.clientSecret = Hex.decodeHex(label[1]);
            if (this.clientSecret.length != 16) {
                throw new Exception("inputted string not in otp friend format.");
            }
        } catch (Exception e) {
            throw new Exception("inputted string not in otp friend format.");
        }
    }

    public String toString() {
        return this.format.toString();
    }
}