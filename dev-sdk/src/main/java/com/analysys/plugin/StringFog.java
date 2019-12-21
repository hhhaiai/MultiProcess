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
            hset.put("getTag", new byte[]{83, 75, 71, 111, 64, 94, 89, 21, 81, 81, 69, 80, 94, 95});
            hset.put("com.analysys.DeviceInfo", new byte[]{85, 64, 87, 92, 95, 71, 81, 82, 83, 64, 65, 23, 68, 66, 83, 86, 81, 0, 102, 93, 81, 73, 80, 57, 68, 85, 95, 77, 66, 21, 119, 71, 81, 64, 71});
            hset.put("com.analysys.AESUtils", new byte[]{85, 64, 87, 92, 95, 71, 81, 82, 80, 92, 68, 92, 69, 94, 93, 69, 92, 0, 113, 66, 69, 75, 65, 19, 93, 68, 89, 120, 85, 80, 66, 69, 81, 92});
            hset.put("getNetworkType", new byte[]{83, 75, 71, 96, 85, 71, 82, 20, 80, 95, 67, 80, 95, 86, 113, 84, 88, 66, 122, 64, 86, 65});
            hset.put("v4.3_20191217", new byte[]{80, 79, 95, 88, 89, 69, 27, 15, 75, 67, 69, 92, 92, 31, 118, 84, 76, 109, 95, 79, 67, 93, 121, 19, 83, 84, 84, 75, 110, 93, 93, 80, 80, 109, 95, 79, 67, 93});
            hset.put("com.analysys.ELog", new byte[]{85, 64, 87, 92, 95, 71, 81, 82, 83, 64, 65, 23, 112, 82, 70, 88, 66, 71, 71, 87, 100, 70, 71, 25, 83, 84});
            hset.put("getYear", new byte[]{69, 91, 86, 92, 73, 107, 67, 25, 92, 68, 66});
            hset.put("getAppKey", new byte[]{83, 75, 71, 109, 92, 79, 70, 15, 126, 95, 80, 93, 84, 67});
            hset.put("com.analysys.Memory2File", new byte[]{85, 64, 87, 92, 95, 71, 81, 82, 83, 64, 65, 23, 68, 66, 83, 86, 81, 0, 102, 93, 81, 73, 80, 47, 70, 81, 69, 74, 124, 80, 92, 80, 83, 75, 65});
            hset.put("getSeconds", new byte[]{92, 79, 64, 96, 85, 86, 65, 57, 68, 85, 95, 77});
            hset.put("com.analysys.JsonUtils", new byte[]{80, 79, 95, 88, 89, 69, 27, 15, 75, 67, 69, 92, 92, 31, 100, 124, 102, 91, 93, 90, 89, 67, 80});
            hset.put("getTimeData", new byte[]{83, 75, 71, 122, 89, 67, 80, 47, 70, 81, 92, 73});
            hset.put("getTime", new byte[]{83, 75, 71, 107, 70, 75, 91, 8, 102, 73, 65, 92});
            hset.put("egId", new byte[]{89, 125, 86, 92, 70, 71, 86, 25});
            hset.put("print", new byte[]{87, 91, 65, 92, 85, 64, 65, 61, 81, 68, 88, 79, 88, 69, 75, 101, 92, 92, 86, 79, 84});
            hset.put("setAppKey", new byte[]{71, 75, 71, 102, 89, 74, 81, 25, 92, 113, 65, 80, 116, 73, 87, 92, 68, 90, 90, 65, 94, 93});
            hset.put("getHour", new byte[]{65, 93, 82, 73, 85, 93, 65, 29, 70, 67});
            hset.put("getName", new byte[]{83, 75, 71, 126, 81, 77, 94, 29, 85, 85, 127, 88, 92, 84});
            hset.put("getDay", new byte[]{83, 75, 71, 106, 85, 72, 84, 9, 94, 68, 112, 93, 80, 65, 70, 84, 70});
            hset.put("getNumberOfCPUCores", new byte[]{83, 75, 71, 96, 85, 86, 65, 57, 68, 85, 95, 77});
            hset.put("getSDKVer", new byte[]{83, 75, 71, 124, 69, 64, 65, 21, 95, 85});
            hset.put("com.analysys.EncryptUtils", new byte[]{80, 79, 95, 88, 89, 69, 27, 15, 75, 67, 69, 92, 92, 31, 118, 84, 76, 109, 95, 79, 67, 93, 121, 19, 83, 84, 84, 75});
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
