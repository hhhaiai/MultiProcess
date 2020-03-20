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
        public final Map<String, String> hset = new HashMap<>();

        public StringFogImpl() {
            GenerateKeyUtil.getJson()
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
