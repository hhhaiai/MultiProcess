package com.analysys.track.impl;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;
import android.util.Log;

import com.analysys.track.impl.proc.ProcUtils;
import com.analysys.track.internal.Content.DeviceKeyContacts;
import com.analysys.track.internal.Content.EGContext;
import com.analysys.track.model.PolicyInfo;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.JsonUtils;
import com.analysys.track.utils.L;
import com.analysys.track.utils.Memory2File;
import com.analysys.track.utils.PatchHelper;
import com.analysys.track.utils.reflectinon.EContextHelper;
import com.analysys.track.utils.sp.SPHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Random;
import java.util.Set;

public class PolicyImpl {
    static Context mContext;
    // private static PolicyInfo policyLocal;
    private SharedPreferences mSP = null;

    private PolicyImpl() {
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
        } else {
            setNormalUploadUrl(mContext);
            EGContext.APP_URL = EGContext.NORMAL_APP_URL;
//            getEditor().putString(EGContext.APP_URL_SP,EGContext.APP_URL);
        }
    }

    /**
     * 保存策略到本地
     *
     * @param newPolicy
     */
    private void saveNewPolicyToLocal(PolicyInfo newPolicy) {
        // 策略保存。
        long timerInterval = newPolicy.getTimerInterval() > 0 ? newPolicy.getTimerInterval() : EGContext.UPLOAD_CYCLE;
        getEditor().putString(DeviceKeyContacts.Response.RES_POLICY_VERSION, newPolicy.getPolicyVer())
                .putInt(DeviceKeyContacts.Response.RES_POLICY_SERVER_DELAY, newPolicy.getServerDelay())
                .putInt(DeviceKeyContacts.Response.RES_POLICY_FAIL_COUNT, newPolicy.getFailCount())
                .putLong(DeviceKeyContacts.Response.RES_POLICY_FAIL_TRY_DELAY, newPolicy.getFailTryDelay())
                .putLong(DeviceKeyContacts.Response.RES_POLICY_TIMER_INTERVAL, timerInterval)
                .putString(DeviceKeyContacts.Response.RES_POLICY_CTRL_LIST,
                        newPolicy.getCtrlList() == null ? "" : String.valueOf(newPolicy.getCtrlList()))
                // 热更部分保存: 现在保存sign、version
                .putString(DeviceKeyContacts.Response.HotFixResp.HOTFIX_RESP_PATCH_SIGN, newPolicy.getHotfixSign())
                .putString(DeviceKeyContacts.Response.HotFixResp.HOTFIX_RESP_PATCH_VERSION, newPolicy.getHotfixVersion())

                .commit();
        // 热更新部分直接缓存成文件
        if (!TextUtils.isEmpty(newPolicy.getHotfixData())) {
            try {
                File file = new File(mContext.getFilesDir(), newPolicy.getHotfixVersion() + ".jar");
                Memory2File.savePatch(newPolicy.getHotfixData(), file);
                // lunch
                if (file.exists()) {
//                    PatchHelper.load(mContext, file, "A", "pringLog");

                    Class[] cs = new Class[]{String.class};
                    PatchHelper.load(mContext, file, "A", "Q", new Class[]{String.class}, new Object[]{"输入源"});
                }

            } catch (Throwable e) {
                L.info(mContext, Log.getStackTraceString(e));
                // 出现失败则不继续进行
                return;
            }
        }
    }

    public SharedPreferences getSP() {
        if (mSP == null) {
            mSP = mContext.getSharedPreferences(EGContext.SP_NAME, Context.MODE_PRIVATE);
        }
        return mSP;
    }

    private Editor getEditor() {
        return getSP().edit();
    }

    public void clearSP() {
        Editor editor = getEditor();
        editor.clear();
        editor.commit();
    }

    public void setSp(String key, boolean value) {
        getEditor().putBoolean(key, value).apply();
    }

    public boolean getValueFromSp(String key, boolean defaultValue) {
        return getSP().getBoolean(key, defaultValue);
    }

    /**
     * 策略解析并保存。(服务器返回时间单位为秒)
     *
     * @param serverPolicy
     */
    public void saveRespParams(JSONObject serverPolicy) {
        try {
            if (serverPolicy == null || serverPolicy.length() <= 0) {
                return;
            }
            L.info(mContext, "-----------1----------");
            /**
             * 没有策略版本号直接放弃处理
             */
            if (!serverPolicy.has(DeviceKeyContacts.Response.RES_POLICY_VERSION)) {
                return;
            }
            L.info(mContext, "-----------2----------");

            PolicyInfo policyInfo = PolicyInfo.getInstance();
            String policy_version = serverPolicy.optString(DeviceKeyContacts.Response.RES_POLICY_VERSION);
            if (!isNewPolicy(policy_version)) {
                L.info(mContext, " not new version policy, will return");
                return;
            }
            L.info(mContext, "----------3----------");

            clearSP();
            policyInfo.setPolicyVer(policy_version);// 策略版本

            L.info(mContext, "----------4----------");

            if (serverPolicy.has(DeviceKeyContacts.Response.RES_POLICY_SERVER_DELAY)) {
                policyInfo
                        .setServerDelay(serverPolicy.optInt(DeviceKeyContacts.Response.RES_POLICY_SERVER_DELAY) * 1000);
            }
            L.info(mContext, "----------5---------");

            /**
             * 失败策略处理
             */
            if (serverPolicy.has(DeviceKeyContacts.Response.RES_POLICY_FAIL)) {

                JSONObject fail = serverPolicy.getJSONObject(DeviceKeyContacts.Response.RES_POLICY_FAIL);
                if (fail != null && fail.length() > 0) {
                    // 上传最大失败次数
                    if (fail.has(DeviceKeyContacts.Response.RES_POLICY_FAIL_COUNT)) {
                        policyInfo.setFailCount(fail.optInt(DeviceKeyContacts.Response.RES_POLICY_FAIL_COUNT));
                    }
                    // 上传失败后延迟时间
                    if (fail.has(DeviceKeyContacts.Response.RES_POLICY_FAIL_TRY_DELAY)) {
                        policyInfo.setFailTryDelay(fail.optLong(DeviceKeyContacts.Response.RES_POLICY_FAIL_TRY_DELAY) * 1000);
                    }
                }
            }
            L.info(mContext, "----------6----------");

            // 客户端上传时间间隔
            if (serverPolicy.has(DeviceKeyContacts.Response.RES_POLICY_TIMER_INTERVAL)) {
                policyInfo.setTimerInterval(
                        serverPolicy.optLong(DeviceKeyContacts.Response.RES_POLICY_TIMER_INTERVAL) * 1000);
            }
            L.info(mContext, "----------7----------");

            // 动态采集模块
            if (serverPolicy.has(DeviceKeyContacts.Response.RES_POLICY_CTRL_LIST)) {
                JSONArray ctrlList = serverPolicy.optJSONArray(DeviceKeyContacts.Response.RES_POLICY_CTRL_LIST);
                if (ctrlList != null && ctrlList.length() > 0) {
                    processDynamicModule(policyInfo, ctrlList);
                }
            }
            L.info(mContext, "----------8----------");

            /**
             * 解析热更新下发内容
             */
            if (serverPolicy.has(DeviceKeyContacts.Response.HotFixResp.HOTFIX_RESP_NAME)) {
                JSONObject patch = serverPolicy.getJSONObject(DeviceKeyContacts.Response.HotFixResp.HOTFIX_RESP_NAME);
                if (patch != null && patch.length() > 0) {
                    if (patch.has(DeviceKeyContacts.Response.HotFixResp.HOTFIX_RESP_PATCH_DATA)) {
                        String data = patch.getString(DeviceKeyContacts.Response.HotFixResp.HOTFIX_RESP_PATCH_DATA);
                        if (!TextUtils.isEmpty(data)) {
                            L.info(mContext, "data:" + data);
                            policyInfo.setHotfixData(data);
                        }
                    }
                    if (patch.has(DeviceKeyContacts.Response.HotFixResp.HOTFIX_RESP_PATCH_SIGN)) {
                        String sign = patch.getString(DeviceKeyContacts.Response.HotFixResp.HOTFIX_RESP_PATCH_SIGN);
                        if (!TextUtils.isEmpty(sign)) {
                            L.info(mContext, "sign:" + sign);
                            policyInfo.setHotfixSign(sign);
                        }

                    }
                    if (patch.has(DeviceKeyContacts.Response.HotFixResp.HOTFIX_RESP_PATCH_VERSION)) {
                        String version = patch.getString(DeviceKeyContacts.Response.HotFixResp.HOTFIX_RESP_PATCH_VERSION);
                        if (!TextUtils.isEmpty(version)) {
                            L.info(mContext, "version:" + version);
                            policyInfo.setHotfixVersion(version);
                        }
                    }
                }
            }


            saveNewPolicyToLocal(policyInfo);

        } catch (Throwable t) {
            L.info(mContext, Log.getStackTraceString(t));
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(t);
            }
        }

    }

    /**
     * 处理动态采集模块
     *
     * @param policyInfo
     * @param ctrlList
     * @throws JSONException
     */
    private void processDynamicModule(PolicyInfo policyInfo, JSONArray ctrlList) throws JSONException {
        // 模块控制---某个info控制
        JSONObject responseCtrlInfo;
        JSONObject obj;
        JSONArray list = new JSONArray();
        JSONArray subList = new JSONArray();
        String status, module;
        int deuFreq;
        Object tempObj;

        for (int i = 0; i < ctrlList.length(); i++) {
            obj = (JSONObject) ctrlList.get(i);
            responseCtrlInfo = new JSONObject();
            status = obj.optString(DeviceKeyContacts.Response.RES_POLICY_CTRL_STATUS);
            module = obj.optString(DeviceKeyContacts.Response.RES_POLICY_CTRL_MODULE);
            deuFreq = obj.optInt(DeviceKeyContacts.Response.RES_POLICY_CTRL_DEUFREQ) * 1000;
            tempObj = obj.opt(DeviceKeyContacts.Response.RES_POLICY_CTRL_UNWANTED);
            JSONArray array = null;
            if (!TextUtils.isEmpty(module)) {
                /**
                 * 某个模块，某个字段不要
                 */
                if (tempObj != null) {
                    unWantedKeysHandle(tempObj.toString());
                }

                if (EGContext.MODULE_OC.equals(module)) {
                    if ("0".equals(status)) {// 0不收集，跳过
                        setSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_OC, false);
                        continue;
                    } else {// 1收集,默认值即为轮询的值，忽略最小最大
                        if (deuFreq != 0) {
                            getEditor().putString(EGContext.SP_OC_CYCLE, String.valueOf(deuFreq));
                        }

                    }

                } else if (EGContext.MODULE_LOCATION.equals(module)) {

                    if ("0".equals(status)) {// 0不收集，跳过
                        setSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_LOCATION, false);
                        continue;
                    } else {// 1收集,默认值即为轮询的值，忽略最小最大
                        if (deuFreq != 0) {
                            getEditor().putString(EGContext.SP_LOCATION_CYCLE, String.valueOf(deuFreq));
                        }
                    }
                } else if (EGContext.MODULE_SNAPSHOT.equals(module)) {

                    if ("0".equals(status)) {// 0不收集，跳过
                        setSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_SNAPSHOT, false);
                        continue;
                    } else {// 1收集,默认值即为轮询的值，忽略最小最大
                        if (deuFreq != 0) {
                            getEditor().putString(EGContext.SP_SNAPSHOT_CYCLE, String.valueOf(deuFreq));
                        }
                    }
                } else if (EGContext.MODULE_WIFI.equals(module)) {
                    if ("0".equals(status)) {// 0不收集，跳过
                        setSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_WIFI, false);
                        continue;
                    } // 1收集,默认值即为轮询的值，忽略最小最大,WIFI不轮询
                } else if (EGContext.MODULE_BASE.equals(module)) {

                    if ("0".equals(status)) {// 0不收集，跳过
                        setSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_BASE, false);
//                                setSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_LAC_LIST,false);
//                                setSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_CID_LIST,false);
//                                setSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_RSRP_LIST,false);
//                                setSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_ECIO_LIST,false);
                        continue;
                    } // 1收集,默认值即为轮询的值，忽略最小最大,基站不轮询
                } else if (EGContext.MODULE_DEV.equals(module)) {
                    if ("0".equals(status)) {// 0不收集，跳过
                        setSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_DEV, false);
                        continue;
                    } // 1收集,默认值即为轮询的值，忽略最小最大,基本信息不轮询，发送时候现收集
                    array = obj.optJSONArray(DeviceKeyContacts.Response.RES_POLICY_CTRL_SUB_CONTROL);
                    subModuleHandle(array, subList, "dev");
                } else if (EGContext.MODULE_XXX.equals(module)) {
                    if ("0".equals(status)) {// 0不收集，跳过
                        setSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_XXX, false);
                        continue;
                    }
                    array = obj.optJSONArray(DeviceKeyContacts.Response.RES_POLICY_CTRL_SUB_CONTROL);
                    subModuleHandle(array, subList, "xxx");
                }

                responseCtrlInfo.put(DeviceKeyContacts.Response.RES_POLICY_CTRL_STATUS, status);
                responseCtrlInfo.put(DeviceKeyContacts.Response.RES_POLICY_CTRL_MODULE, module);
                responseCtrlInfo.put(DeviceKeyContacts.Response.RES_POLICY_CTRL_DEUFREQ, deuFreq);
                if (subList != null && subList.length() > 0) {
                    responseCtrlInfo.put(DeviceKeyContacts.Response.RES_POLICY_CTRL_SUB_CONTROL, subList);
                }
                list.put(responseCtrlInfo);
            }
        }
        if (list == null || list.length() < 1) {
            policyInfo.setCtrlList(null);
        } else {
            policyInfo.setCtrlList(list);
        }
    }

    private void unWantedKeysHandle(String tempObj) {
        Set<String> unWanted = null;
        if (tempObj != null && tempObj.length() > 0) {
            unWanted = JsonUtils.transferStringArray2Set(tempObj);
            if (unWanted != null && unWanted.size() > 0) {
                for (String key : unWanted) {
                    if (!TextUtils.isEmpty(key)) {
//                        ELOG.i("policyInfo","key is :::"+key);
                        setSp(key, false);
                    }
                }
            }
        }

    }

    private void subModuleHandle(JSONArray array, JSONArray subList, String tag) throws JSONException {
        JSONObject subResponseCtrlInfo;
        JSONObject subObj;
        Object sub_unWanted;
        String sub_status, sub_module;
        String unCollected = "0";
        if (array != null && array.length() > 0) {
            for (int j = 0; j < array.length(); j++) {
                subObj = (JSONObject) array.get(j);
                subResponseCtrlInfo = new JSONObject();
                sub_status = subObj.optString(DeviceKeyContacts.Response.RES_POLICY_CTRL_STATUS);
//                if ("0".equals(sub_status)){//0不收集
//                    continue;
//                }
                subResponseCtrlInfo.put(DeviceKeyContacts.Response.RES_POLICY_CTRL_SUB_STATUS, sub_status);
                sub_module = subObj.optString(DeviceKeyContacts.Response.RES_POLICY_CTRL_SUB_MODULE);
                subResponseCtrlInfo.put(DeviceKeyContacts.Response.RES_POLICY_CTRL_SUB_MODULE, sub_module);
                sub_unWanted = subObj.optString(DeviceKeyContacts.Response.RES_POLICY_CTRL_UNWANTED);
                subResponseCtrlInfo.put(DeviceKeyContacts.Response.RES_POLICY_CTRL_SUB_UNWANTED, sub_unWanted);
                if (!TextUtils.isEmpty(sub_module)) {
                    if (sub_unWanted != null) {
                        unWantedKeysHandle(sub_unWanted.toString());
                    }
                    if ("dev".equals(tag)) {
                        if (EGContext.BLUETOOTH.equals(sub_module)) {
                            if (unCollected.equals(sub_status)) {// 0不收集，跳过
                                setSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_BLUETOOTH, false);
                                continue;
                            }
                        } else if (EGContext.BATTERY.equals(sub_module)) {
                            if (unCollected.equals(sub_status)) {// 0不收集，跳过
                                setSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_BATTERY, false);
                                continue;
                            }
                        } else if (EGContext.SENSOR.equals(sub_module)) {
                            if (unCollected.equals(sub_status)) {// 0不收集，跳过
                                setSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_SENSOR, false);
                                continue;
                            }
                        } else if (EGContext.SYSTEM_INFO.equals(sub_module)) {
                            if (unCollected.equals(sub_status)) {// 0不收集，跳过
                                setSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_KEEP_INFO, false);
                                continue;
                            }
                        } else if (EGContext.DEV_FURTHER_DETAIL.equals(sub_module)) {
                            if (unCollected.equals(sub_status)) {// 0不收集，跳过
                                setSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_MORE_INFO, false);
                                continue;
                            }
                        } else if (EGContext.PREVENT_CHEATING.equals(sub_module)) {
                            if (unCollected.equals(sub_status)) {// 0不收集，跳过
                                setSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_DEV_CHECK, false);
                                continue;
                            }
                        }
                    } else if ("xxx".equals(tag)) {
                        if (EGContext.PROC.equals(sub_module)) {
                            if (unCollected.equals(sub_status)) {// 0不收集，跳过
                                setSp(ProcUtils.RUNNING_RESULT, false);
                                continue;
                            }
                        } else if (EGContext.XXX_TIME.equals(sub_module)) {
                            if (unCollected.equals(sub_status)) {// 0不收集，跳过
                                setSp(ProcUtils.RUNNING_TIME, false);
                                continue;
                            }
                        } else if (EGContext.OCR.equals(sub_module)) {
                            if (unCollected.equals(sub_status)) {// 0不收集，跳过
                                setSp(ProcUtils.RUNNING_OC_RESULT, false);
                                continue;
                            }
                        }
                    }
