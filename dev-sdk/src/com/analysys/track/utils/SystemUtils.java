package com.analysys.track.utils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Looper;
import android.os.PowerManager;
import android.text.TextUtils;

import com.analysys.track.internal.Content.DeviceKeyContacts;
import com.analysys.track.internal.Content.EGContext;
import com.analysys.track.impl.PolicyImpl;
import com.analysys.track.utils.reflectinon.EContextHelper;
import com.analysys.track.utils.sp.SPHelper;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @Copyright © 2019 analysys Inc. All rights reserved.
 * @Description: 系统工具方法: 屏幕状态判断/是否授权/用户权限判断/应用版本获取/日期获取/随机数获取
 * @Version: 1.0
 * @Create: Mar 6, 2019 6:35:34 PM
 * @Author: sanbo
 */
public class SystemUtils {
    /**
     * 生成n个不同的随机数，且随机数区间为[0,10)
     * 
     * @param n
     * @return
     */
    public static ArrayList getDiffNO(int n) {
        // 生成 [0-n) 个不重复的随机数
        // list 用来保存这些随机数
        ArrayList list = new ArrayList();
        Random rand = new Random(System.nanoTime());
        boolean[] bool = new boolean[n];
        int num = 0;
        for (int i = 0; i < n; i++) {
            do {
                // 如果产生的数相同继续循环
                num = rand.nextInt(n);
            } while (bool[num]);
            bool[num] = true;
            list.add(num);
        }
        return list;
    }

