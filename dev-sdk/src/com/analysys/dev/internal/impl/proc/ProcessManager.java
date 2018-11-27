package com.analysys.dev.internal.impl.proc;

import android.content.Context;
import android.content.pm.PackageManager;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProcessManager {
  /**
   * Get a list of user apps running in the foreground.
   *
   * @param ctx the application context
   * @return a list of user apps that are in the foreground.
   */
  public static List<AppProcess> getRunningForegroundApps(Context ctx) {
    List<AppProcess> processes = new ArrayList<AppProcess>();
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
          AppProcess process = new AppProcess(pid);
          if (process.foreground
              // ignore system processes. First app user starts at 10000.
              //                            && (process.uid < 1000 || process.uid > 9999)
              // ignore processes that are not running in the default app process.
              && !process.name.contains(":")
              // Ignore processes that the user cannot launch.
              && pm.getLaunchIntentForPackage(process.getPackageName()) != null) {
            //加入同一应用,出现多进程后多条数据的问题
            processes.add(process);
          }
        } catch (AppProcess.NotAndroidAppProcessException ignored) {
        } catch (IOException e) {
        }
      }
    }
    return uniqueElement(processes);
  }

  private static List<AppProcess> uniqueElement(List<AppProcess> processes) {
    //外循环是循环的次数
    for (int i = 0; i < processes.size() - 1; i++) {
      //内循环是 外循环一次比较的次数
      for (int j = processes.size() - 1; j > i; j--) {
        if (processes.get(j).getPackageName().equals(processes.get(i).getPackageName())) {
          processes.remove(j);
        }
      }
    }
    return processes;
  }
}
