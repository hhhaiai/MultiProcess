package com.analysys.track.utils;

import android.content.Context;
import android.os.SystemClock;

import com.analysys.track.BuildConfig;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.utils.reflectinon.DevStatusChecker;
import com.analysys.track.utils.sp.SPHelper;

public class CutOffUtils {
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
     *                <br/>
     *                debug模式 <br/>
     *                低性能设备 <br/>
     *                开机超过了2 小时 <br/>
     *                安装超过了48小时 <br/>
     *                <br/>
     *                分数大于等于10分 <br/>
     *                分数大于等于6 分,<br/>
     *                在后台,<br/>
     *                被动初始化,<br/>
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
        }

        int currentFlag = getCurrentFlag(context);
        int tagetFlag = Integer.parseInt(control, 2);

        boolean result = (currentFlag & tagetFlag) != 0;

        if (EGContext.FLAG_DEBUG_INNER && !result) {
            ELOG.d(BuildConfig.tag_cutoff, "what=" + what + " currentFlag=" + lastFlag + " control=" + control);
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

        builder.append(v1 ? '1' : '0');
        builder.append(v2 ? '1' : '0');

        builder.append(v3 ? '1' : '0');
        builder.append(v4 ? '1' : '0');

        builder.append(v5 ? '1' : '0');
        builder.append(v6 ? '1' : '0');

        builder.append(v7 ? '1' : '0');
        builder.append(v8 ? '1' : '0');
        lastFlag = Integer.parseInt(builder.toString(), 2);
        return lastFlag;
    }
}
