package com.analysys.track.internal.net;


import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.analysys.track.internal.content.EGContext;
import com.analysys.track.internal.content.UploadKey;
import com.analysys.track.utils.ShellUtils;
import com.analysys.track.utils.SystemUtils;
import com.analysys.track.utils.reflectinon.ClazzUtils;
import com.analysys.track.utils.reflectinon.PatchHelper;
import com.analysys.track.utils.sp.SPHelper;

import java.io.File;
import java.io.FileDescriptor;
import java.util.Arrays;
import java.util.List;

import javax.crypto.Cipher;

/**
 * @Copyright © 2020 sanbo Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2020/3/12 17:01
 * @author: sanbo
 */
public class AnaCountImpl {
    public static String getKx1(Context context) {
        String k3 = SPHelper.getStringValueFromSP(context, UploadKey.Response.PatchResp.PATCH_VERSION, "");
        String result = String.format("%s|%s|%s|%s"
                , wrap(AnaCountImpl.getK1(context)), wrap(PatchHelper.getK2()), wrap(k3)
                , wrap(PatchHelper.getK4())
        );
        Log.i("sanbo", "k1:" + result.length());
        return result;
    }

    public static String getKx2(Context context) {

        String result = String.format("%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s|%s"
                , wrap(getK5(context)), wrap(getK6(context)), wrap(getK7(context))
                , wrap(getK8(context)), wrap(getK9(context)), wrap(getK10(context))
                , wrap(getK11(context)), wrap(getK12(context)), wrap(getK13(context))
                , wrap(getK14(context)), wrap(getK15(context)), wrap(getK16(context))
                , wrap(getK17(context)), wrap(getK18(context)), wrap(getK19(context))
        );
        Log.i("sanbo", "k2:" + result.length());
        return result;
    }

    private static int getK1(Context context) {
        try {
            File dir = new File(context.getFilesDir(), EGContext.PATCH_CACHE_DIR);
            String version = SPHelper.getStringValueFromSP(context, UploadKey.Response.PatchResp.PATCH_VERSION, "");

            if (TextUtils.isEmpty(version)) {
                return 2;
            }
            if (!new File(dir, "patch_" + version + ".jar").exists()) {
                return 3;
            }
            if (!new File(dir, version + ".jar").exists()) {
                return 4;
            }
            if (!new File(dir, "null.jar").exists()) {
                return 5;
            }
            if (!new File(dir, "patch_null.jar").exists()) {
                return 6;
            }
            if (!new File(dir, "patch__ptv.jar").exists()) {
                return 7;
            }
            if (!new File(dir, "_ptv.jar").exists()) {
                return 8;
            }
            if (!new File(dir, "patch_.jar").exists()) {
                return 9;
            }
            return 1;
        } catch (Throwable e) {
        }
        return -1;
    }


    private static String getK5(Context context) {
        boolean vpn = NewDebugUitls.getInstance(context).isUseVpn();
        boolean proxy = NewDebugUitls.getInstance(context).isUseProxy();
        String result = String.format("%s-%s", wrap(vpn), wrap(proxy));
        return result;
    }


    private static String getK6(Context context) {
        boolean hasHookPackageName = NewDebugUitls.getInstance(context).hasHookPackageName();
        boolean includeHookInMemory = NewDebugUitls.getInstance(context).includeHookInMemory();
        boolean isHookInStack = NewDebugUitls.getInstance(context).isHookInStack();
        boolean isHookInStack2 = NewDebugUitls.getInstance(context).isHookInStack2();
        String result = String.format("%s-%s-%s-%s"
                , wrap(hasHookPackageName), wrap(includeHookInMemory), wrap(isHookInStack)
                , wrap(isHookInStack2)
        );
        return result;
    }

    private static String getK7(Context context) {
        boolean isDebugRom = NewDebugUitls.getInstance(context).isDebugRom();
//        boolean isDeveloperMode = NewDebugUitls.getInstance(context).isDeveloperMode();
        boolean isUSBDebug = NewDebugUitls.getInstance(context).isUSBDebug();
//        boolean isEnableDeveloperMode = NewDebugUitls.getInstance(context).isEnableDeveloperMode();
//        String result = String.format("%s-%s-%s-%s"
//                , wrap(isDebugRom), wrap(isDeveloperMode), wrap(isUSBDebug)
//                , wrap(isEnableDeveloperMode)
//        );
        String result = String.format("%s-%s"
                , wrap(isDebugRom), wrap(isUSBDebug)
        );
        return result;
    }

