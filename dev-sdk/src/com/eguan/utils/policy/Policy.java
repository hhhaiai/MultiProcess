package com.eguan.utils.policy;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.eguan.Constants;
import com.eguan.utils.commonutils.AppSPUtils;
import com.eguan.utils.commonutils.EgLog;

/**
 * Created by chris on 16/11/16.
 */

public class Policy {
    // private static final String TAG = "EGPolicy";

    private static final String SP_NAME = "eg_policy";

    private static final String POLICY_VER = "policyVer";
    private static final String SERVER_DELAY = "serverDelay";
    private static final String FAIL_COUNT = "failCount";
    private static final String FAIL_TRY_DELAY = "failTryDelay";
    private static final String TIMER_INTERVAL = "timerInterval";
    private static final String EVENT_COUNT = "eventCount";
    private static final String USER_RTP = "useRTP";
    private static final String UPLOAD_SD = "uploadSD";
    private static final String PERMIT_FOR_FAIL_TIME = "permitForFailTime";
    private static final String PERMIT_FOR_SERVER_TIME = "permitForServerTime";
    private static final String USER_RTL = "userRTL";

    private static final String POLICY_VER_DEFALUT = "";
    private static final long SERVER_DELAY_DEFAULT = 0l;
    private static final int FAIL_COUNT_DEFALUT = 5;
    private static final long FAIL_TRY_DELAY_DEFALUT = 60 * 60 * 1000;
    private static final int TIMER_INTERVAL_DEFALUT = 5 * 1000;
    private static final int TIMER_INTERVAL_DEFALUT_60 = 60 * 1000;
    private static final int EVENT_COUNT_DEFALUT = 10;
    private static final boolean USER_RTP_DEFALUT = false;
    private static final boolean USER_RTL_DEFAULT = false;
    // private static final boolean USER_RTL_DEFAULT_TRUE = true;
    private static final boolean UPLOAD_SD_DEFALUT = true;
    private static final long PERMIT_FOR_FAIL_TIME_DEFALUT = 0l;
    private static final long PERMIT_FOR_SERVER_TIME_DEFALUT = 0l;

    private String policyVer;
    private long serverDelay;
    private int failCount;
    private long failTryDelay;
    private long timerInterval;
    private int eventCount;
    private boolean userRTP;
    private boolean uploadSD;
    private long permitForFailTime;
    private long permitForServerTime;

    public boolean isUserRTL() {
        return userRTL;
    }

    private boolean userRTL;

    public long getPermitForServerTime() {
        return permitForServerTime;
    }

    public long getPermitForFailTime() {
        return permitForFailTime;
    }


    public String getPolicyVer() {
        return policyVer;
    }

    public boolean isUserRTP() {
        return userRTP;
    }

    private static Policy policyLocal;

    private Policy() {

    }

    private static Policy readNativePolicyFromLocal(Context mContext) {
        SharedPreferences sp = mContext.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        Policy policyLocal = new Policy();
        policyLocal.policyVer = sp.getString(POLICY_VER, POLICY_VER_DEFALUT);
        policyLocal.serverDelay = sp.getLong(SERVER_DELAY, SERVER_DELAY_DEFAULT);
        policyLocal.failCount = sp.getInt(FAIL_COUNT, FAIL_COUNT_DEFALUT);
        policyLocal.failTryDelay = sp.getLong(FAIL_TRY_DELAY, FAIL_TRY_DELAY_DEFALUT);
        policyLocal.timerInterval = sp.getLong(TIMER_INTERVAL, TIMER_INTERVAL_DEFALUT);
        policyLocal.eventCount = sp.getInt(EVENT_COUNT, EVENT_COUNT_DEFALUT);
        policyLocal.userRTP = sp.getBoolean(USER_RTP, USER_RTP_DEFALUT);
        policyLocal.userRTL = sp.getBoolean(USER_RTL, USER_RTL_DEFAULT);
        policyLocal.uploadSD = sp.getBoolean(UPLOAD_SD, UPLOAD_SD_DEFALUT);
        // 此两项不加入常规的nativepolicy中.因为会随时变动
        policyLocal.permitForFailTime = sp.getLong(PERMIT_FOR_FAIL_TIME, PERMIT_FOR_FAIL_TIME_DEFALUT);
        policyLocal.permitForServerTime = sp.getLong(PERMIT_FOR_SERVER_TIME, PERMIT_FOR_SERVER_TIME_DEFALUT);
        // 重置接口
        updateUpLoadUrl(policyLocal.userRTP, policyLocal.userRTL, AppSPUtils.getInstance(mContext).getDebugMode());
        // 打印本地策略
        EgLog.v(policyLocal.toString());
        return policyLocal;
    }

