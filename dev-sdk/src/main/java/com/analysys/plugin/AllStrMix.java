package com.analysys.plugin;

import android.util.Log;

import com.analysys.track.BuildConfig;
import com.analysys.track.internal.content.EGContext;

import java.io.UnsupportedEncodingException;


public class AllStrMix {


    //由 lysys2020ana base64而来
    public static final String key = "bHlzeXMyMDIwYW5h";
    public static final StrMixImpl FOG = new StrMixImpl();

    public static String encrypt(String data) {
        return FOG.encrypt(data, AllStrMix.key);
    }

    public static String decrypt(String data) {
        return FOG.decrypt(data, AllStrMix.key);
    }

    public static boolean overflow(String data) {
        return FOG.overflow(data, AllStrMix.key);
    }


    public final static class StrMixImpl implements IStrMix {

        private static final String CHARSET_NAME_UTF_8 = "UTF-8";

        @Override
        public String encrypt(String data, String key) {
            String newData = "";
            try {
                try {
                    newData = new String(Base64.encode(xor(data.getBytes(CHARSET_NAME_UTF_8), key), Base64.NO_WRAP));
                } catch (UnsupportedEncodingException e) {
                    newData = new String(Base64.encode(xor(data.getBytes(), key), Base64.NO_WRAP));
                }
                if (BuildConfig.logcat) {
                    Log.d(BuildConfig.tag_StrMix, "[key=" + key + "][" + data + "]-->[" + newData + "]");
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
                    newData = new String(xor(Base64.decode(data, Base64.NO_WRAP), key), CHARSET_NAME_UTF_8);
                } catch (UnsupportedEncodingException e) {
                    newData = new String(xor(Base64.decode(data, Base64.NO_WRAP), key));
                }


            } catch (Throwable e) {
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
