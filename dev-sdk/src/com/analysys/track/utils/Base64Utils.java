package com.analysys.track.utils;

import android.text.TextUtils;
import android.util.Base64;

import java.util.Calendar;

public class Base64Utils {
    /**
     * 数据加密
     */
    public static String encrypt(String info, long time) {
        if (TextUtils.isEmpty(info)) {
            return null;
        }
        int key = getTimeTag(time);
        byte[] bytes = info.getBytes();
        int len = bytes.length;
        for (int i = 0; i < len; i++) {
            bytes[i] = (byte) (bytes[i] ^ key);
            key = bytes[i];
        }
        byte[] bt = Base64.encode(bytes, Base64.NO_WRAP);
        return new String(bt);
    }

    /**
     * 数据解密
     */
    public static String decrypt(String info, long time) throws Exception {
        if (TextUtils.isEmpty(info)) {
            return null;
        }
        int key = getTimeTag(time);
        byte[] bytes = Base64.decode(info.getBytes("UTF-8"), Base64.NO_WRAP);
        int len = bytes.length;
        for (int i = len - 1; i > 0; i--) {
            bytes[i] = (byte) (bytes[i] ^ bytes[i - 1]);
        }
        bytes[0] = (byte) (bytes[0] ^ key);
        return new String(bytes);
    }

    /**
     * 获取时段标记
     */
    public static int getTimeTag(long timestamp) {
        int tag = 0;
        final Calendar mCalendar = Calendar.getInstance();
        mCalendar.setTimeInMillis(timestamp);
        int h = mCalendar.get(Calendar.HOUR_OF_DAY);
        if (0 < h && h < 6) {
            tag = 1;
        } else if (6 <= h && h < 12) {
            tag = 2;
        } else if (12 <= h && h < 18) {
            tag = 3;
        } else if (18 <= h && h < 24) {
            tag = 4;
        }
        return tag;
    }

}
