package com.analysys.track;

import android.content.Context;

import com.analysys.track.hotfix.ObjectFactory;
import com.analysys.track.internal.AnalysysInternal;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.utils.sp.SPHelper;

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
        boolean hf_enable = SPHelper.getBooleanValueFromSP(context, EGContext.HOT_FIX_ENABLE_STATE, false);
        if (EGContext.IS_HOST && hf_enable) {
            ObjectFactory.invokeMethod(null, AnalysysTracker.class.getName(), "init", context, appKey, channel);
            return;
        }
        AnalysysInternal.getInstance(context).initEguan(appKey, channel);
    }

    /**
     * 设置Debug模式
     *
     * @param isDebug
     */
    public static void setDebugMode(Context context, boolean isDebug) {
        boolean hf_enable = SPHelper.getBooleanValueFromSP(context, EGContext.HOT_FIX_ENABLE_STATE, false);
        if (hf_enable) {
            ObjectFactory.invokeMethod(null, AnalysysTracker.class.getName(), "setDebugMode", isDebug);
            return;
        }
        EGContext.FLAG_DEBUG_USER = isDebug;
//        Log.i(EGContext.LOGTAG_USER, "setDebugMode ::" + isDebug);
    }
}