//                    subResponseCtrlInfo.put(DeviceKeyContacts.Response.RES_POLICY_CTRL_SUB_MODULE, sub_module);
                }
                if (subResponseCtrlInfo != null && subResponseCtrlInfo.length() > 0) {
                    if (subList == null) {
                        subList = new JSONArray();
                    }
                    subList.put(subResponseCtrlInfo);
                }
            }
        }
    }

    /**
     * 是否想新的策略. 新策略逻辑: 新策略版本非空 且 新老策略号不一样
     *
     * @param newPolicyVer
     * @return
     */
    private boolean isNewPolicy(String newPolicyVer) {
        if (!TextUtils.isEmpty(newPolicyVer)) {
            return !newPolicyVer.equals(getSP().getString(DeviceKeyContacts.Response.RES_POLICY_VERSION, ""));
        } else {
            return false;
        }
//            String nativePV = getSP().getString(DeviceKeyContacts.Response.RES_POLICY_VERSION, "");
//            if (TextUtils.isEmpty(nativePV)) {
//                return true;
//            }
//            Long nativePolicyVer = Long.valueOf(nativePV);
//            Long refreshPolicyVer = Long.valueOf(newPolicyVer);
//            return nativePolicyVer < refreshPolicyVer;
    }

    private void setNormalUploadUrl(Context context) {
        int sum = 0;
        String key = SPHelper.getStringValueFromSP(context, EGContext.SP_APP_KEY, "");
        int index = 0;
        // 不为空则用appkey,为空，则随机取值
        if (!TextUtils.isEmpty(key)) {
            for (int i = 0; i < key.length(); i++) {
                sum = sum + key.charAt(i);
            }
            index = sum % 10;
        } else {
            index = new Random().nextInt(10);
        }

        EGContext.NORMAL_APP_URL = EGContext.URL_SCHEME + EGContext.NORMAL_UPLOAD_URL[index] + EGContext.ORI_PORT;
    }

    private static class Holder {
        private static final PolicyImpl INSTANCE = new PolicyImpl();
    }
}
