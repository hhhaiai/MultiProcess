//package com.analysys.track.utils;
//
//import android.app.ActivityManager;
//import android.content.Context;
//import android.os.SystemClock;
//
//import com.analysys.track.internal.content.EGContext;
//import com.analysys.track.utils.sp.SPHelper;
//
//import java.io.File;
//
///**
// * @Copyright 2019 analysys Inc. All rights reserved.
// * @Description: 低内存不工作的判断
// * @Version: 1.0
// * @Create: 2019-11-30 17:48:36
// * @author: miqt
// * @mail: miqingtang@analysys.com.cn
// */
//public class NinjaUtils {
//
//    /**
//     * 是否首次安装过去了 time 小时
//     *
//     * @param context
//     * @return
//     */
//    public static boolean newInstall(Context context, long diffTime) {
//        try {
//            if (diffTime <= 0) {
//                return false;
//            }
//            checkOldFile(context);
//            long time = SPHelper.getLongValueFromSP(context, EGContext.SP_INSTALL_TIME, -1);
//            if (time == -1) {
//                SPHelper.setLongValue2SP(context, EGContext.SP_INSTALL_TIME, System.currentTimeMillis());
//                return true;
//            } else if ((System.currentTimeMillis() - time) <= diffTime) {
//                return true;
//            }
//        } catch (Throwable e) {
//        }
//        return false;
//    }
//
//    private static void checkOldFile(Context context) {
//        try {
//            if (SPHelper.getLongValueFromSP(context, EGContext.SP_INSTALL_TIME, -1) != -1) {
//                return;
//            }
//
//            String[] oldSPfiles = new String[]{
//                    "ana_sp_xml_v2.xml",
//                    "ana_sp_xml_v2.sp",
//                    "ana_sp_xml.xml",
//                    "ana_sp_xml.sp",
//                    "sputil.xml",
//                    "sputil.sp",
//            };
//            String[] oldSQLfiles = new String[]{
//                    "ev2.data",
//                    "e.data",
//                    "deanag.data",
//            };
//
//            Long creatTime = null;
//            for (String str : oldSPfiles) {
//                File file1 = new File(context.getCacheDir().getParent() + "/shared_prefs/", str);
//                File file2 = new File(context.getCacheDir().getParent(), str);
//                creatTime = getCreateTime(context, creatTime, file1);
//                creatTime = getCreateTime(context, creatTime, file2);
//            }
//            for (String str : oldSQLfiles) {
//                File file = context.getDatabasePath(str);
//                creatTime = getCreateTime(context, creatTime, file);
//            }
//            if (creatTime == null) {
//                creatTime = (long) -1;
//            }
//            SPHelper.setLongValue2SP(context, EGContext.SP_INSTALL_TIME, creatTime);
//        } catch (Throwable e) {
//        }
//    }
//
//    private static Long getCreateTime(Context context, Long creatTime, File file1) {
//        if (file1.exists() && file1.isFile()) {
//            long cTime = FileUitls.getInstance(context).getCreateTime(file1);
//            if (cTime == 0) {
//                cTime = file1.lastModified();
//            }
//            if (creatTime == null) {
//                creatTime = cTime;
//            } else {
//                creatTime = Math.min(cTime, creatTime);
//            }
//        }
//        return creatTime;
//    }
//
//    /**
//     * 是否开机后过去了 difftime 时间
//     *
//     * @param diffTime
//     * @return
//     */
//    public static boolean bootTimeMore(long diffTime) {
//        long time = SystemClock.elapsedRealtime();
//        return time >= diffTime;
//    }
//
//    public static Boolean isLowDev;
//
//    /**
//     * 判断是否是低内存
//     *
//     * @param context
//     * @return
//     */
//    public static boolean isLowDev(Context context) {
//        try {
//            if (isLowDev != null) {
//                if (!isLowDev) {
//                    isLowDev = isLowMemoryUse(context);
//                }
//            } else {
//                isLowDev = isLowMemoryUse(context) || isLowDevice(context);
//            }
//            return isLowDev;
//        } catch (Throwable e) {
//        }
//        return false;
//    }
//
//    private static boolean isLowMemoryUse(Context context) {
//        try {
//            ActivityManager inst = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
//            float u = 0;
//            if (inst != null) {
//                ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
//                try {
//                    inst.getMemoryInfo(memoryInfo);
//                    return memoryInfo.lowMemory;
//                } catch (Throwable e) {
//                    //异常后用使用比例判断
//                }
//                //后台进程大于这个内存值就被回收
//                long maxBack = memoryInfo.threshold;
//                if (maxBack <= 0) {
//                    //除零异常
//                    return false;
//                }
//                //当前使用了
//                long currUsed = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
//                u = currUsed * 1.0F / maxBack * 100;
//            } else {
//                //最大可用内存
//                long maxCanUsed = Runtime.getRuntime().maxMemory();
//                if (maxCanUsed <= 0) {
//                    //除零异常
//                    return false;
//                }
//                //当前使用了
//                long currUsed = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
//                u = currUsed * 1.0F / maxCanUsed * 100;
//            }
//            //使用率大于60算低内存
//            return u >= 80;
//        } catch (Throwable e) {
//        }
//        return false;
//    }
//
//    /**
//     * | RAM | condition | Year Class |
//     * |----:|----------:|-----------:|
//     * |768MB| 1 core    | 2009       |
//     * |     | 2+ cores  | 2010       |
//     * |  1GB| <1.3GHz   | 2011       |  <---low device
//     * |     | 1.3GHz+   | 2012       |
//     * |1.5GB| <1.8GHz   | 2012       |
//     * |     | 1.8GHz+   | 2013       |
//     * |  2GB|           | 2013       |
//     * |  3GB|           | 2014       |
//     * |  5GB|           | 2015       |
//     * | more|           | 2016       |
//     */
//    private static boolean isLowDevice(Context context) {
//        try {
//            if (context == null) {
//                return false;
//            }
//            float num = DeviceInfo.getCPUMaxFreqKHz();
//            float hz = num / 1024F / 1024;
//
//            float cores = DeviceInfo.getNumberOfCPUCores();
//
//            num = DeviceInfo.getTotalMemory(context);
//            float memory = num / 1024F / 1024 / 1024;
//
//            if (memory == DeviceInfo.DEVICEINFO_UNKNOWN
//                    || hz == DeviceInfo.DEVICEINFO_UNKNOWN
//                    || cores == DeviceInfo.DEVICEINFO_UNKNOWN) {
//                return false;
//            }
//
//            if (memory <= 1.1 && hz <= 1.4) {
//                return true;
//            }
//            if (cores == 1) {
//                return true;
//            }
//        } catch (Throwable e) {
//        }
//
//        return false;
//    }
//
//}
