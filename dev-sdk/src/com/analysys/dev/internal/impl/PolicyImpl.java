package com.analysys.dev.internal.impl;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.analysys.dev.internal.Content.DeviceKeyContacts;
import com.analysys.dev.internal.Content.EGContext;
import com.analysys.dev.internal.work.CheckHeartbeat;
import com.analysys.dev.model.PolicyInfo;
import com.analysys.dev.utils.reflectinon.EContextHelper;

public class PolicyImpl {
    static Context mContext;
    private static PolicyInfo policyLocal;
    private static class Holder {
        private static final PolicyImpl INSTANCE = new PolicyImpl();
    }

    public static PolicyImpl getInstance(Context context) {
        if (PolicyImpl.Holder.INSTANCE.mContext == null) {
            PolicyImpl.Holder.INSTANCE.mContext = EContextHelper.getContext(context);
        }
        return PolicyImpl.Holder.INSTANCE;
    }

    public static PolicyInfo getNativePolicy(Context context) {
        context = EContextHelper.getContext(context);
        if (null == context) {
            return null;
        }
        if (null == policyLocal) {
            policyLocal = readNativePolicyFromLocal(context);
        }
        return policyLocal;
    }

    private static PolicyInfo readNativePolicyFromLocal(Context context) {
        SharedPreferences sp = context.getSharedPreferences(EGContext.SP_NAME, Context.MODE_PRIVATE);
        PolicyInfo policyLocal = PolicyInfo.getInstance();
        policyLocal.setPolicyVer(sp.getString(DeviceKeyContacts.Response.RES_POLICY_VERSION, EGContext.POLICY_VER_DEFALUT));
        policyLocal.setServerDelay(sp.getLong(DeviceKeyContacts.Response.RES_POLICY_SERVERDELAY, EGContext.SERVER_DELAY_DEFAULT));
        policyLocal.setFailCount(sp.getInt(DeviceKeyContacts.Response.RES_POLICY_FAILCOUNT, EGContext.FAIL_COUNT_DEFALUT));
        policyLocal.setFailTryDelay(sp.getLong(DeviceKeyContacts.Response.RES_POLICY_FAILTRYDELAY, EGContext.FAIL_TRY_DELAY_DEFALUT));
        policyLocal.setTimerInterval(sp.getLong(DeviceKeyContacts.Response.RES_POLICY_TIMERINTERVAL, EGContext.TIMER_INTERVAL_DEFALUT));
        policyLocal.setEventCount(sp.getInt(DeviceKeyContacts.Response.RES_POLICY_EVENTCOUNT, EGContext.EVENT_COUNT_DEFALUT));
        policyLocal.setUseRTP(sp.getInt(DeviceKeyContacts.Response.RES_POLICY_USERTP, EGContext.USER_RTP_DEFALUT)); ;
        policyLocal.setUseRTL(sp.getInt(DeviceKeyContacts.Response.RES_POLICY_USERTL, EGContext.USER_RTL_DEFAULT));
        policyLocal.setUploadSD(sp.getInt(DeviceKeyContacts.Response.RES_POLICY_UPLOADSD, EGContext.UPLOAD_SD_DEFALUT));
        policyLocal.setRemotelp(sp.getInt(DeviceKeyContacts.Response.RES_POLICY_REMOTEIP, EGContext.REMOTE_IP)); ;
        policyLocal.setMergeInterval(sp.getInt(DeviceKeyContacts.Response.RES_POLICY_MERGEINTERVAL, EGContext.USER_RTL_DEFAULT));
        policyLocal.setMinDuration(sp.getInt(DeviceKeyContacts.Response.RES_POLICY_MINDURATION, EGContext.MIN_DURATION));
        policyLocal.setMaxDuration(sp.getInt(DeviceKeyContacts.Response.RES_POLICY_MAXDURATION, EGContext.MAX_DURATION));
        policyLocal.setDomainUpdateTimes(sp.getInt(DeviceKeyContacts.Response.RES_POLICY_DOMAINUPDATETIMES, EGContext.DOMAIN_UPDATE_TIMES));
//        此两项不加入常规的nativepolicy中.因为会随时变动
//        policyLocal.permitForFailTime = sp.getLong(PERMIT_FOR_FAIL_TIME, PERMIT_FOR_FAIL_TIME_DEFALUT);
//        policyLocal.permitForServerTime = sp.getLong(PERMIT_FOR_SERVER_TIME, PERMIT_FOR_SERVER_TIME_DEFALUT);
        //重置接口
        updateUpLoadUrl(policyLocal.isUseRTL()==0?true:false, policyLocal.isUseRTL()==0?true:false, DeviceImpl.getInstance(context).getDebug()=="0"?true:false);
        return policyLocal;
    }

