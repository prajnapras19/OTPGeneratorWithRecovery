package com.example.otpgeneratorwithrecovery.crypto;

import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Hex;

import java.nio.charset.StandardCharsets;

/**
 * This class is a wrapper for string to string encode-decode with base32, because the library just support bytes to bytes conversion.
 */
public class Base32Wrapper {
    public static String encodeStringToString(String decoded) {
        byte[] encoded = (new Base32()).encode(decoded.getBytes(StandardCharsets.UTF_8));
        if (encoded == null) {
            return "";
        }
        return new String(encoded, StandardCharsets.UTF_8);
    }

    public static String encodeBytesToString(byte[] decoded) {
        byte[] encoded = (new Base32()).encode(decoded);
        if (encoded == null) {
            return "";
        }
        return new String(encoded, StandardCharsets.UTF_8);
    }

    public static String decodeStringToString(String encoded) {
        byte[] decoded = (new Base32()).decode(encoded.getBytes(StandardCharsets.UTF_8));
        if (decoded == null) {
            return "";
        }
        return new String(decoded, StandardCharsets.UTF_8);
    }

    public static String decodeStringToHexString(String encoded) {
        byte[] decoded = (new Base32()).decode(encoded.getBytes(StandardCharsets.UTF_8));
        if (decoded == null) {
            return "";
        }
        return Hex.encodeHexString(decoded);
    }

    public static byte[] decodeStringToBytes(String encoded) {
        return (new Base32()).encode(encoded.getBytes(StandardCharsets.UTF_8));
    }
}
