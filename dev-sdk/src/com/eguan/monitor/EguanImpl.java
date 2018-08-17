package com.eguan.monitor;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.eguan.monitor.cache.InnerProcessCacheManager;
import com.eguan.monitor.commonutils.AppSPUtils;
import com.eguan.monitor.commonutils.EgLog;
import com.eguan.monitor.commonutils.SPUtil;
import com.eguan.monitor.commonutils.SystemUtils;
import com.eguan.monitor.dbutils.device.DeviceTableOperation;
import com.eguan.monitor.fangzhou.service.MonitorService;
import com.eguan.monitor.policy.PolicyManger;
import com.eguan.monitor.thread.EGQueue;
import com.eguan.monitor.thread.SafeRunnable;

/**
 * Created by chris on 16/11/4.
 */
public class EguanImpl {
    private Context mContext;

    private EguanImpl() {
    }

    private static class Holder {
        private static final EguanImpl INSTANCE = new EguanImpl();
    }

    public static EguanImpl getInstance() {
        return Holder.INSTANCE;
    }

    public void initEguan(final Context context, final String key, final String channel) {
//        if (!SystemUtils.isMainProcess(context)) return;
        EGQueue.execute(new SafeRunnable() {
            @Override
            public void safeRun() {
                if (mContext == null) {
                    if (context != null) {
                        mContext = context.getApplicationContext();
                    }
                }
                if (mContext == null) {
                    return;
                }

                if (TextUtils.isEmpty(key) || key.length() != 17) {
                    EgLog.e("请检查 initEguan()传入Key值!");
                    return;
                }
                Constants.APP_KEY_VALUE = key;
                if (TextUtils.isEmpty(key)) {
                    SystemUtils.getAppKey(mContext);
                } else {
                    SPUtil.getInstance(mContext).setKey(key);
                }
                //确保地址为最新
                Constants.setNormalUploadUrl();
                Constants.setRTLUploadUrl();
                //此处需要进行channel优先级处理,优先处理多渠道打包过来的channel,而后次之,接口传入的channel
                String channelFromApk = SystemUtils.getChannelFromApk(mContext);
                if (TextUtils.isEmpty(channelFromApk)) {
                    if (TextUtils.isEmpty(channel)) {
                        //赋值为空
                        Constants.APP_CHANNEL_VALUE = "";
                        SPUtil.getInstance(mContext).setchannel("");
                        //处理manifest中配置channel
                        SystemUtils.getAppChannel(mContext);
                    } else {
                        //赋值接口传入的channel
                        Constants.APP_CHANNEL_VALUE = channel;
                        SPUtil.getInstance(mContext).setchannel(channel);
                    }
                } else {
                    //赋值多渠道打包的channel
                    Constants.APP_CHANNEL_VALUE = channelFromApk;
                    SPUtil.getInstance(mContext).setchannel(channelFromApk);
                }

                //网络未发生变化前,获取NT信息
                InnerProcessCacheManager.getInstance().dealAppNetworkType(mContext);
                final String tactics = SPUtil.getInstance(mContext).getDeviceTactics();
                try {
                    SystemUtils.startJobService(mContext);
                } catch (Throwable e) {
                    if (Constants.FLAG_DEBUG_INNER) {
                        EgLog.e(e);
                    }
                }

                try {
                    //检测数据库加密是否可用
//                    AppTableOperation.getInstance(mContext).checkEncrypt();
                    DeviceTableOperation.getInstance(mContext).initDB();
                    if (!SystemUtils.isServiceRunning(mContext, Constants.MONITORSERVICE) && SystemUtils.classInspect(Constants.MONITORSERVICE) && !tactics.equals(Constants.TACTICS_STATE)) {
                        Thread.sleep(5 * 1000);
                        Intent intent = new Intent(mContext, MonitorService.class);
                        intent.putExtra(Constants.APP_KEY, Constants.APP_KEY_VALUE);
                        intent.putExtra(Constants.APP_CHANNEL, Constants.APP_CHANNEL_VALUE);
                        mContext.startService(intent);
                    }
                } catch (Throwable e) {
                    if (Constants.FLAG_DEBUG_INNER) {
                        EgLog.e(e);
                    }
                }
                if (Constants.FLAG_DEBUG_INNER) {
                    EgLog.i("[" + SystemUtils.getCurrentProcessName(mContext) + "] (" + SystemUtils.getCurrentPID() + ") init Android Analysys Java sdk success, version:" + Constants.SDK_VERSION);
                } else {
                    EgLog.i("init Android Analysys Java sdk success, version:" + Constants.SDK_VERSION);
                }
            }
        });
    }


    /**
     * @param flag true:DEBUG_MODE;false:RELEASE_MODE.
     */
    public void setDebugMode(final Context context, final boolean flag) {
        EGQueue.execute(new SafeRunnable() {
            @Override
            public void safeRun() {
                if (mContext == null) {
                    if (context != null) {
                        mContext = context.getApplicationContext();
                    }
                }
                if (mContext == null) {
                    return;
                }
                //确保地址为最新
                Constants.setNormalUploadUrl();
                Constants.setRTLUploadUrl();
                Constants.changeUrlNormal(flag);
                SPUtil.getInstance(context).setDebugMode(flag);
                AppSPUtils.getInstance(context).setDebugMode(flag);
//                Constants.FLAG_DEBUG_INNER = flag;
                EgLog.USER_DEBUG = flag;
                if (flag) {
                    PolicyManger.getInstance(context).setDebugPolicy();
                } else {
                    PolicyManger.getInstance(context).useDefaultRtPolicy();
                }
            }
        });
    }
}