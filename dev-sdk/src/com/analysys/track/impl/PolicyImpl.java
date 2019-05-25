package com.analysys.track.impl;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;

import com.analysys.track.impl.proc.ProcUtils;
import com.analysys.track.utils.JsonUtils;
import com.analysys.track.utils.reflectinon.EContextHelper;
import com.analysys.track.internal.Content.DeviceKeyContacts;
import com.analysys.track.internal.Content.EGContext;
import com.analysys.track.model.PolicyInfo;

import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.sp.SPHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;
import java.util.Set;

public class PolicyImpl {
    static Context mContext;
//    private static PolicyInfo policyLocal;
    private SharedPreferences sp = null;
    private PolicyImpl(){
    }
    private static class Holder {
        private static final PolicyImpl INSTANCE = new PolicyImpl();
    }

    public static PolicyImpl getInstance(Context context) {
        if (mContext == null) {
            mContext = EContextHelper.getContext(context);
        }
        return PolicyImpl.Holder.INSTANCE;
    }

    /**
     * @param debug 是否Debug模式
     */
    public void updateUpLoadUrl(boolean debug) {
        if (debug) {
            EGContext.APP_URL = EGContext.TEST_URL;
//            getEditor().putString(EGContext.APP_URL_SP,EGContext.APP_URL);
            return;
        }else {
            setNormalUploadUrl(mContext);
            EGContext.APP_URL = EGContext.NORMAL_APP_URL;
//            getEditor().putString(EGContext.APP_URL_SP,EGContext.APP_URL);
        }
    }

    private void saveNewPolicyToLocal(PolicyInfo newPolicy) {
        long timerInterval =
            newPolicy.getTimerInterval() > 0 ? newPolicy.getTimerInterval(): EGContext.UPLOAD_CYCLE;
        // storage to local
        Editor editor = getEditor();
        editor
            .putString(DeviceKeyContacts.Response.RES_POLICY_VERSION,
                newPolicy.getPolicyVer())
            .putInt(DeviceKeyContacts.Response.RES_POLICY_SERVER_DELAY,
                newPolicy.getServerDelay())
            .putInt(DeviceKeyContacts.Response.RES_POLICY_FAIL_COUNT,
                newPolicy.getFailCount())
            .putLong(DeviceKeyContacts.Response.RES_POLICY_FAIL_TRY_DELAY,
                newPolicy.getFailTryDelay())
            .putLong(DeviceKeyContacts.Response.RES_POLICY_TIMER_INTERVAL, timerInterval)
            .putString(DeviceKeyContacts.Response.RES_POLICY_CTRL_LIST,
                    newPolicy.getCtrlList()== null ? "": String.valueOf(newPolicy.getCtrlList()))
            .apply();

    }

    public SharedPreferences getSP() {
        if(sp == null){
            sp = mContext.getSharedPreferences(EGContext.SP_NAME, Context.MODE_PRIVATE);
        }
        return sp;
    }

    private Editor getEditor() {
        return getSP().edit();
    }
    private void clearSP(){
        Editor editor =  getEditor();
        editor.clear();
        editor.commit();
    }
    public void setSp(String key, boolean value) {
        getEditor().putBoolean(key, value).apply();
    }

    public boolean getValueFromSp(String key, boolean defaultValue) {
        return getSP().getBoolean(key, defaultValue);
    }

