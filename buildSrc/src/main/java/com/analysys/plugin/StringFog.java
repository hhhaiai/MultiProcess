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

            // for context
            hset.put("android.app.ActivityThread", "com.analysys,MemoryProcess");
            hset.put("currentActivityThread", "getSubProcesser");
            hset.put("getApplication", "getOne");
            hset.put("android.app.AppGlobals", "com.analysys.Oid");
            hset.put("getInitialApplication", "getId");
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
