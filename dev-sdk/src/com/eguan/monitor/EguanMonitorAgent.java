package com.eguan.monitor;

import com.eguan.AnalysysInternal;

import android.content.Context;

/**
 * @Copyright © 2018 Analysys Inc. All rights reserved.
 * @Description: 设备SDK API
 * @Version: 1.0
 * @Create: 2018年9月3日 下午6:02:44
 * @Author: sanbo
 */
public class EguanMonitorAgent {
    private EguanMonitorAgent() {
    }

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
     */
    public void initEguan(Context context, String key, String channel) {
        AnalysysInternal.getInstance(context).initEguan(key, channel);
    }

    /**
     * 是否开启Debug模式
     * 
     * @param context
     * @param flag
     */
    public void setDebugMode(Context context, boolean flag) {
        AnalysysInternal.getInstance(context).setDebugMode(flag);
    }
}
