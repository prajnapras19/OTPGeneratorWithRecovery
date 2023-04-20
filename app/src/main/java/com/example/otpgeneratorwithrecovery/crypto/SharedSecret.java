package com.example.otpgeneratorwithrecovery.crypto;

import com.codahale.shamir.Scheme;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

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

        Map<Integer, byte[]> parts;
        if (recipients.length == 1) {
            parts = new HashMap<>();
            parts.put(1, needToShare);
        } else {
            // split concat(iv, key) to recipients
            Scheme scheme = new Scheme(new SecureRandom(), recipients.length, recipients.length);
            parts = scheme.split(needToShare);
        }

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

    public static OTPSecret recover(String[] shares) throws Exception {
        Map<Integer, byte[]> sharedSecret = new HashMap<>();
        SharedSecretToRecover pickedSharedSecretToRecover = new SharedSecretToRecover(shares[0]); // pick one to get its OTPURIFormat, assume shares.length > 0
        int recipientsLength = pickedSharedSecretToRecover.getRecipients().length;
        String encryptedSecret = pickedSharedSecretToRecover.getEncryptedSecret();

        for (String s : shares) {
            SharedSecretToRecover sharedSecretToRecover = new SharedSecretToRecover(s);
            sharedSecret.put(sharedSecretToRecover.getRecipientNumber(), Base32Wrapper.decodeStringToBytes(sharedSecretToRecover.getSharedEncryptionKey()));
        }

        byte[] recovered;
        if (recipientsLength == 1) {
            recovered = sharedSecret.get(1);
        } else {
            Scheme scheme = new Scheme(new SecureRandom(), recipientsLength, recipientsLength);
            recovered = scheme.join(sharedSecret);
        }

        byte[] ivBytes = Arrays.copyOfRange(recovered, 0, 16);
        byte[] keyBytes = Arrays.copyOfRange(recovered, 16, 32);

        String decryptedSecret = SharedSecret.decrypt(encryptedSecret, new SecretKeySpec(keyBytes, "AES"), new IvParameterSpec(ivBytes));

        Map<String, String> newParameterMap = new HashMap<>();
        newParameterMap.putAll(pickedSharedSecretToRecover.getFormat().getParameterMap());

        // omit SharedSecretToRecover parameters
        newParameterMap.remove(SharedSecretToRecover.RECIPIENT);
        newParameterMap.remove(SharedSecretToRecover.RECIPIENTS);
        newParameterMap.remove(SharedSecretToRecover.RECIPIENT_NUMBER);
        newParameterMap.remove(SharedSecretToRecover.ENCRYPTED_SECRET);
        newParameterMap.remove(SharedSecretToRecover.SHARED_ENCRYPTION_KEY);

        // insert secret to map
        newParameterMap.put("secret", decryptedSecret);

        OTPURIFormat format = new OTPURIFormat(OTPSecret.PREFIX_OTP_SECRET, pickedSharedSecretToRecover.getFormat().getType(), pickedSharedSecretToRecover.getFormat().getLabel(), newParameterMap);
        return new OTPSecret(format);
    }

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