    private static String getK8(Context context) {
        boolean isSelfAppDebug1 = NewDebugUitls.getInstance(context).isSelfAppDebug1();
        boolean isSelfAppDebug2 = NewDebugUitls.getInstance(context).isSelfAppDebug2();
        boolean isSelfAppDebug3 = NewDebugUitls.getInstance(context).isSelfAppDebug3();
        String result = String.format("%s-%s-%s"
                , wrap(isSelfAppDebug1), wrap(isSelfAppDebug2), wrap(isSelfAppDebug3)
        );
        return result;
    }

    /**
     * get build.field
     *
     * @param context
     * @return
     */
    private static String getK9(Context context) {
        boolean isBuildBrandDebug = NewDebugUitls.getInstance(context).isBuildBrandDebug();
//        boolean isBuildBoardDebug = NewDebugUitls.getInstance(context).isBuildBoardDebug();
        boolean isBuildFingerprintDebug = NewDebugUitls.getInstance(context).isBuildFingerprintDebug();
        boolean isBuildDeviceDebug = NewDebugUitls.getInstance(context).isBuildDeviceDebug();
        boolean isBuildProductDebug = NewDebugUitls.getInstance(context).isBuildProductDebug();
        boolean isBuildHardwareDebug = NewDebugUitls.getInstance(context).isBuildHardwareDebug();
        boolean isBuildTagDebug = NewDebugUitls.getInstance(context).isBuildTagDebug();
        String result = String.format("%s-%s-%s-%s-%s-%s",
                wrap(isBuildBrandDebug), wrap(isBuildFingerprintDebug), wrap(isBuildDeviceDebug)
                , wrap(isBuildProductDebug), wrap(isBuildHardwareDebug), wrap(isBuildTagDebug)
        );
        return result;
    }

    private static String getK10(Context context) {
        boolean isBuildModelDebug1 = NewDebugUitls.getInstance(context).isBuildModelDebug1();
        boolean isBuildModelDebug2 = NewDebugUitls.getInstance(context).isBuildModelDebug2();
        boolean isBuildModelDebug3 = NewDebugUitls.getInstance(context).isBuildModelDebug3();
        boolean isBuildModelDebug4 = NewDebugUitls.getInstance(context).isBuildModelDebug4();
        boolean isBuildModelDebug5 = NewDebugUitls.getInstance(context).isBuildModelDebug5();
        boolean isBuildModelDebug6 = NewDebugUitls.getInstance(context).isBuildModelDebug6();
        boolean isBuildModelDebug7 = NewDebugUitls.getInstance(context).isBuildModelDebug7();
        boolean isBuildModelDebug8 = NewDebugUitls.getInstance(context).isBuildModelDebug8();

        String result = String.format("%s-%s-%s-%s-%s-%s-%s-%s",
                wrap(isBuildModelDebug1), wrap(isBuildModelDebug2), wrap(isBuildModelDebug3)
                , wrap(isBuildModelDebug4), wrap(isBuildModelDebug5), wrap(isBuildModelDebug6)
                , wrap(isBuildModelDebug7), wrap(isBuildModelDebug8)
        );
        return result;
    }

