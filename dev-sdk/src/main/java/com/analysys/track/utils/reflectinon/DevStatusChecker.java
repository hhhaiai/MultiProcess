package com.analysys.track.utils.reflectinon;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Debug;
import android.provider.Settings;
import android.text.TextUtils;

import com.analysys.track.BuildConfig;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.internal.impl.AppSnapshotImpl;
import com.analysys.track.utils.BuglyUtils;
import com.analysys.track.utils.EContextHelper;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.ShellUtils;
import com.analysys.track.utils.SimulatorUtils;
import com.analysys.track.utils.StreamerUtils;
import com.analysys.track.utils.SystemUtils;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.net.NetworkInterface;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @Copyright © 2019 sanbo Inc. All rights reserved.
 * @Description: 调试测试检测
 * @Version: 1.0
 * @Create: 2019-06-11 11:35:44
 * @author: sanbo
 * @mail: sanbo.xyz@gmail.com
 */
public class DevStatusChecker {

    private boolean isDeviceDebug = false;
    private boolean isSimulator = false;

    private DevStatusChecker() {
    }

    public static DevStatusChecker getInstance() {
        return HOLDER.INSTANCE;
    }

    private static class HOLDER {
        private static DevStatusChecker INSTANCE = new DevStatusChecker();
    }

    private String mShellPropCache;

    public boolean isDebugDevice(Context context) {
        if (!BuildConfig.STRICTMODE) {
            return false;
        }
        if (isDeviceDebug) {
            return true;
        }
        return isDebug(context);
    }

    /**
     * <pre>
     *   调试设备:
     *
     * </pre>
     *
     * @param context
     * @return 是否为调试设备
     */
    private boolean isDebug(Context context) {

        context = EContextHelper.getContext();

        // 1. 抓包[VPN/系统代理]
        if ((isProxy(context) || isVpn())) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(BuildConfig.tag_cutoff, "抓包判断，命中目标");
            }
            isDeviceDebug = true;
            return true;
        }

        // 2. hook检测
        if (isHook(context)) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(BuildConfig.tag_cutoff, "HOOK检测，命中目标");
            }
            isDeviceDebug = true;
            return true;
        }

        // 3. debug rom检测
        if (isDebugRom()) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(BuildConfig.tag_cutoff, "debug rom检测，命中目标");
            }
            isDeviceDebug = true;
            return true;
        }

        // 4. 开发者模式
        if (isDeveloperMode(context)) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(BuildConfig.tag_cutoff, "开发者模式，命中目标");
            }
            isDeviceDebug = true;
            return true;
        }
        // 5. USB调试模式
        if (isUSBDebug(context)) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(BuildConfig.tag_cutoff, "USB调试模式，命中目标");
            }
            isDeviceDebug = true;
            return true;
        }
        // 6. USB状态
        if (EGContext.STATUS_USB_DEBUG) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(BuildConfig.tag_cutoff, "USB状态，命中目标");
            }
            isDeviceDebug = true;
            return true;
        }

        // 7. 宿主debug判断
        if (isSelfDebugApp(context)) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(BuildConfig.tag_cutoff, "宿主debug判断，命中目标");
            }
            isDeviceDebug = true;
            return true;
        }


        // 8. Root检测
        if (SystemUtils.isRooted()) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(BuildConfig.tag_cutoff, "Root检测，命中目标");
            }
            isDeviceDebug = true;
            return true;
        }
        // 9. 模拟器识别
        if (isSimulator(context)) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(BuildConfig.tag_cutoff, "模拟器识别，命中目标");
            }
            isDeviceDebug = true;
            return true;
        }
//            // 10. 容器运行
//            if (isWorkInContainer(context)) {
//                if (EGContext.FLAG_DEBUG_INNER) {
//                    ELOG.e(BuildConfig.tag_cutoff, "容器运行，命中目标");
//                }
//                fixTimeStatus = 1;
//                return true;
//            }
//            fixTimeStatus = 0;


        // 11.手机证书检测[是否安装三方证书]
        // 系统证书名称是CA证书subjectDN的Md5值前四位移位取或，后缀名是.0,比如00673b5b.0。考虑到安全原因，系统CA证书需要有Root权限才能进行添加和删除
        //          /system/etc/security/cacerts
        //          /etc/security/cacerts/

        isDeviceDebug = false;
        return false;
    }

