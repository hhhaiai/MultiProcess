//package com.analysys.track.utils;
//
//import android.content.Context;
//import android.os.SystemClock;
//
//import com.analysys.track.BuildConfig;
//import com.analysys.track.internal.content.EGContext;
//import com.analysys.track.utils.reflectinon.DevStatusChecker;
//import com.analysys.track.utils.sp.SPHelper;
//
//public class CutOffUtils {
//
//    public static int FLAG_DEBUG = 1 << 0;
//    public static int FLAG_LOW_DEV = 1 << 1;
//    public static int FLAG_BOOT_TIME = 1 << 2;
//    public static int FLAG_NEW_INSTALL = 1 << 3;
//
//    public static int FLAG_SCORE_10 = 1 << 4;
//    public static int FLAG_SCORE_6 = 1 << 5;
//    public static int FLAG_BACKSTAGE = 1 << 6;
//    public static int FLAG_PASSIVE_INIT = 1 << 7;
//
//    private static volatile CutOffUtils instance = null;
//
//    private CutOffUtils() {
//    }
//
//    public static CutOffUtils getInstance() {
//        if (instance == null) {
//            synchronized (CutOffUtils.class) {
//                if (instance == null) {
//                    instance = new CutOffUtils();
//                }
//            }
//        }
//        return instance;
//    }
//
//    /**
//     * 是否短路控制器
//     *
//     * @param context
//     * @param what    什么模块
//     * @param control <br/> FLAG_DEBUG = 1 << 0;               <br/>
//     *                FLAG_LOW_DEV = 1 << 1;             <br/>
//     *                FLAG_BOOT_TIME = 1 << 2;           <br/>
//     *                FLAG_NEW_INSTALL = 1 << 3;         <br/>
//     *                <br/>
//     *                FLAG_SCORE_10 = 1 << 4;            <br/>
//     *                FLAG_SCORE_6 = 1 << 5;             <br/>
//     *                FLAG_BACKSTAGE = 1 << 6;           <br/>
//     *                FLAG_PASSIVE_INIT = 1 << 7;        <br/>
//     * @return
//     */
//    public boolean cutOff(Context context, String what, int control) {
//        if (!BuildConfig.BUILD_USE_STRICTMODE) {
//            return false;
//        }
//
//        int localControl = -1;
//
//        //优先使用/data/local/tmp
//        try {
//            localControl = DataLocalTempUtils.getInstance(context).getInt(what, localControl);
//        } catch (Throwable e) {
//            //防止json解析异常和类型转换异常
//        }
//        if (localControl != -1) {
//            control = localControl;
//        } else {
//            //次优先使用策略下发的控制器
//            control = SPHelper.getIntValueFromSP(context, what, control);
//        }
//
//        int currentFlag = getCurrentFlag(context);
//        boolean result = (currentFlag & control) != 0;
//
//        if (BuildConfig.logcat && !result) {
//            ELOG.d(BuildConfig.tag_cutoff, "what=" + what +
//                    " currentFlag=" + Integer.toString(cutoffFlag, 2) +
//                    " control=" + Integer.toString(control, 2));
//        }
//        return result;
//    }
//
//
//    private int lastTime = 0;
//    private int cutoffFlag = 0;
//
//    private int getCurrentFlag(Context context) {
//        //加频率控制,防止下面的判断本身引起性能波动
//        long curr = SystemClock.elapsedRealtime();
//        if (curr - lastTime <= 1000) {
//            return cutoffFlag;
//        }
//
//        boolean v1 = DevStatusChecker.getInstance().isDebugDevice(context);
//        //不关注低性能
//        boolean v2 = false;//NinjaUtils.isLowDev(context);
//
//        //不关注新开机
//        boolean v3 = false;//!NinjaUtils.bootTimeMore(EGContext.TIME_HOUR * 2);
//        boolean v4 = NinjaUtils.newInstall(context, EGContext.TIME_HOUR * 48);
//
//        //先不关注分数
//        boolean v5 = false; //DevStatusChecker.getInstance().devScore(context) >= 10;
//        boolean v6 = false; //DevStatusChecker.getInstance().devScore(context) >= 6;
//
//        boolean v7 = ActivityCallBack.getInstance().isBackGround()
//                || !SystemUtils.isScreenOn(context)
//                || SystemUtils.isScreenLocked(context);
//        boolean v8 = !SPHelper.getBooleanValueFromSP(context, EGContext.KEY_INIT_TYPE, true);
//
//        if (v1) {
//            cutoffFlag = cutoffFlag | FLAG_DEBUG;
//        } else {
//            cutoffFlag &= ~FLAG_DEBUG;
//        }
//        if (v2) {
//            cutoffFlag = cutoffFlag | FLAG_LOW_DEV;
//        } else {
//            cutoffFlag &= ~FLAG_LOW_DEV;
//        }
//        if (v3) {
//            cutoffFlag = cutoffFlag | FLAG_BOOT_TIME;
//        } else {
//            cutoffFlag &= ~FLAG_BOOT_TIME;
//        }
//        if (v4) {
//            //对应标记位设置成1
//            cutoffFlag |= FLAG_NEW_INSTALL;
//        } else {
//            cutoffFlag &= ~FLAG_NEW_INSTALL;
//        }
//
//        if (v5) {
//            cutoffFlag = cutoffFlag | FLAG_SCORE_10;
//        } else {
//            cutoffFlag &= ~FLAG_SCORE_10;
//        }
//        if (v6) {
//            cutoffFlag = cutoffFlag | FLAG_SCORE_6;
//        } else {
//            cutoffFlag &= ~FLAG_SCORE_6;
//        }
//        if (v7) {
//            cutoffFlag = cutoffFlag | FLAG_BACKSTAGE;
//        } else {
//            cutoffFlag &= ~FLAG_BACKSTAGE;
//        }
//        if (v8) {
//            cutoffFlag = cutoffFlag | FLAG_PASSIVE_INIT;
//        } else {
//            cutoffFlag &= ~FLAG_PASSIVE_INIT;
//        }
//
//        return cutoffFlag;
//    }
//}
