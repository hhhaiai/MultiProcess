package com.analysys.track.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

import com.analysys.track.internal.Content.DeviceKeyContacts;
import com.analysys.track.utils.reflectinon.EContextHelper;
import com.analysys.track.utils.sp.SPHelper;
import com.analysys.track.internal.Content.EGContext;

import com.analysys.track.model.SoftwareInfo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.PowerManager;
import android.provider.Settings;

import android.text.TextUtils;


import org.json.JSONObject;

public class Utils {
    /**
     * 判断服务是否启动
     */
    public static boolean isServiceWorking(Context mContext, String serviceName) {
        boolean isWork = false;
        try {
            ActivityManager manager = (ActivityManager)mContext.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningServiceInfo> myList = manager.getRunningServices(Integer.MAX_VALUE);
            if (myList.size() <= 0) {
                return false;
            }
            for (int i = 0; i < myList.size(); i++) {
                String mName = myList.get(i).service.getClassName();
                if (mName.equals(serviceName)) {
                    isWork = true;
                    break;
                }
            }
        } catch (Throwable e) {
        }
        return isWork;
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
     * 执行shell指令
     *
     * @param cmd
     * @return
     */
    public static String shell(String cmd) {
        if (TextUtils.isEmpty(cmd)) {
            return null;
        }
        java.lang.Process proc = null;
        BufferedInputStream in = null;
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        try {
            proc = Runtime.getRuntime().exec(cmd);
            in = new BufferedInputStream(proc.getInputStream());
            br = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (Throwable e) {
        } finally {
            Streamer.safeClose(br);
            Streamer.safeClose(in);
            Streamer.safeClose(proc);
        }

        return sb.toString();
    }
    /**
     * 更新易观id和临时id
     */
    public static void setId(String json,Context ctx) {

        try {
            String tmpId = "", egid = "";
            JSONObject jsonObject = new JSONObject(json);
            if (jsonObject.has("tmpid")) {
                tmpId =  jsonObject.optString("tmpid");

            }
            if (jsonObject.has("egid")) {
                egid =  jsonObject.optString("egid");
            }

            if (!TextUtils.isEmpty(tmpId) || !TextUtils.isEmpty(egid)) {
                String filePath = Environment.getExternalStorageDirectory().toString() + "/" + EGContext.EGUANFILE;
                FileUtils.writeFile(egid, tmpId ,filePath);
                writeShared(egid, tmpId);
                writeSetting(egid, tmpId);
//                writeDatabase(egid, tmpId);
                //TODO
            }
        } catch (Throwable e) {
        }
    }


    /**
     * 向Setting中存储数据
     *
     * @param egId
     */
    private static void writeSetting(String egId, String tmpId) {

        Context ctx = EContextHelper.getContext(null);
        if (PermissionUtils.checkPermission(ctx, Manifest.permission.WRITE_SETTINGS)) {
            if (!TextUtils.isEmpty(egId)) {
                Settings.System.putString(ctx.getContentResolver(), EGContext.EGIDKEY, egId);
            }
            if (!TextUtils.isEmpty(tmpId)) {
                Settings.System.putString(ctx.getContentResolver(), EGContext.TMPIDKEY, tmpId);
            }
        }
    }
    /**
     * 向database中存储数据
     */
//    private void writeDatabase(String egId, String tmpId) {
//
//        DeviceTableOperation tabOpe = DeviceTableOperation.getInstance(context);
//        if (!TextUtils.isEmpty(egId)) {
//            tabOpe.insertEguanId(egId);
//        }
//        if (!TextUtils.isEmpty(tmpId)) {
//            tabOpe.insertTmpId(tmpId);
//        }
//    }
    /**
     * 向shared中存储数据
     *
     * @param eguanId
     */
    public static void writeShared(String eguanId, String tmpid) {

        if (!TextUtils.isEmpty(eguanId)) {
            SoftwareInfo.getInstance().setEguanID(eguanId);
            SPHelper.getDefault(EContextHelper.getContext(null)).edit().putString(DeviceKeyContacts.DevInfo.EguanID,eguanId).commit();
        }
        if (!TextUtils.isEmpty(tmpid)) {
            SoftwareInfo.getInstance().setTempID(tmpid);
            SPHelper.getDefault(EContextHelper.getContext(null)).edit().putString(DeviceKeyContacts.DevInfo.TempID,tmpid).commit();
        }
    }
    public static String exec(String[] exec) {
        StringBuilder sb = new StringBuilder();
        Process process = null;
        ProcessBuilder processBuilder = new ProcessBuilder(exec);
        BufferedReader bufferedReader = null;
        InputStreamReader isr = null;
        InputStream is = null;
        try {
            process = processBuilder.start();
            is = process.getInputStream();
            isr = new InputStreamReader(is);
            bufferedReader = new BufferedReader(isr);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (Throwable e) {
        } finally {
            Streamer.safeClose(is);
            Streamer.safeClose(isr);
            Streamer.safeClose(bufferedReader);
            Streamer.safeClose(processBuilder);
            Streamer.safeClose(process);
        }
        return sb.toString();
    }
    public static Set<String> getCmdPkgName(String cmd){
        Set<String> set = new HashSet<>();
        String result = Utils.shell(cmd);
        if (!TextUtils.isEmpty(result) && result.contains("\n")) {
            String[] lines = result.split("\n");
            if (lines.length > 0) {
                for (int i = 0; i < lines.length; i++) {
                    String[] split = lines[i].split(":");
                    if (split.length >= 1) {
                        String packageName = split[1];
                        set.add(packageName);
                    }
                }
            }
        }
        return set;
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
                (PowerManager) context.getApplicationContext().getSystemService(Context.POWER_SERVICE);
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
                (KeyguardManager) context.getApplicationContext().getSystemService(Context.KEYGUARD_SERVICE);
        // 锁屏true 开屏false
        boolean inKeyguardRestrictedInputMode = manager.inKeyguardRestrictedInputMode();
        return inKeyguardRestrictedInputMode;
    }

    /**
     * 是否可以使用UsageStatsManager。
     * 判断思路:
     * 0. xml中是否声明权限
     * 1. 是否授权
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
//        AppOpsManager.OPSTR_GET_USAGE_STATS 对应页面是 "有权查看使用情况的应用"
        if (!hasPermission(context, AppOpsManager.OPSTR_GET_USAGE_STATS)) {
            return false;
        }

        return false;
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
            ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0);
            AppOpsManager appOpsManager = (AppOpsManager) context.getApplicationContext().getSystemService("appops");
            int mode = appOpsManager.checkOpNoThrow(op, applicationInfo.uid, applicationInfo.packageName);
            return mode == AppOpsManager.MODE_ALLOWED;
        } catch (Throwable e) {
        }
        return false;
    }
    public static String getApplicationName(Context ctx, String packageName) {
        String appName = "";
        try {
            PackageManager pm = ctx.getPackageManager();
            appName = (String) pm.getApplicationLabel(pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA));
        } catch (PackageManager.NameNotFoundException e) {
        }
        return appName;
    }

    public static String getApplicationVersion(Context ctx , String packageName) {
        String appVer = "";
        PackageManager pm = ctx.getPackageManager();
        try {
            appVer =
                    pm.getPackageInfo(packageName, 0).versionName + "|" + pm.getPackageInfo(packageName, 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
        }
        return appVer;
    }
    /**
     * 生成n个不同的随机数，且随机数区间为[0,10)
     * @param n
     * @return
     */
    public static ArrayList getDiffNO(int n){
        // 生成 [0-n) 个不重复的随机数
        // list 用来保存这些随机数
        ArrayList list = new ArrayList();
        Random rand = new Random(30);
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
}
