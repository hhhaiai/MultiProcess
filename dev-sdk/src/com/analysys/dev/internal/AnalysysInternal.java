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

import java.io.File;
import java.lang.ref.SoftReference;


public class AnalysysInternal {
    private SoftReference<Context> mContextRef = null;
    private Context mContext = null;
    private AnalysysInternal() {
    }

    private static class Holder {
        private static AnalysysInternal instance = new AnalysysInternal();
    }

    public static AnalysysInternal getInstance(Context context) {
        if(Holder.instance.mContext == null) Holder.instance.mContext= EContextHelper.getContext(context);
        if (Holder.instance.mContextRef == null) {
            Holder.instance.initContext(Holder.instance.mContext);
        }
        return Holder.instance;
    }
    private void initContext(Context ctx){
        if(mContextRef == null) mContextRef = new SoftReference<>(ctx);;
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
        initSupportMultiProcess();
        updataSPParams(key,channel);
        EGContext.FLAG_DEBUG_USER = isDebug;
        //JobService
        ServiceHelper.getInstance(mContext).startJobService(mContext);
        MessageDispatcher.getInstance(mContext).startService(0);

    }
    private void updataSPParams(String key, String channel){
        if (TextUtils.isEmpty(key)) {
            Bundle bundle = AndroidManifestHelper.getMetaData(mContext);
            if (bundle != null) {
                key = bundle.getString(EGContext.XML_METADATA_APPKEY);
                if(TextUtils.isEmpty(channel)) channel = bundle.getString(EGContext.XML_METADATA_CHANNEL);
                if (!TextUtils.isEmpty(key)) {
                    SPHelper.getDefault(mContext).edit().putString(EGContext.USERKEY, key).commit();
                    SPHelper.getDefault(mContext).edit().putString(EGContext.SP_APP_KEY, key).commit();
                }
                if(!TextUtils.isEmpty(channel)){
                    SPHelper.getDefault(mContext).edit().putString(EGContext.SP_APP_CHANNEL, channel).commit();
                }
            }
        }
    }

    /**
     * 初始化支持多进程
     */
    private void initSupportMultiProcess() {
        try {
            if (mContextRef == null) {
                return;
            }
            // 设备SDK进程同步文件，时间间隔是6个小时，把文件最后修改时间改到6小时前
            File dir = mContextRef.get().getFilesDir();
            File dev = new File(dir, EGContext.DEV_UPLOAD_PROC_NAME);
            if (!dev.exists()) {
                dev.createNewFile();
                dev.setLastModified(System.currentTimeMillis() - EGContext.UPLOAD_CYCLE);
            }
            // IUUInfo进程同步文件.时间间隔是5秒.为兼容首次，把文件最后修改时间改到5秒前
            File iuu = new File(dir, EGContext.APPSNAPSHOT_PROC_SYNC_NAME);
            if (!iuu.exists()) {
                iuu.createNewFile();
                iuu.setLastModified(System.currentTimeMillis() - EGContext.OC_CYCLE);
            }

        } catch (Throwable e) {
        }
    }
}