    /**
     * 获取日期
     */
    public static String getDay() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date date = new Date(System.currentTimeMillis());
        String time = simpleDateFormat.format(date);
        return time;
    }

    /**
     * 是否点亮屏幕
     *
     * @param context
     * @return true: 屏幕点亮 false: 屏幕熄灭
     */
    @SuppressWarnings("deprecation")
    public static boolean isScreenOn(Context context) {
        PowerManager powerManager =
            (PowerManager)context.getApplicationContext().getSystemService(Context.POWER_SERVICE);
        // 锁屏true 开屏false
        return powerManager.isScreenOn();
    }

    /**
     * 是否锁屏
     *
     * @param context
     * @return true:锁屏,有输入密码解锁或者锁屏壁纸页面 false: 进入系统中的任何页面
     */
    public static boolean isScreenLocked(Context context) {

        KeyguardManager manager =
            (KeyguardManager)context.getApplicationContext().getSystemService(Context.KEYGUARD_SERVICE);
        // 锁屏true 开屏false
        boolean inKeyguardRestrictedInputMode = manager.inKeyguardRestrictedInputMode();
        return inKeyguardRestrictedInputMode;
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
            ApplicationInfo applicationInfo =
                context.getPackageManager().getApplicationInfo(context.getPackageName(), 0);
            AppOpsManager appOpsManager = (AppOpsManager)context.getApplicationContext().getSystemService("appops");
            int mode = appOpsManager.checkOpNoThrow(op, applicationInfo.uid, applicationInfo.packageName);
            // return mode == AppOpsManager.MODE_ALLOWED;
            return mode == 0;
        } catch (Throwable e) {
        }
        return false;
    }

    /**
     * 获取上传间隔时间
     *
     * @return
     */
    public static long intervalTime(Context ctx) {
        String reTryTime = PolicyImpl.getInstance(ctx).getSP().getString(DeviceKeyContacts.Response.RES_POLICY_FAIL_TRY_DELAY,String.valueOf(EGContext.FAIL_TRY_DELAY_DEFALUT));
        if(TextUtils.isEmpty(reTryTime)||reTryTime.equals("0")){
            reTryTime = String.valueOf(EGContext.FAIL_TRY_DELAY_DEFALUT);
        }
        //10s间隔
        long time = ((int) (Math.random() * 10) * 1000)  + Integer.parseInt(reTryTime);
        return time;
    }
    /**
     * 获取APP类型
     *
     * @param pkg
     * @return
     */
    public static String getAppType(Context ctx, String pkg) {
        return isSystemApps(ctx ,pkg) ? "SA" : "OA";
    }

    /**
     * 是否为系统应用:
     * <p>
     * 1. shell获取到三方列表判断
     * <p>
     * 2. 获取异常的使用其他方式判断
     *
     * @param pkg
     * @return
     */
    private static boolean isSystemApps(Context mContext ,String pkg) {
        if (TextUtils.isEmpty(pkg)) {
            return false;
        }
        // 1. 没有获取应用列表则获取
        if (!isGetAppList) {
            getPkgList(mSystemAppSet, APP_LIST_SYSTEM);
            isGetAppList = true;
        }
        // 2. 根据列表内容判断
        if (mSystemAppSet.size() > 0) {
            if (mSystemAppSet.contains(pkg)) {
                return true;
            } else {
                return false;
            }
        } else {
            try {
                // 3. 使用系统方法判断
                mContext = EContextHelper.getContext(mContext);
                if (mContext == null) {
                    return false;
                }
                PackageManager pm = mContext.getPackageManager();
                if (pm == null) {
                    return false;
                }
                PackageInfo pInfo = pm.getPackageInfo(pkg, 0);
                if ((pInfo.applicationInfo.flags & android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 1) {
                    return true;
                }
            } catch (Throwable e) {
            }

        }
        return false;
    }
    /**
     * 获取安装列表
     *
     * @param appSet
     * @param shell
     * @return
     */
    public static Set<String> getPkgList(Set<String> appSet, String shell) {
        // Set<String> set = new HashSet<String>();
        String result = ShellUtils.shell(shell);
        if (!TextUtils.isEmpty(result) && result.contains("\n")) {
            String[] lines = result.split("\n");
            if (lines.length > 0) {
                String line = null;
                for (int i = 0; i < lines.length; i++) {
                    line = lines[i];
                    // 单行条件: 非空&&有点&&有冒号
                    if (!TextUtils.isEmpty(line) && line.contains(".") && line.contains(":")) {
                        // 分割. 样例数据:<code>package:com.android.launcher3</code>
                        String[] split = line.split(":");
                        if (split != null && split.length > 1) {
                            String packageName = split[1];
                            appSet.add(packageName);
                        }
                    }
                }
            }
        }
        return appSet;
    }
    private static final String SHELL_PM_LIST_PACKAGES = "pm list packages";//all
    private static final String APP_LIST_SYSTEM = "pm list packages -s";// system
    // private final String APP_LIST_USER = "pm list packages -3";// third party
    // 获取系统应用列表
    private static final Set<String> mSystemAppSet = new HashSet<String>();
    // 已经获取应用列表
    private static volatile boolean isGetAppList = false;


    /**
     * 检测xposed相关文件,尝试加载xposed的类,如果能加载则表示已经安装了
     * @return
     */
    public static boolean byLoadXposedClass(){
        try{
            Object localObject = ClassLoader.getSystemClassLoader()
                    .loadClass("de.robv.android.xposed.XposedHelpers").newInstance();
            // 如果加载类失败 则表示当前环境没有xposed
            return localObject != null;
        }catch (Throwable localThrowable) {
            return false;
        }
    }

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
     * @param key  优先级 传入==>metaData==>XML
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


    public static JSONArray getAppsFromShell(Context mContext,String tag) {
        JSONArray appList = new JSONArray();
        try {
            JSONObject appInfo;
            Set<String> result = new HashSet<>();
            PackageManager pm = mContext.getPackageManager();
            result = getPkgList(result,SHELL_PM_LIST_PACKAGES);
            for(String pkgName : result){
                if (!TextUtils.isEmpty(pkgName) && pm.getLaunchIntentForPackage(pkgName) != null) {
                    appInfo = new JSONObject();
                    appInfo.put(DeviceKeyContacts.AppSnapshotInfo.ApplicationPackageName,
                            pkgName);
                    appInfo.put(DeviceKeyContacts.AppSnapshotInfo.ActionType, tag);
                    appInfo.put(DeviceKeyContacts.AppSnapshotInfo.ActionHappenTime,
                            String.valueOf(System.currentTimeMillis()));
                    appList.put(appInfo);
                }
            }
        } catch (Throwable e) {
        }
        return appList;
    }

    public static long calculateCloseTime(long openTime ){
        long currentTime = System.currentTimeMillis();
        long closeTime = -1;
        if(currentTime - openTime > EGContext.DEFAULT_SPACE_TIME){
            closeTime =(long)(Math.random()*(currentTime - openTime) + openTime);
        }
        return closeTime;
    }
}
