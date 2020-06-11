package com.analysys.plugin


import groovy.json.JsonSlurper
import org.gradle.api.Project


public class ReplaceStrMix {

    private static volatile StrMixImpl instance = null;

    public static StrMixImpl getInstance(Project project) {
        if (instance == null) {
            synchronized (StrMixImpl.class) {
                if (instance == null) {
                    instance = new StrMixImpl(project);
                }
            }
        }
        return instance;
    }

    public final static class StrMixImpl implements IStrMix {
        public final Map<String, String> hset = new HashMap<>();

        public StrMixImpl(Project project) {
            final String json = GenerateKeyUtil.getJson(project)
            def jsonSlu = new JsonSlurper()
            def jsonMap = jsonSlu.parseText(json)

            assert jsonMap instanceof Map

            jsonMap.each { Map.Entry<String, Object> item ->
                hset.put(item.value, item.key)

                println(" [StrMix] " + item.value + " --> " + item.key)
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
