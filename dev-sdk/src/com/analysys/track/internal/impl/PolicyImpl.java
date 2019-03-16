package com.analysys.track.internal.impl;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;

import com.analysys.track.internal.work.CheckHeartbeat;
import com.analysys.track.utils.reflectinon.EContextHelper;
import com.analysys.track.internal.Content.DeviceKeyContacts;
import com.analysys.track.internal.Content.EGContext;
import com.analysys.track.model.PolicyInfo;

import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.sp.SPHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Array;

public class PolicyImpl {
    static Context mContext;
//    private static PolicyInfo policyLocal;

    private static class Holder {
        private static final PolicyImpl INSTANCE = new PolicyImpl();
    }

    public static PolicyImpl getInstance(Context context) {
        if (PolicyImpl.Holder.INSTANCE.mContext == null) {
            PolicyImpl.Holder.INSTANCE.mContext = EContextHelper.getContext(context);
        }
        return PolicyImpl.Holder.INSTANCE;
    }

    /**
     * @param userRTP 是否实时分析,实时分析:8099;非实时分析:8089;
     * @param userRTL 是否开启实时上传
     * @param debug 是否Debug模式
     */
    private void updateUpLoadUrl(boolean userRTP, boolean userRTL, boolean debug) {
        if (debug) {
            EGContext.APP_URL = EGContext.TEST_CALLBACK_URL;
            getEditor().putString(EGContext.APP_URL_SP,EGContext.APP_URL);
            return;
        }
        if (userRTP) {
            if (userRTL) {
                EGContext.APP_URL = EGContext.RT_URL;
                getEditor().putString(EGContext.APP_URL_SP,EGContext.APP_URL);
            }else {
                setNormalUploadUrl(mContext);
                EGContext.APP_URL = EGContext.NORMAL_APP_URL;
                getEditor().putString(EGContext.APP_URL_SP,EGContext.APP_URL);
            }
        } else {
            setNormalUploadUrl(mContext);
            EGContext.APP_URL = EGContext.NORMAL_APP_URL;
            getEditor().putString(EGContext.APP_URL_SP,EGContext.APP_URL);
        }
    }

    private void saveNewPolicyToLocal(PolicyInfo newPolicy) {
        boolean isRTP = newPolicy.isUseRTP() == 0 ? true : false;//是否使用实时策略，0使用，1不使用
        boolean isRTL = newPolicy.isUseRTL() == 1 ? true : false;//是否实时上传，1实时，0不实时
        long timerInterval =
            newPolicy.getTimerInterval() > 0 ? newPolicy.getTimerInterval() : isRTP && isRTL
                ? EGContext.TIMER_INTERVAL_DEFALUT : EGContext.UPLOAD_CYCLE;
        // storage to local
        Editor editor = getEditor();
        editor
            .putString(DeviceKeyContacts.Response.RES_POLICY_VERSION,
                newPolicy.getPolicyVer())
            .putLong(DeviceKeyContacts.Response.RES_POLICY_SERVER_DELAY,
                newPolicy.getServerDelay())
            .putInt(DeviceKeyContacts.Response.RES_POLICY_FAIL_COUNT,
                newPolicy.getFailCount())
            .putLong(DeviceKeyContacts.Response.RES_POLICY_FAIL_TRY_DELAY,
                newPolicy.getFailTryDelay())
            .putLong(DeviceKeyContacts.Response.RES_POLICY_TIMER_INTERVAL, timerInterval)
            .putInt(DeviceKeyContacts.Response.RES_POLICY_USE_RTP, newPolicy.isUseRTP())
            .putInt(DeviceKeyContacts.Response.RES_POLICY_USE_RTL, newPolicy.isUseRTL())
            .putString(DeviceKeyContacts.Response.RES_POLICY_CTRL_LIST,
                newPolicy.getCtrlList().toString())
            .apply();
        // 重置url
        updateUpLoadUrl(newPolicy.isUseRTP() == 0 ? true : false,
                newPolicy.isUseRTL() == 0 ? true : false,
            DeviceImpl.getInstance(mContext).getDebug() == "0" ? true : false);

    }

