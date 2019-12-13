package com.miqt.costtime;

import com.analysys.track.BuildConfig;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户根据key生成解密代码的工具类
 */
public class StringFog_G {
    public static final StringFogImpl FOG = new StringFogImpl();

    public final static class StringFogImpl {
        private final Map<String, String> hset = new HashMap<>();

        public StringFogImpl() {
            hset.put("android.telephony.TelephonyManager", "1");
            hset.put("getNeighboringCellInfo", "2");
            hset.put("ActivityManagerNative", "3");
            hset.put("getDefault", "4");
            hset.put("android.app.ActivityThread", "5");
            hset.put("currentActivityThread", "6");
            hset.put("getApplication", "7");
            hset.put("android.app.usage.IUsageStatsManager$Stub", "8");
            hset.put("asInterface", "9");
            hset.put("android.app.usage.UsageStatsManager", "10");
            hset.put("android.bluetooth.BluetoothAdapter", "11");
            hset.put("mService", "12");
            hset.put("android.os.ServiceManager", "13");
            hset.put("getService", "14");
            hset.put("queryUsageStats", "15");
            hset.put("getDefaultAdapter", "16");
            hset.put("com.android.server.usage.UserUsageStatsService", "17");
            hset.put("getList", "18");
            hset.put("queryEvents", "19");
            hset.put("usagestats", "20");
        }


        public String g() {
            StringBuilder builder = new StringBuilder();
            for (String item : FOG.hset.keySet()
            ) {
                try {
                    String byteS = Arrays.toString(FOG.xor(item.getBytes("utf-8"), BuildConfig.SDK_VERSION));
                    builder
                            .append("hset.put(\"")
                            .append(FOG.hset.get(item))
                            .append("\",")
                            .append("new byte[]{")
                            .append(byteS.substring(1, byteS.length() - 1))
                            .append("});\n");
                    builder.toString();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }


            return builder.toString();
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
    }
}