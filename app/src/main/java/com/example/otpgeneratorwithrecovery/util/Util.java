package com.example.otpgeneratorwithrecovery.util;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;

import android.graphics.Bitmap;

import com.example.otpgeneratorwithrecovery.crypto.Base32Wrapper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

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

    public static Bitmap getQRCode(String str, int width) throws WriterException {
        BitMatrix result;
        try {
            result = new MultiFormatWriter().encode(str,
                    BarcodeFormat.QR_CODE, width, width, null);
        } catch (IllegalArgumentException iae) {
            // Unsupported format
            return null;
        }
        int w = result.getWidth();
        int h = result.getHeight();
        int[] pixels = new int[w * h];
        for (int y = 0; y < h; y++) {
            int offset = y * w;
            for (int x = 0; x < w; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, w, h);
        return bitmap;
    }
}