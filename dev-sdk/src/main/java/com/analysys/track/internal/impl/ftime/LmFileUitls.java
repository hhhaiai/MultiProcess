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

    private static int ITERATOR_LAYER_COUNT = 5;

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
        String sdPre = "/sdcard/Android/data/%s";
        String dataPre = "/data/data/%s";
        for (String pkg : PkgList.getInstance(context).getAppPackageList()) {
            try {
//                backOldMethod(isIteratorDir, list, pkg);
//                Log.i("sanbo", "------开始获取[" + pkg + "]-------");
                String f = String.format(sdPre, pkg);
                String fd = String.format(dataPre, pkg);
                long time = getTime(
                        new File(f, "files")
                        , new File(f, "cache")
                        , new File(f, "MicroMsg")
                        , new File(fd, "files")
                        , new File(fd, "cache")
                );
                if (isIteratorDir) {
                    time = getTime(time
                            , getDirsRealActiveTime(f)
                            , getDirsRealActiveTime(fd)
                    );
                }

                if (time == 0) {
//                    Log.d("sanbo", "===================获取[" + pkg + "]，结果：" + time + "==========>" + MDate.getDateFromTimestamp(time));
                    continue;
                }
//                Log.i("sanbo", "===================获取[" + pkg + "]，结果：" + time + "=========>" + MDate.getDateFromTimestamp(time));
                list.add(new AppTime(pkg, time));
            } catch (Throwable e) {
                if (BuildConfig.logcat) {
                    ELOG.i(BuildConfig.tag_finfo, e);
                }
//                Log.e("sanbo", Log.getStackTraceString(e));
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
        long time = file.lastModified();

        //如果时间是未来的，则返回无效时间
        if (time > System.currentTimeMillis()) {
            return 0;
        } else {
            return time;
        }
    }

    private static long getTime(long... times) {
        if (times.length > 0) {
            long lastestTime = 0;
            for (long time : times) {
                lastestTime = Math.max(time, lastestTime);
            }
            return lastestTime;
        }
        return 0;
    }

    private static long getTime(File... files) {
        if (files.length > 0) {
            long lastestTime = 0;
            for (File file : files) {
                long temTime = getTime(file);
                lastestTime = Math.max(temTime, lastestTime);
            }
            return lastestTime;
        }
        return 0;
    }

    private static long newDirTime = 0;
    private static long newFileTime = 0;

    private static long getDirsRealActiveTime(String fileName) {
        return getDirsRealActiveTime(new File(fileName), false);
    }

    public static long getDirsRealActiveTime(File file, boolean isLog) {
        zero();
        iteratorFiles(file, isLog, ITERATOR_LAYER_COUNT);
        long result = Math.max(newDirTime, newFileTime);
        zero();
        return result;
    }

    private static void zero() {
        newDirTime = 0;
        newFileTime = 0;
    }

    /**
     * 遍历获取末次访问时间，如果target版本为29或以上(android 10以上)或出现没权限获取问题
     * context.getApplicationInfo().targetSdkVersion
     *
     * @param file
     * @param isLog
     * @param layerCount
     * @return
     */
    private static void iteratorFiles(File file, boolean isLog, int layerCount) {
        File[] fs = file.listFiles();
        if (fs != null) {
            int count = 0;
            for (File f : fs) {
                try {
                    long t = getTime(f);

                    if (f.isDirectory()) {
                        if (count <= layerCount) {
                            count += 1;
                            setTime(t, true);
                            iteratorFiles(f, isLog, layerCount);
                        }
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
//
//    private static void backOldMethod(boolean isIteratorDir, List<AppTime> list, String pkg) {
//        File f = new File("/sdcard/Android/data/" + pkg);
//        File fd = new File("/data/data/" + pkg);
//        long time = getTime(new File(f, "files"));
//        time = Math.max(time, getTime(new File(f, "cache")));
//        time = Math.max(time, getTime(new File(f, "MicroMsg")));
//        time = Math.max(getTime(new File(fd, "files")), time);
//        time = Math.max(getTime(new File(fd, "cache")), time);
//        if (isIteratorDir) {
//            time = Math.max(getDirsRealActiveTime(f, false), time);
//            time = Math.max(getDirsRealActiveTime(fd, false), time);
//        }
//
//        if (time == 0) {
//            return;
//        }
//        list.add(new AppTime(pkg, time));
//    }

}
