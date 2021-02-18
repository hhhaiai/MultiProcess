package com.analysys.track.utils.reflectinon;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.os.Process;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.analysys.track.BuildConfig;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.utils.DataLocalTempUtils;
import com.analysys.track.utils.PkgList;
import com.analysys.track.utils.ShellUtils;
import com.analysys.track.utils.StreamerUtils;
import com.analysys.track.utils.SystemUtils;
import com.analysys.track.utils.sp.SPHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.NetworkInterface;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

public class DebugDev {

    private List<String> rootFiles = Arrays.asList("/sbin/su", "/system/bin/su", "/system/xbin/su", "/system/sbin/su", "/vendor/bin/su",
            "/su/bin/su", "/system/sd/xbin/su", "/system/bin/failsafe/su", "/system/bin/failsafe/su",
            "/data/local/xbin/su", "/data/local/bin/su", "/system/sd/xbin/su", "/system/bin/failsafe/su",
            "/data/local/su", "/system/app/Superuser.apk", "/system/priv-app/Superuser.apk");
    private List<String> fns = Arrays.asList("/dev/socket/qemud", "/dev/qemu_pipe", "/system/lib/libc_malloc_debug_qemu.so"
            , "/sys/qemu_trace", "/system/lib/libdroid4x.so", "/system/bin/windroyed", "/system/bin/microvirtd", "/system/bin/nox-prop"
            , "/system/bin/ttVM-prop", "/dev/socket/genyd", "/dev/socket/baseband_genyd"
    );


    // 一次SDK启动，识别成调试设备，则本地启动的所有次判断都是调试设备
    private volatile boolean isDebugDevice = false;

    public boolean isDebugDevice() {

        /**
         * 1.如果有/data/local/tmp/kvs拦截，直接生效。
         */
        int ignoreeDebugTmp = DataLocalTempUtils.getInstance(mContext).getInt(EGContext.KVS_KEY_DEBUG, EGContext.DEBUG_VALUE);
        if (ignoreeDebugTmp != EGContext.DEBUG_VALUE) {
            return false;
        }
        /**
         * 2.  若服务器有下发则以服务器下发为主
         */
        int igoneDebugSP = SPHelper.getIntValueFromSP(mContext, EGContext.KVS_KEY_DEBUG, EGContext.DEBUG_VALUE);
        if (igoneDebugSP != EGContext.DEBUG_VALUE) {
            return false;
        }
        /**
         * 3. 编译控制是否启用严格模式
         */
        if (!BuildConfig.BUILD_USE_STRICTMODE) {
            return false;
        }
        /**
         * 4. 使用内存变量，避免多次检测
         */
        if (isDebugDevice) {
            return true;
        }
        boolean l1 = isUseVpn();
        boolean l2 = isUseProxy();
        boolean m1 = isDebugRom();
        boolean m2 = isUSBDebug();
        boolean m4 = isRoDebuggable();
        boolean m5 = isSelfAppDebugByFlag();
        boolean n1 = isBuildBrandDebug();
        boolean n2 = isBuildFingerprintDebug();
        boolean n3 = isBuildDeviceDebug();
        boolean n4 = isBuildProductDebug();
        boolean n5 = isBuildTagDebug();
        boolean q1 = isFilesExists(rootFiles);
        boolean q2 = isRoot2();
        boolean q3 = isC1();
        boolean q4 = isC2();
        boolean q5 = isC3();
        boolean r1 = isHasNoBaseband();
//        boolean r2 = isHasNoBluetooth();
        boolean r3 = isBluestacks();
    
        isDebugDevice = l1 || l2 || m1 || m2 || m4 || m5 || n1 || n2 || n3 || n4 || n5
                || q1 || q2 || q3 || q4 || q5 || r1 || r3 || isSimulator() || isHook();
        return isDebugDevice;
    }

