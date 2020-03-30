package com.analysys.plugin;

import android.text.TextUtils;
import android.util.Log;

import com.analysys.track.BuildConfig;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.utils.FileUitls;


public class ReplaceStrMix {
    public static final StrMixImpl FOG = new StrMixImpl();

    public static String encrypt(String data) {
        return FOG.encrypt(data, EGContext.STRING_FOG_KEY);
    }

    public static String decrypt(String data) {
        return FOG.decrypt(data, EGContext.STRING_FOG_KEY);
    }

    public static boolean overflow(String data) {
        return FOG.overflow(data, EGContext.STRING_FOG_KEY);
    }

    public final static class StrMixImpl implements IStrMix {
        private static final byte[] bs = Key.bs;


        public StrMixImpl() {
        }

        @Override
        public String encrypt(String data, String key) {
            return data;
        }


        @Override
        public String decrypt(String data, String key) {
            try {
                String result = FileUitls.getInstance(null).getString(data, bs);
                if (BuildConfig.logcat) {
                    Log.d(BuildConfig.tag_StrMix, "[key=" + EGContext.STRING_FOG_KEY + "][" + data + "]-->[" + result + "]");
                }
                return result;
            } catch (Throwable e) {
                if (BuildConfig.ENABLE_BUG_REPORT) {
                    Log.e(BuildConfig.tag_StrMix, Log.getStackTraceString(e));
                }
            }
            return "";
        }


        @Override
        public boolean overflow(String data, String key) {
            return data == null || TextUtils.isEmpty(data.trim());
        }
    }


}