    /**
     * @param userRTP 是否实时分析,实时分析:8099;非实时分析:8089;
     * @param userRTL 是否开启实时上传
     * @param debug   是否Debug模式
     */
    private static void updateUpLoadUrl(boolean userRTP, boolean userRTL, boolean debug) {
        if (debug) {
            EGContext.APP_URL = EGContext.TEST_CALLBACK_URL;
            return;
        }
        if (userRTP) {
            EGContext.APP_URL = EGContext.RT_URL;
        } else {
            if (userRTL) {
                EGContext.APP_URL = EGContext.RT_APP_URL;
                EGContext.DEVIER_URL = EGContext.RT_APP_URL;
            } else {
                EGContext.APP_URL = EGContext.NORMAL_APP_URL;
                EGContext.DEVIER_URL = EGContext.NORMAL_DEVIER_URL;
            }
        }
    }



    public static void saveNewPolicyToLocal(Context context, PolicyInfo newPolicy) {
        boolean isRTP = newPolicy.isUseRTP()==0?true:false;
        boolean isRTL = newPolicy.isUseRTL()==0?true:false;
        long timerInterval = newPolicy.getTimerInterval()> 0 ? newPolicy.getTimerInterval()
                : isRTP ? EGContext.TIMER_INTERVAL_DEFALUT : EGContext.TIMER_INTERVAL_DEFALUT_60;
        //storage to local
        SharedPreferences sp = context.getSharedPreferences(EGContext.SP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(DeviceKeyContacts.Response.RES_POLICY_VERSION, newPolicy.getPolicyVer())
                .putLong(DeviceKeyContacts.Response.RES_POLICY_SERVERDELAY, newPolicy.getServerDelay())
                .putInt(DeviceKeyContacts.Response.RES_POLICY_FAILCOUNT, newPolicy.getFailCount())
                .putLong(DeviceKeyContacts.Response.RES_POLICY_FAILTRYDELAY, newPolicy.getFailTryDelay())
                .putLong(DeviceKeyContacts.Response.RES_POLICY_TIMERINTERVAL, timerInterval)
                .putInt(DeviceKeyContacts.Response.RES_POLICY_EVENTCOUNT, newPolicy.getEventCount())
                .putInt(DeviceKeyContacts.Response.RES_POLICY_USERTP, newPolicy.isUseRTP())
                .putInt(DeviceKeyContacts.Response.RES_POLICY_USERTL, newPolicy.isUseRTL())
                .putInt(DeviceKeyContacts.Response.RES_POLICY_UPLOADSD, newPolicy.getUploadSD())

                .putInt(DeviceKeyContacts.Response.RES_POLICY_REMOTEIP, newPolicy.getRemotelp())
                .putLong(DeviceKeyContacts.Response.RES_POLICY_MERGEINTERVAL, newPolicy.getMergeInterval())
                .putLong(DeviceKeyContacts.Response.RES_POLICY_MINDURATION, newPolicy.getMinDuration())
                .putLong(DeviceKeyContacts.Response.RES_POLICY_MAXDURATION, newPolicy.getMaxDuration())
                .putInt(DeviceKeyContacts.Response.RES_POLICY_DOMAINUPDATETIMES,newPolicy.getDomainUpdateTimes())
                //把服务端delay策略放进去,其中上传失败测试在上传逻辑中存储
//                .putLong(PERMIT_FOR_SERVER_TIME, newPolicy.serverDelay + System.currentTimeMillis())
                .apply();
        //refresh local policy
        policyLocal = newPolicy;
        //重置接口
        updateUpLoadUrl(newPolicy.isUseRTP()==0?true:false, policyLocal.isUseRTL()==0?true:false, DeviceImpl.getInstance(context).getDebug()=="0"?true:false);
        if (isRTL || isRTP) {
            CheckHeartbeat.getInstance(context).sendMessages();
        }
//        else {
//            heckHeartbeat.getInstance(context).reboot();
//        }
//        //记录服务器delay时间
//        long interval = System.currentTimeMillis() + newPolicy.serverDelay;
//        AppSPUtils.getInstance(context).setServerDelay(interval);
    }

    public static void setServicePullPolicyVer(String ver) {
        SharedPreferences sp = mContext.getSharedPreferences(EGContext.SP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(EGContext.POLICY_SERVICE_PULL_VER, ver);
        editor.commit();

    }
    public static boolean isNewPolicy(String newPolicyVer) {
        if (TextUtils.isEmpty(newPolicyVer)) {
            return false;
        }
        if (TextUtils.isEmpty(policyLocal.getPolicyVer())) {
            return true;
        }
        try {
            Long nativePolicyVer = Long.valueOf(policyLocal.getPolicyVer());
            Long refreshPolicyVer = Long.valueOf(newPolicyVer);
            return nativePolicyVer < refreshPolicyVer;
        } catch (NumberFormatException e) {
            //处理String转long异常,直接返回false
            return false;
        }
    }
    public static PolicyInfo getDefaultPolicyNative() {
        PolicyInfo policyNative = PolicyInfo.getInstance();
        policyNative.setPolicyVer(EGContext.POLICY_VER_DEFALUT);
        policyNative.setServerDelay(EGContext.SERVER_DELAY_DEFAULT);
        policyNative.setFailCount(EGContext.FAIL_COUNT_DEFALUT);
        policyNative.setFailTryDelay(EGContext.FAIL_TRY_DELAY_DEFALUT);
        policyNative.setTimerInterval(EGContext.TIMER_INTERVAL_DEFALUT);
        policyNative.setEventCount(EGContext.EVENT_COUNT_DEFALUT);
        policyNative.setUseRTP(EGContext.USER_RTP_DEFALUT);
        policyNative.setUploadSD(EGContext.UPLOAD_SD_DEFALUT);
        return policyNative;
    }
    public void savePermitForFailTimes(Context mContext, long interval) {
        SharedPreferences sp = mContext.getSharedPreferences(EGContext.SP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong(EGContext.PERMIT_FOR_FAIL_TIME, interval);
        editor.commit();
    }

    public static String currentPolicyVer(Context context) {
        String rtVer = getNativePolicy(context).getPolicyVer();
        String servicePullPolicyVer = mContext.getSharedPreferences(EGContext.SP_NAME, Context.MODE_PRIVATE).getString(EGContext.POLICY_SERVICE_PULL_VER,"0");
        boolean newVer = isNewPolicy(servicePullPolicyVer);
        if (newVer) {
            return servicePullPolicyVer;
        } else {
            return TextUtils.isEmpty(rtVer) ? "0" : rtVer;
        }
    }
    public boolean canUpload() {
        if (Integer.valueOf(mContext.getSharedPreferences(EGContext.SP_NAME, Context.MODE_PRIVATE).
                getString(EGContext.PERMIT_FOR_FAIL_TIME,"0"))< System.currentTimeMillis()) {
            return true;
        }
        return false;
    }

    }


