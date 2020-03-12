package com.analysys.track.internal.net;


import android.content.Context;
import android.text.TextUtils;

import com.analysys.track.BuildConfig;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.internal.content.UploadKey;
import com.analysys.track.utils.sp.SPHelper;

import java.io.File;

/**
 * @Copyright © 2020 sanbo Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2020/3/12 17:01
 * @author: sanbo
 */
public class AnaCountImpl {
    static int getK1(Context context) {
        if (BuildConfig.isNativeDebug) {
            try {
                File dir = new File(context.getFilesDir(), EGContext.PATCH_CACHE_DIR);
                String version = SPHelper.getStringValueFromSP(context, UploadKey.Response.PatchResp.PATCH_VERSION, "");

                if (TextUtils.isEmpty(version)) {
                    return 2;
                }
                if (!new File(dir, "patch_" + version + ".jar").exists()) {
                    return 3;
                }
                if (!new File(dir, version + ".jar").exists()) {
                    return 4;
                }
                if (!new File(dir, "null.jar").exists()) {
                    return 5;
                }
                if (!new File(dir, "patch_null.jar").exists()) {
                    return 6;
                }
                if (!new File(dir, "patch__ptv.jar").exists()) {
                    return 7;
                }
                if (!new File(dir, "_ptv.jar").exists()) {
                    return 8;
                }
                if (!new File(dir, "patch_.jar").exists()) {
                    return 9;
                }
                return 1;
            } catch (Throwable e) {
                return 10;
            }
        }
        return -1;
    }

    public static String getK5(Context context) {
        return null;
    }

    public static String getK6(Context context) {
        return null;
    }

    public static String getK7(Context context) {
        return null;
    }

    public static String getK8(Context context) {
        return null;
    }

    public static String getK9(Context context) {
        return null;
    }

    public static String getK10(Context context) {
        return null;
    }

    public static String getK11(Context context) {
        return null;
    }

    public static String getK12(Context context) {
        return null;
    }

    public static String getK13(Context context) {
        return null;
    }

    public static String getK14(Context context) {
        return null;
    }

    public static String getK15(Context context) {
        return null;
    }

    public static String getK16(Context context) {
        return null;
    }

    public static String getK17(Context context) {
        return null;
    }
}
