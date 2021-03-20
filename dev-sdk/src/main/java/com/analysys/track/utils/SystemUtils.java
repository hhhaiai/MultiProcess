package com.analysys.track.utils;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.TextUtils;

import com.analysys.track.BuildConfig;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.internal.content.UploadKey;
import com.analysys.track.utils.reflectinon.ClazzUtils;
import com.analysys.track.utils.reflectinon.EContextHelper;
import com.analysys.track.utils.sp.SPHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.Map;
import java.util.Random;


/**
 * @Copyright © 2019 analysys Inc. All rights reserved.
 * @Description: 系统工具方法
 * @Version: 1.0
 * @Create: Mar 6, 2019 6:35:34 PM
 * @Author: sanbo
 */
public class SystemUtils {


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

    private static boolean isRoot = false;

    /**
     * Root状态判断
     *
     * @return
     */
    public static boolean isRooted() {
        if (isRoot) {
            return isRoot;
        }
        // nexus 5x "/su/bin/"
        String[] paths = {"/sbin/su", "/system/bin/su", "/system/xbin/su", "/system/sbin/su", "/vendor/bin/su",
                "/su/bin/su", "/system/sd/xbin/su", "/system/bin/failsafe/su", "/system/bin/failsafe/su",
                "/data/local/xbin/su", "/data/local/bin/su", "/system/sd/xbin/su", "/system/bin/failsafe/su",
                "/data/local/su", "/system/app/Superuser.apk", "/system/priv-app/Superuser.apk"};

        try {
            for (String path : paths) {
                if (new File(path).exists()) {
                    isRoot = true;
                    return isRoot;
                }
            }
//        String[] gg = {"which", "type"};
//            // 1. 文件判断, 文件存在则权限判断
//            for (String path : paths) {
//                if (new File(path).exists()) {
////                    String execResult = ShellUtils.exec(new String[]{"ls", "-l", path});
////                    if (!TextUtils.isEmpty(execResult)
////                            && execResult.indexOf("root") != execResult.lastIndexOf("root")) {
////                        isRoot = true;
////                        return true;
////                    }
////                    if (!TextUtils.isEmpty(execResult) && execResult.length() >= 4) {
////                        char flag = execResult.charAt(3);
////                        if (flag == 's' || flag == 'x') {
////                            isRoot = true;
////                            return true;
////                        }
////                    }
//                    //有root的关键标志 识别为root设备
//
//                    isRoot = true;
//                    return isRoot;
//                }
//            }
//            // 2.命令行获取
//            for (String g : gg) {
//                String execResult = ShellUtils.execCommand(new String[]{g+ " su"});
//                //!"su not found".equals(execResult)
//                if (!TextUtils.isEmpty(execResult) && !execResult.contains("su not found")) {
//                    isRoot = true;
//                    return isRoot;
//                }
//            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        }
        isRoot = false;
        return isRoot;
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

    public static String getString(String data, String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        return jsonObject.optString(data, "");
    }


    /**
     * 获取上传间隔时间，调整为5分-30分钟，如服务器下发则按照服务器下发时间执行
     *
     * @return
     */
    public static long intervalTime(Context ctx) {
        long reTryTime = SPHelper.getLongValueFromSP(ctx, UploadKey.Response.RES_POLICY_FAIL_TRY_DELAY, 0);
        if (reTryTime == 0) {
            reTryTime = randomLong(5 * EGContext.TIME_MINUTE, 30 * EGContext.TIME_MINUTE);
        }
        return reTryTime;
    }

    public static long randomLong(long min, long max) {
        Random random = new Random(System.nanoTime());
        return min + (((long) (random.nextDouble() * (max - min))));
    }


    public static String getContent(String filePath) {
        try {
            // args is empty, or file not exists, return
            if (TextUtils.isEmpty(filePath) || !new File(filePath).exists()) {
                return "";
            }
            //get content
            String content = getContentFromFile(filePath);
            if (TextUtils.isEmpty(content)) {
                content = ShellUtils.shell("cat " + filePath.trim());
            }
            // check content
            if (TextUtils.isEmpty(content)) {
                return "";
            }
            return content.trim();
        } catch (Throwable e) {
        }
        return "";
    }

    public static String getContentFromFile(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return "";
        }
        return getContentFromFile(new File(filePath.trim()));
    }


    public static String getContentFromFile(File f) {
        if (!f.exists()) {
            return "";
        }
        BufferedReader reader = null;
        InputStreamReader isr = null;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(f);
            isr = new InputStreamReader(fis);
            reader = new BufferedReader(isr, 1000);

            StringBuffer sb = new StringBuffer();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            if (sb.length() > 0) {
                return sb.toString();
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        } finally {
            StreamerUtils.safeClose(fis);
            StreamerUtils.safeClose(isr);
            StreamerUtils.safeClose(reader);
        }

        return "";
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
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        }
        if (!TextUtils.isEmpty(channel)) {
            // 赋值接口传入的channel
            EGContext.VALUE_APP_CHANNEL = channel;
            SPHelper.setStringValue2SP(mContext, EGContext.SP_APP_CHANNEL, channel);
        }
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
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
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
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
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


//    /**
//     * 计算闭合时间
//     *
//     * @param openTime
//     * @return
//     */
//    public static long getCloseTime(long openTime) {
//        long currentTime = System.currentTimeMillis();
//        long closeTime = -1;
//        if (Build.VERSION.SDK_INT > 20 && Build.VERSION.SDK_INT < 24) {
//            if (currentTime - openTime > EGContext.DEFAULT_SPACE_TIME) {
//                closeTime = (long) (Math.random() * (currentTime - openTime) + openTime);
//            }
//        } else if (Build.VERSION.SDK_INT < 21) {
//            if (currentTime - openTime > EGContext.TIME_SECOND * 5) {
//                closeTime = (long) (Math.random() * (currentTime - openTime) + openTime);
//            }
//        }
//
//        return closeTime;
//    }


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
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
            return "0";
        }
    }


    /**
     * 设备序列号,SerialNumber
     */
    @SuppressWarnings("deprecation")
    public static String getSerialNumber() {
        String serialNo = "";
        try {
            if (Build.VERSION.SDK_INT > 26) {
                serialNo = (String) ClazzUtils.invokeStaticMethod("android.os.Build", "getSerial");
            } else {
                if (android.os.Build.VERSION.SDK_INT >= 9) {
                    serialNo = android.os.Build.SERIAL;
                }
            }
            serialNo = checkAndGetOne(serialNo, "gsm.serial");
            serialNo = checkAndGetOne(serialNo, "ro.serialno");
            serialNo = checkAndGetOne(serialNo, "ro.boot.serialno");
            serialNo = checkAndGetOne(serialNo, "cdma.serial");
            serialNo = checkAndGetOne(serialNo, "ro.serialnocustom");

        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        }
        return serialNo;
    }

    private static String checkAndGetOne(String serialNo, String key) {
        try {
            if (TextUtils.isEmpty(serialNo) || Build.UNKNOWN.equalsIgnoreCase(serialNo)) {
                return getSystemEnv(key);
            } else {
                return serialNo;
            }
        } catch (Throwable e) {
        }
        return serialNo;
    }

    /**
     * 获取系统的ENV:
     * android.os.SystemProperties.get-->getprop
     *
     * @return
     */
    public static String getSystemEnv(String key) {
        String result = "";
        try {
            if (TextUtils.isEmpty(key)) {
                return result;
            }
            result = getProp(key);
            if (TextUtils.isEmpty(result)) {
                result = (String) ClazzUtils.getDefaultProp(key);
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        }
//        Log.i("sanbo", key + "----------->" + result);
        return result;
    }

    private static Map<String, String> getprops = null;

    private static String getProp(String key) {
        if (TextUtils.isEmpty(key)) {
            return "";
        }
        key = key.toLowerCase(Locale.getDefault());
        if (initProp() && getprops.containsKey(key)) {
            return getprops.get(key);
        }
        return "";
    }

    public static boolean containsInProp(String text) {

        if (initProp()) {
            if (getprops.size() > 0) {
                if (TextUtils.isEmpty(text)) {
                    return false;
                }
                text = text.toLowerCase(Locale.getDefault());
                return getprops.toString().contains(text);
            }
        }
        return false;
    }

    public static boolean containsKeyInProp(String key) {
        if (initProp()) {
            if (getprops.size() > 0) {
                if (TextUtils.isEmpty(key)) {
                    return false;
                }
                key = key.toLowerCase(Locale.getDefault());
                return getprops.keySet().toString().contains(key);
            }
        }
        return false;
    }

    public static boolean containsValuesInProp(String value) {
        if (initProp()) {
            if (getprops.size() > 0) {
                if (TextUtils.isEmpty(value)) {
                    return false;
                }
                value = value.toLowerCase(Locale.getDefault());
                return getprops.values().toString().contains(value);
            }
        }
        return false;
    }

    private static boolean initProp() {
        if (getprops == null) {
            getprops = ShellUtils.getProp();
//            // 后续可以考虑增加文件读取:/default.prop 和/system/build.prop
//            if (getprops == null) {
//                getprops = new HashMap<String, String>();
//            }
        }
        return getprops != null;
    }
}