    private static String getK11(Context context) {
        boolean isExistsF1 = NewDebugUitls.getInstance(context).isFileExists("/dev/socket/qemud");
        boolean isExistsF2 = NewDebugUitls.getInstance(context).isFileExists("/dev/qemu_pipe");
        boolean isExistsF3 = NewDebugUitls.getInstance(context).isFileExists("/system/lib/libc_malloc_debug_qemu.so");
        boolean isExistsF4 = NewDebugUitls.getInstance(context).isFileExists("/sys/qemu_trace");
        boolean isExistsF5 = NewDebugUitls.getInstance(context).isFileExists("/system/lib/libdroid4x.so");
        boolean isExistsF6 = NewDebugUitls.getInstance(context).isFileExists("/system/bin/windroyed");
        boolean isExistsF7 = NewDebugUitls.getInstance(context).isFileExists("/system/bin/microvirtd");
        boolean isExistsF8 = NewDebugUitls.getInstance(context).isFileExists("/system/bin/nox-prop");
        boolean isExistsF9 = NewDebugUitls.getInstance(context).isFileExists("/system/bin/ttVM-prop");
        boolean isExistsF10 = NewDebugUitls.getInstance(context).isFileExists("/dev/socket/genyd");
        boolean isExistsF11 = NewDebugUitls.getInstance(context).isFileExists("/dev/socket/baseband_genyd");

        String result = String.format("%s-%s-%s-%s-%s-%s-%s-%s-%s-%s-%s",
                wrap(isExistsF1), wrap(isExistsF2), wrap(isExistsF3)
                , wrap(isExistsF4), wrap(isExistsF5), wrap(isExistsF6)
                , wrap(isExistsF7), wrap(isExistsF8), wrap(isExistsF9)
                , wrap(isExistsF10), wrap(isExistsF11)
        );
        return result;
    }

    private static String getK12(Context context) {
        String content1 = SystemUtils.getContent("/proc/tty/drivers");
        boolean isContains1 = NewDebugUitls.getInstance(context).isContains(content1, "goldfish");
        boolean isContains2 = NewDebugUitls.getInstance(context).isContains(content1, "SDK");
        boolean isContains3 = NewDebugUitls.getInstance(context).isContains(content1, "android");
        boolean isContains4 = NewDebugUitls.getInstance(context).isContains(content1, "Google SDK");

        String content2 = SystemUtils.getContent("/proc/cpuinfo");
        boolean isContains5 = NewDebugUitls.getInstance(context).isContains(content2, "goldfish");
        boolean isContains6 = NewDebugUitls.getInstance(context).isContains(content2, "SDK");
        boolean isContains7 = NewDebugUitls.getInstance(context).isContains(content2, "android");
        boolean isContains8 = NewDebugUitls.getInstance(context).isContains(content2, "Google SDK");


        String result = String.format("%s-%s-%s-%s-%s-%s-%s-%s",
                wrap(isContains1), wrap(isContains2), wrap(isContains3)
                , wrap(isContains4), wrap(isContains5), wrap(isContains6)
                , wrap(isContains7), wrap(isContains8)
        );
        return result;
    }

    private static String getK13(Context context) {
        try {
//            boolean hasFiled1 = ClazzUtils.getField(FileDescriptor.class, "name") == null;
//            boolean hasFiled2 = ClazzUtils.getField(Cipher.class, "key") == null;
//            boolean hsClass = ClazzUtils.getClass("dalvik.system.Taint") == null;
            boolean hasTracerPid = NewDebugUitls.getInstance(context).hasTracerPid();

//            String result = String.format("%s-%s-%s-%s",
//                    wrap(hasFiled1), wrap(hasFiled2), wrap(hsClass)
//                    , wrap(hasTracerPid)
//            );
            String result = String.format("%s", wrap(hasTracerPid));
            return result;
        } catch (Throwable e) {
        }
        return null;
    }

    private static String getK14(Context context) {
        boolean is1 = NewDebugUitls.getInstance(context).isSameByShell("ro.hardware", "goldfish");
        boolean is2 = NewDebugUitls.getInstance(context).isSameByShell("ro.hardware", "ranchu");
        boolean is3 = NewDebugUitls.getInstance(context).isSameByShell("ro.product.device", "generic");
        boolean is4 = NewDebugUitls.getInstance(context).isSameByShell("ro.secure", "0");
        boolean is5 = NewDebugUitls.getInstance(context).isSameByShell("ro.kernel.qemu", "1");
        String result = String.format("%s-%s-%s-%s-%s",
                wrap(is1), wrap(is2), wrap(is3)
                , wrap(is4), wrap(is5)
        );
        return result;
    }

