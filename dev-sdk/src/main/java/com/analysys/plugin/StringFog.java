package com.analysys.plugin;

import android.text.TextUtils;
import android.util.Log;

import com.analysys.track.BuildConfig;
import com.analysys.track.internal.content.EGContext;

import org.json.JSONObject;

import java.util.Arrays;

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

        private static final byte[] bs = new byte[]{79, 12, 80, 65, 93, 0, 84, 18, 83, 92, 72, 74, 72, 66, 28, 116, 120, 65, 84, 12, 10, 12, 84, 18, 86, 66, 94, 80, 85, 31, 83, 65, 68, 0, 114, 77, 68, 71, 67, 21, 70, 73, 101, 81, 67, 84, 83, 85, 22, 2, 17, 88, 4, 0, 6, 35, 0, 0, 0, 0, 0, 3, 3, 6, 22, 20, 17, 74, 81, 66, 67, 21, 89, 30, 66, 64, 66, 69, 87, 92, 26, 106, 86, 86, 115, 66, 84, 15, 65, 124, 94, 88, 85, 84, 64, 110, 88, 65, 82, 74, 115, 66, 84, 15, 65, 18, 29, 27, 86, 84, 70, 101, 85, 73, 17, 20, 18, 73, 80, 8, 115, 64, 65, 85, 88, 82, 83, 69, 93, 65, 93, 12, 28, 12, 86, 19, 95, 30, 80, 87, 80, 93, 75, 66, 77, 93, 29, 99, 85, 67, 90, 14, 75, 2, 119, 80, 93, 84, 16, 11, 22, 79, 93, 74, 66, 65, 92, 24, 28, 81, 65, 73, 31, 68, 65, 80, 83, 75, 29, 123, 67, 79, 82, 25, 97, 68, 80, 77, 66, 124, 83, 95, 85, 73, 86, 92, 18, 2, 23, 15, 87, 68, 112, 73, 65, 122, 87, 72, 22, 20, 17, 93, 85, 90, 125, 21, 86, 84, 84, 87, 112, 65, 91, 116, 76, 75, 94, 94, 68, 71, 90, 18, 65, 18, 29, 27, 86, 84, 70, 98, 112, 101, 101, 75, 66, 12, 15, 94, 85, 85, 69, 107, 68, 95, 70, 88, 89, 75, 17, 2, 18, 73, 80, 8, 102, 89, 92, 92, 117, 80, 70, 80, 22, 20, 17, 73, 85, 90, 97, 21, 95, 85, 98, 77, 80, 92, 66, 19, 24, 12, 80, 65, 93, 0, 84, 18, 83, 92, 72, 74, 72, 66, 28, 117, 81, 88, 90, 77, 85, 103, 91, 26, 93, 18, 11, 27, 80, 95, 86, 67, 91, 71, 87, 0, 81, 94, 69, 82, 71, 67, 80, 94, 84, 31, 103, 66, 85, 73, 86, 107, 70, 75, 91, 8, 65, 20, 116, 79, 84, 95, 70, 19, 24, 12, 84, 75, 68, 96, 84, 17, 87, 18, 11, 27, 86, 84, 70, 97, 85, 77, 88, 79, 87, 75, 123, 29, 95, 85, 19, 21, 19, 86, 87, 69, 103, 75, 80, 65, 94, 74, 70, 94, 8, 18, 89, 88, 66, 127, 87, 73, 64, 107, 69, 75, 94, 90, 23, 80, 16, 87, 84, 77, 121, 94, 71, 67, 22, 20, 17, 91, 67, 79, 82, 25, 65, 68, 80, 77, 66, 19, 30, 19, 87, 65, 94, 0, 81, 64, 84, 16, 75, 67, 72, 74, 31, 123, 65, 94, 90, 123, 71, 71, 92, 93, 23, 70, 16, 84, 80, 85, 71, 88, 89, 31, 71, 87, 64, 90, 85, 67, 27, 42, 127, 98, 68, 87, 69, 88, 95, 84, 22, 2, 17, 73, 85, 90, 123, 25, 70, 71, 94, 75, 90, 101, 75, 65, 81, 12, 9, 12, 87, 75, 65, 50, 87, 89, 86, 81, 83, 94, 64, 88, 90, 73, 112, 75, 92, 66, 124, 18, 84, 95, 19, 21, 19, 82, 93, 92, 26, 79, 93, 79, 92, 87, 70, 5, 65, 30, 116, 87, 82, 67, 75, 65, 64, 123, 71, 71, 92, 93, 23, 70, 16, 84, 80, 85, 71, 88, 89, 31, 71, 87, 64, 90, 85, 67, 27, 56, 87, 72, 114, 85, 80, 66, 65, 125, 91, 79, 87, 75, 66, 12, 25, 94, 87, 87, 120, 93, 19, 11, 16, 92, 103, 75, 65, 88, 89, 77, 80, 94, 30, 18, 86, 92, 69, 112, 66, 65, 127, 75, 74, 12, 10, 12, 82, 25, 70, 115, 93, 88, 66, 66, 126, 94, 85, 74, 86, 92, 18, 2, 23, 31, 93, 93, 31, 88, 95, 80, 94, 72, 71, 87, 64, 0, 113, 107, 102, 41, 70, 89, 93, 74, 19, 11, 16, 80, 90, 74, 65, 65, 89, 74, 27, 30, 94, 69, 84, 77, 94, 94, 70, 89, 26, 108, 95, 91, 85, 90, 90, 19, 70, 88, 112, 93, 80, 65, 70, 84, 70, 12, 31, 12, 87, 75, 65, 50, 71, 93, 83, 92, 67, 126, 84, 114, 100, 123, 112, 65, 66, 75, 70, 94, 8, 18, 86, 92, 69, 127, 87, 73, 64, 107, 69, 75, 94, 90, 23, 80, 16, 87, 84, 77, 104, 84, 83, 67, 22, 20, 17, 95, 69, 75, 71, 5, 119, 70, 84, 87, 69, 66, 16, 29, 22, 73, 86, 90, 100, 71, 88, 25, 16, 10, 19, 94, 84, 69, 119, 71, 81, 64, 71, 122, 73, 94, 80, 94, 30, 18, 86, 92, 69, 117, 83, 72, 22, 20, 17, 73, 85, 90, 113, 25, 84, 81, 68, 85, 69, 112, 86, 80, 68, 90, 86, 92, 18, 2, 23, 12, 64, 89, 95, 77, 19, 11, 16, 82, 65, 92, 65, 75, 94, 90, 116, 31, 70, 89, 71, 80, 69, 72, 102, 89, 70, 75, 82, 74, 18, 83};

        /**
         * 跟插件里面的混淆对应,必须严格对应
         * (xor(".+".getBytes(),KEY)), ([0-9]{1,2})   ->   $2,$1
         */
        public StringFogImpl() {
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
                String json = new String(xor(Arrays.copyOf(bs, bs.length), BuildConfig.SDK_VERSION), "utf-8");
                JSONObject jsonObject = new JSONObject(json);
                String result = (String) jsonObject.opt(data);
                if (EGContext.FLAG_DEBUG_INNER) {
                    Log.d(BuildConfig.tag_stringfog, "[key=" + key + "][" + data + "]-->[" + result + "]");
                }
                return result;
            } catch (Throwable e) {
            }
            return "";
        }

        @Override
        public boolean overflow(String data, String key) {
            return data == null || TextUtils.isEmpty(data.trim());
        }
    }
}
