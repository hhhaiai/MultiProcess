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
            hset.put("android.app.usage.UsageStatsManager", "com.analysys.Memory2File");
            hset.put("android.bluetooth.BluetoothAdapter", "com.analysys.AESUtils");
            hset.put("dalvik.system.DexClassLoader", "com.analysys.EncryptUtils");
            hset.put("android.app.usage.UsageEvents$Event", "com.analysys.DeviceInfo");
            hset.put("dalvik.system.DexClassLoader_loadClass", "v4.3_20191217");
            hset.put("dalvik.system.VMRuntime", "com.analysys.JsonUtils");

            hset.put("mService", "egId");

            hset.put("getNeighboringCellInfo", "getNetworkType");
            hset.put("getDefaultAdapter", "getDay");
            hset.put("queryEvents", "getYear");
            hset.put("usagestats", "getHour");
            hset.put("hasNextEvent", "getSeconds");
            hset.put("getPackageName", "getName");
            hset.put("getTimeStamp", "getTimeData");
            hset.put("getEventType", "getTime");
            hset.put("getNextEvent", "getNumberOfCPUCores");
            hset.put("getClassLoader", "getAppKey");
            hset.put("getRuntime", "getSDKVer");
            hset.put("setHiddenApiExemptions", "setAppKey");
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