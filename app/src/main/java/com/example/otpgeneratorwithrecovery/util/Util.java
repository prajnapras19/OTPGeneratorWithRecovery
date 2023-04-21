package com.example.otpgeneratorwithrecovery.util;

import com.example.otpgeneratorwithrecovery.crypto.Base32Wrapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Util {
    public static String getNextSharedPreferenceID(Map<String, ?> sharedPreferenceAllValues) {
        int max = 0;
        if (sharedPreferenceAllValues == null) {
            return String.valueOf(max + 1);
        }
        for (Map.Entry<String,?> entry : sharedPreferenceAllValues.entrySet()) {
            max = Math.max(max, Integer.valueOf(entry.getKey()));
        }
        return String.valueOf(max + 1);
    }

    public static List<String> getSortedMapKey(Map<String, ?> sharedPreferenceAllValues) {
        List<Integer> idList = new ArrayList<>();
        for (Map.Entry<String,?> entry : sharedPreferenceAllValues.entrySet()) {
            idList.add(Integer.valueOf(entry.getKey()));
        }
        Collections.sort(idList);

        List<String> res = new ArrayList<>();
        for (Integer id : idList) {
            res.add(String.valueOf(id));
        }
        return res;
    }

    public static String getRemainingOTPTime(String period) {
        if (period.equals("")) {
            return "-";
        }

        long now = System.currentTimeMillis() / 1000L;
        long x = Long.parseLong(period);
        long time = now / x;

        return String.valueOf(x - (now - time * x));
    }

    public static String getBeautifiedBackup(String backup) {
        return String.format("Hello, I am using OTP Generator With Recovery (https://github.com/prajnapras19/OTPGeneratorWithRecovery) and I need your help to save my backup in your application. You can input this in the application:\n\n%s\n\nThank you in advance!", Base32Wrapper.encodeStringToString(backup));
    }
}