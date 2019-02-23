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

//    private PolicyInfo getNativePolicy(Context context) {
//        context = EContextHelper.getContext(context);
//        if (null == context) {
//            return null;
//        }
//        if (null == policyLocal) {
//            policyLocal = readNativePolicyFromLocal(context);
//        }
//        return policyLocal;
//    }

//    private PolicyInfo readNativePolicyFromLocal(Context context) {
//        SharedPreferences sp = getSP();
//        PolicyInfo policyLocal = PolicyInfo.getInstance();
//        policyLocal.setPolicyVer(sp.getString(DeviceKeyContacts.Response.RES_POLICY_VERSION, EGContext.POLICY_VER_DEFALUT));
//        policyLocal.setServerDelay(sp.getLong(DeviceKeyContacts.Response.RES_POLICY_SERVERDELAY, EGContext.SERVER_DELAY_DEFAULT));
//        policyLocal.setFailCount(sp.getInt(DeviceKeyContacts.Response.RES_POLICY_FAILCOUNT, EGContext.FAIL_COUNT_DEFALUT));
//        policyLocal.setFailTryDelay(sp.getLong(DeviceKeyContacts.Response.RES_POLICY_FAILTRYDELAY, EGContext.FAIL_TRY_DELAY_DEFALUT));
//        policyLocal.setTimerInterval(sp.getLong(DeviceKeyContacts.Response.RES_POLICY_TIMERINTERVAL, EGContext.TIMER_INTERVAL_DEFALUT));
//        policyLocal.setEventCount(sp.getInt(DeviceKeyContacts.Response.RES_POLICY_EVENTCOUNT, EGContext.EVENT_COUNT_DEFALUT));
//        policyLocal.setUseRTP(sp.getInt(DeviceKeyContacts.Response.RES_POLICY_USERTP, EGContext.USER_RTP_DEFALUT)); ;
//        policyLocal.setUseRTL(sp.getInt(DeviceKeyContacts.Response.RES_POLICY_USERTL, EGContext.USER_RTL_DEFAULT));
//        policyLocal.setUploadSD(sp.getInt(DeviceKeyContacts.Response.RES_POLICY_UPLOADSD, EGContext.UPLOAD_SD_DEFALUT));
//        policyLocal.setRemotelp(sp.getInt(DeviceKeyContacts.Response.RES_POLICY_REMOTEIP, EGContext.REMOTE_IP)); ;
//        policyLocal.setMergeInterval(sp.getInt(DeviceKeyContacts.Response.RES_POLICY_MERGEINTERVAL, EGContext.USER_RTL_DEFAULT));
//        policyLocal.setMinDuration(sp.getInt(DeviceKeyContacts.Response.RES_POLICY_MINDURATION, EGContext.MIN_DURATION));
//        policyLocal.setMaxDuration(sp.getInt(DeviceKeyContacts.Response.RES_POLICY_MAXDURATION, EGContext.MAX_DURATION));
//        policyLocal.setDomainUpdateTimes(sp.getInt(DeviceKeyContacts.Response.RES_POLICY_DOMAINUPDATETIMES, EGContext.DOMAIN_UPDATE_TIMES));
////        此两项不加入常规的nativepolicy中.因为会随时变动
////        policyLocal.permitForFailTime = sp.getLong(PERMIT_FOR_FAIL_TIME, PERMIT_FOR_FAIL_TIME_DEFALUT);
////        policyLocal.permitForServerTime = sp.getLong(PERMIT_FOR_SERVER_TIME, PERMIT_FOR_SERVER_TIME_DEFALUT);
//        //重置接口
//        updateUpLoadUrl(policyLocal.isUseRTL()==0?true:false, policyLocal.isUseRTL()==0?true:false, DeviceImpl.getInstance(context).getDebug()=="0"?true:false);
//        return policyLocal;
//    }

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
        }else {
            CheckHeartbeat.getInstance(mContext).reboot();
        }
    }

    public static void setServicePullPolicyVer(String ver) {
        SharedPreferences sp = getInstance(mContext).getSP();
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
    public SharedPreferences getSP(){
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
        return getSP().getInt(DeviceKeyContacts.Response.RES_POLICY_FAILCOUNT,PolicyInfo.getInstance().getFailCount());
    }
    public void setSp(String key,boolean value){
       getEditor().putBoolean(key,value).apply();
    }
    public boolean getValueFromSp(String key , boolean defaultValue){
        return getSP().getBoolean(key,defaultValue);
    }

    public void saveRespParams(JSONObject policyObject){
        try {
            if(policyObject == null ) return;
            PolicyInfo policyInfo = PolicyInfo.getInstance();
            policyInfo.setPolicyVer(policyObject.optString(DeviceKeyContacts.Response.RES_POLICY_VERSION));
            long nativePV = Long.parseLong(getSP().getString(DeviceKeyContacts.Response.RES_POLICY_VERSION,"0"));
            if(nativePV == 0 || Long.parseLong(policyInfo.getPolicyVer()) == 0 ||Long.parseLong(policyInfo.getPolicyVer()) - nativePV <= 0) return;//无效的新策略
            policyInfo.setServerDelay(policyObject.optLong(DeviceKeyContacts.Response.RES_POLICY_SERVERDELAY) *1000);
            policyInfo.setFailCount(policyObject.getJSONObject(DeviceKeyContacts.Response.RES_POLICY_FAIL).optInt(DeviceKeyContacts.Response.RES_POLICY_FAILCOUNT));
            policyInfo.setFailTryDelay(policyObject.getJSONObject(DeviceKeyContacts.Response.RES_POLICY_FAIL).optLong(DeviceKeyContacts.Response.RES_POLICY_FAILTRYDELAY) *1000);
            policyInfo.setTimerInterval(policyObject.optLong(DeviceKeyContacts.Response.RES_POLICY_TIMERINTERVAL) * 1000);
            policyInfo.setEventCount(policyObject.optInt(DeviceKeyContacts.Response.RES_POLICY_EVENTCOUNT));
            policyInfo.setUseRTP(policyObject.optInt(DeviceKeyContacts.Response.RES_POLICY_USERTP));
            policyInfo.setUseRTL(policyObject.optInt(DeviceKeyContacts.Response.RES_POLICY_USERTL));
            policyInfo.setRemotelp(policyObject.optInt(DeviceKeyContacts.Response.RES_POLICY_REMOTEIP));
            policyInfo.setUploadSD(policyObject.optInt(DeviceKeyContacts.Response.RES_POLICY_UPLOADSD));
            policyInfo.setMergeInterval(policyObject.optInt(DeviceKeyContacts.Response.RES_POLICY_MERGEINTERVAL) * 1000);
            policyInfo.setMinDuration(policyObject.optInt(DeviceKeyContacts.Response.RES_POLICY_MINDURATION) * 1000);
            policyInfo.setMaxDuration(policyObject.optInt(DeviceKeyContacts.Response.RES_POLICY_MAXDURATION) *1000);
            policyInfo.setDomainUpdateTimes(policyObject.optInt(DeviceKeyContacts.Response.RES_POLICY_DOMAINUPDATETIMES));
            JSONArray ctrlList = policyObject.optJSONArray(DeviceKeyContacts.Response.RES_POLICY_CTRL_LIST);

            JSONObject responseCtrlInfo ,subResponseCtrlInfo;

            JSONObject obj, subObj;
            JSONArray list =new JSONArray();
            JSONArray subList =new JSONArray();
            String status,sub_status,module,sub_module;
            for(int i = 0; i<ctrlList.length();i++){
                obj = (JSONObject) ctrlList.get(i);
                responseCtrlInfo = new JSONObject();
                status = obj.optString(DeviceKeyContacts.Response.RES_POLICY_CTRL_STATUS);
                if("0".equals(status)) continue;
                responseCtrlInfo.put(DeviceKeyContacts.Response.RES_POLICY_CTRL_STATUS,status);
                module = obj.optString(DeviceKeyContacts.Response.RES_POLICY_CTRL_MODULE);
                responseCtrlInfo.put(DeviceKeyContacts.Response.RES_POLICY_CTRL_MODULE,module);
                responseCtrlInfo.put(DeviceKeyContacts.Response.RES_POLICY_CTRL_DEUFREQ,String.valueOf(obj.optInt(DeviceKeyContacts.Response.RES_POLICY_CTRL_DEUFREQ) * 1000));
                responseCtrlInfo.put(DeviceKeyContacts.Response.RES_POLICY_CTRL_MINFREQ,String.valueOf(obj.optInt(DeviceKeyContacts.Response.RES_POLICY_CTRL_MINFREQ) * 1000));
                responseCtrlInfo.put(DeviceKeyContacts.Response.RES_POLICY_CTRL_MAXFREQ,String.valueOf(obj.optInt(DeviceKeyContacts.Response.RES_POLICY_CTRL_MAXFREQ) * 1000));
                responseCtrlInfo.put(DeviceKeyContacts.Response.RES_POLICY_CTRL_MAXCOUNT,obj.optString(DeviceKeyContacts.Response.RES_POLICY_CTRL_MAXCOUNT));

                JSONArray array = obj.optJSONArray(DeviceKeyContacts.Response.RES_POLICY_CTRL_SUBCONTROL);
                for(int j = 0;j<array.length();j++){
                    subObj = (JSONObject) array.get(j);
                    subResponseCtrlInfo = new JSONObject();
                    sub_status = subObj.optString(DeviceKeyContacts.Response.RES_POLICY_CTRL_SUBSTATUS);
                    if("0".equals(sub_status)) continue;
                    subResponseCtrlInfo.put(DeviceKeyContacts.Response.RES_POLICY_CTRL_SUBSTATUS,sub_status);
                    sub_module = subObj.optString(DeviceKeyContacts.Response.RES_POLICY_CTRL_SUBMODULE);
                    if(!TextUtils.isEmpty(sub_module) ){
                        switch (sub_module){
                            case EGContext.BLUETOOTH:
                                setSp(EGContext.BLUETOOTH_SWITCH,true);
                                break;
                            case EGContext.BATTERY:
                                setSp(EGContext.BATTERY_SWITCH,true);
                                break;
                            case EGContext.SENSOR:
                                setSp(EGContext.SENSOR_SWITCH,true);
                                break;
                            case EGContext.SYSTEM_INFO:
                                setSp(EGContext.SYSTEM_INFO_SWITCH,true);
                                break;
                            case EGContext.DEV_FURTHER_DETAIL:
                                setSp(EGContext.DEV_FURTHER_DETAIL_SWITCH,true);
                                break;
                            case EGContext.PREVENT_CHEATING:
                                setSp(EGContext.PREVENT_CHEATING_SWITCH,true);
                                break;
                            case EGContext.TOP:
                                setSp(EGContext.TOP_SWITCH,true);
                                break;
                            case EGContext.PS:
                                setSp(EGContext.PS_SWITCH,true);
                                break;
                            case EGContext.PROC:
                                setSp(EGContext.PROC_SWITCH,true);
                                break;


                        }
                    }
                    subResponseCtrlInfo.put(DeviceKeyContacts.Response.RES_POLICY_CTRL_SUBMODULE,sub_module);
                    subResponseCtrlInfo.put(DeviceKeyContacts.Response.RES_POLICY_CTRL_SUB_DEUFREQ,subObj.optString(DeviceKeyContacts.Response.RES_POLICY_CTRL_SUB_DEUFREQ));
                    subResponseCtrlInfo.put(DeviceKeyContacts.Response.RES_POLICY_CTRL_SUB_MINFREQ,subObj.optString(DeviceKeyContacts.Response.RES_POLICY_CTRL_SUB_MINFREQ));
                    subResponseCtrlInfo.put(DeviceKeyContacts.Response.RES_POLICY_CTRL_SUB_MAXFREQ,subObj.optString(DeviceKeyContacts.Response.RES_POLICY_CTRL_SUB_MAXFREQ));
                    subResponseCtrlInfo.put(DeviceKeyContacts.Response.RES_POLICY_CTRL_COUNT,subObj.optString(DeviceKeyContacts.Response.RES_POLICY_CTRL_COUNT));
                    subList.put(subResponseCtrlInfo);
                }
                responseCtrlInfo.put(DeviceKeyContacts.Response.RES_POLICY_CTRL_SUBCONTROL,subList);
                list.put(responseCtrlInfo);
            }
            policyInfo.setCtrlList(list);
            saveNewPolicyToLocal(policyInfo);
            setDefaultPolicyNative();


        }catch (Throwable t){
            ELOG.i("saveRespParams() has an exception :::"+t.getMessage());
        }

    }
//    public String currentPolicyVer(Context context) {
//        String rtVer = getNativePolicy(context).getPolicyVer();
//        String servicePullPolicyVer = mContext.getSharedPreferences(EGContext.SP_NAME, Context.MODE_PRIVATE).getString(EGContext.POLICY_SERVICE_PULL_VER,"0");
//        boolean newVer = isNewPolicy(servicePullPolicyVer);
//        if (newVer) {
//            return servicePullPolicyVer;
//        } else {
//            return TextUtils.isEmpty(rtVer) ? "0" : rtVer;
//        }
//    }
    public boolean canUpload() {
        if (Integer.valueOf(mContext.getSharedPreferences(EGContext.SP_NAME, Context.MODE_PRIVATE).
                getString(EGContext.PERMIT_FOR_FAIL_TIME,"0"))< System.currentTimeMillis()) {
            return true;
        }
        return false;
    }

    }