    public void saveRespParams(JSONObject policyObject) {
        try {
            if (policyObject == null){
                return;
            }
            PolicyInfo policyInfo = PolicyInfo.getInstance();
            String policy_version = policyObject.optString(DeviceKeyContacts.Response.RES_POLICY_VERSION);
            if(!isNewPolicy(policy_version)){
                return;
            }
            clearSP();
            policyInfo.setPolicyVer(policy_version);//策略版本
            try {
                //有效策略处理
                //时间统一返回值类型为秒
                policyInfo.setServerDelay(
                        policyObject.optInt(DeviceKeyContacts.Response.RES_POLICY_SERVER_DELAY) * 1000);// 服务器延迟上传时间
                policyInfo.setFailCount(
                        policyObject.getJSONObject(DeviceKeyContacts.Response.RES_POLICY_FAIL)
                                .optInt(DeviceKeyContacts.Response.RES_POLICY_FAIL_COUNT)); // 上传最大失败次数
                policyInfo.setFailTryDelay(
                        policyObject.getJSONObject(DeviceKeyContacts.Response.RES_POLICY_FAIL)
                                .optLong(DeviceKeyContacts.Response.RES_POLICY_FAIL_TRY_DELAY) * 1000); // 上传失败后延迟时间
                policyInfo.setTimerInterval(
                        policyObject.optLong(DeviceKeyContacts.Response.RES_POLICY_TIMER_INTERVAL)
                                * 1000);// 客户端上传时间间隔
                JSONArray ctrlList = policyObject
                        .optJSONArray(DeviceKeyContacts.Response.RES_POLICY_CTRL_LIST);//动态采集模块
                //模块控制---某个info控制
                JSONObject responseCtrlInfo;
                JSONObject obj;
                JSONArray list = new JSONArray();
                JSONArray subList = new JSONArray();
                String status, module;
                int deuFreq;
                Object tempObj;
                if(ctrlList == null || ctrlList.length() <1 ){
                    return;
                }
                for (int i = 0; i < ctrlList.length(); i++) {
                    obj = (JSONObject)ctrlList.get(i);
                    responseCtrlInfo = new JSONObject();
                    status = obj.optString(DeviceKeyContacts.Response.RES_POLICY_CTRL_STATUS);
                    module = obj.optString(DeviceKeyContacts.Response.RES_POLICY_CTRL_MODULE);
                    deuFreq = obj.optInt(DeviceKeyContacts.Response.RES_POLICY_CTRL_DEUFREQ) * 1000;
                    tempObj = obj.opt(DeviceKeyContacts.Response.RES_POLICY_CTRL_UNWANTED);
                    JSONArray array = null;
                    if(!TextUtils.isEmpty(module)){
                        /**
                         * 某个模块，某个字段不要
                         */
                        if(tempObj != null){
                            unWantedKeysHandle(tempObj.toString());
                        }

                        if(EGContext.MODULE_OC.equals(module)){
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
//                                setSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_LAC_LIST,false);
//                                setSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_CID_LIST,false);
//                                setSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_RSRP_LIST,false);
//                                setSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_ECIO_LIST,false);
                                continue;
                            }//1收集,默认值即为轮询的值，忽略最小最大,基站不轮询
                        }else if(EGContext.MODULE_DEV.equals(module)){
                            if ("0".equals(status)){//0不收集，跳过
                                setSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_DEV,false);
                                continue;
                            }//1收集,默认值即为轮询的值，忽略最小最大,基本信息不轮询，发送时候现收集
                            array = obj
                                    .optJSONArray(DeviceKeyContacts.Response.RES_POLICY_CTRL_SUB_CONTROL);
                            subModuleHandle(array,subList,"dev");
                        }else if(EGContext.MODULE_XXX.equals(module)){
                            if ("0".equals(status)){//0不收集，跳过
                                setSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_XXX, false);
                                continue;
                            }
                            array = obj
                                    .optJSONArray(DeviceKeyContacts.Response.RES_POLICY_CTRL_SUB_CONTROL);
                            subModuleHandle(array,subList,"xxx");
                        }

                        responseCtrlInfo.put(DeviceKeyContacts.Response.RES_POLICY_CTRL_STATUS,
                                status);
                        responseCtrlInfo.put(DeviceKeyContacts.Response.RES_POLICY_CTRL_MODULE,
                                module);
                        responseCtrlInfo.put(DeviceKeyContacts.Response.RES_POLICY_CTRL_DEUFREQ,deuFreq);
                        if(subList != null && subList.length()> 0 ){
                            responseCtrlInfo.put(DeviceKeyContacts.Response.RES_POLICY_CTRL_SUB_CONTROL, subList);
                        }
                        list.put(responseCtrlInfo);
                    }
                }