    public boolean isHook() {
        boolean l3 = hasHookPackageName();
        boolean l4 = includeHookInMemory();
        boolean l5 = isHookInStack();
        return l3 || l4 || l5;
    }

    public boolean isSimulator() {
        String drivers = SystemUtils.getContent("/proc/tty/drivers");
        String cpuinfo = SystemUtils.getContent("/proc/cpuinfo");

        // line 4
        boolean o1 = isBuildModelDebug();
        boolean o2 = isFilesExists(fns);
        boolean o3 = isContainsArray(drivers, Arrays.asList("goldfish", "SDK", "android", "Google SDK"));
        boolean o4 = isContainsArray(cpuinfo, Arrays.asList("goldfish", "SDK", "android", "Google SDK", "intel", "amd"));
        boolean o5 = isSameByShell("ro.hardware", "goldfish");
        // line 5
        boolean p1 = isSameByShell("ro.hardware", "ranchu");
        boolean p2 = isSameByShell("ro.product.device", "generic");
        boolean p3 = isSameByShell("ro.secure", "0");
        boolean p4 = isSameByShell("ro.kernel.qemu", "1");
        boolean p5 = isGetPropKey();
        return o1 || o2 || o3 || o4 || o5 || p1 || p2 || p3 || p4 || p5;
    }

    private boolean isGetPropKey() {

        try {
            String f = SystemUtils.getContent("/system/build.prop");
            if (TextUtils.isEmpty(f)) {
                f = ShellUtils.shell("getprop");
            }
            if (!TextUtils.isEmpty(f)) {
                //            return isContainsArray(f, Arrays.asList("vbox86p", "vbox", "Genymotion", "eth0"));
                return isContainsArray(f, Arrays.asList("vbox86p", "vbox", "Genymotion"));
            }
        } catch (Throwable e) {
        }
        return false;
    }


    private boolean isBluestacks() {
        try {
            List<String> known_bluestacks = Arrays.asList(
                    "/data/app/com.bluestacks.appmart-1.apk", "/data/app/com.bluestacks.BstCommandProcessor-1.apk",
                    "/data/app/com.bluestacks.help-1.apk", "/data/app/com.bluestacks.home-1.apk", "/data/app/com.bluestacks.s2p-1.apk",
                    "/data/app/com.bluestacks.searchapp-1.apk", "/data/bluestacks.prop", "/data/data/com.androVM.vmconfig",
                    "/data/data/com.bluestacks.accelerometerui", "/data/data/com.bluestacks.appfinder",
                    "/data/data/com.bluestacks.appmart", "/data/data/com.bluestacks.appsettings",
                    "/data/data/com.bluestacks.BstCommandProcessor", "/data/data/com.bluestacks.bstfolder",
                    "/data/data/com.bluestacks.help", "/data/data/com.bluestacks.home",
                    "/data/data/com.bluestacks.s2p", "/data/data/com.bluestacks.searchapp",
                    "/data/data/com.bluestacks.settings", "/data/data/com.bluestacks.setup",
                    "/data/data/com.bluestacks.spotlight", "/mnt/prebundledapps/bluestacks.prop.orig");

            for (String blueStacks : known_bluestacks) {
                if (new File(blueStacks).exists()) {
                    return true;
                }
            }
        } catch (Throwable e) {
        }

        return false;
    }




    /**
     * 基带检测
     *
     * @return true: 有基带
     */
    private boolean isHasNoBaseband() {

        String gsm = SystemUtils.getSystemEnv("gsm.version.baseband");
        if (TextUtils.isEmpty(gsm)) {
            return true;
        }
        return false;
    }


    /**
     * 容器判断1
     *
     * @return true:容器中运行
     */
    private boolean isC1() {
        try {
            String pid = String.valueOf(Process.myPid());
            String cmdline = SystemUtils.getContent("/proc/self/cmdline");
            if (!TextUtils.isEmpty(cmdline) && !cmdline.trim().equals(mContext.getPackageName())) {
                return true;
            }
            cmdline = SystemUtils.getContent("/proc/" + pid + "/cmdline");
            if (!TextUtils.isEmpty(cmdline) && !cmdline.trim().equals(mContext.getPackageName())) {
                return true;
            }
        } catch (Throwable e) {
        }
        return false;
    }


