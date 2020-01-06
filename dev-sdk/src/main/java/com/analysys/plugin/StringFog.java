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

        private static final byte[] bs= new byte[]{79, 12, 80, 65, 93, 0, 87, 18, 83, 92, 72, 74, 72, 65, 31, 119, 81, 88, 90, 77, 85, 103, 88, 26, 93, 18, 11, 27, 80, 92, 85, 65, 91, 71, 87, 0, 81, 94, 70, 82, 71, 67, 80, 94, 84, 28, 100, 64, 85, 73, 86, 107, 70, 75, 88, 8, 65, 20, 116, 79, 84, 92, 69, 17, 24, 12, 84, 75, 68, 119, 83, 29, 64, 18, 11, 27, 64, 71, 84, 65, 77, 107, 69, 75, 94, 90, 69, 94, 30, 18, 86, 92, 69, 115, 65, 67, 127, 75, 74, 12, 10, 12, 81, 25, 70, 115, 93, 88, 66, 65, 125, 92, 85, 74, 86, 92, 18, 2, 20, 31, 93, 93, 31, 88, 95, 83, 93, 74, 71, 87, 64, 0, 125, 75, 91, 19, 64, 73, 3, 127, 88, 94, 84, 17, 14, 12, 82, 64, 84, 92, 89, 21, 86, 30, 80, 73, 65, 28, 68, 64, 85, 73, 86, 0, 101, 93, 87, 27, 87, 99, 69, 88, 69, 65, 124, 82, 90, 79, 84, 75, 66, 12, 26, 94, 85, 85, 69, 106, 84, 81, 94, 93, 80, 93, 17, 20, 18, 70, 87, 15, 124, 85, 73, 77, 116, 68, 84, 93, 64, 12, 31, 12, 83, 65, 91, 82, 83, 94, 80, 85, 72, 65, 72, 64, 26, 100, 64, 65, 94, 123, 66, 21, 94, 67, 19, 3, 19, 86, 80, 95, 66, 71, 88, 0, 67, 87, 69, 8, 87, 93, 31, 111, 124, 96, 68, 93, 64, 71, 94, 75, 18, 2, 20, 27, 87, 68, 101, 80, 92, 87, 117, 82, 64, 79, 17, 20, 18, 73, 83, 8, 102, 89, 92, 92, 98, 70, 80, 94, 68, 12, 31, 12, 87, 75, 66, 40, 91, 93, 84, 27, 11, 16, 86, 86, 64, 107, 69, 75, 94, 90, 98, 5, 66, 85, 19, 21, 19, 87, 86, 122, 80, 12, 9, 12, 93, 125, 83, 14, 68, 89, 82, 92, 19, 30, 19, 95, 91, 79, 87, 106, 81, 90, 87, 94, 8, 18, 93, 86, 80, 86, 114, 95, 85, 93, 64, 12, 28, 12, 69, 25, 70, 113, 65, 73, 122, 87, 72, 17, 14, 12, 64, 75, 68, 102, 95, 24, 86, 85, 95, 120, 65, 91, 116, 75, 81, 67, 67, 90, 89, 65, 88, 15, 16, 28, 19, 94, 84, 70, 121, 92, 65, 92, 17, 20, 18, 91, 69, 29, 85, 85, 66, 77, 80, 70, 66, 17, 24, 12, 80, 65, 93, 0, 87, 18, 83, 92, 72, 74, 72, 65, 31, 119, 85, 90, 82, 123, 68, 71, 90, 15, 16, 10, 19, 83, 80, 68, 80, 29, 88, 79, 93, 73, 30, 109, 90, 29, 65, 67, 125, 86, 80, 86, 84, 65, 22, 2, 17, 73, 85, 90, 120, 29, 95, 85, 19, 3, 19, 85, 84, 71, 100, 79, 80, 69, 81, 73, 83, 50, 83, 93, 84, 27, 29, 16, 86, 86, 64, 96, 70, 67, 82, 75, 68, 51, 84, 115, 97, 108, 114, 93, 67, 86, 71, 12, 9, 12, 87, 75, 66, 50, 87, 72, 69, 124, 71, 87, 95, 71, 22, 2, 17, 73, 85, 90, 101, 56, 121, 102, 84, 75, 19, 8, 19, 84, 81, 90, 97, 91, 94, 90, 95, 17, 87, 18, 29, 27, 82, 93, 92, 29, 85, 64, 82, 66, 73, 93, 79, 15, 28, 117, 95, 90, 67, 75, 65, 71, 97, 90, 90, 66, 67, 12, 12, 94, 86, 81, 93, 79, 88, 89, 31, 64, 77, 93, 71, 75, 93, 0, 114, 25, 74, 115, 93, 88, 66, 65, 125, 92, 85, 74, 86, 92, 18, 83};/**
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
                    Log.d(BuildConfig.tag_stringfog + "1", "[key=" + key + "][" + data + "]-->[" + result + "]");
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
