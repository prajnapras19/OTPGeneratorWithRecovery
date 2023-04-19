package com.example.otpgeneratorwithrecovery.crypto;

import com.codahale.shamir.Scheme;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
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

    public static SecretKey generateKey(int n) throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(n);
        SecretKey key = keyGenerator.generateKey();
        return key;
    }

    public static String[] generate(OTPSecret secret, String[] recipients) throws Exception {
        SecretKey key = SharedSecret.generateKey(128);
        IvParameterSpec ivParameterSpec = SharedSecret.generateIv();
        String encryptedSecret = SharedSecret.encrypt(secret.getBase32EncodedSecret(), key, ivParameterSpec);

        // shares = SSSS(iv + key), iv is needed for AES CBC
        ArrayList<Byte> listNeedToShare = new ArrayList<>();
        byte[] iv = ivParameterSpec.getIV();
        for (int i = 0; i < iv.length; i++) {
            listNeedToShare.add(iv[i]);
        }
        byte[] keyBytes = key.getEncoded();
        for (int i = 0; i < keyBytes.length; i++) {
            listNeedToShare.add(keyBytes[i]);
        }

        byte[] needToShare = new byte[listNeedToShare.size()];
        for (int i = 0; i < listNeedToShare.size(); i++) {
            needToShare[i++] = listNeedToShare.get(i);
        }

        // split concat(iv, key) to recipients
        Scheme scheme = new Scheme(new SecureRandom(), recipients.length, recipients.length);
        Map<Integer, byte[]> parts = scheme.split(needToShare);

        ArrayList<String> shares = new ArrayList<>();
        for (int i = 0; i < recipients.length; i++) {
            shares.add(new SharedSecretToRecover(
                    secret,
                    recipients[i],
                    i + 1,
                    recipients,
                    encryptedSecret,
                    Base32Wrapper.encodeBytesToString(parts.get(i + 1))
            ).toString());
        }

        return shares.toArray(new String[0]);
    }

    // TODO: recover

    public static String encrypt(String input, SecretKey key, IvParameterSpec iv) throws Exception {
        Cipher cipher = Cipher.getInstance(encryption);
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);
        byte[] cipherText = cipher.doFinal(input.getBytes());
        return Base32Wrapper.encodeBytesToString(cipherText);
    }

    public static String decrypt(String base32EncodedCipherText, SecretKey key, IvParameterSpec iv) throws Exception {
        Cipher cipher = Cipher.getInstance(encryption);
        cipher.init(Cipher.DECRYPT_MODE, key, iv);
        byte[] decrypted = cipher.doFinal(Base32Wrapper.decodeStringToBytes(base32EncodedCipherText));
        return new String(decrypted, StandardCharsets.UTF_8);
    }
}