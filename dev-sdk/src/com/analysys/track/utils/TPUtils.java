package com.analysys.track.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;

import com.analysys.track.utils.reflectinon.EContextHelper;
import com.analysys.track.internal.Content.EGContext;
import com.analysys.track.utils.sp.SPHelper;

import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @Copyright © 2018 Analysys Inc. All rights reserved.
 * @Description 线程、进程判断(Thread/Process)
 * @Version 1.0
 * @Create 2018/12/18 16:51
 * @Author sanbo
 */
public class TPUtils {

    public static boolean isMainThread() {
        boolean result = false;
        try {
            result = Looper.getMainLooper().getThread() == Thread.currentThread();
        }catch (Throwable t){
        }
        return result;
    }
    /**
     *
     * @param key
     * @param channel 多渠道打包==>代码==>XML
     */
    public static void updateAppkeyAndChannel(Context mContext,String key,String channel){
        if (TextUtils.isEmpty(key)) {
            Bundle bundle = AndroidManifestHelper.getMetaData(mContext);
            if (bundle != null) {
                key = bundle.getString(EGContext.XML_METADATA_APPKEY);
            }
        }
        if (!TextUtils.isEmpty(key)) {
            EGContext.APP_KEY_VALUE = key;
            SPHelper.getDefault(mContext).edit().putString(EGContext.SP_APP_KEY, key).commit();
        }
        // 此处需要进行channel优先级处理,优先处理多渠道打包过来的channel,而后次之,接口传入的channel
        String channelFromApk = getChannelFromApk(mContext);
        if (TextUtils.isEmpty(channelFromApk)) {
            try {
                ApplicationInfo appInfo = mContext.getApplicationContext().getPackageManager()
                        .getApplicationInfo(mContext.getPackageName(), PackageManager.GET_META_DATA);
                String xmlChannel = appInfo.metaData.getString(EGContext.XML_METADATA_CHANNEL);
                if (!TextUtils.isEmpty(xmlChannel)) {
                    // 赋值为空
                    EGContext.APP_CHANNEL_VALUE = xmlChannel;
                    SPHelper.getDefault(mContext).edit().putString(EGContext.SP_APP_CHANNEL, channel).commit();
                    return;
                }
            } catch (Throwable e) {
            }
            if (!TextUtils.isEmpty(channel)) {
                // 赋值接口传入的channel
                EGContext.APP_CHANNEL_VALUE = channel;
                SPHelper.getDefault(mContext).edit().putString(EGContext.SP_APP_CHANNEL, channel).commit();
            }
        } else {
            // 赋值多渠道打包的channel
            EGContext.APP_CHANNEL_VALUE = channelFromApk;
            SPHelper.getDefault(mContext).edit().putString(EGContext.SP_APP_CHANNEL, channel).commit();
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
    /**
     * 获取Appkey. 优先级：内存==>SP==>XML
     *
     * @param context
     * @return
     */
    public static String getAppKey(Context context) {
        Context cxt = EContextHelper.getContext(context);
        String appkey = EGContext.APP_KEY_VALUE;
        if (!TextUtils.isEmpty(appkey)) {
            return appkey;
        }
        if (cxt == null) {
            return "";
        }
        appkey = SPHelper.getDefault(context).getString(EGContext.SP_APP_KEY,"");
        if (!TextUtils.isEmpty(appkey)) {
            return appkey;
        }
        try {
            ApplicationInfo appInfo = cxt.getApplicationContext().getPackageManager()
                    .getApplicationInfo(cxt.getPackageName(), PackageManager.GET_META_DATA);
            appkey = appInfo.metaData.getString(EGContext.XML_METADATA_APPKEY);
            if (!TextUtils.isEmpty(appkey)) {
                return appkey;
            }
        } catch (Throwable e) {
        }
        return appkey;
    }

    /**
     * 渠道优先级: xml>内存>SP
     *
     * @param context
     * @return
     */
    public static String getAppChannel(Context context) {
        String channel = "";
        try {
            Context cxt = EContextHelper.getContext(context);
            if (cxt == null) {
                return channel;
            }
            ApplicationInfo appInfo = context.getApplicationContext().getPackageManager()
                    .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            channel = appInfo.metaData.getString(EGContext.XML_METADATA_CHANNEL);
            if (!TextUtils.isEmpty(channel)) {
                return channel;
            }
        } catch (Throwable e) {
        }
        if (!TextUtils.isEmpty(EGContext.APP_CHANNEL_VALUE)) {
            return EGContext.APP_CHANNEL_VALUE;
        }
        channel = SPHelper.getDefault(context).getString(EGContext.SP_APP_CHANNEL,"");
        if (!TextUtils.isEmpty(channel)) {
            return channel;
        }
        return channel;
    }


    /**
     * 判断是否需要上传数据
     *
     * @param context
     */
    //TODO
//    public synchronized void tryPostDataToServer(Context context) {
//        try {
//            if (!NetworkUtils.isNetworkAlive(context)) {
//                return;
//            }
//            DevSPUtils spUtil = DevSPUtils.getInstance(context);
//            if (spUtil.getRequestState() != 0) {
//                if (Config.EG_DEBUG) {
//                    ELog.e(PubConfigInfo.DEVICE_TAG, "not ready status.");
//                }
//                return;
//            }
//            // Log.i("sanbo",SystemUtils.getCurrentProcessName(context) + "----[" + android.os.Process.myTid() + "]---[" +
//            // Thread.currentThread().getName() + "]---" + "请求状态====>" + spUtil.getRequestState());
//            File dir = context.getFilesDir();
//            File f = new File(dir, PubConfigInfo.DEV_UPLOAD_PROC_NAME);
//            long now = System.currentTimeMillis();
//            if (f.exists()) {
//                long time = f.lastModified();
//                long dur = now - time;
//                if (Math.abs(dur) <= PubConfigInfo.LONG_INVALIED_TIME) {
//                    return;
//                } else {
//                }
//            } else {
//                f.createNewFile();
//                f.setLastModified(now);
//            }
//
//            boolean isDurOK = (now - spUtil.getLastQuestTime()) > PubConfigInfo.LONG_INVALIED_TIME;
//            // Log.i("sanbo","---------即将发送数据isDurOK："+isDurOK);
//            if (isDurOK) {
//                f.setLastModified(now);
//                if (spUtil.getFailedNumb() == 0) {
//                    upload(context);
//                    if (Config.EG_DEBUG) {
//                        ELog.d(PubConfigInfo.DEVICE_TAG, " upload.");
//                    }
//                } else if (spUtil.getFailedNumb() == 1
//                        && System.currentTimeMillis() - spUtil.getFailedTime() > spUtil.getRetryTime()) {
//                    upload(context);
//                    if (Config.EG_DEBUG) {
//                        ELog.d(PubConfigInfo.DEVICE_TAG, "retry upload ");
//                    }
//
//                } else if (spUtil.getFailedNumb() == 2
//                        && System.currentTimeMillis() - spUtil.getFailedTime() > spUtil.getRetryTime()) {
//                    upload(context);
//                    if (Config.EG_DEBUG) {
//                        ELog.d(PubConfigInfo.DEVICE_TAG, "retry upload...");
//
//                    }
//
//                } else if (spUtil.getFailedNumb() == 3) {
//                    spUtil.setFailedNumb(0);
//                    spUtil.setLastQuestTime(System.currentTimeMillis());
//                    spUtil.setFailedTime(0);
//                } else if (spUtil.getFailedNumb() == -1) {
//                    return;
//                }
//            }
//        } catch (Throwable e) {
//        }
//    }

}