//    /**
//     * 是否在容器运行
//     *
//     * @param context
//     * @return
//     */
//    private boolean isWorkInContainer(Context context) {
//        String pkgName = context.getPackageName();
//        //1. 安装列表不包含自己,肯定不行
//        if (!SystemUtils.hasPackageNameInstalled(context, pkgName)) {
//            return true;
//        }
//        // 2. /data/data/pkg/files
//        //   /data/user/0/pkg/files
//        // 下面代码兼容性文件比较严重，小米双开无法识别
////        String fPath = context.getFilesDir().getAbsolutePath();
////        L.i("file path:" + fPath);
////        if (!fPath.startsWith("/data/data/" + pkgName + "/")
////                && !fPath.startsWith("/data/user/0/" + pkgName + "/")
////        ) {
////            return true;
////        }
//        // 3. 遍历文件夹
//        try {
//            File dir = new File("/data/data/" + pkgName + "/files");
//            if (dir.exists()) {
//                if (EGContext.FLAG_DEBUG_INNER) {
//                    ELOG.i("容器运行检测: " + dir.exists() + "----文件个数:" + dir.list().length + "-------->" + Arrays.asList(dir.list()));
//                }
//            } else {
//                dir.mkdirs();
//            }
//            if (!dir.exists()) {
//                return true;
//            }
//            File temp = new File(dir, "test");
//            if (!temp.exists()) {
//                boolean result = temp.createNewFile();
//                if (!result) {
//                    return true;
//                }
//            }
//        } catch (Throwable e) {
//            if (EGContext.FLAG_DEBUG_INNER) {
//                ELOG.e(e);
//            }
//        }
//
//        // 4. 通过shell ps获取对应进程信息，理论上只有自己的包名和和子进程的。 必须包含自己包名
////        try {
////            String psInfo = ShellUtils.shell("ps");
////            if (EGContext.FLAG_DEBUG_INNER) {
////                ELOG.i("容器运行检测 shell ps: " + psInfo);
////            }
////            if (!TextUtils.isEmpty(psInfo) && !psInfo.contains(pkgName)) {
////                return true;
////            }
////        } catch (Throwable e) {
////            if (EGContext.FLAG_DEBUG_INNER) {
////                ELOG.e(e);
////            }
////        }
//
//
////        // 5. pid check /proc/pid/cmdline
////        int pid = android.os.Process.myPid();
////        L.e("pid:" + pid);
//        // 6. classloader name check failed
////        L.i("----------->" + getClass().getClassLoader().getClass().getName());
////        L.i("---+++++++-->" + context.getClassLoader().getClass().getName());
//        return false;
//    }


    private boolean isSupportLightSensor(Context context) {
        if (context == null) {
            return true;
        }
        // 获取传感器管理器的实例
        SensorManager sensorManager = (SensorManager) context
                .getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager == null) {
            return true;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
            return countSensor != null;
        }
        return true;
    }

    private boolean hasCamera(Context context) {
        boolean hasCamera = true;
        try {
            PackageManager pm = context.getPackageManager();
            hasCamera = pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)
                    || pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)
                    || Camera.getNumberOfCameras() > 0;
        } catch (Throwable e) {

        }
        return hasCamera;
    }

    @SuppressWarnings("deprecation")
    private boolean isDeveloperMode(Context context) {
        try {
            if (Build.VERSION.SDK_INT >= 17) {
                return (Settings.Secure.getInt(context.getContentResolver(), Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) > 0);
            } else {
                return (Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.DEVELOPMENT_SETTINGS_ENABLED, 0) > 0);
            }
        } catch (Throwable e) {
        }
        return false;
    }

    /**
     * 是否被HOOK
     *
     * @param context
     * @return
     */
    public boolean isHook(Context context) {
        if (hName(context) || hFile() || hStack()) {
            return true;
        }
        return false;
    }

    private boolean hName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            List<ApplicationInfo> applicationInfoList = packageManager
                    .getInstalledApplications(PackageManager.GET_META_DATA);

            for (ApplicationInfo applicationInfo : applicationInfoList) {
                if ("de.robv.android.xposed.installer".equals(applicationInfo.packageName)) {
                    return true;
                }
                if ("com.saurik.substrate".equals(applicationInfo.packageName)) {
                    return true;
                }
            }
        } catch (Throwable e) {
        }
        return false;
    }

    private boolean hFile() {
        BufferedReader reader = null;
        FileReader fr = null;
        try {
            Set<String> libraries = new HashSet<String>();
            String mapsf = "/proc/" + android.os.Process.myPid() + "/maps";
            File f = new File(mapsf);
            if (f.exists() && f.canRead()) {
                fr = new FileReader(f);
                reader = new BufferedReader(fr);
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.endsWith(".so") || line.endsWith(".jar")) {
                        int n = line.lastIndexOf(" ");
                        libraries.add(line.substring(n + 1));
                    }
                }
                for (String library : libraries) {
                    if (library.contains("com.saurik.substrate")) {
                        return true;
                    }
                    if (library.contains("XposedBridge.jar")) {
                        return true;
                    }
                }
            }

        } catch (Exception e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
        } finally {
            StreamerUtils.safeClose(fr);
            StreamerUtils.safeClose(reader);
        }
        return false;
    }

    private boolean hStack() {
        try {
            throw new Exception("test");
        } catch (Exception e) {
            int zygoteInitCallCount = 0;
            for (StackTraceElement stackTraceElement : e.getStackTrace()) {
                if ("com.android.internal.os.ZygoteInit".equals(stackTraceElement.getClassName())) {
                    zygoteInitCallCount++;
                    if (zygoteInitCallCount == 2) {
                        return true;
                    }
                }
                if ("com.saurik.substrate.MS$2".equals(stackTraceElement.getClassName())
                        && "invoked".equals(stackTraceElement.getMethodName())) {
                    return true;
                }
                if ("de.robv.android.xposed.XposedBridge".equals(stackTraceElement.getClassName())
                        && "main".equals(stackTraceElement.getMethodName())) {
                    return true;
                }
                if ("de.robv.android.xposed.XposedBridge".equals(stackTraceElement.getClassName())
                        && "handleHookedMethod".equals(stackTraceElement.getMethodName())) {
                    return true;
                }
            }
        }
        return false;
    }

    private Boolean hasDebugApp;

    /**
     * 获取到安装列表，且手机中有小于两个调试app
     *
     * @param context
     * @return
     */
    @SuppressWarnings("deprecation")
    private boolean hasDebugApp(Context context) {
        if (hasDebugApp != null) {
            return hasDebugApp;
        }
        // 单次生成确保有数据的安装列表
        List<JSONObject> list = AppSnapshotImpl.getInstance(context).getAppDebugStatus();

        if (list.size() > 0) {
            int count = 0;
            for (JSONObject obj : list) {
                if (obj.has(EGContext.TEXT_DEBUG_STATUS)) {
                    if (obj.optBoolean(EGContext.TEXT_DEBUG_STATUS, false)) {
                        count += 1;
                    }
                }
            }
            hasDebugApp = count > 2;
            return hasDebugApp;
        } else {
            // 获取安装列表失败，作为调试设备查看
            hasDebugApp = true;
            return hasDebugApp;
        }

    }

    /**
     * 判断设备 是否使用代理上网
     */
    private boolean isProxy(Context context) {
        // 是否大于等于4.0
        final boolean IS_ICS_OR_LATER = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
        String proxyAddress;
        int proxyPort;
        if (IS_ICS_OR_LATER) {
            proxyAddress = System.getProperty("http.proxyHost");
            String portStr = System.getProperty("http.proxyPort");
            proxyPort = Integer.parseInt((portStr != null ? portStr : "-1"));
        } else {
            proxyAddress = android.net.Proxy.getHost(context);
            proxyPort = android.net.Proxy.getPort(context);
        }
        return (!TextUtils.isEmpty(proxyAddress)) && (proxyPort != -1);
    }

    /**
     * 设备是否开启了VPN
     */
    private boolean isVpn() {
        try {
            Enumeration<NetworkInterface> niList = NetworkInterface.getNetworkInterfaces();
            if (niList != null) {
                for (NetworkInterface intf : Collections.list(niList)) {
                    if (!intf.isUp() || intf.getInterfaceAddresses().size() == 0) {
                        continue;
                    }
                    // vpn 开启
                    if ("tun0".equals(intf.getName()) || "ppp0".equals(intf.getName())) {
                        return true;
                    }
                }
            }
        } catch (Throwable e) {
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    private boolean isUSBDebug(Context context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                return (Settings.Secure.getInt(context.getContentResolver(), Settings.Global.ADB_ENABLED, 0) > 0);
            } else {
                return (Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.ADB_ENABLED, 0) > 0);
            }
        } catch (Throwable e) {
        }
        return false;
    }

    public boolean isSelfDebugApp(Context context) {
        //1.通过pkg.BuildConfig 的DEBUG判断
        try {
            String packageName = context.getPackageName();
            Class<?> buildConfig = ClazzUtils.getClass(packageName + ".BuildConfig");
            Field debugField = buildConfig.getField("DEBUG");
            debugField.setAccessible(true);
            if (debugField.getBoolean(null)) {
                return true;
            }
        } catch (Throwable e) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(e);
            }
        }

        try {
            // 2. 系统判断是否debug
            if ("1".equals(ShellUtils.shell("getprop ro.debuggable"))) {
                return true;
            }
            // 3.通过ApplicationInfo的flag判断
            if ((context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
                return true;
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
        }


        return false;
    }

    /**
     * 手机版本是debug ROOM
     *
     * @return
     */
    public boolean isDebugRom() {
        //增加复用
        try {
            if (TextUtils.isEmpty(mShellPropCache)) {
                mShellPropCache = ShellUtils.shell("getprop ro.build.type");
            }
            if (TextUtils.isEmpty(mShellPropCache)) {
                return false;
            }
            return mShellPropCache.contains("userdebug") || mShellPropCache.contains("debug");
        } catch (Throwable e) {
        }
        return false;
    }

    private boolean hasEmulatorWifi(String shellProp, String buildProp) {
        if (!TextUtils.isEmpty(shellProp)) {
            if (shellProp.contains("eth0")) {
                return true;
            }
        }
        if (!TextUtils.isEmpty(buildProp)) {
            if (buildProp.contains("eth0")) {
                return true;
            }
        }

        return false;
    }

    private boolean hasEth0Interface() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                if ("eth0".equals(intf.getName())) {
                    return true;
                }
            }
        } catch (Exception ex) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(ex);
            }
        }
        return false;
    }

    private boolean isUserAMonkey() {
        return ActivityManager.isUserAMonkey();
    }

    /**
     * 是否存在解锁密码
     *
     * @param context
     * @return true: 有密码
     * </p>
     * false: 没有密码
     */
    @SuppressWarnings("deprecation")
    public boolean isLockP(Context context) {
        boolean isLock = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
            if (keyguardManager != null) {
                isLock = keyguardManager.isKeyguardSecure() || keyguardManager.isDeviceSecure();
            }
        } else {
            try {
                isLock = Settings.System.getInt(
                        context.getContentResolver(), Settings.System.LOCK_PATTERN_ENABLED, 0) == 1;
            } catch (Throwable e) {
            }
        }
        return isLock;
    }


    /**
     * <pre>
     * 模拟器判断:
     *
     * </pre>
     *
     * @param context
     * @return
     */
    public boolean isSimulator(Context context) {
        if (isSimulator) {
            return true;
        }
        if (SimulatorUtils.hasEmulatorBuild()) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(BuildConfig.tag_cutoff, "hasEmulatorBuild");
            }
            isSimulator = true;
            return isSimulator;
        }
        if (SimulatorUtils.hasQEmuFiles()) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(BuildConfig.tag_cutoff, "hasQEmuFiles");
            }
            isSimulator = true;
            return isSimulator;
        }
        if (SimulatorUtils.hasQEmuDrivers()) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(BuildConfig.tag_cutoff, "hasQEmuDrivers");
            }
            isSimulator = true;
            return isSimulator;
        }
        if (SimulatorUtils.hasTaintMemberVariables()) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(BuildConfig.tag_cutoff, "hasTaintMemberVariables");
            }
            isSimulator = true;
            return isSimulator;
        }
        if (SimulatorUtils.hasTaintClass()) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(BuildConfig.tag_cutoff, "hasTaintClass");
            }
            isSimulator = true;
            return isSimulator;
        }
        if (SimulatorUtils.hasTracerPid()) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(BuildConfig.tag_cutoff, "hasTracerPid");
            }
            isSimulator = true;
            return isSimulator;
        }
        if (SimulatorUtils.hasEmulatorAdb()) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(BuildConfig.tag_cutoff, "hasEmulatorAdb");
            }
            isSimulator = true;
            return isSimulator;
        }
        if (SimulatorUtils.hasQemuBuildProps(context)) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(BuildConfig.tag_cutoff, "hasQemuBuildProps");
            }
            isSimulator = true;
            return isSimulator;
        }
        if (SimulatorUtils.isVbox(context)) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(BuildConfig.tag_cutoff, "isVbox");
            }
            isSimulator = true;
            return isSimulator;
        }
        isSimulator = false;
        return isSimulator;
    }

