package com.analysys.plugin;

import android.text.TextUtils;
import android.util.Log;

import com.analysys.track.BuildConfig;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.utils.FileUitls;

/**
 * @Copyright 2019 analysys Inc. All rights reserved.
 * @Description: 字符串加密
 * @Version: 1.0
 * @Create: 2019-12-07 12:58:44
 * @author: miqt
 * @mail: miqingtang@analysys.com.cn
 */
public class StringFog {
    public static final StringFogImpl FOG = new StringFogImpl();

    public static String encrypt(String data) {
        return FOG.encrypt(data, EGContext.STRING_FOG_KEY);
    }

    public static String decrypt(String data) {
        return FOG.decrypt(data, EGContext.STRING_FOG_KEY);
    }

    public static boolean overflow(String data) {
        return FOG.overflow(data, EGContext.STRING_FOG_KEY);
    }

    public final static class StringFogImpl implements IStringFog {
        private static final byte[] bs = Key.bs;

        /**
         * 跟插件里面的混淆对应,必须严格对应
         * (xor(".+".getBytes(),KEY)), ([0-9]{1,2})   ->   $2,$1
         */
        public StringFogImpl() {
        }

        @Override
        public String encrypt(String data, String key) {
            return data;
        }


        @Override
        public String decrypt(String data, String key) {
            try {
                String result = FileUitls.getInstance(null).getString(data, bs);
                if (EGContext.FLAG_DEBUG_INNER) {
                    Log.d(BuildConfig.tag_stringfog + "1", "[key=" + EGContext.STRING_FOG_KEY + "][" + data + "]-->[" + result + "]");
                }
                return result;
            } catch (Throwable e) {
            }
            if (EGContext.FLAG_DEBUG_INNER) {
                Log.d(BuildConfig.tag_stringfog + "1", "[key=" + key + "][" + data + "]-->[" + null + "]");
            }
            return "";
        }


        @Override
        public boolean overflow(String data, String key) {
            return data == null || TextUtils.isEmpty(data.trim());
        }
    }


}
