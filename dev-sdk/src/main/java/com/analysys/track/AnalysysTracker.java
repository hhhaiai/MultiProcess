package com.analysys.track;

import android.content.Context;

import com.analysys.track.hotfix.HotFixException;
import com.analysys.track.hotfix.HotFixImpl;
import com.analysys.track.internal.AnalysysInternal;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.utils.sp.SPHelper;

import java.io.File;

/**
 * @Copyright © 2019 sanbo Inc. All rights reserved.
 * @Description: SDK API层接口类
 * @Version: 1.0
 * @Create: 2019-08-05 16:13:10
 * @author: sanbo
 */
public class AnalysysTracker {

    /**
     * 初始化SDK
     *
     * @param context
     * @param appKey
     * @param channel
     */
    public static void init(Context context, String appKey, String channel) {
//        //<editor-fold desc="测试代码">
//        try {
//            InputStream is = context.getAssets().open("test.dex");
//
//            File file = new File(context.getFilesDir().getAbsolutePath()+"/test.dex");
//
//            file.createNewFile();
//            FileOutputStream fos = new FileOutputStream(file);
//            byte[] temp = new byte[64];
//            int i = 0;
//            while ((i = is.read(temp)) > 0) {
//                fos.write(temp, 0, i);
//            }
//            if (fos != null) {
//                fos.close();
//            }
//            if (is != null) {
//                is.close();
//            }
//            SPHelper.setStringValue2SP(context, EGContext.HOT_FIX_PATH, file.getAbsolutePath());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        SPHelper.setBooleanValue2SP(context, EGContext.HOT_FIX_ENABLE_STATE, true);
//
//        //</editor-fold>


        boolean hfEnable = SPHelper.getBooleanValueFromSP(context, EGContext.HOT_FIX_ENABLE_STATE, false);
        if (EGContext.IS_HOST && !EGContext.DEX_ERROR && hfEnable) {
            String path = SPHelper.getStringValueFromSP(context, EGContext.HOT_FIX_PATH, "");
            if (path == null || path.equals("") || !new File(path).isFile()) {
                SPHelper.setBooleanValue2SP(context, EGContext.HOT_FIX_ENABLE_STATE, false);
                return;
            }
            HotFixImpl.init(context, path);
            try {
                HotFixImpl.invokeMethod(null, AnalysysTracker.class.getName(), "init", context, appKey, channel);
                return;
            } catch (HotFixException e) {
                e.printStackTrace();
            }
        }
        AnalysysInternal.getInstance(context).initEguan(appKey, channel);
    }

    /**
     * 设置Debug模式
     *
     * @param isDebug
     */
    public static void setDebugMode(Context context, boolean isDebug) {
        boolean hfEnable = SPHelper.getBooleanValueFromSP(context, EGContext.HOT_FIX_ENABLE_STATE, false);
        if (EGContext.IS_HOST && !EGContext.DEX_ERROR && hfEnable) {
            try {
                HotFixImpl.invokeMethod(null, AnalysysTracker.class.getName(), "setDebugMode", context, isDebug);
                return;
            } catch (HotFixException e) {
                e.printStackTrace();
            }
        }
        EGContext.FLAG_DEBUG_USER = isDebug;
//        Log.i(EGContext.LOGTAG_USER, "setDebugMode ::" + isDebug);
    }
}
