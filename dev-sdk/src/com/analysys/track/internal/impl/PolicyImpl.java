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

    /**
     * @param userRTP 是否实时分析,实时分析:8099;非实时分析:8089;
     * @param userRTL 是否开启实时上传
     * @param debug 是否Debug模式
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
        boolean isRTP = newPolicy.isUseRTP() == 0 ? true : false;
        boolean isRTL = newPolicy.isUseRTL() == 0 ? true : false;
        long timerInterval =
            newPolicy.getTimerInterval() > 0 ? newPolicy.getTimerInterval() : isRTP
                ? EGContext.TIMER_INTERVAL_DEFALUT : EGContext.TIMER_INTERVAL_DEFALUT_60;
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
        // refresh local policy
        policyLocal = newPolicy;
        // 重置接口
        updateUpLoadUrl(newPolicy.isUseRTP() == 0 ? true : false,
            policyLocal.isUseRTL() == 0 ? true : false,
            DeviceImpl.getInstance(mContext).getDebug() == "0" ? true : false);
        if (isRTL || isRTP) {
            CheckHeartbeat.getInstance(mContext).sendMessages();
        } else {
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
            // 处理String转long异常,直接返回false
            return false;
        }
    }

    private void setDefaultPolicyNative() {
        PolicyInfo policyNative = PolicyInfo.getInstance();
        if (policyNative.getPolicyVer() == null)
            policyNative.setPolicyVer(EGContext.POLICY_VER_DEFALUT);
        if (policyNative.getServerDelay() == EGContext.DEFAULT)
            policyNative.setServerDelay(EGContext.SERVER_DELAY_DEFAULT);
        if (policyNative.getFailCount() == EGContext.DEFAULT)
            policyNative.setFailCount(EGContext.FAIL_COUNT_DEFALUT);
        if (policyNative.getFailTryDelay() == EGContext.DEFAULT)
            policyNative.setFailTryDelay(EGContext.FAIL_TRY_DELAY_DEFALUT);
        if (policyNative.getTimerInterval() == EGContext.DEFAULT)
            policyNative.setTimerInterval(EGContext.TIMER_INTERVAL_DEFALUT);
        if (policyNative.getUseRTP() == EGContext.DEFAULT)
            policyNative.setUseRTP(EGContext.USER_RTP_DEFALUT);
        if (policyNative.getUseRTL() == EGContext.DEFAULT)
            policyNative.setUseRTL(EGContext.USER_RTL_DEFAULT);

        if (policyNative.getCtrlList() == null)
            policyNative.setCtrlList(null);
    }

    public SharedPreferences getSP() {
        return mContext.getSharedPreferences(EGContext.SP_NAME, Context.MODE_PRIVATE);
    }

    private Editor getEditor() {
        return getSP().edit();
    }

    public void savePermitForFailTimes(long interval) {
        Editor editor = getEditor();
        editor.putLong(EGContext.PERMIT_FOR_FAIL_TIME, interval);
        editor.commit();
    }

    public int getFailCount() {
        return getSP().getInt(DeviceKeyContacts.Response.RES_POLICY_FAIL_COUNT,
            PolicyInfo.getInstance().getFailCount());
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
            policyInfo.setPolicyVer(
                policyObject.optString(DeviceKeyContacts.Response.RES_POLICY_VERSION));//策略版本
            long nativePV = Long.parseLong(
                getSP().getString(DeviceKeyContacts.Response.RES_POLICY_VERSION, "0"));
            if (nativePV == 0 || Long.parseLong(policyInfo.getPolicyVer()) == 0
                || Long.parseLong(policyInfo.getPolicyVer()) - nativePV <= 0) {
                return;// 无效的新策略
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

            JSONObject responseCtrlInfo, subResponseCtrlInfo;
            JSONObject obj, subObj;
            JSONArray list = new JSONArray();
            JSONArray subList = new JSONArray();
            String status, sub_status, module, sub_module;
            for (int i = 0; i < ctrlList.length(); i++) {
                obj = (JSONObject)ctrlList.get(i);
                responseCtrlInfo = new JSONObject();
                status = obj.optString(DeviceKeyContacts.Response.RES_POLICY_CTRL_STATUS);
                if ("0".equals(status)){
                    continue;
                }
                responseCtrlInfo.put(DeviceKeyContacts.Response.RES_POLICY_CTRL_STATUS,
                    status);
                module = obj.optString(DeviceKeyContacts.Response.RES_POLICY_CTRL_MODULE);
                responseCtrlInfo.put(DeviceKeyContacts.Response.RES_POLICY_CTRL_MODULE,
                    module);
                responseCtrlInfo.put(DeviceKeyContacts.Response.RES_POLICY_CTRL_DEUFREQ,
                    String.valueOf(
                        obj.optInt(DeviceKeyContacts.Response.RES_POLICY_CTRL_DEUFREQ)
                            * 1000));
                responseCtrlInfo.put(DeviceKeyContacts.Response.RES_POLICY_CTRL_MIN_FREQ,
                    String.valueOf(
                        obj.optInt(DeviceKeyContacts.Response.RES_POLICY_CTRL_MIN_FREQ)
                            * 1000));
                responseCtrlInfo.put(DeviceKeyContacts.Response.RES_POLICY_CTRL_MAX_FREQ,
                    String.valueOf(
                        obj.optInt(DeviceKeyContacts.Response.RES_POLICY_CTRL_MAX_FREQ)
                            * 1000));
                responseCtrlInfo.put(DeviceKeyContacts.Response.RES_POLICY_CTRL_MAX_COUNT,
                    obj.optString(DeviceKeyContacts.Response.RES_POLICY_CTRL_MAX_COUNT));

                JSONArray array = obj
                    .optJSONArray(DeviceKeyContacts.Response.RES_POLICY_CTRL_SUB_CONTROL);
                for (int j = 0; j < array.length(); j++) {
                    subObj = (JSONObject)array.get(j);
                    subResponseCtrlInfo = new JSONObject();
                    sub_status = subObj
                        .optString(DeviceKeyContacts.Response.RES_POLICY_CTRL_SUB_STATUS);
                    if ("0".equals(sub_status))
                        continue;
                    subResponseCtrlInfo.put(
                        DeviceKeyContacts.Response.RES_POLICY_CTRL_SUB_STATUS, sub_status);
                    sub_module = subObj
                        .optString(DeviceKeyContacts.Response.RES_POLICY_CTRL_SUB_MODULE);
                    if (!TextUtils.isEmpty(sub_module)) {
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
                    }
                    subResponseCtrlInfo.put(
                        DeviceKeyContacts.Response.RES_POLICY_CTRL_SUB_MODULE, sub_module);
                    subResponseCtrlInfo.put(
                        DeviceKeyContacts.Response.RES_POLICY_CTRL_SUB_DEUFREQ,
                        subObj.optString(
                            DeviceKeyContacts.Response.RES_POLICY_CTRL_SUB_DEUFREQ));
                    subResponseCtrlInfo.put(
                        DeviceKeyContacts.Response.RES_POLICY_CTRL_SUB_MIN_FREQ,
                        subObj.optString(
                            DeviceKeyContacts.Response.RES_POLICY_CTRL_SUB_MIN_FREQ));
                    subResponseCtrlInfo.put(
                        DeviceKeyContacts.Response.RES_POLICY_CTRL_SUB_MAX_FREQ,
                        subObj.optString(
                            DeviceKeyContacts.Response.RES_POLICY_CTRL_SUB_MAX_FREQ));
                    subResponseCtrlInfo
                        .put(DeviceKeyContacts.Response.RES_POLICY_CTRL_COUNT, subObj
                            .optString(DeviceKeyContacts.Response.RES_POLICY_CTRL_COUNT));
                    subList.put(subResponseCtrlInfo);
                }
                responseCtrlInfo
                    .put(DeviceKeyContacts.Response.RES_POLICY_CTRL_SUB_CONTROL, subList);
                list.put(responseCtrlInfo);
            }
            policyInfo.setCtrlList(list);
            saveNewPolicyToLocal(policyInfo);
            setDefaultPolicyNative();

        } catch (Throwable t) {
            ELOG.i("saveRespParams() has an exception :::" + t.getMessage());
        }

    }
}
