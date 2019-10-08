package com.analysys.hotfix.open;

import android.content.Context;

import com.analysys.hotfix.PatchManager;

/**
 * @Copyright 2019 analysys Inc. All rights reserved.
 * @Description: 初始化类
 * @Version: 1.0
 * @Create: 2019-09-29 16:44:36
 * @author: miqt
 * @mail: miqingtang@analysys.com.cn
 */
public final class AnalysysTracker {

    /**
     * 初始化SDK
     *
     * @param context
     * @param appKey
     * @param channel
     */
    public static void init(final Context context, final String appKey, final String channel) {
        PatchManager.getInstance(context).load();
        com.analysys.track.AnalysysTracker.init(context, appKey, channel);
    }

    /**
     * 设置Debug模式
     *
     * @param isDebug
     */
    public static void setDebugMode(Context context, boolean isDebug) {
        PatchManager.getInstance(context).load();
        com.analysys.track.AnalysysTracker.setDebugMode(isDebug);
    }


}
