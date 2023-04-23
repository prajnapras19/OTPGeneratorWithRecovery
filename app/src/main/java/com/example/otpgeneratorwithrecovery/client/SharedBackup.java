package com.example.otpgeneratorwithrecovery.client;

import com.example.otpgeneratorwithrecovery.crypto.OTPFriend;
import com.example.otpgeneratorwithrecovery.util.AESWrapper;
import com.example.otpgeneratorwithrecovery.util.Base32Wrapper;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class SharedBackup {
    private String clientID;
    private String sharedSecret;
    private String share;

    public SharedBackup(OTPFriend friend, String share, boolean isReceiving) throws Exception {
        if (isReceiving) {
            this.sharedSecret = share;
            this.clientID = friend.getClientID();

            String[] splitted = this.sharedSecret.split(":");
            if (splitted.length != 2) {
                throw new Exception("share not in desired format");
            }

            byte[] iv = Base32Wrapper.decodeStringToBytes(splitted[0]);
            this.share = AESWrapper.decrypt(splitted[1], new SecretKeySpec(friend.getClientSecret(), AESWrapper.AES), new IvParameterSpec(iv));
            return;
        }

        this.clientID = friend.getClientID();
        this.share = share;
        IvParameterSpec ivParameterSpec = AESWrapper.generateIv();
        byte[] iv = ivParameterSpec.getIV();
        this.sharedSecret = String.format("%s:%s", Base32Wrapper.encodeBytesToString(iv), AESWrapper.encrypt(share, new SecretKeySpec(friend.getClientSecret(), AESWrapper.AES), ivParameterSpec));
    }

    public String getClientID() {
        return this.clientID;
    }

    public String getShare() {
        return this.share;
    }

    public String getSharedSecret() {
        return this.sharedSecret;
    }
}
