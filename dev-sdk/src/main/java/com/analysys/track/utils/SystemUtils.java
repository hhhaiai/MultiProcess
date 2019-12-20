package com.analysys.track.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.PowerManager;
import android.text.TextUtils;

import com.analysys.track.BuildConfig;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.internal.content.UploadKey;
import com.analysys.track.utils.sp.SPHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @Copyright © 2019 analysys Inc. All rights reserved.
 * @Description: 系统工具方法
 * @Version: 1.0
 * @Create: Mar 6, 2019 6:35:34 PM
 * @Author: sanbo
 */
public class SystemUtils {
    /**
     * 获取日期
     */
    public static String getDay() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date date = new Date(System.currentTimeMillis());
        String time = simpleDateFormat.format(date);
        return time;
    }

    public static String getTime(long time) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:sss", Locale.getDefault());
        Date date = new Date(System.currentTimeMillis());
        return simpleDateFormat.format(date);
    }


    public static boolean hasPackageNameInstalled(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        try {
            packageManager.getInstallerPackageName(packageName);
            return true;
        } catch (IllegalArgumentException e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
            return false;
        }
    }


    /**
     * 检查指定包名的app是否为调试模式
     *
     * @param context
     * @param packageName
     * @return
     */
    public static boolean isApkDebugable(Context context, String packageName) {
        try {
            context = EContextHelper.getContext();
            if (context == null) {
                return false;
            }
            @SuppressLint("WrongConstant")
            PackageInfo pkginfo = context.getPackageManager().getPackageInfo(
                    packageName, 1);
            if (pkginfo != null) {
                return (pkginfo.applicationInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
            }
        } catch (Exception e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }

        }
        return false;
    }

    private static HashSet<String> catchPackage = new HashSet<>();

    /**
     * getLaunchIntentForPackage 这个方法某些设备比较耗时 引起波动, 在这里缓存一下
     *
     * @param manager
     * @param packageName
     * @return
     */
    public static boolean hasLaunchIntentForPackage(PackageManager manager, String packageName) {
        try {
            if (manager == null || packageName == null) {
                return false;
            }
            if (catchPackage.contains(packageName)) {
                return true;
            }
            if (manager.getLaunchIntentForPackage(packageName) != null) {
                catchPackage.add(packageName);
                return true;
            }
        } catch (Throwable e) {
        }
        return false;
    }

    /**
     * Method to reflectively invoke the SystemProperties.get command - which is the equivalent to the adb shell getProp
     * command.
     *
     * @param context  A {@link Context} object used to get the proper ClassLoader (just needs to be Application Context
     *                 object)
     * @param property A {@code String} object for the property to retrieve.
     * @return {@code String} value of the property requested.
     */
    public static String getProp(Context context, String property) {
        try {
            ClassLoader classLoader = context.getClassLoader();
            Class<?> systemProperties = classLoader.loadClass("android.os.SystemProperties");
            Method get = systemProperties.getMethod("get", String.class);
            Object[] params = {property};
            return (String) get.invoke(systemProperties, params);
        } catch (IllegalArgumentException iAE) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(iAE);
            }
            throw iAE;
        } catch (Exception exception) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(exception);
            }
        }
        return "";
    }

    /**
     * 获取日期
     */
    public static String getDate() {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = new Date(System.currentTimeMillis());
            String time = simpleDateFormat.format(date);
            return time;
        } catch (Throwable e) {

        }
        return "";
    }

    /**
     * 是否点亮屏幕
     *
     * @param context
     * @return true: 屏幕点亮 false: 屏幕熄灭
     */
    @SuppressWarnings("deprecation")
    public static boolean isScreenOn(Context context) {
        PowerManager powerManager = (PowerManager) context.getApplicationContext()
                .getSystemService(Context.POWER_SERVICE);
        if (powerManager == null) {
            return false;
        }
        // 锁屏true 开屏false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            return powerManager.isInteractive();
        } else {
            return powerManager.isScreenOn();
        }
    }

    /**
     * Root状态判断
     *
     * @return
     */
    public static boolean isRooted() {
        // nexus 5x "/su/bin/"
        String[] paths = {"/sbin/su", "/system/bin/su", "/system/xbin/su", "/system/sbin/su", "/vendor/bin/su",
                "/su/bin/su", "/system/sd/xbin/su", "/system/bin/failsafe/su", "/system/bin/failsafe/su",
                "/data/local/xbin/su", "/data/local/bin/su", "/system/sd/xbin/su", "/system/bin/failsafe/su",
                "/data/local/su", "/system/app/Superuser.apk", "/system/priv-app/Superuser.apk"};
        String[] gg = {"which", "type"};
        try {
            // 1. 文件判断, 文件存在则权限判断
            for (String path : paths) {
                if (new File(path).exists()) {
                    String execResult = ShellUtils.exec(new String[]{"ls", "-l", path});
                    if (!TextUtils.isEmpty(execResult)
                            && execResult.indexOf("root") != execResult.lastIndexOf("root")) {
                        return true;
                    }
                    if (!TextUtils.isEmpty(execResult) && execResult.length() >= 4) {
                        char flag = execResult.charAt(3);
                        if (flag == 's' || flag == 'x') {
                            return true;
                        }
                    }
                }
            }
            // 2.命令行获取
            for (String g : gg) {
                String execResult = ShellUtils.exec(new String[]{g, "su"});
                if (!TextUtils.isEmpty(execResult) && !"su not found".equals(execResult)) {
                    return true;
                }
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
        }
        return false;
    }

    /**
     * 是否锁屏
     *
     * @param context
     * @return true:锁屏,有输入密码解锁或者锁屏壁纸页面 false: 进入系统中的任何页面
     */
    @SuppressWarnings("deprecation")
    public static boolean isScreenLocked(Context context) {

        try {
            KeyguardManager manager = (KeyguardManager) context.getApplicationContext()
                    .getSystemService(Context.KEYGUARD_SERVICE);
            // 锁屏true 开屏false
            boolean inKeyguardRestrictedInputMode = false;
            if (manager != null) {
                inKeyguardRestrictedInputMode = manager.inKeyguardRestrictedInputMode();
            }
            return inKeyguardRestrictedInputMode;
        } catch (Throwable e) {
        }
        return true;
    }

    /**
     * 是否可以使用UsageStatsManager。 判断思路: 0. xml中是否声明权限 1. 是否授权
     *
     * @param context
     * @return
     */
    @TargetApi(21)
    public static boolean canUseUsageStatsManager(Context context) {
        if (context == null) {
            return false;
        }
        if (!AndroidManifestHelper.isPermissionDefineInManifest(context, "android.permission.PACKAGE_USAGE_STATS")) {
            return false;
        }
        // AppOpsManager.OPSTR_GET_USAGE_STATS 对应页面是 "有权查看使用情况的应用"
        if (!hasPermission(context, AppOpsManager.OPSTR_GET_USAGE_STATS)) {
            return false;
        }

        return true;
    }

    /**
     * 是否授权
     *
     * @param context
     * @param op
     * @return
     */
    @SuppressLint("WrongConstant")
    private static boolean hasPermission(Context context, String op) {
        try {
            if (context == null || TextUtils.isEmpty(op)) {
                return false;
            }
            ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(),
                    0);
            AppOpsManager appOpsManager = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                appOpsManager = (AppOpsManager) context.getApplicationContext().getSystemService("appops");
                if (appOpsManager != null) {
                    int mode = appOpsManager.checkOpNoThrow(op, applicationInfo.uid, applicationInfo.packageName);
                    // return mode == AppOpsManager.MODE_ALLOWED;
                    return mode == 0;
                }
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
        }
        return false;
    }

    /**
     * 获取上传间隔时间
     *
     * @return
     */
    public static long intervalTime(Context ctx) {
//        long reTryTime = PolicyImpl.getInstance(ctx).getSP() .getLong(UploadKey.Response.RES_POLICY_FAIL_TRY_DELAY, 0);
        long reTryTime = SPHelper.getLongValueFromSP(ctx, UploadKey.Response.RES_POLICY_FAIL_TRY_DELAY, 0);
        if (reTryTime == 0) {
            reTryTime = EGContext.TIME_MINUTE;
            // 10s间隔
            reTryTime = ((int) (Math.random() * 10) * 1000) + reTryTime;
        }

        return reTryTime;
    }


    public static String getContentFromFile(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return "";
        }
        return getContentFromFile(new File(filePath.trim()));
    }

    public static String getContentFromFile(File f) {
        if (!f.exists() || !f.canRead()) {
            return "";
        }

        byte[] data = new byte[1024];
        InputStream is = null;
        try {
            is = new FileInputStream(f);
            is.read(data);
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
        } finally {
            StreamerUtils.safeClose(is);
        }

        return new String(data);
    }


    /**
     * @param key     优先级 传入==>metaData==>XML
     * @param channel 多渠道打包==>代码==>XML
     */
    public static void updateAppkeyAndChannel(Context mContext, String key, String channel) {
        if (TextUtils.isEmpty(key)) {
            Bundle bundle = AndroidManifestHelper.getMetaData(mContext);
            if (bundle != null) {
                key = bundle.getString(EGContext.XML_METADATA_APPKEY);
            }
        }
        if (!TextUtils.isEmpty(key)) {
            EGContext.VALUE_APPKEY = key;
            SPHelper.setStringValue2SP(mContext, EGContext.SP_APP_KEY, key);
        }
        // 此处需要进行channel优先级处理,优先处理多渠道打包过来的channel,配置文件次之,接口传入的channel优先级最低
        String channelFromApk = getChannelFromApk(mContext);
        if (TextUtils.isEmpty(channelFromApk)) {
            try {
                ApplicationInfo appInfo = mContext.getApplicationContext().getPackageManager()
                        .getApplicationInfo(mContext.getPackageName(), PackageManager.GET_META_DATA);
                String xmlChannel = appInfo.metaData.getString(EGContext.XML_METADATA_CHANNEL);
                if (!TextUtils.isEmpty(xmlChannel)) {
                    // 赋值为空
                    EGContext.VALUE_APP_CHANNEL = xmlChannel;
                    SPHelper.setStringValue2SP(mContext, EGContext.SP_APP_CHANNEL, xmlChannel);
                    return;
                }
            } catch (Throwable e) {
                if (BuildConfig.ENABLE_BUGLY) {
                    BuglyUtils.commitError(e);
                }
            }
            if (!TextUtils.isEmpty(channel)) {
                // 赋值接口传入的channel
                EGContext.VALUE_APP_CHANNEL = channel;
                SPHelper.setStringValue2SP(mContext, EGContext.SP_APP_CHANNEL, channel);
            }
        } else {
            // 赋值多渠道打包的channel
            EGContext.VALUE_APP_CHANNEL = channelFromApk;
            SPHelper.setStringValue2SP(mContext, EGContext.SP_APP_CHANNEL, channelFromApk);
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
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
        } finally {
            StreamerUtils.safeClose(apkZip);
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
        Context cxt = EContextHelper.getContext();
        String appkey = EGContext.VALUE_APPKEY;
        if (!TextUtils.isEmpty(appkey)) {
            return appkey;
        }
        if (cxt == null) {
            return "";
        }
        appkey = SPHelper.getStringValueFromSP(context, EGContext.SP_APP_KEY, "");
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
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
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
            Context cxt = EContextHelper.getContext();
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
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
        }
        if (!TextUtils.isEmpty(EGContext.VALUE_APP_CHANNEL)) {
            return EGContext.VALUE_APP_CHANNEL;
        }
        channel = SPHelper.getStringValueFromSP(context, EGContext.SP_APP_CHANNEL, "");
        if (!TextUtils.isEmpty(channel)) {
            return channel;
        }
        return channel;
    }


    /**
     * 计算闭合时间
     *
     * @param openTime
     * @return
     */
    public static long getCloseTime(long openTime) {
        long currentTime = System.currentTimeMillis();
        long closeTime = -1;
        if (Build.VERSION.SDK_INT > 20 && Build.VERSION.SDK_INT < 24) {
            if (currentTime - openTime > EGContext.DEFAULT_SPACE_TIME) {
                closeTime = (long) (Math.random() * (currentTime - openTime) + openTime);
            }
        } else if (Build.VERSION.SDK_INT < 21) {
            if (currentTime - openTime > EGContext.TIME_SECOND * 5) {
                closeTime = (long) (Math.random() * (currentTime - openTime) + openTime);
            }
        }

        return closeTime;
    }

    /**
     * 是否是主线程
     *
     * @return
     */
    public static boolean isMainThread() {
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }

    /**
     * 非主线程调用
     */
    public static void runOnWorkThread(final Runnable runnable) {
        if (runnable == null) {
            return;
        }
        if (isMainThread()) {
            EThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    runnable.run();
                }
            });
        } else {
            runnable.run();
        }
    }

    /**
     * 获取当前进程的名称
     *
     * @param context
     * @return
     */
    public static String getCurrentProcessName(Context context) {
        try {
            int pid = android.os.Process.myPid();
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (am != null) {
                for (ActivityManager.RunningAppProcessInfo info : am.getRunningAppProcesses()) {
                    if (info.pid == pid) {
                        return info.processName;
                    }
                }
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
        }
        return "";
    }

    /**
     * 获取APP版本，HTTP请求头中使用。墨迹版本时使用
     *
     * @param context
     * @return
     */
    public static String getAppV(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
            return "0";
        }
    }
}
