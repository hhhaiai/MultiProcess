package com.analysys.track.internal.impl.ftime;

import android.content.Context;

import com.analysys.track.BuildConfig;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.MDate;
import com.analysys.track.utils.pkg.PkgList;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * @Copyright © 2020 analsys Inc. All rights reserved.
 * @Description: last modify by file utils
 * @Version: 1.0
 * @Create: 2020-11-17 11:58:02
 * @author: Administrator
 */
public class LmFileUitls {


    public static class AppTime {
        private String sPackageName;
        private long lastActiveTime;

        public String getPackageName() {
            return sPackageName;
        }

        public long getLastActiveTime() {
            return lastActiveTime;
        }

        public AppTime(String __pkg, long __time) {
            this.sPackageName = __pkg;
            this.lastActiveTime = __time;
        }

        @Override
        public String toString() {
            return String.format("[%s]---->%s ", sPackageName, MDate.getDateFromTimestamp(lastActiveTime));
        }
    }

    /**
     * 通过包名获取活跃时间
     *
     * @param context       上下文
     * @param isIteratorDir 是否遍历内层文件夹。PS:targetSdkVersion>28,无权限遍历
     * @return
     */
    public static List<AppTime> getLastAliveTimeByPkgName(Context context, boolean isIteratorDir) {
        List<AppTime> list = new ArrayList<AppTime>();
        for (String pkg : PkgList.getInstance(context).getAppPackageList()) {
            try {
                File f = new File("/sdcard/Android/data/" + pkg);
                File fd = new File("/data/data/" + pkg);
                long time = getTime(new File(f, "files"));
                time = Math.max(time, getTime(new File(f, "cache")));
                time = Math.max(time, getTime(new File(f, "MicroMsg")));
                time = Math.max(getTime(new File(fd, "files")), time);
                time = Math.max(getTime(new File(fd, "cache")), time);
                if (isIteratorDir) {
                    time = Math.max(getDirsRealActiveTime(f, false), time);
                    time = Math.max(getDirsRealActiveTime(fd, false), time);
                }

                if (time == 0) {
                    continue;
                }
                list.add(new AppTime(pkg, time));
            } catch (Throwable e) {
                if (BuildConfig.logcat) {
                    ELOG.i(BuildConfig.tag_finfo, e);
                }
            }

        }

        Collections.sort(list, new Comparator<AppTime>() {
            @Override
            public int compare(AppTime at1, AppTime at2) {
                return (int) (at2.lastActiveTime / 1000 - at1.lastActiveTime / 1000);
            }
        });

        return list;
    }

    private static long getTime(File file) {
        if (file == null || !file.exists()) {
            return 0;
        }
        return file.lastModified();
    }

    private static long newDirTime = 0;
    private static long newFileTime = 0;


    public static long getDirsRealActiveTime(File file, boolean isLog) {
        newDirTime = 0;
        newFileTime = 0;
        iteratorFiles(file, isLog);
        return Math.max(newDirTime, newFileTime);

    }

    /**
     * 遍历获取末次访问时间，如果target版本为29或以上(android 10以上)或出现没权限获取问题
     * context.getApplicationInfo().targetSdkVersion
     *
     * @param file
     * @param isLog
     * @return
     */
    private static void iteratorFiles(File file, boolean isLog) {
        File[] fs = file.listFiles();
        if (fs != null) {
            for (File f : fs) {
                try {
                    long t = getTime(f);

                    if (f.isDirectory()) {
                        setTime(t, true);
                        iteratorFiles(f, isLog);
                    } else {
                        setTime(t, false);
                    }
//                    // 支持宏编译,不打印日志可直接隐藏
//                    if (BuildConfig.logcat) {
//                        if (isLog) {
//                            long now=System.currentTimeMillis();
//                            Log.d("sanbo", "[" + f.getAbsolutePath()
//                                    + "] \n\t本次[" + t + "]:" + MDate.getDateFromTimestamp(t)
//                                    + "\n\t文件夹[" + newDirTime + "]:" + MDate.getDateFromTimestamp(newDirTime)
//                                    + "\n\t文件[" + newFileTime + "]:" + MDate.getDateFromTimestamp(newFileTime)
//                                    + "\n\t现在[" + now + "]:" + MDate.getDateFromTimestamp(now)
//                            );
//                        }
//                    }
                } catch (Throwable e) {
                    if (BuildConfig.logcat) {
                        ELOG.i(BuildConfig.tag_finfo, e);
                    }
                }
            }
        }
    }

    /**
     * 文件时间戳赋值到内存中
     *
     * @param fileTimestamp
     * @param isDir
     */
    private static void setTime(long fileTimestamp, boolean isDir) {
        // 出现部分时间戳超过现在的。如淘宝的部分隐藏文件
        if (System.currentTimeMillis() < fileTimestamp) {
            return;
        }
        if (isDir) {
            newDirTime = Math.max(newDirTime, fileTimestamp);
        } else {
            newFileTime = Math.max(newFileTime, fileTimestamp);
        }
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
