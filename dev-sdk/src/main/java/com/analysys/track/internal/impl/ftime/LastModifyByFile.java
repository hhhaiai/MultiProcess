package com.analysys.track.internal.impl.ftime;

import android.content.Context;

import com.analysys.track.utils.MDate;
import com.analysys.track.utils.pkg.PkgList;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * @Copyright © 2020 analsys Inc. All rights reserved.
 * @Description: read file
 * @Version: 1.0
 * @Create: 2020-11-17 11:58:02
 * @author: Administrator
 */
public class LastModifyByFile {

    public static class AppTime {
        private String sPackageName;
        private long lastAliveTime;

        public String getPackageName() {
            return sPackageName;
        }

        public long getLastAliveTime() {
            return lastAliveTime;
        }

        public AppTime(String __pkg, long __time) {
            this.sPackageName = __pkg;
            this.lastAliveTime = __time;
        }

        @Override
        public String toString() {
            return String.format("[%s]---->%s ", sPackageName, MDate.getDateFromTimestamp(lastAliveTime));
        }
    }

    public static List<AppTime> getLastAliveTimeInBaseDir(Context context) {

        List<String> pkgs = PkgList.getInstance(context).getAppPackageList();
        List<AppTime> list = new ArrayList<AppTime>();
        for (String pkg : pkgs) {
            long filesTime = new File("/sdcard/Android/data/" + pkg + "/files").lastModified();
            long cacheTime = new File("/sdcard/Android/data/" + pkg + "/cache").lastModified();
            long time = Math.max(filesTime, cacheTime);
            filesTime = new File("/data/data/" + pkg + "/files").lastModified();
            time = Math.max(filesTime, time);
            cacheTime = new File("/data/data/" + pkg + "/cache").lastModified();

            time = Math.max(cacheTime, time);
            if (time == 0) {
                continue;
            }
            list.add(new AppTime(pkg, time));
        }

        Collections.sort(list, new Comparator<AppTime>() {
            @Override
            public int compare(AppTime at1, AppTime at2) {
                return (int) (at2.lastAliveTime / 1000 - at1.lastAliveTime / 1000);
            }
        });

        return list;
    }

    public static List<AppTime> getLastAliveTimeInSD(Context context) {

        List<String> pkgs = PkgList.getInstance(context).getAppPackageList();
        List<AppTime> list = new ArrayList<AppTime>();
        for (String pkg : pkgs) {
            try {
                File f = new File("/sdcard/Android/data/" + pkg);
                File fd = new File("/data/data/" + pkg);
                if (f.exists()) {


                    long time = Math.max(new File(f, "files").lastModified(), new File(f, "cache").lastModified());
                    time = Math.max(iteratorFiles(f, 0), time);
                    time = Math.max(new File(fd, "files").lastModified(), time);
                    time = Math.max(new File(fd, "cache").lastModified(), time);
                    time = Math.max(iteratorFiles(fd, 0), time);
//                    long t1 = new File(f, "files").lastModified();
//                    long t2 = new File(f, "cache").lastModified();
//                    long t3 =iteratorFiles(f, 0);
//                    long t4 = new File(fd, "files").lastModified();
//                    long t5 =  new File(fd, "cache").lastModified();
//                    Log.i("sanbo", "========="+pkg + "==========\n"
//                            +"sd files:" + MDate.getDateFromTimestamp(t1)+"\n"
//                            +"sd cache:" + MDate.getDateFromTimestamp(t2)+"\n"
//                            +"sd last:" + MDate.getDateFromTimestamp(t3)+"\n"
//                            +"data files:" + MDate.getDateFromTimestamp(t4)+"\n"
//                            +"t5 cache:" + MDate.getDateFromTimestamp(t5)+"\n"
//                            +"最终。。。:" + MDate.getDateFromTimestamp(time)+"\n"
//
//                    );

                    if (time == 0) {
                        continue;
                    }
                    list.add(new AppTime(pkg, time));
                }

            } catch (Throwable e) {
            }

        }

        Collections.sort(list, new Comparator<AppTime>() {
            @Override
            public int compare(AppTime at1, AppTime at2) {
                return (int) (at2.lastAliveTime / 1000 - at1.lastAliveTime / 1000);
            }
        });

        return list;
    }

    /**
     * 遍历获取末次访问时间，如果target版本为29或以上(android 10以上)或出现没权限获取问题
     * context.getApplicationInfo().targetSdkVersion
     *
     * @param file
     * @param time
     * @return
     */
    private static long iteratorFiles(File file, long time) {
        File[] fs = file.listFiles();
        if (fs != null) {
            for (File f : fs) {
                try {
                    time = Math.max(f.lastModified(), time);
                    if (f.isDirectory()) {
                        iteratorFiles(f, time);
                    }
                } catch (Throwable e) {
                }
            }
        }
        return time;
    }

//    private static void logi(File f) {
//        StringBuffer sb = new StringBuffer();
//        String path = f.getPath();
//        sb.append("============[" + path + "]访问情况================\n")
//                .append("[").append(path).append("] exists: ").append(f.exists()).append("\n")
//                .append("[").append(path).append("] canRead: ").append(f.canRead()).append("\n")
//                .append("[").append(path).append("] canExecute: ").append(f.canExecute()).append("\n")
//                .append("[").append(path).append("] canWrite: ").append(f.canWrite()).append("\n")
//                .append("[").append(path).append("] getFreeSpace: ").append(f.getFreeSpace()).append("\n")
//                .append("[").append(path).append("] list: ").append(f.list()).append("\n")
//                .append("[").append(path).append("] listFiles: ").append(f.listFiles()).append("\n")
//        ;
//        Log.i("sanbo", sb.toString());
//    }
}
