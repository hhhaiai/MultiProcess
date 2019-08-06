package com.analysys.track;

import android.content.Context;

import com.analysys.track.internal.AnalysysInternal;
import com.analysys.track.internal.Content.EGContext;

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
        AnalysysInternal.getInstance(context).initEguan(appKey, channel);
    }

    /**
     * 设置Debug模式
     *
     * @param isDebug
     */
    public static void setDebugMode(boolean isDebug) {
        EGContext.FLAG_DEBUG_USER = isDebug;
//        Log.i(EGContext.USER_TAG_DEBUG, "setDebugMode ::" + isDebug);
    }
}