    /**
     * 容器判断2
     *
     * @return true:容器运行
     */
    private boolean isC2() {
        try {
            File filesDir = mContext.getFilesDir();

            if (filesDir.exists()) {
                String path = filesDir.getCanonicalPath();
                if (!TextUtils.isEmpty(path)) {
                    String pkg = mContext.getPackageName();
                    if (!path.startsWith("/data/data/" + pkg)
                            && !path.startsWith("/data/user/0/" + pkg)) {
                        return true;
                    }
                }
            }
        } catch (Throwable e) {
        }

        return false;
    }

    /**
     * 容器判断2
     *
     * @return true:容器运行
     */
    private boolean isC3() {
        try {
            return !PkgList.getInstance(mContext).getAppPackageList().contains(mContext.getPackageName());
        } catch (Throwable e) {
        }
        return false;
    }

    private boolean isRoot2() {
        try {
            List<String> gg = Arrays.asList("which", "type");
            for (String g : gg) {
                String re = ShellUtils.shell(g + " su");
                // if has root,then will print the patch of su.
                // eg: /system/bin/su  or  su is aliased to `/system/bin/su -c /data/data/xxx/bin/bash`
                if (!TextUtils.isEmpty(re) && re.contains("su") && !re.contains("not")) {
                    return true;
                }
            }
        } catch (Throwable e) {
        }

        return false;
    }


    private boolean isBuildBrandDebug() {
        try {
            String brand = ClazzUtils.g().getBuildStaticField("BRAND");
            if (!TextUtils.isEmpty(brand) && "general".equalsIgnoreCase(brand)) {
                return true;
            }
        } catch (Throwable e) {
        }
        return false;
    }


    private boolean isBuildFingerprintDebug() {
        try {
            String fingerprint = ClazzUtils.g().getBuildStaticField("FINGERPRINT");
            if (!TextUtils.isEmpty(fingerprint) && fingerprint.startsWith(EGContext.TEXT_UNKNOWN)) {
                return true;
            }
        } catch (Throwable e) {
        }
        return false;
    }

    private boolean isBuildDeviceDebug() {
        try {
            String device = ClazzUtils.g().getBuildStaticField("DEVICE");
            if (!TextUtils.isEmpty(device) && "generic".equalsIgnoreCase(device)) {
                return true;
            }
        } catch (Throwable e) {
        }
        return false;
    }

    private boolean isBuildProductDebug() {
        try {
            String product = ClazzUtils.g().getBuildStaticField("PRODUCT");
            if (!TextUtils.isEmpty(product) && "sdk".equalsIgnoreCase(product)) {
                return true;
            }
        } catch (Throwable e) {
        }
        return false;
    }

    private boolean isBuildTagDebug() {
        try {
            String tags = ClazzUtils.g().getBuildStaticField("TAGS");
            if (!TextUtils.isEmpty(tags) && tags.contains("test-keys")) {
                return true;
            }
        } catch (Throwable e) {
        }
        return false;
    }

    private boolean isBuildModelDebug() {
        try {
            List<String> models = Arrays.asList("sdk", "emulator", "google_sdk", "android sdk", "droid4x", "lgshouyou", "nox", "ttvm_hdragon");
            String model = ClazzUtils.g().getBuildStaticField("MODEL");
            if (!TextUtils.isEmpty(model) && models.contains(model.toLowerCase(Locale.getDefault()))) {
                return true;
            }
        } catch (Throwable e) {
        }
        return false;
    }


