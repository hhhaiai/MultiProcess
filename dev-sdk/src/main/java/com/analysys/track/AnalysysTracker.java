package com.analysys.track;

import android.content.Context;

import com.analysys.track.hotfix.HotFixTransform;
import com.analysys.track.internal.AnalysysInternal;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.utils.EContextHelper;
import com.analysys.track.utils.ELOG;

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
        AnalysysTracker.setContext(context);
        if (BuildConfig.enableHotFix) {
            try {
                HotFixTransform.transform(null, AnalysysTracker.class.getName(), "init", context, appKey, channel);
                return;
            } catch (Throwable e) {

            }
        }
        AnalysysInternal.getInstance(context).initEguan(appKey, channel, true);
    }


    /**
     * 设置Debug模式
     *
     * @param isDebug
     */
    public static void setDebugMode(Context context, boolean isDebug) {
        AnalysysTracker.setContext(context);
        if (BuildConfig.enableHotFix) {
            try {
                HotFixTransform.transform(null, AnalysysTracker.class.getName(), "setDebugMode", context, isDebug);
                return;
            } catch (Throwable e) {

            }
        }
        EGContext.FLAG_DEBUG_USER = isDebug;
    }

    public static void setContext(Context context) {
        if (!BuildConfig.IS_HOST || context == null) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e("非宿主,不进行设置context");
            }
            return;
        }
        EContextHelper.setContext(context);

        if (BuildConfig.enableHotFix) {
            try {
                HotFixTransform.transform(null, AnalysysTracker.class.getName(), "setContext", context);
            } catch (Throwable e) {

            }
        }
    }
}
