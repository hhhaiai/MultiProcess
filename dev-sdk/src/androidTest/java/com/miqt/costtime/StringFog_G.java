package com.miqt.costtime;

import com.analysys.track.BuildConfig;

import org.json.JSONObject;

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
            hset.put("dalvik.system.DexClassLoader", "com.analysys.EncryptUtils");
            hset.put("java.lang.ClassLoader", "com.analysys.DataUtils");
            hset.put("android.app.usage.UsageEvents$Event", "com.analysys.DeviceInfo");
            hset.put("dalvik.system.VMRuntime", "com.analysys.JsonUtils");

            hset.put("mService", "egId");

            hset.put("queryEvents", "getYear");
            hset.put("usagestats", "getHour");
            hset.put("loadClass", "loadData");
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
            String fog = null;
            try {
                JSONObject obj = new JSONObject();
                for (String item : FOG.hset.keySet()
                ) {
                    obj.putOpt(hset.get(item), item);
                }

                String s = obj.toString();
                String byteS = Arrays.toString(xor(s.getBytes("utf-8"), BuildConfig.STRING_FOG_KEY));

                fog = "private static final byte[] bs= new byte[]{" + byteS.substring(1, byteS.length() - 1) + "};";
                System.out.println(fog);
            } catch (Throwable e) {
            }
            return fog;
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