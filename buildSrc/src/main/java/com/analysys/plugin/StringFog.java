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
