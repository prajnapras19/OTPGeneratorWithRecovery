package com.example.otpgeneratorwithrecovery.crypto;

import com.codahale.shamir.Scheme;

import org.apache.commons.codec.binary.Hex;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
    public static String[] generate(OTPSecret secret, String[] recipients, int threshold) throws Exception {
        SecretKey key = AESWrapper.generateKey(128);
        IvParameterSpec ivParameterSpec = AESWrapper.generateIv();
        String encryptedSecret = AESWrapper.encrypt(secret.getBase32EncodedSecret(), key, ivParameterSpec);

        // shares = SSSS(iv + key), iv is needed for AES CBC
        byte[] iv = ivParameterSpec.getIV();
        byte[] keyBytes = key.getEncoded();
        byte[] needToShare = new byte[iv.length + keyBytes.length];
        for (int i = 0; i < iv.length; i++) {
            needToShare[i] = iv[i];
        }
        for (int i = 0; i < keyBytes.length; i++) {
            needToShare[i + iv.length] = keyBytes[i];
        }

        Map<Integer, byte[]> parts;
        if (recipients.length == 1) {
            parts = new HashMap<>();
            parts.put(1, needToShare);
        } else {
            // split concat(iv, key) to recipients
            // constraint:
            // threshold > 1
            // recipients.length >= threshold
            // recipients.length < 256
            if (!((threshold > 1) && (recipients.length >= threshold) && (recipients.length < 256))) {
                throw new Exception("threshold must be a number between 2 and number of recipients (255 at max).");
            }

            Scheme scheme = new Scheme(new SecureRandom(), recipients.length, threshold);
            parts = scheme.split(needToShare);
        }

        // random uid
        UUID uid = UUID.randomUUID();

        ArrayList<String> shares = new ArrayList<>();
        for (int i = 0; i < recipients.length; i++) {
            shares.add(new SharedSecretToRecover(
                    secret,
                    recipients[i],
                    i + 1,
                    recipients,
                    encryptedSecret,
                    Base32Wrapper.encodeBytesToString(parts.get(i + 1)),
                    threshold,
                    uid.toString()
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
            if (!pickedSharedSecretToRecover.hasSameSource(sharedSecretToRecover)) {
                throw new Exception("the given backups doesn't belong to the same OTP.");
            }
            sharedSecret.put(sharedSecretToRecover.getRecipientNumber(), Base32Wrapper.decodeStringToBytes(sharedSecretToRecover.getSharedEncryptionKey()));
        }

        byte[] recovered;
        if (recipientsLength == 1) {
            recovered = sharedSecret.get(1);
        } else {
            // constraint:
            // threshold > 1
            // recipients.length >= threshold
            // recipients.length < 256
            if (!((pickedSharedSecretToRecover.getThreshold() > 1) && (recipientsLength >= pickedSharedSecretToRecover.getThreshold()) && (recipientsLength < 256))) {
                throw new Exception("threshold must be a number between 2 and number of recipients (255 at max).");
            }

            if (sharedSecret.size() < pickedSharedSecretToRecover.getThreshold()) {
                throw new Exception("recipients count is lower than threshold, recovery cannot be done.");
            }

            Scheme scheme = new Scheme(new SecureRandom(), recipientsLength, pickedSharedSecretToRecover.getThreshold());
            recovered = scheme.join(sharedSecret);
        }

        byte[] ivBytes = Arrays.copyOfRange(recovered, 0, 16);
        byte[] keyBytes = Arrays.copyOfRange(recovered, 16, 32);

        String decryptedSecret = AESWrapper.decrypt(encryptedSecret, new SecretKeySpec(keyBytes, AESWrapper.AES), new IvParameterSpec(ivBytes));

        Map<String, String> newParameterMap = new HashMap<>();
        newParameterMap.putAll(pickedSharedSecretToRecover.getFormat().getParameterMap());

        // omit SharedSecretToRecover parameters
        newParameterMap.remove(SharedSecretToRecover.RECIPIENT);
        newParameterMap.remove(SharedSecretToRecover.RECIPIENTS);
        newParameterMap.remove(SharedSecretToRecover.RECIPIENT_NUMBER);
        newParameterMap.remove(SharedSecretToRecover.ENCRYPTED_SECRET);
        newParameterMap.remove(SharedSecretToRecover.SHARED_ENCRYPTION_KEY);
        newParameterMap.remove(SharedSecretToRecover.THRESHOLD);
        newParameterMap.remove(SharedSecretToRecover.UID);

        // insert secret to map
        newParameterMap.put("secret", decryptedSecret);

        OTPURIFormat format = new OTPURIFormat(OTPSecret.PREFIX_OTP_SECRET, pickedSharedSecretToRecover.getFormat().getType(), pickedSharedSecretToRecover.getFormat().getLabel(), newParameterMap);
        return new OTPSecret(format);
    }
}