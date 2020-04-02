package com.analysys.track;

import android.content.Context;

import com.analysys.track.impl.CusHotTransform;
import com.analysys.track.internal.AnalysysInternal;
import com.analysys.track.utils.BugReportForTest;
import com.analysys.track.utils.EContextHelper;

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
        try {
            setContext(context);
            if (BuildConfig.enableHotFix) {
                try {
                    CusHotTransform.transform(false, AnalysysTracker.class.getName(), "init", context, appKey, channel);
                    return;
                } catch (Throwable e) {
                    if (BuildConfig.ENABLE_BUG_REPORT) {
                        BugReportForTest.commitError(e);
                    }
                }
            }
            AnalysysInternal.getInstance(context).initEguan(appKey, channel, true);
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        }
    }


    /**
     * 设置Debug模式
     *
     * @param isDebug
     */
    @Deprecated
    public static void setDebugMode(Context context, boolean isDebug) {
        try {
            setContext(context);
            if (BuildConfig.enableHotFix) {
                try {
                    CusHotTransform.transform(false, AnalysysTracker.class.getName(), "setDebugMode", context, isDebug);
                    return;
                } catch (Throwable e) {
                    if (BuildConfig.ENABLE_BUG_REPORT) {
                        BugReportForTest.commitError(e);
                    }
                }
            }
//            EGContext.FLAG_DEBUG_USER = isDebug;
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        }
    }

    @Deprecated
    public static void setDebugMode(boolean isDebug) {
        if (BuildConfig.enableHotFix) {
            try {
                CusHotTransform.transform(false, AnalysysTracker.class.getName(), "setDebugMode", isDebug);
                return;
            } catch (Throwable e) {
                if (BuildConfig.ENABLE_BUG_REPORT) {
                    BugReportForTest.commitError(e);
                }
            }
        }
    }

    public static void setContext(Context context) {
        EContextHelper.setContext(context);
        if (BuildConfig.enableHotFix) {
            try {
                CusHotTransform.transform(false, AnalysysTracker.class.getName(), "setContext", context);
            } catch (Throwable e) {
                if (BuildConfig.ENABLE_BUG_REPORT) {
                    BugReportForTest.commitError(e);
                }
            }
        }
    }
}
