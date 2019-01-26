package com.analysys.dev.internal.impl;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;

import com.analysys.dev.internal.Content.DeviceKeyContacts;
import com.analysys.dev.internal.Content.EGContext;
import com.analysys.dev.internal.work.CheckHeartbeat;
import com.analysys.dev.model.PolicyInfo;
import com.analysys.dev.utils.ELOG;
import com.analysys.dev.utils.reflectinon.EContextHelper;

import org.json.JSONArray;
import org.json.JSONObject;

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

    private PolicyInfo getNativePolicy(Context context) {
        context = EContextHelper.getContext(context);
        if (null == context) {
            return null;
        }
        if (null == policyLocal) {
            policyLocal = readNativePolicyFromLocal(context);
        }
        return policyLocal;
    }

    private PolicyInfo readNativePolicyFromLocal(Context context) {
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
    private void updateUpLoadUrl(boolean userRTP, boolean userRTL, boolean debug) {
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



    private void saveNewPolicyToLocal(PolicyInfo newPolicy) {
        boolean isRTP = newPolicy.isUseRTP()==0?true:false;
        boolean isRTL = newPolicy.isUseRTL()==0?true:false;
        long timerInterval = newPolicy.getTimerInterval()> 0 ? newPolicy.getTimerInterval()
                : isRTP ? EGContext.TIMER_INTERVAL_DEFALUT : EGContext.TIMER_INTERVAL_DEFALUT_60;
        //storage to local
        Editor editor = getEditor();
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
                .putString(DeviceKeyContacts.Response.RES_POLICY_CTRL_LIST, newPolicy.getCtrlList().toString())
                .apply();
        //refresh local policy
        policyLocal = newPolicy;
        //重置接口
        updateUpLoadUrl(newPolicy.isUseRTP()==0?true:false, policyLocal.isUseRTL()==0?true:false, DeviceImpl.getInstance(mContext).getDebug()=="0"?true:false);
        if (isRTL || isRTP) {
            CheckHeartbeat.getInstance(mContext).sendMessages();
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
    private boolean isNewPolicy(String newPolicyVer) {
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
    private void setDefaultPolicyNative() {
        PolicyInfo policyNative = PolicyInfo.getInstance();
        if(policyNative.getPolicyVer() == null)policyNative.setPolicyVer(EGContext.POLICY_VER_DEFALUT);
        if(policyNative.getServerDelay() == EGContext.DEFAULT)policyNative.setServerDelay(EGContext.SERVER_DELAY_DEFAULT);
        if(policyNative.getFailCount() == EGContext.DEFAULT)policyNative.setFailCount(EGContext.FAIL_COUNT_DEFALUT);
        if(policyNative.getFailTryDelay() == EGContext.DEFAULT)policyNative.setFailTryDelay(EGContext.FAIL_TRY_DELAY_DEFALUT);
        if(policyNative.getTimerInterval() == EGContext.DEFAULT)policyNative.setTimerInterval(EGContext.TIMER_INTERVAL_DEFALUT);
        if(policyNative.getEventCount() == EGContext.DEFAULT)policyNative.setEventCount(EGContext.EVENT_COUNT_DEFALUT);
        if(policyNative.getUseRTP() == EGContext.DEFAULT)policyNative.setUseRTP(EGContext.USER_RTP_DEFALUT);
        if(policyNative.getUploadSD() == EGContext.DEFAULT)policyNative.setUploadSD(EGContext.UPLOAD_SD_DEFALUT);
        if(policyNative.getUseRTL() == EGContext.DEFAULT)policyNative.setUseRTL(EGContext.USER_RTL_DEFAULT);
        if(policyNative.getRemotelp() == EGContext.DEFAULT)policyNative.setRemotelp(EGContext.REMOTE_IP);
        if(policyNative.getMergeInterval() == EGContext.DEFAULT)policyNative.setMergeInterval(EGContext.MERGE_INTERVAL);
        if(policyNative.getMinDuration() == EGContext.DEFAULT)policyNative.setMinDuration(EGContext.MIN_DURATION);
        if(policyNative.getMaxDuration() == EGContext.DEFAULT)policyNative.setMaxDuration(EGContext.MAX_DURATION);
        if(policyNative.getDomainUpdateTimes() == EGContext.DEFAULT)policyNative.setDomainUpdateTimes(EGContext.DOMAIN_UPDATE_TIMES);
        if(policyNative.getCtrlList() == null)policyNative.setCtrlList(null);
    }
    private SharedPreferences getSP(){
        return mContext.getSharedPreferences(EGContext.SP_NAME, Context.MODE_PRIVATE);
    }
    private Editor getEditor(){
        return getSP().edit();
    }
    public void savePermitForFailTimes(long interval) {
        Editor editor = getEditor();
        editor.putLong(EGContext.PERMIT_FOR_FAIL_TIME, interval);
        editor.commit();
    }
    public int getFailCount(){
        return getSP().getInt(DeviceKeyContacts.Response.RES_POLICY_FAILCOUNT,0);
    }
    public void setFailCount(int count){
       getEditor().putInt(DeviceKeyContacts.Response.RES_POLICY_FAILCOUNT,count);
    }
    public void saveRespParams(JSONObject policyObject){
        try {
            PolicyInfo policyInfo = PolicyInfo.getInstance();
            policyInfo.setPolicyVer(policyObject.optString("policyVer"));
            policyInfo.setServerDelay(policyObject.optLong("serverDelay"));
            policyInfo.setFailCount(policyObject.getJSONObject("fail").optInt("failCount"));
            policyInfo.setFailTryDelay(policyObject.getJSONObject("fail").optLong("failTryDelay"));
            policyInfo.setTimerInterval(policyObject.optLong("timerInterval"));
            policyInfo.setEventCount(policyObject.optInt("eventCount"));
            policyInfo.setUseRTP(policyObject.optInt("useRTP"));
            policyInfo.setUseRTL(policyObject.optInt("useRTL"));
            policyInfo.setRemotelp(policyObject.optInt("remoteIp"));
            policyInfo.setUploadSD(policyObject.optInt("uploadSD"));
            policyInfo.setMergeInterval(policyObject.optInt("mergeInterval"));
            policyInfo.setMinDuration(policyObject.optInt("minDuration"));
            policyInfo.setMaxDuration(policyObject.optInt("maxDuration"));
            policyInfo.setDomainUpdateTimes(policyObject.optInt("domainUpdateTimes"));

            JSONArray ctrlList = policyObject.optJSONArray("ctrlList");
            policyInfo.setCtrlList(ctrlList);
//            ResponseCtrlInfo ctrlInfo = null;
//            JSONObject object ,subJsonObj;
//            for(int i = 0;i < ctrlList.length();i++){
//                ctrlInfo = ResponseCtrlInfo.getInstance();
//                object = (JSONObject) ctrlList.get(i);
//                ctrlInfo.setModule(object.optString("module"));
//                ctrlInfo.setStatus(object.optString("status"));
//                ctrlInfo.setDefaultFreq(object.optString("defFreq"));
//                ctrlInfo.setMinFreq(object.optString("minFreq"));
//                ctrlInfo.setMaxFreq(object.optString("maxFreq"));
//                ctrlInfo.setSubControl(object.optJSONObject("subControl"));
//                subJsonObj = object.optJSONObject("subControl");
//                ctrlInfo.setSubModule(subJsonObj.optString("subModule"));
//                ctrlInfo.setSubStatus(subJsonObj.optString("status"));
//                ctrlInfo.setSubDefaultFreq(subJsonObj.optString("defFreq"));
//                ctrlInfo.setSubMinFreq(subJsonObj.optString("minFreq"));
//                ctrlInfo.setSubMaxFreq(subJsonObj.optString("maxFreq"));
//                ctrlInfo.setCount(subJsonObj.optString("count"));
//            }
            setDefaultPolicyNative();
            saveNewPolicyToLocal(policyInfo);

        }catch (Throwable t){
            ELOG.i("saveRespParams() has an exception :::"+t.getMessage());
        }

    }
    public String currentPolicyVer(Context context) {
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


