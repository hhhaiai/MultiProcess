package com.analysys.dev.internal;

import android.content.Context;
import android.os.Bundle;
import android.os.Process;
import android.text.TextUtils;

import com.analysys.dev.internal.Content.EGContext;
import com.analysys.dev.internal.work.ServiceHelper;
import com.analysys.dev.utils.AndroidManifestHelper;
import com.analysys.dev.utils.ELOG;
import com.analysys.dev.utils.reflectinon.EContextHelper;
import com.analysys.dev.utils.reflectinon.Reflecer;
import com.analysys.dev.utils.sp.SPHelper;
import com.analysys.dev.internal.work.MessageDispatcher;

/**
 * @Copyright © 2018 Analysys Inc. All rights reserved.
 * @Description: 设备SDK入口
 * @Version: 1.0
 * @Create: 2018年8月30日 上午11:45:43
 * @Author: sanbo
 */
public class AnalysysInternal {
    private Context mContext = null;

    private AnalysysInternal() {
    }

    private static class Holder {
        private static AnalysysInternal instance = new AnalysysInternal();
    }

    public static AnalysysInternal getInstance(Context context) {
        if (Holder.instance.mContext == null) {
            Holder.instance.mContext = EContextHelper.getContext(context);
        }
        return Holder.instance;
    }

    /**
     * 初始化函数
     * key支持参数设置、XML文件设置，
     * 参数设置优先级大于XML设置
     *
     * @param isDebug 只保留日志控制
     */
    public void initEguan(String key, String channel, boolean isDebug) {

        Reflecer.init();
        ELOG.d("初始化，进程Id：< " + Process.myPid() + " >");

        if (TextUtils.isEmpty(key)) {
            Bundle bundle = AndroidManifestHelper.getMetaData(mContext);
            if (bundle == null) {
                ELOG.e(EGContext.LOGINFO.LOG_NOT_APPKEY);
            }
            key = bundle.getString(EGContext.XML_METADATA_APPKEY);
            channel = bundle.getString(EGContext.XML_METADATA_CHANNEL);
            if (TextUtils.isEmpty(key)) {
                ELOG.e(EGContext.LOGINFO.LOG_NOT_APPKEY);
            }
        }
        SPHelper.getDefault(mContext).edit().putString(EGContext.USERKEY, key).commit();
        SPHelper.getDefault(mContext).edit().putString(EGContext.SP_APP_KEY, key).commit();
        SPHelper.getDefault(mContext).edit().putString(EGContext.SP_APP_CHANNEL, channel).commit();
        SPHelper.getDefault(mContext).edit().putInt(EGContext.SP_WIFI_DETAIL, 1).commit();

        EGContext.FLAG_DEBUG_USER = isDebug;
        //JobService
        ServiceHelper.getInstance(mContext).startJobService(mContext);
        MessageDispatcher.getInstance(mContext).startService(0);
        // 4.启动页面监听相关的
        // PageViewHelper.getInstance(mContext).init();

    }
}
