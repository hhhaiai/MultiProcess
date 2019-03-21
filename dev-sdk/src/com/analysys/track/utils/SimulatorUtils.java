package com.analysys.track.utils;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class SimulatorUtils {

    /**
     * 获取模拟器状态
     * 
     * @param context
     * @return
     */
    public static String getSimulatorStatus(Context context) {
        if (SimulatorUtils.hasEmulatorBuild(context) // 检查设备的板载,品牌,工业设计,硬件等信息是否匹配模拟器的信息,如果相同,则为模拟器
            || SimulatorUtils.hasPipes() // 检查设备是否有模拟器特有的pipe目录,如果有,则为模拟器
            || SimulatorUtils.hasQEmuFiles() // 同上,检查设备是否有模拟器特有的QEmu目录,如果有,则为模拟器
            || SimulatorUtils.hasQEmuDrivers() // 同上,检查设备是否有模拟器特有对应的QEmu设备对应的目录,如果有则为模拟器
            || SimulatorUtils.hasEmulatorAdb() // 通过读取proc/net/tcp查看adb是否对应模拟器,如果对应,则为模拟器
            || SimulatorUtils.hasGenyFiles() // 检查设备上是否有模拟器目录,如果有,则为模拟器
            || SimulatorUtils.hasEmulatorWifi()) {
            return "1";
        } else {
            return "0";
        }
    }

    /**
     * Check the existence of known pipes used by the Android QEmu environment.
     *
     * @return {@code true} if any pipes where found to exist or {@code false} if not.
     */
    private static boolean hasPipes() {
        for (String pipe : known_pipes) {
            File qemu_socket = new File(pipe);
            if (qemu_socket.exists()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check the existence of known files used by the Android QEmu environment.
     *
     * @return {@code true} if any files where found to exist or {@code false} if not.
     */
    private static boolean hasQEmuFiles() {
        for (String pipe : known_files) {
            File qemu_file = new File(pipe);
            if (qemu_file.exists()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check the existence of known files used by the Genymotion environment.
     *
     * @return {@code true} if any files where found to exist or {@code false} if not.
     */
    private static boolean hasGenyFiles() {
        for (String file : known_geny_files) {
            File geny_file = new File(file);
            if (geny_file.exists()) {
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
    private static boolean hasQEmuDrivers() {
        for (File drivers_file : new File[] {new File("/proc/tty/drivers"), new File("/proc/cpuinfo")}) {
            if (drivers_file.exists() && drivers_file.canRead()) {
                // We don't care to read much past things since info we care about should be inside here
                byte[] data = new byte[1024];
                InputStream is = null;
                try {
                    is = new FileInputStream(drivers_file);
                    is.read(data);
                } catch (Exception exception) {
                } finally {
                    StreamerUtils.safeClose(is);
                }

                String driver_data = new String(data);
                for (String known_qemu_driver : SimulatorUtils.known_qemu_drivers) {
                    if (driver_data.indexOf(known_qemu_driver) != -1) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private static boolean hasEmulatorBuild(Context context) {
        String BOARD = android.os.Build.BOARD; // The name of the underlying board, like "unknown".
        // This appears to occur often on real hardware... that's sad
        // String BOOTLOADER = android.os.Build.BOOTLOADER; // The system bootloader version number.
        String BRAND = android.os.Build.BRAND; // The brand (e.g., carrier) the software is customized for, if any.
        // "generic"
        String DEVICE = android.os.Build.DEVICE; // The name of the industrial design. "generic"
        String HARDWARE = android.os.Build.HARDWARE; // The name of the hardware (from the kernel command line or
        // /proc). "goldfish"
        String MODEL = android.os.Build.MODEL; // The end-user-visible name for the end product. "sdk"
        String PRODUCT = android.os.Build.PRODUCT; // The name of the overall product.
        return (BOARD.compareTo("unknown") == 0) /* || (BOOTLOADER.compareTo("unknown") == 0) */
            || (BRAND.compareTo("generic") == 0) || (DEVICE.compareTo("generic") == 0) || (MODEL.compareTo("sdk") == 0)
            || (PRODUCT.compareTo("sdk") == 0) || (HARDWARE.compareTo("goldfish") == 0);
    }

    private static boolean hasEmulatorAdb() {
        try {
            return hasAdbInEmulator();
        } catch (Exception exception) {
            return false;
        }
    }

    private static boolean hasEmulatorWifi() {
        String netType = ShellUtils.shell("getprop wifi.interface");
        return "eth0".equals(netType);
    }

    /**
     * This was reversed from a sample someone was submitting to sandboxes for a thesis, can't find paper anymore
     *
     * @throws IOException
     */
    private static boolean hasAdbInEmulator() throws IOException {
        boolean adbInEmulator = false;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream("/proc/net/Tcp")), 1000);
            String line;
            // Skip column names
            reader.readLine();

            ArrayList<Tcp> tcpList = new ArrayList<Tcp>();

            while ((line = reader.readLine()) != null) {
                tcpList.add(Tcp.create(line.split("\\W+")));
            }

            reader.close();

            // Adb is always bounce to 0.0.0.0 - though the port can change
            // real devices should be != 127.0.0.1
            int adbPort = -1;
            for (Tcp tcpItem : tcpList) {
                if (tcpItem.localIp == 0) {
                    adbPort = tcpItem.localPort;
                    break;
                }
            }

            if (adbPort != -1) {
                for (Tcp tcpItem : tcpList) {
                    if ((tcpItem.localIp != 0) && (tcpItem.localPort == adbPort)) {
                        adbInEmulator = true;
                    }
                }
            }
        } catch (Throwable e) {
        } finally {
            if (reader != null) {
                reader.close();
            }
        }

        return adbInEmulator;
    }

    private static class Tcp {

        // public int id;
        // public int remoteIp;
        // public int remotePort;
        public long localIp;
        public int localPort;

        public Tcp(String id, String localIp, String localPort, String remoteIp, String remotePort, String state,
            String tx_queue, String rx_queue, String tr, String tm_when, String retrnsmt, String uid, String timeout,
            String inode) {
            // this.id = Integer.parseInt(id, 16);
            this.localIp = Long.parseLong(localIp, 16);
            this.localPort = Integer.parseInt(localPort, 16);
        }

        static Tcp create(String[] params) {
            return new Tcp(params[1], params[2], params[3], params[4], params[5], params[6], params[7], params[8],
                params[9], params[10], params[11], params[12], params[13], params[14]);
        }
    }

    private static String[] known_pipes = {"/dev/socket/qemud", "/dev/qemu_pipe"};
    private static String[] known_files =
        {"/system/lib/libc_malloc_debug_qemu.so", "/sys/qemu_trace", "/system/bin/qemu-props"};
    private static String[] known_geny_files = {"/dev/socket/genyd", "/dev/socket/baseband_genyd"};
    private static String[] known_qemu_drivers = {"goldfish", "SDK", "android", "Google SDK"};

}
