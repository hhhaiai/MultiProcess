package com.analysys.track.utils;

import android.content.Context;
import android.text.TextUtils;

import com.analysys.track.BuildConfig;
import com.analysys.track.utils.reflectinon.ClazzUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import javax.crypto.Cipher;

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
    private static List<String> models = Arrays.asList(new String[]{
            "sdk"
            , "Emulator"
            , "google_sdk"
            , "Android SDK built for x86"
            , "Droid4X"
            , "lgshouyou"
            , "nox"
            , "ttVM_Hdragon"
    });
    private static String tracerpid = "TracerPid";

    public static boolean hasTaintClass() {

        try {
            Class.forName("dalvik.system.Taint");
            return true;
        } catch (Throwable exception) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(exception);
            }
            return false;
        }

    }

    @SuppressWarnings("unused")
    public static boolean hasTaintMemberVariables() {
        boolean taintDetected = false;

        Field f = ClazzUtils.getField(FileDescriptor.class, "name");
        if (f != null) {
            taintDetected = true;
        }

        if (!taintDetected) {
            f = ClazzUtils.getField(Cipher.class, "key");
            if (f != null) {
                taintDetected = true;
            }
        }

        return taintDetected;
    }


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
//        for (String pipe : known_files) {
//            if (new File(pipe).exists()) {
//                return true;
//            }
//        }
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
            if (drivers_file.exists() && drivers_file.canRead()) {
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

//        if (android.os.Build.BOARD.compareTo("unknown") == 0) {
//            return true;
//        }
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
//        if (android.os.Build.FINGERPRINT.startsWith("unknown")) {
//            return true;
//        }
        if (android.os.Build.HARDWARE.compareTo("goldfish") == 0) {
            return true;
        }

        return false;
//        return (android.os.Build.BOARD.compareTo("unknown") == 0)
//                || (android.os.Build.BRAND.compareTo("generic") == 0)
//                || (android.os.Build.DEVICE.compareTo("generic") == 0)
//                || models.contains(android.os.Build.MODEL)
//                || (android.os.Build.PRODUCT.compareTo("sdk") == 0)
//                || android.os.Build.FINGERPRINT.startsWith("unknown")
//                || (android.os.Build.HARDWARE.compareTo("goldfish") == 0);
    }

    public static boolean hasQemuBuildProps(Context context) {

        if ("goldfish".equals(ShellUtils.shell("getprop ro.hardware"))) {
            return true;
        }
        if ("ranchu".equals(ShellUtils.shell("getprop ro.hardware"))) {
            return true;
        }
        if ("generic".equals(ShellUtils.shell("getprop ro.product.device"))) {
            return true;
        }
        if ("1".equals(ShellUtils.shell("getprop ro.kernel.qemu"))) {
            return true;
        }
//        if ("0".equals(ShellUtils.shell("getprop ro.secure"))) {
//            return true;
//        }
        return false;
//        return "goldfish".equals(ShellUtils.shell("getprop ro.hardware"))
//                || "ranchu".equals(ShellUtils.shell("getprop ro.hardware"))
//                || "generic".equals(ShellUtils.shell("getprop ro.product.device"))
//                || "1".equals(ShellUtils.shell("getprop ro.kernel.qemu"))
//                || "0".equals(ShellUtils.shell("getprop ro.secure"));
    }

