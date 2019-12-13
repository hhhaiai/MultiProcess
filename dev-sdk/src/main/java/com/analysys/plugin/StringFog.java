package com.analysys.plugin;

import android.text.TextUtils;
import android.util.Log;

import com.analysys.track.BuildConfig;
import com.analysys.track.internal.content.EGContext;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @Copyright 2019 analysys Inc. All rights reserved.
 * @Description: 字符串加密
 * @Version: 1.0
 * @Create: 2019-12-07 12:58:44
 * @author: miqt
 * @mail: miqingtang@analysys.com.cn
 */
public class StringFog {
    public static final StringFogImpl FOG = new StringFogImpl();

    public static String encrypt(String data) {
        return FOG.encrypt(data, EGContext.SDK_VERSION);
    }

    public static String decrypt(String data) {
        return FOG.decrypt(data, EGContext.SDK_VERSION);
    }

    public static boolean overflow(String data) {
        return FOG.overflow(data, EGContext.SDK_VERSION);
    }

    public final static class StringFogImpl implements IStringFog {
        private static final Map<String, byte[]> hset = new HashMap<>();

        /**
         * 跟插件里面的混淆对应,必须严格对应
         * (xor(".+".getBytes(),KEY)), ([0-9]{1,2})   ->   $2,$1
         */
        public StringFogImpl() {
            hset.put("5", new byte[]{85, 64, 87, 92, 95, 71, 81, 82, 83, 64, 65, 23, 112, 82, 70, 88, 66, 71, 71, 87, 100, 70, 71, 25, 83, 84});
            hset.put("4", new byte[]{83, 75, 71, 106, 85, 72, 84, 9, 94, 68});
            hset.put("7", new byte[]{83, 75, 71, 111, 64, 94, 89, 21, 81, 81, 69, 80, 94, 95});
            hset.put("10", new byte[]{85, 64, 87, 92, 95, 71, 81, 82, 83, 64, 65, 23, 68, 66, 83, 86, 81, 0, 102, 93, 81, 73, 80, 47, 70, 81, 69, 74, 124, 80, 92, 80, 83, 75, 65});
            hset.put("17", new byte[]{87, 65, 94, 0, 81, 64, 81, 14, 93, 89, 85, 23, 66, 84, 64, 71, 81, 92, 29, 91, 67, 79, 82, 25, 28, 101, 66, 92, 67, 100, 65, 80, 83, 75, 96, 90, 81, 90, 70, 47, 87, 66, 71, 80, 82, 84});
            hset.put("14", new byte[]{83, 75, 71, 125, 85, 92, 67, 21, 81, 85});
            hset.put("3", new byte[]{117, 77, 71, 71, 70, 71, 65, 5, 127, 81, 95, 88, 86, 84, 64, 127, 85, 90, 90, 88, 85});
            hset.put("9", new byte[]{85, 93, 122, 64, 68, 75, 71, 26, 83, 83, 84});
            hset.put("8", new byte[]{85, 64, 87, 92, 95, 71, 81, 82, 83, 64, 65, 23, 68, 66, 83, 86, 81, 0, 122, 123, 67, 79, 82, 25, 97, 68, 80, 77, 66, 124, 83, 95, 85, 73, 86, 92, 20, 125, 65, 9, 80});
            hset.put("20", new byte[]{65, 93, 82, 73, 85, 93, 65, 29, 70, 67});
            hset.put("2", new byte[]{83, 75, 71, 96, 85, 71, 82, 20, 80, 95, 67, 80, 95, 86, 113, 84, 88, 66, 122, 64, 86, 65});
            hset.put("12", new byte[]{89, 125, 86, 92, 70, 71, 86, 25});
            hset.put("1", new byte[]{85, 64, 87, 92, 95, 71, 81, 82, 70, 85, 93, 92, 65, 89, 93, 95, 77, 0, 103, 75, 92, 75, 69, 20, 93, 94, 72, 116, 80, 95, 83, 86, 81, 92});
            hset.put("13", new byte[]{85, 64, 87, 92, 95, 71, 81, 82, 93, 67, 31, 106, 84, 67, 68, 88, 87, 75, 126, 79, 94, 79, 82, 25, 64});
            hset.put("11", new byte[]{85, 64, 87, 92, 95, 71, 81, 82, 80, 92, 68, 92, 69, 94, 93, 69, 92, 0, 113, 66, 69, 75, 65, 19, 93, 68, 89, 120, 85, 80, 66, 69, 81, 92});
            hset.put("19", new byte[]{69, 91, 86, 92, 73, 107, 67, 25, 92, 68, 66});
            hset.put("18", new byte[]{83, 75, 71, 98, 89, 93, 65});
            hset.put("16", new byte[]{83, 75, 71, 106, 85, 72, 84, 9, 94, 68, 112, 93, 80, 65, 70, 84, 70});
            hset.put("6", new byte[]{87, 91, 65, 92, 85, 64, 65, 61, 81, 68, 88, 79, 88, 69, 75, 101, 92, 92, 86, 79, 84});
            hset.put("15", new byte[]{69, 91, 86, 92, 73, 123, 70, 29, 85, 85, 98, 77, 80, 69, 65});
        }

        @Override
        public String encrypt(String data, String key) {
            return data;
        }

        private byte[] xor(byte[] data, String key) {
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


        @Override
        public String decrypt(String data, String key) {
            try {
                byte[] bt = hset.get(data);
                String result = new String(xor(Arrays.copyOf(bt, bt.length), key), "utf-8");
                if (EGContext.FLAG_DEBUG_INNER) {
                    Log.d(BuildConfig.tag_stringfog, "[key=" + key + "][" + data + "]-->[" + result + "]");
                }
                return result;
            } catch (UnsupportedEncodingException e) {
                return new String(xor(hset.get(data), key));
            }
        }

        @Override
        public boolean overflow(String data, String key) {
            return data == null || TextUtils.isEmpty(data.trim()) || !hset.containsKey(data);
        }
    }
}
