package com.example.otpgeneratorwithrecovery.crypto;

import org.apache.commons.codec.binary.Base32;

import java.nio.charset.StandardCharsets;

public class Base32Wrapper {
    public static String encode(String decoded) {
        byte[] encoded = (new Base32()).encode(decoded.getBytes(StandardCharsets.UTF_8));
        if (encoded == null) {
            return "";
        }
        return new String(encoded, StandardCharsets.UTF_8);
    }

    public static String decode(String encoded) {
        byte[] decoded = (new Base32()).decode(encoded.getBytes(StandardCharsets.UTF_8));
        if (decoded == null) {
            return "";
        }
        return new String(decoded, StandardCharsets.UTF_8);
    }
}