//    /**
//     * 通过读取/proc/net/tcp的信息来判断是否存在adb. 比如真机的的信息为0: 4604D20A:B512 A3D13AD8..., 而模拟器上的对应信息就是0: 00000000:0016 00000000:0000, 因为adb通常是反射到0.0.0.0这个ip上, 虽然端口有可能改变, 但确实是可行的.
//     *
//     * @return
//     */
//    public static boolean hasEmulatorAdb() {
//
//        mStatus = 7;
//        String[] tcps = new String[]{"/proc/net/Tcp", "/proc/net/tcp", "/proc/net/tcp6"};
//        try {
//
//            for (int i = 0; i < tcps.length; i++) {
//                String tcp = tcps[i];
//                File f = new File(tcp);
//                if (f.exists() && f.canRead()) {
//                    FileInputStream fis = null;
//                    InputStreamReader isr = null;
//                    BufferedReader reader = null;
//                    try {
//                        mStatus = 710 + i;
//                        fis = new FileInputStream(f);
//                        isr = new InputStreamReader(fis);
//                        reader = new BufferedReader(isr, 1000);
//                        String line;
//                        // Skip column names
//                        reader.readLine();
//
//                        ArrayList<Tcp> tcpList = new ArrayList<Tcp>();
//
//                        while ((line = reader.readLine()) != null) {
//                            tcpList.add(Tcp.create(line.split("\\W+")));
//                        }
//                        mStatus = 720 + i;
//                        // Adb is always bounce to 0.0.0.0 - though the port can change
//                        // real devices should be != 127.0.0.1
//                        int adbPort = -1;
//                        for (Tcp tcpItem : tcpList) {
//                            if (tcpItem.localIp == 0) {
//                                adbPort = tcpItem.localPort;
//                                break;
//                            }
//                        }
//                        mStatus = 730 + i;
//                        if (adbPort != -1) {
//                            for (Tcp tcpItem : tcpList) {
//                                if ((tcpItem.localIp != 0) && (tcpItem.localPort == adbPort)) {
//                                    mStatus = 740 + i;
//                                    return true;
//                                }
//                            }
//                        }
//                    } catch (Throwable e) {
//                        if (BuildConfig.ENABLE_BUGLY) {
//                            BugReportForTest.commitError(e);
//                        }
//                    } finally {
//                        StreamerUtils.safeClose(fis);
//                        StreamerUtils.safeClose(isr);
//                        StreamerUtils.safeClose(reader);
//                    }
//                }
//            }
////            for (String tcp : tcps) {
////                File f = new File(tcp);
////                if (f.exists() && f.canRead()) {
////                    FileInputStream fis = null;
////                    InputStreamReader isr = null;
////                    BufferedReader reader = null;
////                    try {
////                        fis = new FileInputStream(f);
////                        isr = new InputStreamReader(fis);
////                        reader = new BufferedReader(isr, 1000);
////                        String line;
////                        // Skip column names
////                        reader.readLine();
////
////                        ArrayList<Tcp> tcpList = new ArrayList<Tcp>();
////
////                        while ((line = reader.readLine()) != null) {
////                            tcpList.add(Tcp.create(line.split("\\W+")));
////                        }
////
////                        // Adb is always bounce to 0.0.0.0 - though the port can change
////                        // real devices should be != 127.0.0.1
////                        int adbPort = -1;
////                        for (Tcp tcpItem : tcpList) {
////                            if (tcpItem.localIp == 0) {
////                                adbPort = tcpItem.localPort;
////                                break;
////                            }
////                        }
////
////                        if (adbPort != -1) {
////                            for (Tcp tcpItem : tcpList) {
////                                if ((tcpItem.localIp != 0) && (tcpItem.localPort == adbPort)) {
////                                    return true;
////                                }
////                            }
////                        }
////                    } catch (Throwable e) {
////                        if (BuildConfig.ENABLE_BUGLY) {
////                            BuglyUtils.commitError(e);
////                        }
////                    } finally {
////                        StreamerUtils.safeClose(fis);
////                        StreamerUtils.safeClose(isr);
////                        StreamerUtils.safeClose(reader);
////                    }
////                }
////            }
//
//        } catch (Throwable e) {
//            if (BuildConfig.ENABLE_BUGLY) {
//                BugReportForTest.commitError(e);
//            }
//
//        }
//
//        return false;
//    }

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
            if (!f.exists() || !f.canRead()) {
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
            StreamerUtils.safeClose(fis);
            StreamerUtils.safeClose(isr);
            StreamerUtils.safeClose(reader);
        }
        return false;
    }

//    public static class Tcp {
//
//        public int id;
//        public long localIp;
//        public int localPort;
//        public int remoteIp;
//        public int remotePort;
//
//        public Tcp(String id, String localIp, String localPort, String remoteIp, String remotePort, String state,
//                   String tx_queue, String rx_queue, String tr, String tm_when, String retrnsmt, String uid, String timeout,
//                   String inode) {
//            this.id = Integer.parseInt(id, 16);
//            this.localIp = Long.parseLong(localIp, 16);
//            this.localPort = Integer.parseInt(localPort, 16);
//        }
//
//        static Tcp create(String[] params) {
//            return new Tcp(params[1], params[2], params[3], params[4], params[5], params[6], params[7], params[8],
//                    params[9], params[10], params[11], params[12], params[13], params[14]);
//        }
//    }
}

