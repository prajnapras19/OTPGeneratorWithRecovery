package com.example.otpgeneratorwithrecovery.util;

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
}