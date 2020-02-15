package com.miqt.costtime;

import com.analysys.plugin.StringFog;
import com.analysys.track.BuildConfig;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @Copyright 2020 analysys Inc. All rights reserved.
 * @Description: 字符串混淆，解密 ps字段 生成工具
 * @Version: 1.0
 * @Create: 2020-02-15 11:28:35
 * @author: miqt
 * @mail: miqingtang@analysys.com.cn
 */
public class StringFogPsGenerate {

    final Map<String, String> hset = new HashMap<>();

    @Before
    public void before() {
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

    @Test
    public void testPs() {
        for (Map.Entry<String, String> entry : hset.entrySet()) {
            String str = StringFog.FOG.decrypt(entry.getValue(), entry.getKey());
            Assert.assertEquals(str, entry.getKey());
        }
    }


    @Test
    public void generatePs() {
        String ps = null;
        try {
            JSONObject obj = new JSONObject();
            for (String item : hset.keySet()
            ) {
                obj.putOpt(hset.get(item), item);
            }

            String s = obj.toString();
            String byteS = Arrays.toString(xor(s.getBytes("utf-8"), BuildConfig.STRING_FOG_KEY));

            ps = "private static final byte[] bs= new byte[]{" + byteS.substring(1, byteS.length() - 1) + "};";
            System.out.println(ps);
        } catch (Throwable e) {
        }
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