    public Policy(String policyVer, long serverDelay, int failCount, long failTryDelay, long timerInterval,
                  int eventCount, boolean userRTP, boolean uploadSD) {
        this.policyVer = policyVer;
        this.serverDelay = serverDelay;
        this.failCount = failCount;
        this.failTryDelay = failTryDelay;
        this.timerInterval = timerInterval;
        this.eventCount = eventCount;
        this.userRTP = userRTP;
        this.uploadSD = uploadSD;
    }

    public static Policy getNativePolicy(Context context) {
        if (null == context) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.v("getNativePolicy context is null");
            }
            return null;
        }
        if (null == policyLocal) {
            policyLocal = readNativePolicyFromLocal(context);
        }
        if (Constants.FLAG_DEBUG_INNER) {
            EgLog.d("policyLocal : " + policyLocal.toString());
        }

        return policyLocal;
    }

    public static void savePolicyNative(Context context, Policy newPolicy) {
        boolean isRTP = newPolicy.isUserRTP();
        // boolean isRTL = newPolicy.isUserRTL();
        long timerInterval = newPolicy.timerInterval > 0 ? newPolicy.timerInterval
                : isRTP ? TIMER_INTERVAL_DEFALUT : TIMER_INTERVAL_DEFALUT_60;
        // storage to local
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(POLICY_VER, newPolicy.policyVer).putLong(SERVER_DELAY, newPolicy.serverDelay)
                .putInt(FAIL_COUNT, newPolicy.failCount).putLong(FAIL_TRY_DELAY, newPolicy.failTryDelay)
                .putLong(TIMER_INTERVAL, timerInterval).putInt(EVENT_COUNT, newPolicy.eventCount)
                .putBoolean(USER_RTP, newPolicy.userRTP).putBoolean(USER_RTL, newPolicy.userRTL)
                .putBoolean(UPLOAD_SD, newPolicy.uploadSD)
                // 把服务端delay策略放进去,其中上传失败测试在上传逻辑中存储
                .putLong(PERMIT_FOR_SERVER_TIME, newPolicy.serverDelay + System.currentTimeMillis()).apply();
        // refresh local policy
        policyLocal = newPolicy;
        // 重置接口
        updateUpLoadUrl(newPolicy.userRTP, newPolicy.userRTL, AppSPUtils.getInstance(context).getDebugMode());
        // if (isRTL || isRTP) {
        // EguanQueue.getInstance(mContext).checkOrLaunchDaemon();
        // } else {
        // EguanQueue.getInstance(mContext).reboot();
        // }
        // //记录服务器delay时间
        // long interval = System.currentTimeMillis() + newPolicy.serverDelay;
        // AppSPUtils.getInstance(mContext).setServerDelay(interval);
    }

    public static boolean isNewPolicy(String newPolicyVer) {
        if (TextUtils.isEmpty(newPolicyVer))
            return false;
        if (TextUtils.isEmpty(policyLocal.policyVer))
            return true;
        try {
            Long nativePolicyVer = Long.valueOf(policyLocal.policyVer);
            Long refreshPolicyVer = Long.valueOf(newPolicyVer);
            return nativePolicyVer < refreshPolicyVer;
        } catch (Exception e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
            // 处理String转long异常,直接返回false
            return false;
        }
    }

    public static Policy getDefaultPolicyNative() {
        Policy policyNaitve = new Policy();
        policyNaitve.policyVer = POLICY_VER_DEFALUT;
        policyNaitve.serverDelay = SERVER_DELAY_DEFAULT;
        policyNaitve.failCount = FAIL_COUNT_DEFALUT;
        policyNaitve.failTryDelay = FAIL_TRY_DELAY_DEFALUT;
        policyNaitve.timerInterval = TIMER_INTERVAL_DEFALUT;
        policyNaitve.eventCount = EVENT_COUNT_DEFALUT;
        policyNaitve.userRTP = USER_RTP_DEFALUT;
        policyNaitve.uploadSD = UPLOAD_SD_DEFALUT;
        return policyNaitve;
    }

    public static Policy getDefaultRtPolicNative() {
        Policy policyNaitve = new Policy();
        policyNaitve.policyVer = POLICY_VER_DEFALUT;
        policyNaitve.serverDelay = SERVER_DELAY_DEFAULT;
        policyNaitve.failCount = FAIL_COUNT_DEFALUT;
        policyNaitve.failTryDelay = FAIL_TRY_DELAY_DEFALUT;
        policyNaitve.timerInterval = TIMER_INTERVAL_DEFALUT_60;
        policyNaitve.userRTL = USER_RTL_DEFAULT; // 修改后为，默认不走实时上传
        policyNaitve.eventCount = EVENT_COUNT_DEFALUT;
        policyNaitve.userRTP = USER_RTP_DEFALUT; // 实时分析 false
        policyNaitve.uploadSD = UPLOAD_SD_DEFALUT;
        return policyNaitve;
    }

    @Override
    public String toString() {
        return "Policy:\r\n\tpolciyVer:" + policyVer + "\tserverDelay:" + serverDelay + "\tfailCount:" + failCount
                + "\r\n\tfailTryDelay:" + failTryDelay + "\ttimerInterval:" + timerInterval + "\teventCount:"
                + eventCount + "\r\n\tuserRTP:" + userRTP + "\r\n\tuserRTL:" + userRTL + "\tuploadSD:" + uploadSD
                + "\r\n\tpermitForFailTime:" + permitForFailTime + "\tpermitForServerTime:" + permitForServerTime;
    }

    public static void savePermitForFailTime(Context mContext, long interval) {
        SharedPreferences sp = mContext.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong(PERMIT_FOR_FAIL_TIME, interval);
        editor.commit();
        policyLocal.permitForFailTime = interval;
    }

    public static void savePermitForServerTime(Context mContext, long interval) {
        SharedPreferences sp = mContext.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong(PERMIT_FOR_SERVER_TIME, interval);
        editor.commit();
        policyLocal.permitForServerTime = interval;
    }

    /**
     * @param userRTP 是否实时分析,实时分析:8099;非实时分析:8089;
     * @param userRTL 是否开启实时上传
     * @param debug   是否Debug模式
     */
    private static void updateUpLoadUrl(boolean userRTP, boolean userRTL, boolean debug) {
        // if (debug) {
        // Constants.APP_URL = Constants.TEST_CALLBACK_URL;
        // return;
        // }
        // if (userRTP) {
        // Constants.APP_URL = Constants.RT_URL;
        // } else {
        // if (userRTL) {
        // Constants.APP_URL = Constants.RT_APP_URL;
        // Constants.DEVIER_URL = Constants.RT_APP_URL;
        // } else {
        // Constants.APP_URL = Constants.NORMAL_APP_URL;
        // Constants.DEVIER_URL = Constants.NORMAL_DEVIER_URL;
        // }
        // }
        if (userRTL) {
            Constants.DEVIER_URL = Constants.RT_DEVIER_URL;
        } else {
            Constants.DEVIER_URL = Constants.NORMAL_DEVIER_URL;
        }
    }

    public static Policy getDebugPolicyFromLocal(Context mContext) {
        Policy debugPolicy = new Policy();
        debugPolicy.policyVer = mContext.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE).getString(POLICY_VER,
                POLICY_VER_DEFALUT);
        debugPolicy.serverDelay = SERVER_DELAY_DEFAULT;
        debugPolicy.failCount = FAIL_COUNT_DEFALUT;
        debugPolicy.failTryDelay = FAIL_TRY_DELAY_DEFALUT;
        debugPolicy.timerInterval = TIMER_INTERVAL_DEFALUT;
        debugPolicy.eventCount = EVENT_COUNT_DEFALUT;
        debugPolicy.userRTP = true; // Debug模式直接上传
        debugPolicy.uploadSD = UPLOAD_SD_DEFALUT;
        // 此两项不加入常规的nativepolicy中.因为会随时变动
        debugPolicy.permitForFailTime = PERMIT_FOR_FAIL_TIME_DEFALUT;
        debugPolicy.permitForServerTime = PERMIT_FOR_SERVER_TIME_DEFALUT;
        return debugPolicy;
    }

}
