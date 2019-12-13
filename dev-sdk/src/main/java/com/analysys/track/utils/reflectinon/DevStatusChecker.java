package com.analysys.track.utils.reflectinon;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

import com.analysys.track.BuildConfig;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.internal.impl.AppSnapshotImpl;
import com.analysys.track.utils.BuglyUtils;
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
import java.lang.reflect.Method;
import java.net.NetworkInterface;
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

    private DevStatusChecker() {
    }

    public static DevStatusChecker getInstance() {
        return HOLDER.INSTANCE;
    }

    /**
     * <pre>
     *   调试设备:
     *
     *
     * </pre>
     *
     * @param context
     * @return 是否为调试设备
     */
    public boolean isDebugDevice(Context context) {
        context = EContextHelper.getContext(context);

        // 1. 模拟器识别
        if (isSimulator(context)) {
//            L.i("simulator");
            return true;
        }

        //增加复用
        String shellProp = ShellUtils.shell("getprop");
        String buildProp = SystemUtils.getContentFromFile("/system/build.prop");

        // 2. 设备是debug的
        if (isDebugRom(context, shellProp, buildProp)) {
            return true;
        }
        // 3. app是debug的
        if (isSelfDebugApp(context)) {
            return true;
        }
        // 4. 有线判断
        if (hasEmulatorWifi(shellProp, buildProp) || hasEth0Interface()) {
            return true;
        }
        // 5. 是否有root
        if (SystemUtils.isRooted()) {
            return true;
        }
        // 6. USB调试模式
        if (isUSBDebug(context)) {
            return true;
        }
        // 7. StrictMode，无单独判断的方法.跟随app的debug状态判断进行

        // 8. 网络判断
        if (isProxy(context) || isVpn()) {
            return true;
        }
        //9. 设备中存在debug版本apk
        if (hasDebugApp(context)) {
            return true;
        }

        // 10. USB状态
        if (EGContext.STATUS_USB_DEBUG) {
            return true;
        }

        // 11. 没有解锁密码则认为是调试设备
        if (!isLockP(context)) {
//            L.i(" 没有解锁密码   ");
            return true;
        }

        // 12. 是否被HOOK
        if (isHook(context)) {
            return true;
        }
        // 13. 使用monkey
        if (isUserAMonkey()) {
            return true;
        }

        // 14. 开发者模式
        if (isDeveloperMode(context)) {
            return true;
        }
//        // 14. 使用debug链接-(已知百度加固会用)
//        if (isDebugged()) {
//            return true;
//        }
        return false;
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
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
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

    /**
     * 获取到安装列表，且手机中有小于两个调试app
     *
     * @param context
     * @return
     */
    @SuppressWarnings("deprecation")
    private boolean hasDebugApp(Context context) {

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
            if (count > 2) {
                return true;
            } else {
                return false;
            }
        } else {
            // 获取安装列表失败，作为调试设备查看
            return true;
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
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    private boolean isUSBDebug(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return (Settings.Secure.getInt(context.getContentResolver(), Settings.Global.ADB_ENABLED, 0) > 0);
        } else {
            return (Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.ADB_ENABLED, 0) > 0);
        }
    }

    public boolean isSelfDebugApp(Context context) {
        //1.通过pkg.BuildConfig 的DEBUG判断
        try {
            String packageName = context.getPackageName();
            Class<?> buildConfig = Class.forName(packageName + ".BuildConfig");
            Field debugField = buildConfig.getField("DEBUG");
            debugField.setAccessible(true);
            if (debugField.getBoolean(null)) {
                return true;
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(e);
            }
        }

        try {
            // 2. 系统判断是否debug
            if ("1".equals(SystemUtils.getProp(context, "ro.debuggable"))) {
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
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(e);
            }
        }


        return false;
    }

    /**
     * 手机版本是debug ROOM
     *
     * @param context
     * @param shellProp
     * @param buildProp
     * @return
     */
    private boolean isDebugRom(Context context, String shellProp, String buildProp) {

        String version = "";
        try {
            Method method = Build.class.getDeclaredMethod("getString", String.class);
            method.setAccessible(true);
            version = (String) method.invoke(new Build(), "ro.build.type");
            version.toLowerCase();
        } catch (Exception e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
        }
        if ("userdebug".equals(version) || version.contains("debug")) {
            return true;
        } else {
            return false;
        }
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
            isLock = keyguardManager.isKeyguardSecure() || keyguardManager.isDeviceSecure();
        } else {
            isLock = Settings.System.getInt(
                    context.getContentResolver(), Settings.System.LOCK_PATTERN_ENABLED, 0) == 1;
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
        return SimulatorUtils.hasEmulatorBuild()
                || SimulatorUtils.hasQEmuFiles()
                || SimulatorUtils.hasQEmuDrivers()
                || SimulatorUtils.hasAppAnalysisPackage(context)
                || SimulatorUtils.hasTaintMemberVariables()
                || SimulatorUtils.hasTaintClass()
                || SimulatorUtils.hasTracerPid()
                || SimulatorUtils.hasEmulatorAdb()
                || SimulatorUtils.hasQemuBuildProps(context)
                || SimulatorUtils.isVbox(context);
    }

    private static class HOLDER {
        private static DevStatusChecker INSTANCE = new DevStatusChecker();
    }

//    private boolean isDebugged() {
//        return Debug.isDebuggerConnected();
//    }


}
