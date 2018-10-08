package com.analysys.dev;

import com.analysys.dev.internal.Content.EDContext;
import com.analysys.dev.internal.utils.AndroidManifestHelper;
import com.analysys.dev.internal.utils.EContextHelper;
import com.analysys.dev.internal.work.ServiceHelper;

import android.content.Context;
import android.os.Bundle;

/**
 * @Copyright © 2018 Analysys Inc. All rights reserved.
 * @Description: 设备SDK入口
 * @Version: 1.0
 * @Create: 2018年8月30日 上午11:45:43
 * @Author: sanbo
 */
public class AnalysysInternal {
    private Context mContext = null;

    private AnalysysInternal() {}

    private static class Holder {
        private static AnalysysInternal instance = new AnalysysInternal();
    }

    public static AnalysysInternal getInstance(Context context) {
        if (Holder.instance.mContext == null) {
            if (context != null) {
                Holder.instance.mContext = context.getApplicationContext();
            } else {
                Holder.instance.mContext = EContextHelper.getContext();
            }
        }
        return Holder.instance;
    }

    /**
     * 初始化函数
     * 
     * @param key
     * @param channel
     * @param isDebug 只保留日志控制
     */
    public void initEguan(String key, String channel, boolean isDebug) {

        // Debug.
        EDContext.FLAG_DEBUG_USER = isDebug;

        // 1.保存appkey、channel

        Bundle bundle = AndroidManifestHelper.getMetaData(mContext);

        // if (bundle != null) {
        // L.i("key:" + bundle.getString("PUSH_APPID"));
        // }
        // 2. 确保appkey正常使用

        // 3.开启服务
        ServiceHelper.getInstance(mContext).startService(5 * 1000);
        // 4.启动页面监听相关的
        // PageViewHelper.getInstance(mContext).init();
    }

}
