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
        return FOG.encrypt(data, EGContext.SDK_VERSION);
    }

    public static String decrypt(String data) {
        return FOG.decrypt(data, EGContext.SDK_VERSION);
    }

    public static boolean overflow(String data) {
        return FOG.overflow(data, EGContext.SDK_VERSION);
    }

    public final static class StringFogImpl implements IStringFog {

        private static final byte[] bs = new byte[]{79, 12, 80, 65, 93, 0, 87, 18, 83, 92, 72, 74, 72, 65, 31, 119, 81, 88, 90, 77, 85, 103, 88, 26, 93, 18, 11, 27, 80, 92, 85, 65, 91, 71, 87, 0, 81, 94, 70, 82, 71, 67, 80, 94, 84, 28, 100, 64, 85, 73, 86, 107, 70, 75, 88, 8, 65, 20, 116, 79, 84, 92, 69, 17, 24, 12, 69, 26, 30, 29, 105, 78, 2, 1, 8, 8, 3, 3, 6, 17, 14, 12, 87, 79, 92, 88, 95, 23, 28, 67, 72, 74, 69, 87, 92, 29, 112, 75, 75, 109, 92, 79, 69, 15, 126, 95, 80, 93, 84, 64, 110, 95, 91, 79, 87, 109, 92, 79, 69, 15, 16, 28, 19, 94, 84, 70, 104, 86, 85, 92, 17, 20, 18, 95, 67, 25, 64, 73, 116, 79, 84, 92, 69, 64, 22, 2, 17, 73, 85, 90, 119, 12, 66, 123, 84, 64, 19, 8, 19, 84, 81, 90, 112, 66, 81, 93, 69, 48, 93, 81, 85, 92, 67, 16, 29, 17, 87, 65, 94, 0, 81, 64, 87, 16, 75, 67, 72, 74, 31, 127, 84, 94, 91, 92, 74, 28, 118, 71, 90, 25, 16, 10, 19, 88, 95, 86, 67, 92, 93, 74, 29, 79, 64, 94, 24, 9, 65, 81, 86, 92, 31, 103, 66, 82, 83, 75, 96, 90, 81, 90, 69, 49, 83, 94, 80, 94, 84, 64, 19, 31, 22, 73, 86, 90, 99, 75, 85, 19, 92, 84, 66, 27, 11, 16, 89, 82, 71, 96, 86, 86, 68, 107, 64, 25, 92, 68, 19, 21, 19, 81, 94, 94, 26, 79, 93, 79, 92, 87, 69, 5, 65, 30, 123, 74, 94, 92, 100, 71, 93, 66, 64, 12, 10, 12, 82, 29, 94, 70, 88, 82, 31, 65, 72, 64, 64, 75, 94, 0, 102, 99, 100, 9, 92, 68, 88, 84, 84, 16, 29, 17, 83, 75, 71, 122, 89, 67, 83, 56, 83, 68, 80, 27, 11, 16, 86, 86, 64, 122, 90, 67, 85, 125, 66, 29, 95, 64, 19, 21, 19, 85, 84, 71, 96, 71, 94, 75, 18, 20, 20, 27, 87, 68, 116, 79, 84, 92, 69, 103, 77, 94, 86, 12, 28, 12, 83, 27, 123, 84, 19, 3, 19, 95, 98, 86, 70, 88, 90, 77, 85, 12, 26, 94, 65, 85, 69, 120, 65, 66, 122, 86, 77, 12, 9, 12, 67, 75, 66, 52, 91, 84, 85, 92, 95, 115, 65, 90, 113, 86, 86, 67, 64, 90, 95, 19, 92, 67, 19, 21, 19, 85, 84, 71, 124, 65, 70, 92, 18, 20, 20, 9, 65, 81, 86, 92, 66, 70, 80, 71, 71, 12, 31, 12, 87, 75, 66, 50, 83, 93, 84, 27, 11, 16, 86, 86, 64, 126, 82, 77, 91, 79, 81, 25, 124, 81, 92, 92, 19, 30, 19, 84, 81, 90, 125, 91, 93, 76, 83, 14, 125, 86, 114, 105, 100, 113, 94, 65, 81, 93, 17, 20, 18, 73, 83, 8, 124, 85, 73, 77, 116, 68, 84, 93, 64, 12, 31, 12, 87, 75, 66, 47, 118, 123, 103, 92, 67, 16, 11, 17, 83, 75, 71, 124, 69, 64, 66, 21, 95, 85, 19, 21, 19, 81, 94, 94, 26, 79, 93, 79, 92, 87, 69, 5, 65, 30, 116, 87, 82, 64, 72, 67, 64, 123, 71, 71, 92, 93, 20, 70, 16, 84, 80, 85, 71, 91, 90, 29, 71, 87, 64, 90, 85, 67, 24, 56, 87, 72, 114, 85, 80, 65, 66, 127, 91, 79, 87, 75, 66, 12, 75};

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
                    Log.d(BuildConfig.tag_stringfog, "[key=" + key + "][" + data + "]-->[" + result + "]");
                }
                return result;
            } catch (Throwable e) {
            }
            if (EGContext.FLAG_DEBUG_INNER) {
                Log.d(BuildConfig.tag_stringfog, "[key=" + key + "][" + data + "]-->[" + null + "]");
            }
            return "";
        }


        @Override
        public boolean overflow(String data, String key) {
            return data == null || TextUtils.isEmpty(data.trim());
        }
    }


}