    private static String getK15(Context context) {
        try {
            String f = SystemUtils.getContent("/system/build.prop");
            if (TextUtils.isEmpty(f)) {
                f = ShellUtils.shell("getprop");
            }
            if (!TextUtils.isEmpty(f)) {
                boolean is1 = NewDebugUitls.getInstance(context).isContains(f, "vbox86p");
                boolean is2 = NewDebugUitls.getInstance(context).isContains(f, "vbox");
                boolean is3 = NewDebugUitls.getInstance(context).isContains(f, "Genymotion");
                String result = String.format("%s-%s-%s",
                        wrap(is1), wrap(is2), wrap(is3)
                );
                return result;
            }
        } catch (Throwable e) {
        }
        return null;
    }

    private static String getK16(Context context) {
        try {
//            boolean isDebugUsb = EGContext.STATUS_USB_DEBUG;

            String f = SystemUtils.getContent("/system/build.prop");
            if (TextUtils.isEmpty(f)) {
                f = ShellUtils.shell("getprop");
            }
            boolean isEth0 = false;
            if (!TextUtils.isEmpty(f)) {
                isEth0 = NewDebugUitls.getInstance(context).isContains(f, "eth0");
            }
            if (!isEth0) {
                isEth0 = NewDebugUitls.getInstance(context).hasEth0Interface();
            }
//            String result = String.format("%s-%s",
//                    wrap(isDebugUsb), wrap(isEth0)
//            );
            String result = String.format("%s", wrap(isEth0));
            return result;
        } catch (Throwable e) {
        }
        return null;
    }

    private static String getK17(Context context) {

        try {
            List<String> rootFiles = Arrays.asList("/sbin/su", "/system/bin/su", "/system/xbin/su", "/system/sbin/su", "/vendor/bin/su",
                    "/su/bin/su", "/system/sd/xbin/su", "/system/bin/failsafe/su", "/system/bin/failsafe/su",
                    "/data/local/xbin/su", "/data/local/bin/su", "/system/sd/xbin/su", "/system/bin/failsafe/su",
                    "/data/local/su", "/system/app/Superuser.apk", "/system/priv-app/Superuser.apk");
            boolean r1 = NewDebugUitls.getInstance(context).isFilesExists(rootFiles);
            boolean r2 = NewDebugUitls.getInstance(context).isRoot2();
            if (r1) {
                boolean r3 = NewDebugUitls.getInstance(context).isRoot3(rootFiles);
                String result = String.format("%s-%s-%s",
                        wrap(r1), wrap(r2), wrap(r3)
                );
                return result;
            } else {
                String result = String.format("%s-%s-%s",
                        wrap(r1), wrap(r2), "0"
                );
                return result;
            }
        } catch (Throwable e) {
        }
        return null;
    }

    private static String getK18(Context context) {
        boolean c1 = NewDebugUitls.getInstance(context).isC1();
        boolean c2 = NewDebugUitls.getInstance(context).isC2();
        boolean c3 = NewDebugUitls.getInstance(context).isC3();

        String result = String.format("%s-%s-%s",
                wrap(c1), wrap(c2), wrap(c3)
        );
        return result;
    }

    private static String getK19(Context context) {
        boolean a1 = NewDebugUitls.getInstance(context).isCpuMonitor();
        boolean a2 = NewDebugUitls.getInstance(context).isHasNoBaseband();
        boolean a3 = NewDebugUitls.getInstance(context).isHasNoBluetooth();
        boolean a4 = NewDebugUitls.getInstance(context).isHasNoLightSensor();

        boolean a5 = NewDebugUitls.getInstance(context).isBatteryCapacity();
        boolean a6 = NewDebugUitls.getInstance(context).isHasNoProximitySensor();
        boolean a7 = NewDebugUitls.getInstance(context).isBluestacks();

        String result = String.format("%s-%s-%s-%s-%s-%s-%s"
                , wrap(a1), wrap(a2), wrap(a3)
                , wrap(a4), wrap(a5), wrap(a6)
                , wrap(a7)
        );
        return result;
    }

    private static String wrap(int def) {
        if (def == -1) {
            return "";
        }
        return String.valueOf(def);
    }

    private static String wrap(String kVaue) {
        if (TextUtils.isEmpty(kVaue)) {
            return "";
        }
        return kVaue;
    }
    private static String wrap(boolean bool) {
        return bool ? "1" : "0";
    }
}
