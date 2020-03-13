package com.analysys.track.internal.net;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.analysys.track.BuildConfig;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.utils.BugReportForTest;
import com.analysys.track.utils.EContextHelper;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.ShellUtils;
import com.analysys.track.utils.StreamerUtils;
import com.analysys.track.utils.reflectinon.ClazzUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * @Copyright © 2020 sanbo Inc. All rights reserved.
 * @Description: debug设备判断
 * @Version: 1.0
 * @Create: 2020/3/13 14:19
 * @author: sanbo
 */
public class NewDebugUitls {


    public boolean isSelfAppDebug1() {
        try {
            String packageName = mContext.getPackageName();
            Class<?> buildConfig = ClazzUtils.getClass(packageName + ".BuildConfig");
            Field debugField = buildConfig.getField("DEBUG");
            debugField.setAccessible(true);
            if (debugField.getBoolean(null)) {
                return true;
            }
        } catch (Throwable e) {
        }
        return false;
    }

    public boolean isSelfAppDebug2() {
        try {
            if ("1".equals(ShellUtils.shell("getprop ro.debuggable"))) {
                return true;
            }
        } catch (Throwable e) {
        }
        return false;
    }

    public boolean isSelfAppDebug3() {
        try {
            // 3.通过ApplicationInfo的flag判断
            if ((mContext.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
                return true;
            }
        } catch (Throwable e) {
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    public boolean isDeveloperMode() {
        try {
            if (Build.VERSION.SDK_INT >= 17) {
                return (Settings.Secure.getInt(mContext.getContentResolver(), Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) > 0);
            } else {
                return (Settings.Secure.getInt(mContext.getContentResolver(), Settings.Secure.DEVELOPMENT_SETTINGS_ENABLED, 0) > 0);
            }
        } catch (Throwable e) {
            try {
                return (Settings.Secure.getInt(mContext.getContentResolver(), "development_settings_enabled", 0) > 0);
            } catch (Throwable ex) {
            }
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    public boolean isUSBDebug() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                return (Settings.Secure.getInt(mContext.getContentResolver(), Settings.Global.ADB_ENABLED, 0) > 0);
            } else {
                return (Settings.Secure.getInt(mContext.getContentResolver(), Settings.Secure.ADB_ENABLED, 0) > 0);
            }
        } catch (Throwable e) {
        }
        return false;
    }

    public boolean isEnableDeveloperMode() {
        try {
            if (Build.VERSION.SDK_INT >= 17) {
                return (Settings.Secure.getInt(mContext.getContentResolver(), Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) > 0)
                        && (Settings.Secure.getInt(mContext.getContentResolver(), Settings.Global.ADB_ENABLED, 0) > 0);
            } else {
                return (Settings.Secure.getInt(mContext.getContentResolver(), Settings.Secure.DEVELOPMENT_SETTINGS_ENABLED, 0) > 0)
                        && (Settings.Secure.getInt(mContext.getContentResolver(), Settings.Secure.ADB_ENABLED, 0) > 0);
            }
        } catch (Throwable e) {
            try {
                return (Settings.Secure.getInt(mContext.getContentResolver(), "development_settings_enabled", 0) > 0)
                        && (Settings.Secure.getInt(mContext.getContentResolver(), "adb_enabled", 0) > 0)
                        ;
            } catch (Throwable ex) {
            }
        }
        return false;
    }

    public boolean isDebugRom() {
        String getprop = ShellUtils.shell("getprop ro.build.type");
        if (!TextUtils.isEmpty(getprop)) {
            return getprop.contains("userdebug") || getprop.contains("debug");
        }
        return false;
    }

    public boolean isHookInStack() {
        try {
            throw new Exception("eg");
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

    public boolean isHookInStack2() {
        try {
            throw new Exception("eg");
        } catch (Exception e) {
            String info = Log.getStackTraceString(e);
            if (info.indexOf("com.android.internal.os.ZygoteInit") != info.lastIndexOf("com.android.internal.os.ZygoteInit")) {
                return true;
            }
            if (info.contains("com.saurik.substrate.MS$2") || info.contains("de.robv.android.xposed.XposedBridge")) {
                return true;
            }
        }
        return false;
    }

    public boolean hasHookPackageName() {
        try {
            PackageManager packageManager = mContext.getPackageManager();
            List<ApplicationInfo> applicationInfoList = packageManager
                    .getInstalledApplications(PackageManager.GET_META_DATA);

            for (ApplicationInfo applicationInfo : applicationInfoList) {
                if (applicationInfo != null) {
                    if ("de.robv.android.xposed.installer".equals(applicationInfo.packageName)
                            || "com.saurik.substrate".equals(applicationInfo.packageName)) {
                        return true;
                    }
                }
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        }
        return false;
    }

    public boolean includeHookInMemory() {
        BufferedReader reader = null;
        FileReader fr = null;
        try {
            String mapsf = "/proc/" + android.os.Process.myPid() + "/maps";
            File f = new File(mapsf);
            if (f.exists() && f.canRead()) {
                fr = new FileReader(f);
                reader = new BufferedReader(fr);
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("com.saurik.substrate") ||
                            line.contains("XposedBridge.jar")) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        } finally {
            StreamerUtils.safeClose(fr);
            StreamerUtils.safeClose(reader);
        }
        return false;
    }

    public boolean isUseProxy() {
        // 是否大于等于4.0
        String proxyAddress;
        int proxyPort;
        if (Build.VERSION.SDK_INT >= 14) {
            proxyAddress = System.getProperty("http.proxyHost");
            String portStr = System.getProperty("http.proxyPort");
            proxyPort = Integer.parseInt((portStr != null ? portStr : "-1"));
        } else {
            proxyAddress = android.net.Proxy.getHost(mContext);
            proxyPort = android.net.Proxy.getPort(mContext);
        }
        return (!TextUtils.isEmpty(proxyAddress)) && (proxyPort != -1);
    }

    public boolean isUseVpn() {
        try {
            Enumeration<NetworkInterface> niList = NetworkInterface.getNetworkInterfaces();
            if (niList != null) {
                for (NetworkInterface intf : Collections.list(niList)) {
                    try {
                        if (!intf.isUp() || intf.getInterfaceAddresses().size() == 0) {
                            continue;
                        }
                        // vpn 开启
                        if ("tun0".equals(intf.getName()) || "ppp0".equals(intf.getName())) {
                            return true;
                        }
                    } catch (Throwable e) {
                    }
                }
            }
        } catch (Throwable e) {
        }
        return false;
    }


    /********************* get instance begin **************************/
    public static NewDebugUitls getInstance(Context context) {
        return HLODER.INSTANCE.initContext(context);
    }

    private NewDebugUitls initContext(Context context) {
        mContext = EContextHelper.getContext(context);
        return HLODER.INSTANCE;
    }


    private static class HLODER {
        private static final NewDebugUitls INSTANCE = new NewDebugUitls();
    }

    private NewDebugUitls() {
    }

    private Context mContext = null;
    /********************* get instance end **************************/
}
