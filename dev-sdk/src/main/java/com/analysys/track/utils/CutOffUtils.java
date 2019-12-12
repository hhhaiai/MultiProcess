package com.analysys.track.utils;

import android.content.Context;
import android.os.SystemClock;

import com.analysys.track.BuildConfig;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.utils.reflectinon.DevStatusChecker;
import com.analysys.track.utils.sp.SPHelper;

public class CutOffUtils {

    public static int FLAG_DEBUG = 1 << 0;
    public static int FLAG_LOW_DEV = 1 << 1;
    public static int FLAG_BOOT_TIME = 1 << 2;
    public static int FLAG_OLD_INSTALL = 1 << 3;

    public static int FLAG_SCORE_10 = 1 << 4;
    public static int FLAG_SCORE_6 = 1 << 5;
    public static int FLAG_BACKSTAGE = 1 << 6;
    public static int FLAG_PASSIVE_INIT = 1 << 7;

    private static volatile CutOffUtils instance = null;

    private CutOffUtils() {
    }

    public static CutOffUtils getInstance() {
        if (instance == null) {
            synchronized (CutOffUtils.class) {
                if (instance == null) {
                    instance = new CutOffUtils();
                }
            }
        }
        return instance;
    }


    /**
     * 是否短路控制器
     *
     * @param context
     * @param what    什么模块
     * @param control 关心啥 关心就在对应的位置传1 默认控制字符串<br/> 全部关心传1111 1111
     * @return
     */
    public boolean cutOff(Context context, String what, String control) {
        if (!BuildConfig.STRICTMODE) {
            return false;
        }
        //优先使用策略下发的控制器
        control = SPHelper.getStringValueFromSP(context, what, control);
        if (control == null) {
            return false;
        }
        control = control.trim();
        if (control.length() != 8 || !control.matches("[01]{8}")) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(new IllegalArgumentException("短路参数不对:" + "what=" + what + " currentFlag=" + lastFlag + " control=" + control));
            }
            if (EGContext.FLAG_DEBUG_INNER) {
                BuglyUtils.commitError(new IllegalArgumentException("短路参数不对:" + "what=" + what + " currentFlag=" + lastFlag + " control=" + control));
            }
            return true;
        }
        return cutOff(context, what, Integer.valueOf(control, 2));
    }


    /**
     * 是否短路控制器
     *
     * @param context
     * @param what    什么模块
     * @param control <br/> FLAG_DEBUG = 1 << 0;               <br/>
     *                FLAG_LOW_DEV = 1 << 1;             <br/>
     *                FLAG_BOOT_TIME = 1 << 2;           <br/>
     *                FLAG_OLD_INSTALL = 1 << 3;         <br/>
     *                <br/>
     *                FLAG_SCORE_10 = 1 << 4;            <br/>
     *                FLAG_SCORE_6 = 1 << 5;             <br/>
     *                FLAG_BACKSTAGE = 1 << 6;           <br/>
     *                FLAG_PASSIVE_INIT = 1 << 7;        <br/>
     * @return
     */
    public boolean cutOff(Context context, String what, int control) {
        if (!BuildConfig.STRICTMODE) {
            return false;
        }

        int currentFlag = getCurrentFlag(context);
        boolean result = (currentFlag & control) != 0;

        if (EGContext.FLAG_DEBUG_INNER && !result) {
            ELOG.d(BuildConfig.tag_cutoff, "what=" + what +
                    " currentFlag=" + Integer.toString(lastFlag, 2) +
                    " control=" + Integer.toString(control, 2));
        }
        return result;
    }


    int lasttime = 0;
    int lastFlag = 0;

    private int getCurrentFlag(Context context) {
        //加频率控制,防止下面的判断本身引起性能波动
        long curr = SystemClock.elapsedRealtime();
        if (curr - lasttime <= 1000) {
            return lastFlag;
        }
        StringBuilder builder = new StringBuilder();

        boolean v1 = DevStatusChecker.getInstance().isDebugDevice(context);
        boolean v2 = NinjaUtils.isLowDev(context);

        boolean v3 = !NinjaUtils.bootTimeMore(EGContext.TIME_HOUR * 2);
        boolean v4 = !NinjaUtils.newInstall(context, EGContext.TIME_HOUR * 48);

        //先不关注分数
        boolean v5 = false; //DevStatusChecker.getInstance().devScore(context) >= 10;
        boolean v6 = false; //DevStatusChecker.getInstance().devScore(context) >= 6;

        boolean v7 = ActivityCallBack.getInstance().isBackGround() || !SystemUtils.isScreenOn(context) || SystemUtils.isScreenLocked(context);
        boolean v8 = !SPHelper.getBooleanValueFromSP(context, EGContext.KEY_INIT_TYPE, true);

        if (v1) {
            lastFlag = lastFlag | FLAG_DEBUG;
        } else {
            lastFlag &= ~FLAG_DEBUG;
        }
        if (v2) {
            lastFlag = lastFlag | FLAG_LOW_DEV;
        } else {
            lastFlag &= ~FLAG_LOW_DEV;
        }
        if (v3) {
            lastFlag = lastFlag | FLAG_BOOT_TIME;
        } else {
            lastFlag &= ~FLAG_BOOT_TIME;
        }
        if (v4) {
            //对应标记位设置成1
            lastFlag |= FLAG_OLD_INSTALL;
        } else {
            lastFlag &= ~FLAG_OLD_INSTALL;
        }

        if (v5) {
            lastFlag = lastFlag | FLAG_SCORE_10;
        } else {
            lastFlag &= ~FLAG_SCORE_10;
        }
        if (v6) {
            lastFlag = lastFlag | FLAG_SCORE_6;
        } else {
            lastFlag &= ~FLAG_SCORE_6;
        }
        if (v7) {
            lastFlag = lastFlag | FLAG_BACKSTAGE;
        } else {
            lastFlag &= ~FLAG_BACKSTAGE;
        }
        if (v8) {
            lastFlag = lastFlag | FLAG_PASSIVE_INIT;
        } else {
            lastFlag &= ~FLAG_PASSIVE_INIT;
        }

        return lastFlag;
    }
}