    private boolean isRoDebuggable() {
        try {
            if ("1".equals(SystemUtils.getSystemEnv("ro.debuggable"))) {
                return true;
            }
            int x = Integer.valueOf(SystemUtils.getSystemEnv("ro.debuggable"));
            if (1 == x) {
                return true;
            }
        } catch (Throwable e) {
        }
        return false;
    }

    public boolean isSelfAppDebugByFlag() {
        try {
            // 3.通过ApplicationInfo的flag判断
            if ((mContext.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
                return true;
            }
        } catch (Throwable e) {
        }
        return false;
    }


    /**
     * 很多机器上，USB模式开了即可使用，检测开发者模式意义不大
     */
    private boolean isUSBDebugInDevelopeMode = false;

    @SuppressWarnings("deprecation")
    private boolean isUSBDebug() {
        try {
            if (isUSBDebugInDevelopeMode) {
                return isUSBDebugInDevelopeMode;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                isUSBDebugInDevelopeMode = (Settings.Secure.getInt(mContext.getContentResolver(), Settings.Global.ADB_ENABLED, 0) > 0);
            } else {
                isUSBDebugInDevelopeMode = (Settings.Secure.getInt(mContext.getContentResolver(), Settings.Secure.ADB_ENABLED, 0) > 0);
            }
        } catch (Throwable e) {
            try {
                isUSBDebugInDevelopeMode = (Settings.Secure.getInt(mContext.getContentResolver(), "adb_enabled", 0) > 0);
            } catch (Throwable ex) {
            }
        }
        return isUSBDebugInDevelopeMode;
    }

    private boolean isDebugRom() {
        String type = SystemUtils.getSystemEnv("ro.build.type");
        if (!TextUtils.isEmpty(type)) {
            return type.contains("userdebug") || type.contains("debug");
        }
        return false;
    }


    private boolean isHookInStack() {
        try {
            throw new Exception("eg");
        } catch (Exception e) {
            String info = Log.getStackTraceString(e).toLowerCase();
            String zygote = "com.android.internal.os.zygoteinit";
            if (info.indexOf(zygote) != info.lastIndexOf(zygote)) {
                return true;
            }
            if (info.contains("com.saurik.substrate.MS$2")
                    || info.contains("de.robv.android.xposed.XposedBridge")
                    || info.contains("xpose")
                    || info.contains("cuckoo")
                    || info.contains("droidbox")
            ) {
                return true;
            }
        }
        return false;
    }

    private boolean hasHookPackageName() {
        try {
    
            List<String> applicationInfoList = PkgList.getInstance(mContext).getAppPackageList();
    
            if (applicationInfoList.contains("de.robv.android.xposed.installer") || applicationInfoList.contains("com.saurik.substrate")) {
                return true;
            }
        } catch (Throwable e) {
        }
        return false;
    }

    private boolean includeHookInMemory() {
        BufferedReader reader = null;
        FileReader fr = null;
        try {
            String mapsf = "/proc/" + android.os.Process.myPid() + "/maps";
            File f = new File(mapsf);
            if (f.exists()) {
                fr = new FileReader(f);
                reader = new BufferedReader(fr);
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.toLowerCase(Locale.getDefault());
                    if (line.contains("com.saurik.substrate")
                            || line.contains("xposedbridge.jar")
                            || line.contains("xpose")
                            || line.contains("libfrida")
                    ) {
                        return true;
                    }
                }
            }
        } catch (Throwable e) {
        } finally {
            StreamerUtils.safeClose(fr);
            StreamerUtils.safeClose(reader);
        }
        return false;
    }

    private boolean isUseProxy() {
        try {
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
        } catch (Throwable e) {
        }
        return false;
    }

