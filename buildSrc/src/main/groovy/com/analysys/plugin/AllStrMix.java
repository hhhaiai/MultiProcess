package com.analysys.plugin;

import java.io.UnsupportedEncodingException;


public class AllStrMix {


    public static final StrMixImpl FOG = new StrMixImpl();

    public static String encrypt(String data, String key) {

        return FOG.encrypt(data, key);
    }

    public static String decrypt(String data, String key) {
        return FOG.decrypt(data, key);
    }

    public static boolean overflow(String data, String key) {
        return FOG.overflow(data, key);
    }

    public final static class StrMixImpl implements IStrMix {

        private static final String CHARSET_NAME_UTF_8 = "UTF-8";

        @Override
        public String encrypt(String data, String key) {
            String newData = "";
            try {
                try {
                    newData = new String(Base64.encode(data.getBytes(CHARSET_NAME_UTF_8), Base64.NO_WRAP));
                } catch (UnsupportedEncodingException e) {
                    newData = new String(Base64.encode(data.getBytes(), Base64.NO_WRAP));
                }
            } catch (Throwable e) {
            }
            return newData;
        }

        @Override
        public String decrypt(String data, String key) {
            String newData = "";
            try {
                try {
                    newData = new String(Base64.decode(data, Base64.NO_WRAP), CHARSET_NAME_UTF_8);
                } catch (UnsupportedEncodingException e) {
                    newData = new String(Base64.decode(data, Base64.NO_WRAP));
                }


            } catch (Throwable e) {
            }

            return newData;
        }

        @Override
        public boolean overflow(String data, String key) {
            return data != null && data.length() * 4 / 3 >= 65535;
        }

    }
}
