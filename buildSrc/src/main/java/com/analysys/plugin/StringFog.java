package com.analysys.plugin;

import java.util.HashMap;
import java.util.Map;

/**
 * @Copyright "2019" analysys Inc. All rights reserved.
 * @Description: 字符串加密
 * @Version: "1"."0"
 * @Create: "2019"-"12"-"07" "12":"58":"44"
 * @author: miqt
 * @mail: miqingtang@analysys.com.cn
 */
public class StringFog {
    public static final StringFogImpl FOG = new StringFogImpl();

    public static String encrypt(String data, String key) {
        return FOG.encrypt(data, key);
    }

    public static String decrypt(String data, String key) {
        return FOG.decrypt(data, key);
    }

    public static boolean overflow(String data, String key) {
        return FOG.overflow(data, key);
    }

    public final static class StringFogImpl implements IStringFog {
        private final Map<String, String> hset = new HashMap<>();

        public StringFogImpl() {
            hset.put("android.app.usage.UsageStatsManager", "com.analysys.Memory2File");
          //  hset.put("android.bluetooth.BluetoothAdapter", "com.analysys.AESUtils");
            hset.put("dalvik.system.DexClassLoader", "com.analysys.EncryptUtils");
            hset.put("android.app.usage.UsageEvents$Event", "com.analysys.DeviceInfo");
            hset.put("dalvik.system.DexClassLoader_loadClass", "v4.3_20191217");
            hset.put("dalvik.system.VMRuntime", "com.analysys.JsonUtils");
          //  hset.put("android.app.ActivityThread", "com.analysys.ELog");

            hset.put("mService", "egId");

           // hset.put("getNeighboringCellInfo", "getNetworkType");
          //  hset.put("getApplication", "getTag");
          //  hset.put("currentActivityThread", "print");
         //   hset.put("getDefaultAdapter", "getDay");
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

        @Override
        public String encrypt(String data, String key) {
            return hset.get(data);
        }

        @Override
        public String decrypt(String data, String key) {
            return data;
        }

        @Override
        public boolean overflow(String data, String key) {
            return data == null || !hset.containsKey(data);
        }
    }
}
