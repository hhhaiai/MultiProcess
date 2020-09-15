package com.analysys.track.utils;

import android.text.TextUtils;
import android.util.Base64;
import android.util.Pair;
/**
 * @Copyright 2020 analysys Inc. All rights reserved.
 * @Description: 魔改的一个混淆工具，为base64+二进制异或操作，二进制异或Key为时间戳取余120
 * @Version: 1.0
 * @Create: 2020-09-15 14:37:40
 * @author: miqt
 * @mail: miqingtang@analysys.com.cn
 */
public class EncUtils {
    public static Pair<String, String> enc(String data, int strength) {
        if (data == null || data.length() <= 1) {
            return new Pair<>("", data);
        }
        if (strength <= 0) {
            return new Pair<>("", data);
        }
        long curTime = System.currentTimeMillis();
        String key = String.valueOf(curTime % 120);
        String result = Base64.encodeToString(data.getBytes(), Base64.NO_WRAP);
        byte[] xorData = xor(result.getBytes(), key);
        return new Pair<>(Base64.encodeToString(String.valueOf(curTime).getBytes(), Base64.NO_WRAP),
                new String(xorData));
    }

    public static String dec(Pair<String, String> data) {
        if (data == null) {
            return null;
        }
        if (TextUtils.isEmpty(data.first)) {
            return data.second;
        }
        String key = new String(Base64.decode(data.first, Base64.NO_WRAP));
        key = String.valueOf(Long.valueOf(key) % 120);
        String result = data.second;
        byte[] xorData = xor(result.getBytes(), key);
        return new String(Base64.decode(xorData, Base64.NO_WRAP));
    }

    public static byte[] xor(byte[] data, String key) {
        int len = data.length;
        int lenKey = key.length();
        int i = 0;
        int j = 0;
        while (i < len) {
            if (j >= lenKey) {
                j = 0;
            }
            data[i] = (byte) (data[i] ^ key.charAt(j));
            i++;
            j++;
        }
        return data;
    }
}
