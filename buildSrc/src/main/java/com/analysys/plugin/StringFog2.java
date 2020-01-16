package com.analysys.plugin;

import java.io.UnsupportedEncodingException;

/**
 * @Copyright 2019 analysys Inc. All rights reserved.
 * @Description: 字符串加密实现类
 * @Version: 1.0
 * @Create: 2019-11-13 11:20:25
 * @author: miqt
 * @mail: miqingtang@analysys.com.cn
 */
public class StringFog2 {


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

        private static final String CHARSET_NAME_UTF_8 = "UTF-8";

        @Override
        public String encrypt(String data, String key) {
            String newData;
            try {
                newData = new String(Base64.encode(xor(data.getBytes(CHARSET_NAME_UTF_8), key), Base64.NO_WRAP));
            } catch (UnsupportedEncodingException e) {
                newData = new String(Base64.encode(xor(data.getBytes(), key), Base64.NO_WRAP));
            }
            return newData;
        }

        @Override
        public String decrypt(String data, String key) {
            String newData;
            try {
                newData = new String(xor(Base64.decode(data, Base64.NO_WRAP), key), CHARSET_NAME_UTF_8);
            } catch (UnsupportedEncodingException e) {
                newData = new String(xor(Base64.decode(data, Base64.NO_WRAP), key));
            }
            return newData;
        }

        @Override
        public boolean overflow(String data, String key) {
            return data != null && data.length() * 4 / 3 >= 65535;
        }

        public byte[] xor(byte[] data, String key) {
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