package com.analysys;

import android.text.TextUtils;
import android.util.Base64;
import android.util.Pair;

import java.util.Random;

public class EncUtils {
    public static Pair<String, String> enc(String data, int strength) {
        if (data == null || data.length() <= 1) {
            return new Pair<>("", data);
        }
        if (strength <= 0) {
            return new Pair<>("", data);
        }
        String key = "";
        String result = Base64.encodeToString(data.getBytes(), Base64.NO_WRAP);
        int len = result.length();
        for (int i = 0; i < strength; i++) {
            int random = new Random().nextInt(len);
            key = key + random + "|";
            String rw1 = result.substring(0, random);
            String rw2 = result.substring(random, len);
            result = rw2 + rw1;
        }
        key = key.substring(0, key.length() - 1);
        return new Pair<>(Base64.encodeToString(key.getBytes(), Base64.NO_WRAP), result);
    }

    public static String dec(Pair<String, String> data) {
        if (data == null) {
            return null;
        }
        if (TextUtils.isEmpty(data.first)) {
            return data.second;
        }
        String key = new String(Base64.decode(data.first, Base64.NO_WRAP));
        String result = data.second;
        String[] keys = key.split("\\|");
        int len = result.length();
        for (int i = keys.length - 1; i >= 0; i--) {
            int random = Integer.parseInt(keys[i]);
            String rw1 = result.substring(0, len - random);
            String rw2 = result.substring(len - random, len);
            result = rw2 + rw1;
        }
        return new String(Base64.decode(result, Base64.NO_WRAP));
    }
}
