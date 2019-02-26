package com.analysys.dev.internal;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Process;
import android.text.TextUtils;

import com.analysys.dev.internal.Content.EGContext;
import com.analysys.dev.internal.work.ServiceHelper;
import com.analysys.dev.utils.AndroidManifestHelper;
import com.analysys.dev.utils.ELOG;
import com.analysys.dev.utils.ReceiverUtils;
import com.analysys.dev.utils.Streamer;
import com.analysys.dev.utils.reflectinon.EContextHelper;
import com.analysys.dev.utils.reflectinon.Reflecer;
import com.analysys.dev.utils.sp.SPHelper;
import com.analysys.dev.internal.work.MessageDispatcher;

import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@SuppressWarnings("all")
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
        ReceiverUtils.getInstance().setWork(true);
        updateAppkey(key);
        updateChannel(mContext, channel);
        EGContext.FLAG_DEBUG_USER = isDebug;
        //JobService
        ReceiverUtils.getInstance().registAllReceiver(mContext);
        PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = pm.isScreenOn();
        // 如果为true，则表示屏幕正在使用，false则屏幕关闭。
        if (!isScreenOn) {
            ReceiverUtils.getInstance().setWork(false);
        }
        ServiceHelper.getInstance(mContext).startJobService(mContext);
        MessageDispatcher.getInstance(mContext).startService();


    }
    private void updateAppkey(String key){
        if (TextUtils.isEmpty(key)) {
            Bundle bundle = AndroidManifestHelper.getMetaData(mContext);
            if (bundle != null) {
                key = bundle.getString(EGContext.XML_METADATA_APPKEY);
            }
        }
        if (!TextUtils.isEmpty(key)) {
            EGContext.APP_KEY_VALUE = key;
            SPHelper.getDefault(mContext).edit().putString(EGContext.USERKEY, key).commit();
            SPHelper.getDefault(mContext).edit().putString(EGContext.SP_APP_KEY, key).commit();
        }
    }
    /**
     * 多渠道打包==>代码==>XML
     *
     * @param context
     * @param channel
     */
    public static void updateChannel(Context context, String channel) {
        // 此处需要进行channel优先级处理,优先处理多渠道打包过来的channel,而后次之,接口传入的channel
        String channelFromApk = getChannelFromApk(context);
        if (TextUtils.isEmpty(channelFromApk)) {
            try {
                ApplicationInfo appInfo = context.getApplicationContext().getPackageManager()
                        .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
                String xmlChannel = appInfo.metaData.getString(EGContext.XML_METADATA_CHANNEL);

                if (!TextUtils.isEmpty(xmlChannel)) {
                    // 赋值为空
                    EGContext.APP_CHANNEL_VALUE = xmlChannel;
                    SPHelper.getDefault(context).edit().putString(EGContext.SP_APP_CHANNEL, channel).commit();
                    return;
                }
            } catch (Throwable e) {
            }
            if (!TextUtils.isEmpty(channel)) {
                // 赋值接口传入的channel
                EGContext.APP_CHANNEL_VALUE = channel;
                SPHelper.getDefault(context).edit().putString(EGContext.SP_APP_CHANNEL, channel).commit();
            }
        } else {
            // 赋值多渠道打包的channel
            EGContext.APP_CHANNEL_VALUE = channelFromApk;
            SPHelper.getDefault(context).edit().putString(EGContext.SP_APP_CHANNEL, channel).commit();
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

    /**
     * 仅用作多渠道打包,获取apk文件中的渠道信息
     *
     * @param context
     * @return
     */
    public static String getChannelFromApk(Context context) {
        ApplicationInfo appinfo = context.getApplicationInfo();
        String sourceDir = appinfo.sourceDir;
        // 注意这里：默认放在meta-inf/里， 所以需要再拼接一下
        String channel_pre = "META-INF/" + EGContext.EGUAN_CHANNEL_PREFIX;
        String channelName = "";
        ZipFile apkZip = null;
        try {
            apkZip = new ZipFile(sourceDir);
            Enumeration<?> entries = apkZip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = ((ZipEntry) entries.nextElement());
                String entryName = entry.getName();
                if (entryName.startsWith(channel_pre)) {
                    channelName = entryName;
                    break;
                }
            }
            // 假如没有在apk文件中找到相关渠道信息,则返回空串,表示没有调用易观多渠道打包方式
            if (TextUtils.isEmpty(channelName)) {
                return "";
            }
        } catch (IOException e) {
        } finally {
            Streamer.safeClose(apkZip);
        }
        // Eg的渠道文件以EGUAN_CHANNEL_XXX为例,其XXX为最终的渠道信息
        return channelName.substring(23);
    }
}