                if(list == null || list.length()<1){
                    policyInfo.setCtrlList(null);
                }else {
                    policyInfo.setCtrlList(list);
                }
            }catch (Throwable t){

            }
            saveNewPolicyToLocal(policyInfo);

        } catch (Throwable t) {
            if(EGContext.FLAG_DEBUG_INNER){
                ELOG.e(t);
            }
        }

    }
    private void unWantedKeysHandle(String tempObj){
        Set<String> unWanted = null;
        if(tempObj != null && tempObj.length()>0){
            unWanted = JsonUtils.transferStringArray2Set(tempObj);
            if(unWanted != null && unWanted.size() > 0){
                for(String key:unWanted){
                    if(!TextUtils.isEmpty(key)){
//                        ELOG.i("policyInfo","key is :::"+key);
                        setSp(key,false);
                    }
                }
            }
        }

    }
    private void subModuleHandle(JSONArray array,JSONArray subList,String tag)throws JSONException {
        JSONObject subResponseCtrlInfo;
        JSONObject subObj;
        Object sub_unWanted;
        String sub_status, sub_module;
        String unCollected = "0";
        if(array != null && array.length()> 0){
            for (int j = 0; j < array.length(); j++) {
                subObj = (JSONObject)array.get(j);
                subResponseCtrlInfo = new JSONObject();
                sub_status = subObj
                        .optString(DeviceKeyContacts.Response.RES_POLICY_CTRL_STATUS);
//                if ("0".equals(sub_status)){//0不收集
//                    continue;
//                }
                subResponseCtrlInfo.put(
                        DeviceKeyContacts.Response.RES_POLICY_CTRL_SUB_STATUS, sub_status);
                sub_module = subObj
                        .optString(DeviceKeyContacts.Response.RES_POLICY_CTRL_SUB_MODULE);
                subResponseCtrlInfo.put(
                        DeviceKeyContacts.Response.RES_POLICY_CTRL_SUB_MODULE, sub_module);
                sub_unWanted = subObj
                        .optString(DeviceKeyContacts.Response.RES_POLICY_CTRL_UNWANTED);
                subResponseCtrlInfo.put(
                        DeviceKeyContacts.Response.RES_POLICY_CTRL_SUB_UNWANTED, sub_unWanted);
                if (!TextUtils.isEmpty(sub_module)) {
                    if(sub_unWanted != null){
                        unWantedKeysHandle(sub_unWanted.toString());
                    }
                    if("dev".equals(tag)){
                        if (EGContext.BLUETOOTH.equals(sub_module)) {
                            if (unCollected.equals(sub_status)){//0不收集，跳过
                                setSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_BLUETOOTH, false);
                                continue;
                            }
                        } else if (EGContext.BATTERY.equals(sub_module)) {
                            if (unCollected.equals(sub_status)) {//0不收集，跳过
                                setSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_BATTERY, false);
                                continue;
                            }
                        } else if (EGContext.SENSOR.equals(sub_module)) {
                            if (unCollected.equals(sub_status)) {//0不收集，跳过
                                setSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_SENSOR, false);
                                continue;
                            }
                        } else if (EGContext.SYSTEM_INFO.equals(sub_module)) {
                            if (unCollected.equals(sub_status)) {//0不收集，跳过
                                setSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_KEEP_INFO, false);
                                continue;
                            }
                        } else if (EGContext.DEV_FURTHER_DETAIL.equals(sub_module)) {
                            if (unCollected.equals(sub_status)) {//0不收集，跳过
                                setSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_MORE_INFO, false);
                                continue;
                            }
                        } else if (EGContext.PREVENT_CHEATING.equals(sub_module)) {
                            if (unCollected.equals(sub_status)) {//0不收集，跳过
                                setSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_DEV_CHECK, false);
                                continue;
                            }
                        }
                    }else if("xxx".equals(tag)){
                        if (EGContext.PROC.equals(sub_module)) {
                            if (unCollected.equals(sub_status)) {//0不收集，跳过
                                setSp(ProcUtils.RUNNING_RESULT, false);
                                continue;
                            }
                        }else if(EGContext.XXX_TIME.equals(sub_module)) {
                            if (unCollected.equals(sub_status)) {//0不收集，跳过
                                setSp(ProcUtils.RUNNING_TIME, false);
                                continue;
                            }
                        }else if(EGContext.OCR.equals(sub_module)) {
                            if (unCollected.equals(sub_status)) {//0不收集，跳过
                                setSp(ProcUtils.RUNNING_OC_RESULT, false);
                                continue;
                            }
                        }
                    }
//                    subResponseCtrlInfo.put(DeviceKeyContacts.Response.RES_POLICY_CTRL_SUB_MODULE, sub_module);
                }
                if(subResponseCtrlInfo != null && subResponseCtrlInfo.length() > 0){
                    if(subList == null){
                        subList = new JSONArray();
                    }
                    subList.put(subResponseCtrlInfo);
                }
            }
        }
    }
    private boolean isNewPolicy(String newPolicyVer) {
        try {
            if (TextUtils.isEmpty(newPolicyVer)) {
                return false;
            }
            String nativePV =
                getSP().getString(DeviceKeyContacts.Response.RES_POLICY_VERSION, "");
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
        String key = SPHelper.getStringValueFromSP(context,EGContext.SP_APP_KEY,"");
        int index = 0;
        //不为空则用appkey,为空，则随机取值
        if(!TextUtils.isEmpty(key)){
            for (int i = 0; i <key.length(); i++) {
                sum = sum + key.charAt(i);
            }
            index = sum % 10;
        }else {
            index = new Random().nextInt(10);
        }

        EGContext.NORMAL_APP_URL = EGContext.URL_SCHEME + EGContext.NORMAL_UPLOAD_URL[index] + EGContext.ORI_PORT;
    }
}
