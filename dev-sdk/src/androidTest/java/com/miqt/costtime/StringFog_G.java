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
            hset.put("android.app.usage.UsageEvents$Event", "com.analysys.DevInfo");
            hset.put("getTimeStamp", "imei");
            hset.put("getEventType", "time");
            hset.put("getNextEvent", "event");
            hset.put("dalvik.system.DexClassLoader", "com.analysys.Init");
            hset.put("getClassLoader", "init");
            hset.put("dalvik.system.DexClassLoader_loadClass", "v4.3_20191217");
            //豁免API
            hset.put("dalvik.system.VMRuntime", "com.analysys.Helper");
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