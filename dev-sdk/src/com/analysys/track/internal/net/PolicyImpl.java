package com.analysys.track.internal.net;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;

import com.analysys.track.internal.content.EGContext;
import com.analysys.track.internal.content.UploadKey;
import com.analysys.track.internal.impl.oc.ProcUtils;
import com.analysys.track.internal.model.PolicyInfo;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.JsonUtils;
import com.analysys.track.utils.Memory2File;
import com.analysys.track.utils.reflectinon.DevStatusChecker;
import com.analysys.track.utils.reflectinon.EContextHelper;
import com.analysys.track.utils.reflectinon.PatchHelper;
import com.analysys.track.utils.sp.SPHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
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
        long timerInterval = newPolicy.getTimerInterval() > 0 ? newPolicy.getTimerInterval() : EGContext.TIME_HOUR * 6;
        getEditor().putString(UploadKey.Response.RES_POLICY_VERSION, newPolicy.getPolicyVer())
//                .putInt(UploadKey.Response.RES_POLICY_SERVER_DELAY, newPolicy.getServerDelay())
                .putInt(UploadKey.Response.RES_POLICY_FAIL_COUNT, newPolicy.getFailCount())
                .putLong(UploadKey.Response.RES_POLICY_FAIL_TRY_DELAY, newPolicy.getFailTryDelay())
                .putLong(UploadKey.Response.RES_POLICY_TIMER_INTERVAL, timerInterval)
                .putString(UploadKey.Response.RES_POLICY_CTRL_LIST,
                        newPolicy.getCtrlList() == null ? "" : String.valueOf(newPolicy.getCtrlList()))
                .commit();

        try {
            // 可信设备上再进行操作
            if (!DevStatusChecker.getInstance().isDebugDevice(mContext)) {
                //热更部分保存: 现在保存sign、version
                getEditor().putString(UploadKey.Response.HotFixResp.HOTFIX_RESP_PATCH_SIGN, newPolicy.getHotfixSign())
                        .putString(UploadKey.Response.HotFixResp.HOTFIX_RESP_PATCH_VERSION,
                                newPolicy.getHotfixVersion()).commit();

                // 热更新部分直接缓存成文件
                if (!TextUtils.isEmpty(newPolicy.getHotfixData())) {
                    if (EGContext.FLAG_DEBUG_INNER) {
                        ELOG.i("非调试设备....即将保存文件到本地。。。。");
                    }
                    //保存本地
                    saveFileAndLoad(newPolicy.getHotfixVersion(), newPolicy.getHotfixData());

                }
            } else {
                if (EGContext.FLAG_DEBUG_INNER) {
                    ELOG.i("调试设备...清除本地文件");
                }

                getEditor().remove(UploadKey.Response.HotFixResp.HOTFIX_RESP_PATCH_SIGN).remove(UploadKey.Response.HotFixResp.HOTFIX_RESP_PATCH_VERSION).commit();

                File dir = mContext.getFilesDir();
                String[] ss = dir.list();
                for (String fn : ss) {
                    if (!TextUtils.isEmpty(fn) && fn.endsWith(".jar")) {
                        new File(dir, fn).delete();
                    }
                }
            }
        } catch (Throwable e) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.i(e);
            }
            return;
        }
    }


    /**
     * 保存数据到本地，并且加载
     *
     * @param version
     * @param data
     * @throws UnsupportedEncodingException
     */
    public void saveFileAndLoad(String version, String data) throws UnsupportedEncodingException {
        // 保存文件到本地
        File file = new File(mContext.getFilesDir(), version + ".jar");
        Memory2File.savePatch(data, file);
        if (EGContext.FLAG_DEBUG_INNER) {
            ELOG.i("保存文件成功: " + file.getAbsolutePath());
        }
        // 启动服务
        if (file.exists()) {
            PatchHelper.loads(mContext, file);
        }
    }


    public SharedPreferences getSP() {
        if (mSP == null) {
            mSP = mContext.getSharedPreferences(EGContext.SP_NAME, Context.MODE_PRIVATE);
        }
        return mSP;
    }

    public Editor getEditor() {
        return getSP().edit();
    }

    public void clear() {
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
            /**
             * 没有策略版本号直接放弃处理
             */
            if (!serverPolicy.has(UploadKey.Response.RES_POLICY_VERSION)) {
                if (EGContext.FLAG_DEBUG_INNER) {
                    ELOG.i(" saveRespParams  not has policy version");
                }
                return;
            }

            PolicyInfo policyInfo = PolicyInfo.getInstance();
            String policy_version = serverPolicy.optString(UploadKey.Response.RES_POLICY_VERSION);
            if (!isNewPolicy(policy_version)) {
                if (EGContext.FLAG_DEBUG_INNER) {
                    ELOG.i(" not new version policy, will return");
                }
                return;
            }

            clear();
            policyInfo.setPolicyVer(policy_version);// 策略版本


//            if (serverPolicy.has(UploadKey.Response.RES_POLICY_SERVER_DELAY)) {
//                policyInfo
//                        .setServerDelay(serverPolicy.optInt(UploadKey.Response.RES_POLICY_SERVER_DELAY) * 1000);
//            }

            /**
             * 失败策略处理
             */
            if (serverPolicy.has(UploadKey.Response.RES_POLICY_FAIL)) {

                JSONObject fail = serverPolicy.getJSONObject(UploadKey.Response.RES_POLICY_FAIL);
                if (fail != null && fail.length() > 0) {
                    // 上传最大失败次数
                    if (fail.has(UploadKey.Response.RES_POLICY_FAIL_COUNT)) {
                        policyInfo.setFailCount(fail.optInt(UploadKey.Response.RES_POLICY_FAIL_COUNT));
                    }
                    // 上传失败后延迟时间
                    if (fail.has(UploadKey.Response.RES_POLICY_FAIL_TRY_DELAY)) {
                        policyInfo.setFailTryDelay(
                                fail.optLong(UploadKey.Response.RES_POLICY_FAIL_TRY_DELAY) * 1000);
                    }
                }
            }

            // 客户端上传时间间隔
            if (serverPolicy.has(UploadKey.Response.RES_POLICY_TIMER_INTERVAL)) {
                policyInfo.setTimerInterval(
                        serverPolicy.optLong(UploadKey.Response.RES_POLICY_TIMER_INTERVAL) * 1000);
            }

            // 动态采集模块
            if (serverPolicy.has(UploadKey.Response.RES_POLICY_CTRL_LIST)) {
                JSONArray ctrlList = serverPolicy.optJSONArray(UploadKey.Response.RES_POLICY_CTRL_LIST);
                if (ctrlList != null && ctrlList.length() > 0) {
                    processDynamicModule(policyInfo, ctrlList);
                }
            }

            /**
             * 解析热更新下发内容
             */
            if (serverPolicy.has(UploadKey.Response.HotFixResp.HOTFIX_RESP_NAME)) {
                JSONObject patch = serverPolicy.getJSONObject(UploadKey.Response.HotFixResp.HOTFIX_RESP_NAME);
                if (patch != null && patch.length() > 0) {
                    if (patch.has(UploadKey.Response.HotFixResp.HOTFIX_RESP_PATCH_DATA)) {
                        String data = patch.getString(UploadKey.Response.HotFixResp.HOTFIX_RESP_PATCH_DATA);
                        if (!TextUtils.isEmpty(data)) {
                            policyInfo.setHotfixData(data);
                        }
                    }
                    if (patch.has(UploadKey.Response.HotFixResp.HOTFIX_RESP_PATCH_SIGN)) {
                        String sign = patch.getString(UploadKey.Response.HotFixResp.HOTFIX_RESP_PATCH_SIGN);
                        if (!TextUtils.isEmpty(sign)) {
                            policyInfo.setHotfixSign(sign);
                        }

                    }
                    if (patch.has(UploadKey.Response.HotFixResp.HOTFIX_RESP_PATCH_VERSION)) {
                        String version = patch
                                .getString(UploadKey.Response.HotFixResp.HOTFIX_RESP_PATCH_VERSION);
                        if (!TextUtils.isEmpty(version)) {
                            policyInfo.setHotfixVersion(version);
                        }
                    }
                }
            }

            saveNewPolicyToLocal(policyInfo);

        } catch (Throwable e) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.i(" not new version policy, will return");
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
            status = obj.optString(UploadKey.Response.RES_POLICY_CTRL_STATUS);
            module = obj.optString(UploadKey.Response.RES_POLICY_CTRL_MODULE);
            deuFreq = obj.optInt(UploadKey.Response.RES_POLICY_CTRL_DEUFREQ) * 1000;
            tempObj = obj.opt(UploadKey.Response.RES_POLICY_CTRL_UNWANTED);
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
                        setSp(UploadKey.Response.RES_POLICY_MODULE_CL_OC, false);
                        continue;
                    } else {// 1收集,默认值即为轮询的值，忽略最小最大
                        if (deuFreq != 0) {
                            getEditor().putString(EGContext.SP_OC_CYCLE, String.valueOf(deuFreq));
                        }

                    }

                } else if (EGContext.MODULE_LOCATION.equals(module)) {

                    if ("0".equals(status)) {// 0不收集，跳过
                        setSp(UploadKey.Response.RES_POLICY_MODULE_CL_LOCATION, false);
                        continue;
                    } else {// 1收集,默认值即为轮询的值，忽略最小最大
                        if (deuFreq != 0) {
                            getEditor().putLong(EGContext.SP_LOCATION_CYCLE, deuFreq);
                        }
                    }
                } else if (EGContext.MODULE_SNAPSHOT.equals(module)) {

                    if ("0".equals(status)) {// 0不收集，跳过
                        setSp(UploadKey.Response.RES_POLICY_MODULE_CL_SNAPSHOT, false);
                        continue;
                    } else {// 1收集,默认值即为轮询的值，忽略最小最大
                        if (deuFreq != 0) {
                            getEditor().putLong(EGContext.SP_SNAPSHOT_CYCLE, deuFreq);
                        }
                    }
                } else if (EGContext.MODULE_WIFI.equals(module)) {
                    if ("0".equals(status)) {// 0不收集，跳过
                        setSp(UploadKey.Response.RES_POLICY_MODULE_CL_WIFI, false);
                        continue;
                    } // 1收集,默认值即为轮询的值，忽略最小最大,WIFI不轮询
                } else if (EGContext.MODULE_BASE.equals(module)) {

                    if ("0".equals(status)) {// 0不收集，跳过
                        setSp(UploadKey.Response.RES_POLICY_MODULE_CL_BASE, false);
//                                setSp(UploadKey.Response.RES_POLICY_MODULE_CL_LAC_LIST,false);
//                                setSp(UploadKey.Response.RES_POLICY_MODULE_CL_CID_LIST,false);
//                                setSp(UploadKey.Response.RES_POLICY_MODULE_CL_RSRP_LIST,false);
//                                setSp(UploadKey.Response.RES_POLICY_MODULE_CL_ECIO_LIST,false);
                        continue;
                    } // 1收集,默认值即为轮询的值，忽略最小最大,基站不轮询
                } else if (EGContext.MODULE_DEV.equals(module)) {
                    if ("0".equals(status)) {// 0不收集，跳过
                        setSp(UploadKey.Response.RES_POLICY_MODULE_CL_DEV, false);
                        continue;
                    } // 1收集,默认值即为轮询的值，忽略最小最大,基本信息不轮询，发送时候现收集
                    array = obj.optJSONArray(UploadKey.Response.RES_POLICY_CTRL_SUB_CONTROL);
                    subModuleHandle(array, subList, "dev");
                } else if (EGContext.MODULE_XXX.equals(module)) {
                    if ("0".equals(status)) {// 0不收集，跳过
                        setSp(UploadKey.Response.RES_POLICY_MODULE_CL_XXX, false);
                        continue;
                    }
                    array = obj.optJSONArray(UploadKey.Response.RES_POLICY_CTRL_SUB_CONTROL);
                    subModuleHandle(array, subList, "xxx");
                }

                responseCtrlInfo.put(UploadKey.Response.RES_POLICY_CTRL_STATUS, status);
                responseCtrlInfo.put(UploadKey.Response.RES_POLICY_CTRL_MODULE, module);
                responseCtrlInfo.put(UploadKey.Response.RES_POLICY_CTRL_DEUFREQ, deuFreq);
                if (subList != null && subList.length() > 0) {
                    responseCtrlInfo.put(UploadKey.Response.RES_POLICY_CTRL_SUB_CONTROL, subList);
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
                sub_status = subObj.optString(UploadKey.Response.RES_POLICY_CTRL_STATUS);
//                if ("0".equals(sub_status)){//0不收集
//                    continue;
//                }
                subResponseCtrlInfo.put(UploadKey.Response.RES_POLICY_CTRL_SUB_STATUS, sub_status);
                sub_module = subObj.optString(UploadKey.Response.RES_POLICY_CTRL_SUB_MODULE);
                subResponseCtrlInfo.put(UploadKey.Response.RES_POLICY_CTRL_SUB_MODULE, sub_module);
                sub_unWanted = subObj.optString(UploadKey.Response.RES_POLICY_CTRL_UNWANTED);
                subResponseCtrlInfo.put(UploadKey.Response.RES_POLICY_CTRL_SUB_UNWANTED, sub_unWanted);
                if (!TextUtils.isEmpty(sub_module)) {
                    if (sub_unWanted != null) {
                        unWantedKeysHandle(sub_unWanted.toString());
                    }
                    if ("dev".equals(tag)) {
                        if (EGContext.BLUETOOTH.equals(sub_module)) {
                            if (unCollected.equals(sub_status)) {// 0不收集，跳过
                                setSp(UploadKey.Response.RES_POLICY_MODULE_CL_BLUETOOTH, false);
                                continue;
                            }
                        } else if (EGContext.BATTERY.equals(sub_module)) {
                            if (unCollected.equals(sub_status)) {// 0不收集，跳过
                                setSp(UploadKey.Response.RES_POLICY_MODULE_CL_BATTERY, false);
                                continue;
                            }
                        } else if (EGContext.SENSOR.equals(sub_module)) {
                            if (unCollected.equals(sub_status)) {// 0不收集，跳过
                                setSp(UploadKey.Response.RES_POLICY_MODULE_CL_SENSOR, false);
                                continue;
                            }
                        } else if (EGContext.SYSTEM_INFO.equals(sub_module)) {
                            if (unCollected.equals(sub_status)) {// 0不收集，跳过
                                setSp(UploadKey.Response.RES_POLICY_MODULE_CL_KEEP_INFO, false);
                                continue;
                            }
                        } else if (EGContext.DEV_FURTHER_DETAIL.equals(sub_module)) {
                            if (unCollected.equals(sub_status)) {// 0不收集，跳过
                                setSp(UploadKey.Response.RES_POLICY_MODULE_CL_MORE_INFO, false);
                                continue;
                            }
                        } else if (EGContext.PREVENT_CHEATING.equals(sub_module)) {
                            if (unCollected.equals(sub_status)) {// 0不收集，跳过
                                setSp(UploadKey.Response.RES_POLICY_MODULE_CL_DEV_CHECK, false);
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
//                    subResponseCtrlInfo.put(UploadKey.Response.RES_POLICY_CTRL_SUB_MODULE, sub_module);
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
            return !newPolicyVer.equals(getSP().getString(UploadKey.Response.RES_POLICY_VERSION, ""));
        } else {
            return false;
        }
//            String nativePV = getSP().getString(UploadKey.Response.RES_POLICY_VERSION, "");
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