    private boolean isUseVpn() {
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


    private boolean isFilesExists(List<String> fs) {
        try {
            if (fs != null && fs.size() > 0) {
                for (String path : fs) {
                    if (isFileExists(path)) {
                        return true;
                    }
                }
            }
        } catch (Throwable e) {
        }
        return false;
    }

    private boolean isFileExists(String path) {
        try {
            if (!TextUtils.isEmpty(path) && new File(path).exists()) {
                return true;
            }
        } catch (Throwable e) {
        }

        return false;
    }

    private boolean isSameByShell(String shellCommod, String text) {
        try {
            if (TextUtils.isEmpty(shellCommod) || TextUtils.isEmpty(text)) {
                return false;
            }
            String shellResult = SystemUtils.getSystemEnv(shellCommod);
            if (!TextUtils.isEmpty(shellResult)) {
                if (shellResult.toLowerCase(Locale.getDefault()).equals(text.toLowerCase(Locale.getDefault()))) {
                    return true;
                }
            }

        } catch (Throwable e) {
        }
        return false;
    }

    private boolean isContainsArray(String fileContent, List<String> arr) {
        try {
            // args is empty, return
            if (TextUtils.isEmpty(fileContent) || arr == null || arr.size() <= 0) {
                return false;
            }
            for (String a : arr) {
                if (isContains(fileContent, a)) {
                    return true;
                }
            }
        } catch (Throwable e) {
        }
        return false;
    }

    private boolean isContains(String fileContent, String text) {
        try {
            // args is empty, return
            if (TextUtils.isEmpty(fileContent) || TextUtils.isEmpty(text)) {
                return false;
            }
            if (fileContent.toLowerCase(Locale.getDefault()).contains(text.toLowerCase(Locale.getDefault()))) {
                return true;
            }
        } catch (Throwable e) {
        }
        return false;
    }


    /********************* get instance begin **************************/
    public static DebugDev get(Context context) {
        return HLODER.INSTANCE.initContext(context);
    }

    private DebugDev initContext(Context context) {
        if (mContext == null && context != null) {
            mContext = context.getApplicationContext();
        }
        return HLODER.INSTANCE;
    }

    private static class HLODER {
        private static final DebugDev INSTANCE = new DebugDev();
    }

    private DebugDev() {
    }

    private Context mContext = null;
    /********************* get instance end **************************/


//    /**
//     * 有线设备
//     *
//     * @return
//     */
//    private boolean hasEth0Interface() {
//        try {
//            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
//                NetworkInterface intf = en.nextElement();
//                if ("eth0".equals(intf.getName())) {
//                    return true;
//                }
//            }
//        } catch (Throwable ex) {
//        }
//        return false;
//    }
//
//    public String isDebugDevice() {
//        String drivers = SystemUtils.getContent("/proc/tty/drivers");
//        String cpuinfo = SystemUtils.getContent("/proc/cpuinfo");
//
//        // line 1
//        boolean l1 = isUseVpn();
//        boolean l2 = isUseProxy();
//        boolean l3 = hasHookPackageName();
//        boolean l4 = includeHookInMemory();
//        boolean l5 = isHookInStack();
//        // line 2
//        boolean m1 = isDebugRom();
//        boolean m2 = isUSBDebug();
//        boolean m3 = isAppDebugByBuildConfig();
//        boolean m4 = isRoDebuggable();
//        boolean m5 = isSelfAppDebugByFlag();
//        // line 3
//        boolean n1 = isBuildBrandDebug();
//        boolean n2 = isBuildFingerprintDebug();
//        boolean n3 = isBuildDeviceDebug();
//        boolean n4 = isBuildProductDebug();
//        boolean n5 = isBuildTagDebug();
//        // line 4
//        boolean o1 = isBuildModelDebug();
//        boolean o2 = isFilesExists(fns);
//        boolean o3 = isContainsArray(drivers, Arrays.asList("goldfish", "SDK", "android", "Google SDK"));
//        boolean o4 = isContainsArray(cpuinfo, Arrays.asList("goldfish", "SDK", "android", "Google SDK", "intel", "amd"));
//        boolean o5 = isSameByShell("ro.hardware", "goldfish");
//        // line 5
//        boolean p1 = isSameByShell("ro.hardware", "ranchu");
//        boolean p2 = isSameByShell("ro.product.device", "generic");
//        boolean p3 = isSameByShell("ro.secure", "0");
//        boolean p4 = isSameByShell("ro.kernel.qemu", "1");
//        boolean p5 = isGetPropKey();
//        // line 6
//        boolean q1 = isFilesExists(rootFiles);
//        boolean q2 = isRoot2();
//        boolean q3 = isC1();
//        boolean q4 = isC2();
//        boolean q5 = isC3();
//        // line 7
//        boolean r1 = isHasNoBaseband();
//        boolean r2 = isHasNoBluetooth();
//        boolean r3 = isBluestacks();
//        boolean r4 = hasEth0Interface();
//
//        String result = String.format("%s-%s-%s-%s-%s" +
//                        "-%s-%s-%s-%s-%s" +
//                        "-%s-%s-%s-%s-%s" +
//                        "-%s-%s-%s-%s-%s" +
//                        "-%s-%s-%s-%s-%s" +
//                        "-%s-%s-%s-%s-%s" +
//                        "-%s-%s-%s-%s",
//                wrap(l1), wrap(l2), wrap(l3), wrap(l4), wrap(l5)
//                , wrap(m1), wrap(m2), wrap(m3), wrap(m4), wrap(m5)
//                , wrap(n1), wrap(n2), wrap(n3), wrap(n4), wrap(n5)
//                , wrap(o1), wrap(o2), wrap(o3), wrap(o4), wrap(o5)
//                , wrap(p1), wrap(p2), wrap(p3), wrap(p4), wrap(p5)
//                , wrap(q1), wrap(q2), wrap(q3), wrap(q4), wrap(q5)
//                , wrap(r1), wrap(r2), wrap(r3), wrap(r4)
//        );
//
////        boolean isDebug = l1 || l2 || l3 || l4 || l5
////                || m1 || m2 || m3 || m4 || m5
////                || n1 || n2 || n3 || n4 || n5
////                || o1 || o2 || o3 || o4 || o5
////                || p1 || p2 || p3 || p4 || p5
////                || q1 || q2 || q3 || q4 || q5
////                || r1|| r2|| r3|| r4;
//        return result;
//    }
//
//    private boolean isAppDebugByBuildConfig() {
//        try {
//            Field debugField = ClazzUtils.g().getField(mContext.getPackageName() + ".BuildConfig", "DEBUG");
//            if (debugField != null && debugField.getBoolean(null)) {
//                return true;
//            }
//        } catch (Throwable e) {
//        }
//        return false;
//    }
//
//    private static String wrap(int def) {
//        if (def == -1) {
//            return "";
//        }
//        return String.valueOf(def);
//    }
//
//    private static String wrap(String kVaue) {
//        if (TextUtils.isEmpty(kVaue)) {
//            return "";
//        }
//        return kVaue;
//    }
//
//    private static String wrap(boolean bool) {
//        return bool ? "1" : "0";
//    }
//
//    /**
//     * 很多机器上，USB模式开了即可使用，检测开发者模式意义不大
//     *
//     * @return
//     */
//    private boolean isEnableDelepopeModeInDevelopeMode = false;
//
//    @SuppressWarnings("deprecation")
//    private boolean isDeveloperMode() {
//        try {
//            if (isEnableDelepopeModeInDevelopeMode) {
//                return isEnableDelepopeModeInDevelopeMode;
//            }
//            if (Build.VERSION.SDK_INT >= 17) {
//                isEnableDelepopeModeInDevelopeMode = (Settings.Secure.getInt(mContext.getContentResolver(), Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) > 0);
//            } else {
//                isEnableDelepopeModeInDevelopeMode = (Settings.Secure.getInt(mContext.getContentResolver(), Settings.Secure.DEVELOPMENT_SETTINGS_ENABLED, 0) > 0);
//            }
//        } catch (Throwable e) {
//            try {
//                isEnableDelepopeModeInDevelopeMode = (Settings.Secure.getInt(mContext.getContentResolver(), "development_settings_enabled", 0) > 0);
//            } catch (Throwable ex) {
//            }
//        }
//        return isEnableDelepopeModeInDevelopeMode;
//    }
//
//    private boolean isEnableDeveloperMode() {
//        try {
//            if (Build.VERSION.SDK_INT >= 17) {
//                return (Settings.Secure.getInt(mContext.getContentResolver(), Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) > 0)
//                        || (Settings.Secure.getInt(mContext.getContentResolver(), Settings.Global.ADB_ENABLED, 0) > 0);
//            } else {
//                return (Settings.Secure.getInt(mContext.getContentResolver(), Settings.Secure.DEVELOPMENT_SETTINGS_ENABLED, 0) > 0)
//                        || (Settings.Secure.getInt(mContext.getContentResolver(), Settings.Secure.ADB_ENABLED, 0) > 0);
//            }
//        } catch (Throwable e) {
//            try {
//                return (Settings.Secure.getInt(mContext.getContentResolver(), "development_settings_enabled", 0) > 0)
//                        || (Settings.Secure.getInt(mContext.getContentResolver(), "adb_enabled", 0) > 0)
//                        ;
//            } catch (Throwable ex) {
//            }
//        }
//        return false;
//    }
//    /**
//     * 距离传感器.  误伤率高，建议去除
//     *
//     * @return
//     */
//    private boolean isHasNoProximitySensor() {
//        try {
//            SensorManager sm = (SensorManager) mContext.getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
//            if (sm != null) {
//                Sensor sensor = sm.getDefaultSensor(Sensor.TYPE_PROXIMITY);
//                if (sensor == null) {
//                    return true;
//                }
//            }
//
//        } catch (Throwable e) {
//        }
//
//        return false;
//    }
//
//    /**
//     * 电池容量。部分机型，如锤子不准确，建议去除
//     *
//     * @return
//     */
//    private boolean isBatteryCapacity() {
//
//        try {
//            Object c = ClazzUtils.g().newInstance("com.android.internal.os.PowerProfile",
//                    new Class[]{Context.class}, new Object[]{context});
//            int batteryCapacity = (int) Double.parseDouble(ClazzUtils.g().invokeObjectMethod(c, "getBatteryCapacity").toString());
//            if (batteryCapacity > 1000) {
//                return true;
//            }
//        } catch (Throwable e) {
//        }
//        return false;
//    }
//
//    /**
//     * 光感建议去除
//     *
//     * @return
//     */
//    private boolean isHasNoLightSensor() {
//        try {
//            SensorManager sm = (SensorManager) mContext.getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
//            if (sm != null) {
//                Sensor sensor = sm.getDefaultSensor(Sensor.TYPE_LIGHT);
//                if (sensor == null) {
//                    return true;
//                }
//            }
//        } catch (Throwable e) {
//        }
//
//        return false;
//    }
//
//    /**
//     * rooot文件是否有操作权限，暂时废弃
//     *
//     * @param fs
//     * @return
//     */
//    private boolean isRoot3(List<String> fs) {
//        try {
//            if (fs != null && fs.size() > 0) {
//                for (String path : fs) {
//                    // file exits
//                    if (isFileExists(path)) {
//                        String execResult = ShellUtils.exec(new String[]{"ls", "-l", path});
//                        if (!TextUtils.isEmpty(execResult)
//                                && execResult.indexOf("root") != execResult.lastIndexOf("root")) {
//                            return true;
//                        }
//                        if (!TextUtils.isEmpty(execResult) && execResult.length() >= 4) {
//                            char flag = execResult.charAt(3);
//                            if (flag == 's' || flag == 'x') {
//                                return true;
//                            }
//                        }
//                    }
//                }
//            }
//        } catch (Throwable e) {
//        }
//        return false;
//    }
}
