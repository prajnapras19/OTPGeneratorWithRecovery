package com.example.otpgeneratorwithrecovery.util;

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
}