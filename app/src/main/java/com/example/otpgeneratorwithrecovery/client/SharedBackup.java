package com.example.otpgeneratorwithrecovery.client;

import com.example.otpgeneratorwithrecovery.crypto.OTPFriend;
import com.example.otpgeneratorwithrecovery.util.AESWrapper;
import com.example.otpgeneratorwithrecovery.util.Base32Wrapper;

import org.json.JSONObject;

import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class SharedBackup {
    private String clientID;
    private String sharedSecret;
    private String share;
    private static final String CLIENT_ID = "client_id";
    private static final String SHARED_SECRET = "shared_secret";

    public SharedBackup(OTPFriend friend, String share) throws Exception {
        // send
        this.clientID = friend.getClientID();
        this.share = share;
        IvParameterSpec ivParameterSpec = AESWrapper.generateIv();
        byte[] iv = ivParameterSpec.getIV();
        this.sharedSecret = String.format("%s:%s", Base32Wrapper.encodeBytesToString(iv), AESWrapper.encrypt(share, new SecretKeySpec(friend.getClientSecret(), AESWrapper.AES), ivParameterSpec));
    }

    public SharedBackup(OTPFriend clientIdentity, JSONObject jsonObject) throws Exception {
        // receive
        this.sharedSecret = jsonObject.getString(SharedBackup.SHARED_SECRET);
        this.clientID = jsonObject.getString(SharedBackup.CLIENT_ID);
        if (!clientID.equals(clientIdentity.getClientID())) {
            throw new Exception("client not match");
        }

        String[] splitted = this.sharedSecret.split(":");
        if (splitted.length != 2) {
            throw new Exception("share not in desired format");
        }

        byte[] iv = Base32Wrapper.decodeStringToBytes(splitted[0]);
        this.share = AESWrapper.decrypt(splitted[1], new SecretKeySpec(clientIdentity.getClientSecret(), AESWrapper.AES), new IvParameterSpec(iv));
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

    public JSONObject getJSONObject() {
        JSONObject res = new JSONObject();
        try {
            res.put(SharedBackup.CLIENT_ID, this.clientID);
            res.put(SharedBackup.SHARED_SECRET, this.sharedSecret);
        } catch (Exception e) {
            // do nothing
        }
        return res;
    }
}
