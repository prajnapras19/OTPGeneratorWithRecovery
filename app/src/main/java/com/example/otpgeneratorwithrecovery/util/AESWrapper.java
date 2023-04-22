package com.example.otpgeneratorwithrecovery.util;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class AESWrapper {
    public static final String ENCRYPTION = "AES/CBC/PKCS5Padding";
    public static final String AES = "AES";

    // https://www.baeldung.com/java-aes-encryption-decryption
    public static IvParameterSpec generateIv() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }

    public static SecretKey generateKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(AESWrapper.AES);
        keyGenerator.init(128);
        SecretKey key = keyGenerator.generateKey();
        return key;
    }

    public static String encrypt(String input, SecretKey key, IvParameterSpec iv) throws Exception {
        Cipher cipher = Cipher.getInstance(AESWrapper.ENCRYPTION);
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);
        byte[] cipherText = cipher.doFinal(input.getBytes());
        return Base32Wrapper.encodeBytesToString(cipherText);
    }

    public static String decrypt(String base32EncodedCipherText, SecretKey key, IvParameterSpec iv) throws Exception {
        Cipher cipher = Cipher.getInstance(AESWrapper.ENCRYPTION);
        cipher.init(Cipher.DECRYPT_MODE, key, iv);
        byte[] decrypted = cipher.doFinal(Base32Wrapper.decodeStringToBytes(base32EncodedCipherText));
        return new String(decrypted, StandardCharsets.UTF_8);
    }
}