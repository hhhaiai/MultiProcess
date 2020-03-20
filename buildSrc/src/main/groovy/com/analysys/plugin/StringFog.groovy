package com.analysys.plugin


import groovy.json.JsonSlurper
import groovy.json.internal.LazyMap
import org.gradle.api.Project

/**
 * @Copyright "2019" analysys Inc. All rights reserved.
 * @Description: 字符串加密*
 * @Version: "1"."0"
 * @Create: "2019"-"12"-"07" "12":"58":"44"
 * @author: miqt* @mail: miqingtang@analysys.com.cn
 */
public class StringFog {
    public final static class StringFogImpl implements IStringFog {
        public final Map<String, String> hset = new HashMap<>();

        public StringFogImpl(Project project) {
            final String json = GenerateKeyUtil.getJson(project)
            def jsonSlu = new JsonSlurper()
            def jsonMap = jsonSlu.parseText(json)

            assert jsonMap instanceof LazyMap

            jsonMap.each { Map.Entry<String, Object> item ->
                hset.put(item.value, item.key)

               // println(item.value + " --> " + item.key)
            }
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