    public SharedPreferences getSP() {
        return mContext.getSharedPreferences(EGContext.SP_NAME, Context.MODE_PRIVATE);
    }

    private Editor getEditor() {
        return getSP().edit();
    }

    public void setSp(String key, boolean value) {
        getEditor().putBoolean(key, value).apply();
    }

    public boolean getValueFromSp(String key, boolean defaultValue) {
        return getSP().getBoolean(key, defaultValue);
    }

    public void saveRespParams(JSONObject policyObject) {
        try {
            if (policyObject == null)
                return;
            PolicyInfo policyInfo = PolicyInfo.getInstance();
            String policy_version = policyObject.optString(DeviceKeyContacts.Response.RES_POLICY_VERSION);
            policyInfo.setPolicyVer(policy_version);//策略版本
            if(!isNewPolicy(policy_version)){
                return;
            }
            //有效策略处理
            //时间统一返回值类型为秒
            policyInfo.setServerDelay(
                policyObject.optInt(DeviceKeyContacts.Response.RES_POLICY_SERVER_DELAY) * 1000);// 服务器延迟上传时间
            policyInfo.setFailCount(
                policyObject.getJSONObject(DeviceKeyContacts.Response.RES_POLICY_FAIL)
                    .optInt(DeviceKeyContacts.Response.RES_POLICY_FAIL_COUNT));
            policyInfo.setFailTryDelay(
                policyObject.getJSONObject(DeviceKeyContacts.Response.RES_POLICY_FAIL)
                    .optLong(DeviceKeyContacts.Response.RES_POLICY_FAIL_TRY_DELAY) * 1000);
            policyInfo.setTimerInterval(
                policyObject.optLong(DeviceKeyContacts.Response.RES_POLICY_TIMER_INTERVAL)
                    * 1000);
            policyInfo.setUseRTP(
                policyObject.optInt(DeviceKeyContacts.Response.RES_POLICY_USE_RTP));
            policyInfo.setUseRTL(
                policyObject.optInt(DeviceKeyContacts.Response.RES_POLICY_USE_RTL));
            JSONArray ctrlList = policyObject
                .optJSONArray(DeviceKeyContacts.Response.RES_POLICY_CTRL_LIST);
            //子模块控制---某个info控制
            JSONObject responseCtrlInfo, subResponseCtrlInfo;
            JSONObject obj, subObj;
            JSONArray list = new JSONArray();
            JSONArray subList = new JSONArray();
            String status, sub_status, module, sub_module;
            for (int i = 0; i < ctrlList.length(); i++) {
                obj = (JSONObject)ctrlList.get(i);
                responseCtrlInfo = new JSONObject();
                status = obj.optString(DeviceKeyContacts.Response.RES_POLICY_CTRL_STATUS);
                module = obj.optString(DeviceKeyContacts.Response.RES_POLICY_CTRL_MODULE);
                int deuFreq = obj.optInt(DeviceKeyContacts.Response.RES_POLICY_CTRL_DEUFREQ) * 1000;
                int minFreq = obj.optInt(DeviceKeyContacts.Response.RES_POLICY_CTRL_MIN_FREQ) * 1000;
                int maxFreq = obj.optInt(DeviceKeyContacts.Response.RES_POLICY_CTRL_MAX_FREQ)* 1000;
                Object tempObj = obj.opt(DeviceKeyContacts.Response.RES_POLICY_CTRL_UNWANTED);
                //获取不到的时候，重试次数,忽略
                String maxCount = obj.optString(DeviceKeyContacts.Response.RES_POLICY_CTRL_MAX_COUNT);
                if(!TextUtils.isEmpty(module)){
                    /**
                     * 某个模块，某个字段不要
                     */
                    String[]  unWanted = null;
                    if(tempObj.getClass().isArray()){
                        int length = Array.getLength(tempObj);
                        unWanted = new String[length];
                        for (int k = 0; k < unWanted.length; k++) {
                            unWanted[k] = (String) Array.get(obj, k);
                        }
                    }
                    if(unWanted != null){
                        for(String key:unWanted){
                            setSp(key,false);
                        }
                    }
                    if(EGContext.MODULE_OCCOUNT.equals(module)){
                        if ("0".equals(status)){//0不收集，跳过
                            setSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_OC, false);
                            continue;
                        }else {//1收集,默认值即为轮询的值，忽略最小最大
                            if(deuFreq != 0){
                                getEditor().putString(EGContext.SP_OC_CYCLE, String.valueOf(deuFreq));
                            }

                        }

                    }else if(EGContext.MODULE_LOCATION.equals(module)){

                        if ("0".equals(status)){//0不收集，跳过
                            setSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_LOCATION, false);
                            continue;
                        }else {//1收集,默认值即为轮询的值，忽略最小最大
                            if(deuFreq != 0){
                                getEditor().putString(EGContext.SP_LOCATION_CYCLE, String.valueOf(deuFreq));
                            }
                        }
                    }else if(EGContext.MODULE_SNAPSHOT.equals(module)){

                        if ("0".equals(status)){//0不收集，跳过
                            setSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_SNAPSHOT, false);
                            continue;
                        }else {//1收集,默认值即为轮询的值，忽略最小最大
                            if(deuFreq != 0){
                                getEditor().putString(EGContext.SP_SNAPSHOT_CYCLE, String.valueOf(deuFreq));
                            }
                        }
                    }else if(EGContext.MODULE_WIFI.equals(module)){
                        if ("0".equals(status)){//0不收集，跳过
                            setSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_WIFI, false);
                            continue;
                        }//1收集,默认值即为轮询的值，忽略最小最大,WIFI不轮询
                    }else if(EGContext.MODULE_BASE.equals(module)){

                        if ("0".equals(status)){//0不收集，跳过
                            setSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_BASE,false);
                            continue;
                        }//1收集,默认值即为轮询的值，忽略最小最大,基站不轮询
                    }else if(EGContext.MODULE_DEV.equals(module)){
                        if ("0".equals(status)){//0不收集，跳过
                            setSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_DEV,false);
                            continue;
                        }//1收集,默认值即为轮询的值，忽略最小最大,基本信息不轮询，发送时候现收集
                    }

                    responseCtrlInfo.put(DeviceKeyContacts.Response.RES_POLICY_CTRL_STATUS,
                            status);
                    responseCtrlInfo.put(DeviceKeyContacts.Response.RES_POLICY_CTRL_MODULE,
                            module);
                    responseCtrlInfo.put(DeviceKeyContacts.Response.RES_POLICY_CTRL_DEUFREQ,deuFreq);
                    responseCtrlInfo.put(DeviceKeyContacts.Response.RES_POLICY_CTRL_MIN_FREQ,minFreq);
                    responseCtrlInfo.put(DeviceKeyContacts.Response.RES_POLICY_CTRL_MAX_FREQ, maxFreq);
                    responseCtrlInfo.put(DeviceKeyContacts.Response.RES_POLICY_CTRL_MAX_COUNT,maxCount);

                    JSONArray array = obj
                            .optJSONArray(DeviceKeyContacts.Response.RES_POLICY_CTRL_SUB_CONTROL);
                    for (int j = 0; j < array.length(); j++) {
                        subObj = (JSONObject)array.get(j);
                        subResponseCtrlInfo = new JSONObject();
                        sub_status = subObj
                                .optString(DeviceKeyContacts.Response.RES_POLICY_CTRL_SUB_STATUS);
                        if ("0".equals(sub_status)){//0不收集
                            continue;
                        }
                        subResponseCtrlInfo.put(
                                DeviceKeyContacts.Response.RES_POLICY_CTRL_SUB_STATUS, sub_status);
                        sub_module = subObj
                                .optString(DeviceKeyContacts.Response.RES_POLICY_CTRL_SUB_MODULE);

                        if (!TextUtils.isEmpty(sub_module)) {
                            String subDeufreq = subObj.optString(DeviceKeyContacts.Response.RES_POLICY_CTRL_SUB_DEUFREQ);
                            String subMinFreq = subObj.optString(DeviceKeyContacts.Response.RES_POLICY_CTRL_SUB_MIN_FREQ);
                            String subMaxFreq = subObj.optString(DeviceKeyContacts.Response.RES_POLICY_CTRL_SUB_MAX_FREQ);
                            String subCount = subObj.optString(DeviceKeyContacts.Response.RES_POLICY_CTRL_COUNT);
                            if (EGContext.BLUETOOTH.equals(sub_module)) {
                                setSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_BLUETOOTH, true);
                            } else if (EGContext.BATTERY.equals(sub_module)) {
                                setSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_BATTERY, true);
                            } else if (EGContext.SENSOR.equals(sub_module)) {
                                setSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_SENSOR, true);
                            } else if (EGContext.SYSTEM_INFO.equals(sub_module)) {
                                setSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_KEEP_INFO, true);
                            } else if (EGContext.DEV_FURTHER_DETAIL.equals(sub_module)) {
                                setSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_MORE_INFO, true);
                            } else if (EGContext.PREVENT_CHEATING.equals(sub_module)) {
                                setSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_DEV_CHECK, true);
                            } else if (EGContext.TOP.equals(sub_module)) {
                                setSp(DeviceKeyContacts.Response.RES_POLICY_CL_MODULE_TOP, true);
                            } else if (EGContext.PS.equals(sub_module)) {
                                setSp(DeviceKeyContacts.Response.RES_POLICY_CL_MODULE_PS, true);
                            } else if (EGContext.PROC.equals(sub_module)) {
                                setSp(DeviceKeyContacts.Response.RES_POLICY_CL_MODULE_PROC, true);
                            }
                            subResponseCtrlInfo.put(DeviceKeyContacts.Response.RES_POLICY_CTRL_SUB_MODULE, sub_module);
                            subResponseCtrlInfo.put(DeviceKeyContacts.Response.RES_POLICY_CTRL_SUB_DEUFREQ,subDeufreq);
                            subResponseCtrlInfo.put(DeviceKeyContacts.Response.RES_POLICY_CTRL_SUB_MIN_FREQ, subMinFreq);
                            subResponseCtrlInfo.put(DeviceKeyContacts.Response.RES_POLICY_CTRL_SUB_MAX_FREQ,subMaxFreq);
                            subResponseCtrlInfo.put(DeviceKeyContacts.Response.RES_POLICY_CTRL_COUNT, subCount);
                        }
                        subList.put(subResponseCtrlInfo);

                    }
                    responseCtrlInfo.put(DeviceKeyContacts.Response.RES_POLICY_CTRL_SUB_CONTROL, subList);
                    list.put(responseCtrlInfo);
                }
                }

            if(list == null || list.length()<1){
                policyInfo.setCtrlList(null);
            }else {
                policyInfo.setCtrlList(list);
            }
            saveNewPolicyToLocal(policyInfo);

        } catch (Throwable t) {
            ELOG.i("saveRespParams() has an exception :::" + t.getMessage());
        }

    }

    private boolean isNewPolicy(String newPolicyVer) {
        try {
            if (TextUtils.isEmpty(newPolicyVer)) {
                return false;
            }
            String nativePV =
                getSP().getString(DeviceKeyContacts.Response.RES_POLICY_VERSION, "0");
            if (TextUtils.isEmpty(nativePV)) {
                return true;
            }
            Long nativePolicyVer = Long.valueOf(nativePV);
            Long refreshPolicyVer = Long.valueOf(newPolicyVer);
            return nativePolicyVer < refreshPolicyVer;
        } catch (Throwable e) {
            // 处理String转long异常,直接返回false
            return false;
        }
    }
    private void setNormalUploadUrl(Context context) {
        int sum = 0;
        String key = SPHelper.getDefault(context).getString(EGContext.SP_APP_KEY,"");
        for (int i = 0; i <key.length(); i++) {
            sum = sum + key.charAt(i);
        }
        int index = sum % 10;
        EGContext.NORMAL_APP_URL = EGContext.URL_SCHEME + EGContext.NORMAL_UPLOAD_URL[index] + EGContext.ORI_PORT;
    }
}
