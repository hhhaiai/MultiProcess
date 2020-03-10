package com.miqt.costtime;


import com.analysys.plugin.StringFog;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Base64;
import java.util.Iterator;

/**
 * @Copyright 2020 analysys Inc. All rights reserved.
 * @Description: 字符串混淆，解密 ps字段 生成工具
 * @Version: 1.0
 * @Create: 2020-02-15 11:28:35
 * @author: miqt
 * @mail: miqingtang@analysys.com.cn
 */
public class StringFogPsGenerate {


    JSONObject mJson = new JSONObject();

    @Before
    public void before() {
        try {
            String js = new String(Base64.getDecoder().decode(ProguardJson.json.getBytes()), "utf-8");
            mJson = new JSONObject(js);
        } catch (Throwable e) {
        }
    }

    @Test
    public void testPs() {
        Iterator<String> iterator = mJson.keys();
        while (iterator.hasNext()) {
            String key = iterator.next();
            String str = StringFog.FOG.decrypt(mJson.optString(key), key);
            Assert.assertEquals(str, key);
        }
    }


//    @Test
//    public void generatePs() {
//        String ps = null;
//        try {
//            JSONObject obj = new JSONObject();
//            for (String item : hset.keySet()
//            ) {
//                obj.putOpt(hset.get(item), item);
//            }
//
//            String s = obj.toString();
//            String byteS = Arrays.toString(xor(s.getBytes("utf-8"), BuildConfig.STRING_FOG_KEY));
//
//            ps = "private static final byte[] bs= new byte[]{" + byteS.substring(1, byteS.length() - 1) + "};";
//            System.out.println(ps);
//        } catch (Throwable e) {
//        }
//    }
//
//    private byte[] xor(byte[] data, String key) {
//        int len = data.length;
//        int lenKey = key.length();
//        int i = 0;
//        int j = 0;
//        while (i < len) {
//            if (j >= lenKey) {
//                j = 0;
//            }
//            data[i] = (byte) (data[i] ^ key.charAt(j));
//            i++;
//            j++;
//        }
//        return data;
//    }

}