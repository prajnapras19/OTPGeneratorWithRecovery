package com.example.otpgeneratorwithrecovery.crypto;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

/**
 * This class provides OTP secret sharing scheme using SSSS (Shamir's Secret Sharing Scheme).
 * Sharing and assembling is done within this class.
 */
public class SharedSecret {
    private static final String encryption = "AES/CBC/PKCS5Padding";

    // https://www.baeldung.com/java-aes-encryption-decryption
    public static IvParameterSpec generateIv() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }

    public static SecretKey generateKey(int n) throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(n);
        SecretKey key = keyGenerator.generateKey();
        return key;
    }

    public static String[] generate(OTPSecret secret, String[] recipients) throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {
        OTPURIFormat secretFormat = secret.getFormat();

        Map<String, String> newParameterMap = new HashMap<>();
        newParameterMap.putAll(secretFormat.getParameterMap());

        // omit secret
        newParameterMap.remove("secret");

        SecretKey key = SharedSecret.generateKey(128);
        IvParameterSpec ivParameterSpec = SharedSecret.generateIv();
        String encryptedSecret = SharedSecret.encrypt(secret.getBase32EncodedSecret(), key, ivParameterSpec);

        // TODO: share secret
        return new String[32];
    }

    // TODO: recover

    public static String encrypt(String input, SecretKey key, IvParameterSpec iv) throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance(encryption);
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);
        byte[] cipherText = cipher.doFinal(input.getBytes());
        return Base32Wrapper.encodeBytesToString(cipherText);
    }

    public static String decrypt(String base32EncodedCipherText, SecretKey key,
                                 IvParameterSpec iv) throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance(encryption);
        cipher.init(Cipher.DECRYPT_MODE, key, iv);
        byte[] decrypted = cipher.doFinal(Base32Wrapper.decodeStringToBytes(base32EncodedCipherText));
        return new String(decrypted, StandardCharsets.UTF_8);
    }
}