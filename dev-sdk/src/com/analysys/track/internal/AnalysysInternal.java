package com.analysys.track.internal;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Process;
import android.text.TextUtils;

import com.analysys.track.internal.Content.EGContext;
import com.analysys.track.internal.work.CrashHandler;
import com.analysys.track.internal.work.MessageDispatcher;
import com.analysys.track.utils.AndroidManifestHelper;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.EThreadPool;
import com.analysys.track.utils.FileUtils;
import com.analysys.track.utils.ReceiverUtils;
import com.analysys.track.utils.Streamer;
import com.analysys.track.utils.TPUtils;
import com.analysys.track.utils.reflectinon.EContextHelper;
import com.analysys.track.utils.sp.SPHelper;

import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@SuppressWarnings("all")
public class AnalysysInternal {
    private SoftReference<Context> mContextRef = null;
    private boolean hasInit = false;

    private AnalysysInternal() {
    }

    private static class Holder {
        private static AnalysysInternal instance = new AnalysysInternal();
    }

    public static AnalysysInternal getInstance(Context context) {
        if (Holder.instance.mContextRef == null) {
            Holder.instance.mContextRef = new SoftReference<Context>(EContextHelper.getContext(context));
        }
        return Holder.instance;
    }

    /**
     * 初始化函数,可能为耗时操作的，判断是否主线程，需要开子线程做
     *
     * @param key     appkey
     * @param channel 渠道
     */
    public void initEguan(String key, String channel) {
        try {
            //单进程内防止重复注册
            if (hasInit) {
                return;
            }
            updateAppkey(key);//updata sp
            updateChannel(mContextRef.get(), channel);
            // 问题,线程中初始化是否会导致用户的卡顿？
            if (TPUtils.isMainThread()) {
                EThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        init();
                    }
                });
            } else {
                init();
            }
        } catch (Throwable t) {

        }

    }

    /**
     * 初始化
     */
    private void init() {
//        ELOG.d("初始化，进程Id：< " + Process.myPid() + " >");
////        initSupportMultiProcess();//TODO
//        PowerManager pm = (PowerManager) mContextRef.get().getSystemService(Context.POWER_SERVICE);
//        boolean isScreenOn = pm.isScreenOn();
//        // 如果为true，则表示屏幕正在使用，false则屏幕关闭。
//        if (!isScreenOn) {
//            ReceiverUtils.getInstance().setWork(false);
//        }
//        //JobService
////        ServiceHelper.getInstance(mContextRef.get()).startJobService(mContextRef.get());
//        MessageDispatcher.getInstance(mContextRef.get()).startService();

        hasInit = true;
        ELOG.d("初始化，进程Id：< " + Process.myPid() + " >");

        // 0.首先检查是否有Context
        Context cxt = EContextHelper.getContext(mContextRef.get());
        if (cxt != null) {
            // 1.初始化多进程
            initSupportMultiProcess(cxt);

            // 2. 设置错误回调
            CrashHandler.getInstance().setCallback(null);
            // 3. 启动工作机制
            MessageDispatcher.getInstance(cxt).startService();
            // 4. 根据屏幕调整工作状态
            PowerManager pm = (PowerManager)cxt.getSystemService(Context.POWER_SERVICE);
            if (pm != null) {
                boolean isScreenOn = pm.isScreenOn();
                // 如果为true，则表示屏幕正在使用，false则屏幕关闭。
                if (!isScreenOn) {
                    ReceiverUtils.getInstance().setWork(false);
                }
            }

        }
    }

    private void updateAppkey(String key) {
        if (TextUtils.isEmpty(key)) {
            Bundle bundle = AndroidManifestHelper.getMetaData(mContextRef.get());
            if (bundle != null) {
                key = bundle.getString(EGContext.XML_METADATA_APPKEY);
            }
        }
        if (!TextUtils.isEmpty(key)) {
            EGContext.APP_KEY_VALUE = key;
            SPHelper.getDefault(mContextRef.get()).edit().putString(EGContext.USERKEY, key).commit();
            SPHelper.getDefault(mContextRef.get()).edit().putString(EGContext.SP_APP_KEY, key).commit();
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
     *
     * @param cxt
     */
    private void initSupportMultiProcess(Context cxt) {
        try {
            if (cxt == null) {
                return;
            }
            FileUtils.createLockFile(cxt, EGContext.FILES_SYNC_UPLOAD, EGContext.TIME_SYNC_UPLOAD);
            FileUtils.createLockFile(cxt, EGContext.FILES_SYNC_APPSNAPSHOT, EGContext.TIME_SYNC_DEFAULT);
            if (Build.VERSION.SDK_INT < 21) {
                FileUtils.createLockFile(cxt, EGContext.FILES_SYNC_OC, EGContext.TIME_SYNC_DEFAULT);
            } else {
                FileUtils.createLockFile(cxt, EGContext.FILES_SYNC_OC, EGContext.TIME_SYNC_OC_ABOVE_FIVE);
            }
            FileUtils.createLockFile(cxt, EGContext.FILES_SYNC_LOCATION, EGContext.TIME_SYNC_OC_LOCATION);
            FileUtils.createLockFile(cxt, EGContext.FILES_SYNC_DB_WRITER, EGContext.TIME_SYNC_DEFAULT);

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
