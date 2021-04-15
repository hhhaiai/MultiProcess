package com.analysys.track.utils;

import android.content.Context;
import android.text.TextUtils;

import com.analysys.track.BuildConfig;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

/**
 * @Copyright © 2018 Analysys Inc. All rights reserved.
 * @Description 模拟器判断.参考:https://github.com/strazzere/anti-emulator
 * @Version 1.0
 * @Create 2018年12月31日 下午3:28:49
 * @Author tstrazzere
 */
public class SimulatorUtils {

    private static String[] known_files =
            {
                    //-------------------PP管道------------
                    "/dev/socket/qemud"
                    , "/dev/qemu_pipe"
                    //-------------文件判断------------
//                    , "/init.goldfish.rc"
                    , "/system/lib/libc_malloc_debug_qemu.so"
                    , "/sys/qemu_trace"
//                    , "/system/bin/qemu-props"
                    , "/system/lib/libdroid4x.so"//文卓爷
                    , "/system/bin/windroyed"//文卓爷
                    , "/system/bin/microvirtd"//逍遥
                    , "/system/bin/nox-prop"//夜神
                    , "/system/bin/ttVM-prop"//天天模拟器
                    //-------------------Genymotion模拟器环境------------
                    , "/dev/socket/genyd"
                    , "/dev/socket/baseband_genyd"

            };
    private static String[] knownQemuDrivers = {"goldfish", "SDK", "android", "Google SDK"};
    private static List<String> models = Arrays.asList(
            "sdk"
            , "Emulator"
            , "google_sdk"
            , "Android SDK built for x86"
            , "Droid4X"
            , "lgshouyou"
            , "nox"
            , "ttVM_Hdragon"
    );
    private static String tracerpid = "TracerPid";


    /**
     * 文件是否存在
     *
     * @return
     */
    public static boolean hasQEmuFiles() {

        for (int i = 0; i < known_files.length; i++) {
            String pipe = known_files[i];
            if (new File(pipe).exists()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Reads in the driver file, then checks a list for known QEmu drivers.
     *
     * @return {@code true} if any known drivers where found to exist or {@code false} if not.
     */
    public static boolean hasQEmuDrivers() {
        File[] fs = new File[]{new File("/proc/tty/drivers"), new File("/proc/cpuinfo")};
        for (int i = 0; i < fs.length; i++) {
            File drivers_file = fs[i];
            if (drivers_file.exists()) {
                String driverData = SystemUtils.getContentFromFile(drivers_file);

                if (!TextUtils.isEmpty(driverData)) {
                    for (int j = 0; j < knownQemuDrivers.length; j++) {
                        String qemuDriver = knownQemuDrivers[j];
                        if (driverData.indexOf(qemuDriver) != -1) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }


    public static boolean hasEmulatorBuild() {

        if (android.os.Build.BRAND.compareTo("generic") == 0) {
            return true;
        }
        if (android.os.Build.DEVICE.compareTo("generic") == 0) {
            return true;
        }
        if (models.contains(android.os.Build.MODEL)) {
            return true;
        }
        if (android.os.Build.PRODUCT.compareTo("sdk") == 0) {
            return true;
        }
        if (android.os.Build.HARDWARE.compareTo("goldfish") == 0) {
            return true;
        }

        return false;
    }

    public static boolean hasQemuBuildProps(Context context) {

        String hardware = SystemUtils.getSystemEnv("ro.hardware");
        if ("goldfish".equalsIgnoreCase(hardware)) {
            return true;
        }
        if ("ranchu".equalsIgnoreCase(hardware)) {
            return true;
        }

        if ("generic".equals(SystemUtils.getSystemEnv("ro.product.device"))) {
            return true;
        }
        if ("1".equals(SystemUtils.getSystemEnv("ro.kernel.qemu"))) {
            return true;
        }
        return false;
    }


    // 在vivo 5.1.1 机型上耗时异常，导致广播来的时候anr
    public static boolean isVbox(Context context) {
        try {
            String getProp = ShellUtils.shell("getprop");
            if (!TextUtils.isEmpty(getProp)) {
                if (getProp.contains("vbox86p")
                        || getProp.contains("vbox")
                        || getProp.contains("Genymotion")
                ) {
                    return true;
                }
            }
            getProp = SystemUtils.getContentFromFile("/system/build.prop");
            if (!TextUtils.isEmpty(getProp)) {
                if (getProp.contains("vbox86p")
                        || getProp.contains("vbox")
                        || getProp.contains("Genymotion")
                ) {
                    return true;
                }
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        }
        return false;
    }

    /**
     * 阿里巴巴用于检测是否在跟踪应用进程
     * <p>
     * 容易规避, 用法是创建一个线程每3秒检测一次, 如果检测到则程序崩溃
     *
     * @return
     * @throws IOException
     */
    public static boolean hasTracerPid() {
        BufferedReader reader = null;
        InputStreamReader isr = null;
        FileInputStream fis = null;
        try {
            File f = new File("/proc/self/status");
            if (!f.exists()) {
                return false;
            }
            fis = new FileInputStream(f);
            isr = new InputStreamReader(fis);
            reader = new BufferedReader(isr, 1000);
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.length() > tracerpid.length()) {
                    if (line.substring(0, tracerpid.length()).equalsIgnoreCase(tracerpid)) {
                        if (Integer.decode(line.substring(tracerpid.length() + 1).trim()) > 0) {
                            return true;
                        }
                        break;
                    }
                }
            }

        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        } finally {
            StreamerUtils.safeClose(fis,isr,reader);
        }
        return false;
    }

}