//    /**
//     * 可疑设备打分
//     *
//     * @param context
//     * @return 分值 0 - 10 可能大于10  建议大于6分是可疑设备 , 大于10分一定是可疑设备 30分以上,直接停止工作
//     */
//    public int devScore(Context context) {
//        if (!BuildConfig.STRICTMODE) {
//            return 0;
//        }
//        context = EContextHelper.getContext();
//        int score = 0;
//        //region ★★★★★ 2.1、调试状态识别
//        //        2.1.1. 正在被抓包 – 检测VPN
//        if (isVpn()) {
//            score += 10;
//            if (EGContext.FLAG_DEBUG_INNER) {
//                ELOG.d(BuildConfig.tag_cutoff, "isVpn");
//            }
//            return score;
//        }
////        2.1.2. 网络设置代理 – 检测wifi代理对象
//        if (isProxy(context)) {
//            score += 10;
//            if (EGContext.FLAG_DEBUG_INNER) {
//                ELOG.d(BuildConfig.tag_cutoff, "isProxy");
//            }
//            return score;
//        }
////        2.1.3. HOOK检测
//        if (isHook(context)) {
//            score += 10;
//            if (EGContext.FLAG_DEBUG_INNER) {
//                ELOG.d(BuildConfig.tag_cutoff, "isHook");
//            }
//            return score;
//        }
////        2.1.4.  //todo 手机证书检测—三方安装证书[需要调研]
////        2.1.5. 开发者模式
//        if (isDeveloperMode(context)) {
//            score += 10;
//            if (EGContext.FLAG_DEBUG_INNER) {
//                ELOG.d(BuildConfig.tag_cutoff, "isDeveloperMode");
//            }
//            return score;
//        }
////        2.1.6. USB调试
//        if (isUSBDebug(context)) {
//            score += 10;
//            if (EGContext.FLAG_DEBUG_INNER) {
//                ELOG.d(BuildConfig.tag_cutoff, "isUSBDebug");
//            }
//            return score;
//        }
//        //endregion
//        //region ★★★☆☆ 2.3、不安全设备识别
//        //        2.3.1. root设备     4分
//        if (SystemUtils.isRooted()) {
//            if (EGContext.FLAG_DEBUG_INNER) {
//                ELOG.d(BuildConfig.tag_cutoff, "isRooted");
//            }
//            score += 4;
//        }
////        2.3.2. //todo 容器运行 [特征需要增加]     3分
////        2.3.3. 模拟器 [针对国内部分游戏玩家使用的就是模拟器，这项需要组合其他选项来使用]  3分
//        if (isSimulator(context)) {
//            if (EGContext.FLAG_DEBUG_INNER) {
//                ELOG.d(BuildConfig.tag_cutoff, "isSimulator");
//            }
//            score += 3;
//        }
////        2.3.4. 有线设备  2分
//        String shellProp = ShellUtils.shell("getprop");
//        String buildProp = SystemUtils.getContentFromFile("/system/build.prop");
//        if (hasEmulatorWifi(shellProp, buildProp) || hasEth0Interface()) {
//            if (EGContext.FLAG_DEBUG_INNER) {
//                ELOG.d(BuildConfig.tag_cutoff, "hasEmulatorWifi");
//            }
//            score += 2;
//        }
////        2.3.5. 设备里安装调试app数量 2分
//        if (hasDebugApp(context)) {
//            if (EGContext.FLAG_DEBUG_INNER) {
//                ELOG.d(BuildConfig.tag_cutoff, "hasDebugApp");
//            }
//            score += 2;
//        }
////        2.3.6. 自己的app是否为调试app 2分
//        if (isSelfDebugApp(context)) {
//            if (EGContext.FLAG_DEBUG_INNER) {
//                ELOG.d(BuildConfig.tag_cutoff, "isSelfDebugApp");
//            }
//            score += 2;
//        }
////        2.3.7. 是否为monkey模式  1分
//        if (isUserAMonkey()) {
//            if (EGContext.FLAG_DEBUG_INNER) {
//                ELOG.d(BuildConfig.tag_cutoff, "isUserAMonkey");
//            }
//            score += 1;
//        }
////        2.3.8. 是否为调试模式  [Debug.isDebuggerConnected] 1分
//        if (Debug.isDebuggerConnected()) {
//            if (EGContext.FLAG_DEBUG_INNER) {
//                ELOG.d(BuildConfig.tag_cutoff, "isDebuggerConnected");
//            }
//            score += 1;
//        }
////        2.3.9. 没有摄像头 1分
//        if (!hasCamera(context)) {
//            if (EGContext.FLAG_DEBUG_INNER) {
//                ELOG.d(BuildConfig.tag_cutoff, "hasCamera");
//            }
//            score += 1;
//        }
////        2.3.10.//todo 没有蓝牙   1分
////        2.3.11. 没有光传感器  1分
//        if (!isSupportLightSensor(context)) {
//            if (EGContext.FLAG_DEBUG_INNER) {
//                ELOG.d(BuildConfig.tag_cutoff, "isSupportLightSensor");
//            }
//            score += 1;
//        }
////        2.3.12. 没有解锁密码  1分
//        if (!isLockP(context)) {
//            if (EGContext.FLAG_DEBUG_INNER) {
//                ELOG.d(BuildConfig.tag_cutoff, "isLockP");
//            }
//            score += 1;
//        }
//        // 2.3.13. 设备是debug的  1分
//        if (isDebugRom()) {
//            if (EGContext.FLAG_DEBUG_INNER) {
//                ELOG.d(BuildConfig.tag_cutoff, "isDebugRom");
//            }
//            score += 1;
//        }
//        //  2.3.13.. USB状态
//        if (EGContext.STATUS_USB_DEBUG) {
//            if (EGContext.FLAG_DEBUG_INNER) {
//                ELOG.d(BuildConfig.tag_cutoff, "STATUS_USB_DEBUG");
//            }
//            score += 1;
//        }
//        //endregion
//
//        if (EGContext.FLAG_DEBUG_INNER) {
//            ELOG.e(BuildConfig.tag_cutoff, "可疑设备评分->[" + score + "]");
//        }
//        return score;
//    }


//    private boolean isDebugged() {
//        return Debug.isDebuggerConnected();
//    }


}
