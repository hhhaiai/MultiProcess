package com.analysys.dev.monitor;

import com.analysys.dev.AnalysysInternal;
import com.analysys.dev.internal.Content.EDContext;
import com.analysys.dev.internal.utils.LL;

import android.content.Context;
import android.text.TextUtils;

/**
 * @Copyright © 2018 Analysys Inc. All rights reserved.
 * @Description: 设备SDK API
 * @Version: 1.0
 * @Create: 2018年9月3日 下午6:02:44
 * @Author: sanbo
 */
public class EguanMonitorAgent {
    private EguanMonitorAgent() {}

    private static class Holder {
        private static final EguanMonitorAgent INSTANCE = new EguanMonitorAgent();
    }

    public static EguanMonitorAgent getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * 初始化SDK
     * 
     * @param context
     * @param key
     * @param channel
     * @param isDebug
     */
    public void initEguan(Context context, String key, String channel, boolean isDebug) {
        if (TextUtils.isEmpty(key)) {
            LL.e(EDContext.LOGINFO.LOG_NOT_APPKEY);
            return;
        }
        AnalysysInternal.getInstance(context).initEguan(key, channel, isDebug);
    }

}
