package com.eguan.utils.procutils;

import android.content.Context;
import android.content.pm.PackageManager;

import com.eguan.Constants;
import com.eguan.utils.commonutils.EgLog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AndroidProcessManager {

    /**
     * Get a list of user apps running in the foreground.
     *
     * @param ctx
     *            the application context
     * @return a list of user apps that are in the foreground.
     */
    public static List<AndroidAppProcess> getRunningForegroundApps(Context ctx) {
        List<AndroidAppProcess> processes = new ArrayList<AndroidAppProcess>();
        File[] files = new File("/proc").listFiles();
        PackageManager pm = ctx.getPackageManager();
        for (File file : files) {
            if (file.isDirectory()) {
                int pid;
                try {
                    pid = Integer.parseInt(file.getName());
                } catch (NumberFormatException e) {
                    continue;
                }
                try {
                    AndroidAppProcess process = new AndroidAppProcess(pid);
                    if (process.foreground
                            // ignore system processes. First app user starts at 10000.
                            // && (process.uid < 1000 || process.uid > 9999)
                            // ignore processes that are not running in the default app process.
                            && !process.name.contains(":")
                            // Ignore processes that the user cannot launch.
                            && pm.getLaunchIntentForPackage(process.getPackageName()) != null) {
                        // 加入同一应用,出现多进程后多条数据的问题
                        processes.add(process);
                    }
                } catch (Exception e) {
                    // System apps will not be readable on Android 5.0+ if SELinux is enforcing.
                    // You will need root access or an elevated SELinux context to read all files
                    // under /proc.
                    if (Constants.FLAG_DEBUG_INNER) {
                        EgLog.e(String.format(Locale.getDefault(), "Error reading from /proc/%d.", pid), e);
                    }

                }
            }
        }
        return uniqueElement(processes);
    }

    private static List<AndroidAppProcess> uniqueElement(List<AndroidAppProcess> processes) {
        for (int i = 0; i < processes.size() - 1; i++) // 外循环是循环的次数
        {
            for (int j = processes.size() - 1; j > i; j--) // 内循环是 外循环一次比较的次数
            {
                if (processes.get(j).getPackageName().equals(processes.get(i).getPackageName())) {
                    processes.remove(j);
                }
            }
        }
        return processes;
    }
}
