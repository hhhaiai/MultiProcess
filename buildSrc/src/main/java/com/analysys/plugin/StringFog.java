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
            //hset.put("android.telephony.TelephonyManager", "1");
            //获取基站信息的,保留并混淆
            hset.put("getNeighboringCellInfo", "id");
            //获取字体的 替换了
//            hset.put("ActivityManagerNative", "3");
//            hset.put("getDefault", "4");
//            hset.put("android.app.ActivityThread", "5");
//            hset.put("currentActivityThread", "6");
//            hset.put("getApplication", "7");
            // hset.put("android.app.usage.IUsageStatsManager$Stub", "8");
            //  hset.put("asInterface", "9");
            hset.put("android.app.usage.UsageStatsManager", "name");
            //蓝牙的,混淆处理
            hset.put("android.bluetooth.BluetoothAdapter", "state");
            //蓝牙和usm的,混淆处理
            hset.put("mService", "12");
            //usm的
            //  hset.put("android.os.ServiceManager", "13");
            //usm获取service的一个方法
            // hset.put("getService", "14");
            //usm获取最后使用的,usm获取数据用的不是它
            //hset.put("queryUsageStats", "15");
            //蓝牙的
            hset.put("getDefaultAdapter", "ishas");
            //从数据库文件直接读usm信息 几乎读不到, 除非是root权限可以尝试下,待验证,先删除
//            hset.put("com.android.server.usage.UserUsageStatsService", "17");
            //usm获取数据用的不是它
            //hset.put("getList", "18");
            hset.put("queryEvents", "open");
            hset.put("usagestats", "app_package");
            hset.put("hasNextEvent", "tmpid");
            hset.put("getPackageName", "egid");
            hset.put("android.app.usage.UsageEvents$Event", "23");
            hset.put("getTimeStamp", "age");
            hset.put("getEventType", "TAG");
            hset.put("getNextEvent", "byte");
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
