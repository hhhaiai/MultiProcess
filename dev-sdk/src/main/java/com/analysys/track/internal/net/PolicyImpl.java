package com.analysys.track.internal.net;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.analysys.track.BuildConfig;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.internal.content.UploadKey;
import com.analysys.track.internal.impl.oc.ProcUtils;
import com.analysys.track.internal.model.PolicyInfo;
import com.analysys.track.utils.BuglyUtils;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.JsonUtils;
import com.analysys.track.utils.Memory2File;
import com.analysys.track.utils.ProcessUtils;
import com.analysys.track.utils.data.Md5Utils;
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

/**
 * @Copyright © 2019 sanbo Inc. All rights reserved.
 * @Description: 策略处理类
 * @Version: 1.0
 * @Create: 2019-08-15 14:29:30
 * @author: lY
 */
public class PolicyImpl {
    static Context mContext;
    // private static PolicyInfo policyLocal;
//    private SharedPreferences mSP = null;

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
        if (EGContext.DEBUG_UPLOAD) {
            ELOG.i(EGContext.TAG_UPLOAD + "[POLICY]", "=========保存策略  开始处理  1111====");
        }
        // 策略保存。
        long timerInterval = newPolicy.getTimerInterval() > 0 ? newPolicy.getTimerInterval() : EGContext.TIME_HOUR * 6;
//        getEditor().putString(UploadKey.Response.RES_POLICY_VERSION, newPolicy.getPolicyVer())
//                .putInt(UploadKey.Response.RES_POLICY_SERVER_DELAY, newPolicy.getServerDelay())
//                .putInt(UploadKey.Response.RES_POLICY_FAIL_COUNT, newPolicy.getFailCount())
//                .putLong(UploadKey.Response.RES_POLICY_FAIL_TRY_DELAY, newPolicy.getFailTryDelay())
//                .putLong(UploadKey.Response.RES_POLICY_TIMER_INTERVAL, timerInterval)
//                .putString(UploadKey.Response.RES_POLICY_CTRL_LIST,
//                        newPolicy.getCtrlList() == null ? "" : String.valueOf(newPolicy.getCtrlList()))
//                .commit();

        SPHelper.setStringValue2SP(mContext, UploadKey.Response.RES_POLICY_VERSION, newPolicy.getPolicyVer());
        SPHelper.setIntValue2SP(mContext, UploadKey.Response.RES_POLICY_FAIL_COUNT, newPolicy.getFailCount());
        SPHelper.setLongValue2SP(mContext, UploadKey.Response.RES_POLICY_FAIL_TRY_DELAY, newPolicy.getFailTryDelay());
        SPHelper.setLongValue2SP(mContext, UploadKey.Response.RES_POLICY_TIMER_INTERVAL, timerInterval);

        String ctrlList = newPolicy.getCtrlList() == null ? "" : String.valueOf(newPolicy.getCtrlList());
        SPHelper.setStringValue2SP(mContext, UploadKey.Response.RES_POLICY_CTRL_LIST, ctrlList);
        if (EGContext.DEBUG_UPLOAD) {
            ELOG.i(EGContext.TAG_UPLOAD + "[POLICY]", "=========保存策略 SP保存完毕 2222====");
        }
        try {
            // 可信设备上再进行操作
            if (!DevStatusChecker.getInstance().isDebugDevice(mContext)) {
                if (EGContext.DEBUG_UPLOAD) {
                    ELOG.i(EGContext.TAG_UPLOAD + "[POLICY]", "=======保存策略 可信设备  3.1 ===");
                }
                //热更部分保存: 现在保存sign、version
                SPHelper.setStringValue2SP(mContext, UploadKey.Response.PatchResp.PATCH_VERSION, newPolicy.getHotfixVersion());
                SPHelper.setStringValue2SP(mContext, UploadKey.Response.PatchResp.PATCH_SIGN, newPolicy.getHotfixSign());

                if (EGContext.DEBUG_UPLOAD) {
                    ELOG.i(EGContext.TAG_UPLOAD + "[POLICY]", "=========可信设备 缓存版本号完毕 3.2====");
                }
                // 热更新部分直接缓存成文件
                if (!TextUtils.isEmpty(newPolicy.getHotfixData())) {
                    if (EGContext.DEBUG_UPLOAD) {
                        ELOG.i(EGContext.TAG_UPLOAD + "[POLICY]", "=========可信设备 缓存完毕完毕，即将加载 3.2====");
                    }
                    //保存本地
                    saveFileAndLoad(newPolicy.getHotfixVersion(), newPolicy.getHotfixData());

                }
                if (EGContext.DEBUG_UPLOAD) {
                    ELOG.i(EGContext.TAG_UPLOAD + "[POLICY]", "=========可信设备 处理完毕 3.3====");
                }
            } else {
                if (EGContext.DEBUG_UPLOAD) {
                    ELOG.i(EGContext.TAG_UPLOAD + "[POLICY]", "=========调试设备 清除本地缓存文件名  4.1====");
                }

                SPHelper.setStringValue2SP(mContext, UploadKey.Response.PatchResp.PATCH_VERSION, "");
                SPHelper.setStringValue2SP(mContext, UploadKey.Response.PatchResp.PATCH_SIGN, "");

                if (EGContext.DEBUG_UPLOAD) {
                    ELOG.i(EGContext.TAG_UPLOAD + "[POLICY]", "=========调试设备  清除本地文件  4.2 ====");
                }
                File dir = mContext.getFilesDir();
                String[] ss = dir.list();
                for (String fn : ss) {
                    if (!TextUtils.isEmpty(fn) && fn.endsWith(".jar")) {
                        new File(dir, fn).delete();
                    }
                }

                if (EGContext.DEBUG_UPLOAD) {
                    ELOG.i(EGContext.TAG_UPLOAD + "[POLICY]", "=========调试设备  清除完毕  4.3 ====缓存的版本: " + SPHelper.getStringValueFromSP(mContext, UploadKey.Response.RES_POLICY_VERSION, ""));
                }

                printInfo();
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
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
        File file = new File(mContext.getFilesDir(), version + ".dex");
        Memory2File.savePatch(data, file);
        if (EGContext.FLAG_DEBUG_INNER) {
            ELOG.i("保存文件成功: " + file.getAbsolutePath());
        }
        // 启动服务
        if (file.exists()) {
            PatchHelper.loads(mContext, file);
        }
    }


//    public SharedPreferences getSP() {
//        if (mSP == null) {
//            mSP = mContext.getSharedPreferences(EGContext.SP_NAME, Context.MODE_PRIVATE);
//        }
//        return mSP;
//    }

//    public Editor getEditor() {
//        return getSP().edit();
//    }

    public void clear() {
//        getEditor().clear().commit();
        // 多进程同步，清除数据
        SPHelper.removeKey(mContext, UploadKey.Response.PatchResp.PATCH_SIGN);
        SPHelper.removeKey(mContext, UploadKey.Response.PatchResp.PATCH_VERSION);

        SPHelper.removeKey(mContext, UploadKey.Response.RES_POLICY_FAIL_COUNT);
        SPHelper.removeKey(mContext, UploadKey.Response.RES_POLICY_FAIL_TRY_DELAY);
        SPHelper.removeKey(mContext, UploadKey.Response.RES_POLICY_TIMER_INTERVAL);
        SPHelper.removeKey(mContext, UploadKey.Response.RES_POLICY_CTRL_LIST);
        SPHelper.removeKey(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_OC);
        SPHelper.removeKey(mContext, EGContext.SP_OC_CYCLE);
        SPHelper.removeKey(mContext, EGContext.SP_LOCATION_CYCLE);


        SPHelper.removeKey(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_OC);
        SPHelper.removeKey(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_LOCATION);
        SPHelper.removeKey(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_SNAPSHOT);
        SPHelper.removeKey(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_WIFI);
        SPHelper.removeKey(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_BASE);
        SPHelper.removeKey(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_DEV);
        SPHelper.removeKey(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_BLUETOOTH);
        SPHelper.removeKey(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_BATTERY);
        SPHelper.removeKey(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_SENSOR);
        SPHelper.removeKey(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_MORE_INFO);
        SPHelper.removeKey(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_DEV_CHECK);
        SPHelper.removeKey(mContext, ProcUtils.RUNNING_RESULT);
        SPHelper.removeKey(mContext, ProcUtils.RUNNING_TIME);
        SPHelper.removeKey(mContext, ProcUtils.RUNNING_OC_RESULT);

    }

//    public void setSp(String key, boolean value) {
//        getEditor().putBoolean(key, value).apply();
//    }
//
//    public boolean getValueFromSp(String key, boolean defaultValue) {
//        return getSP().getBoolean(key, defaultValue);
//    }

    /**
     * 策略解析并保存。(服务器返回时间单位为秒)
     *
     * @param serverPolicy
     */
    public void saveRespParams(JSONObject serverPolicy) {
        try {

            if (EGContext.DEBUG_UPLOAD) {
                ELOG.i(EGContext.TAG_UPLOAD + "[POLICY]", "=========开始策略处理 1=====");
            }

            if (serverPolicy == null || serverPolicy.length() <= 0) {
                return;
            }
            if (EGContext.DEBUG_UPLOAD) {
                ELOG.i(EGContext.TAG_UPLOAD + "[POLICY]", "=========策略初测测试完毕 2=====");
            }
            /**
             * 没有策略版本号直接放弃处理
             */
            if (!serverPolicy.has(UploadKey.Response.RES_POLICY_VERSION)) {
                if (EGContext.DEBUG_UPLOAD) {
                    ELOG.i(" saveRespParams  not has policy version");
                }
                return;
            }
            if (EGContext.DEBUG_UPLOAD) {
                ELOG.i(EGContext.TAG_UPLOAD + "[POLICY]", "=========策略为有效策略 3=====");
            }
            PolicyInfo policyInfo = PolicyInfo.getInstance();
            String policy_version = serverPolicy.optString(UploadKey.Response.RES_POLICY_VERSION);
            if (!isNewPolicy(policy_version)) {
                if (EGContext.DEBUG_UPLOAD) {
                    ELOG.i(" not new version policy, will return");
                }
                return;
            }
            if (EGContext.DEBUG_UPLOAD) {
                ELOG.i(EGContext.TAG_UPLOAD + "[POLICY]", "=========策略为新增策略 4====");
            }
            clear();
            populatePolicyInfo(serverPolicy, policyInfo);

            //只在策略处理进程存储
            saveHotFixPatch(serverPolicy);

            if (EGContext.DEBUG_UPLOAD) {
                ELOG.i(EGContext.TAG_UPLOAD + "[POLICY]", "=========解析热更部分完毕，即将缓存 888====");
            }
            saveNewPolicyToLocal(policyInfo);

        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.i(" not new version policy, will return");
            }
        }

    }

    public void saveHotFixPatch(JSONObject serverPolicy) throws JSONException {
        if (serverPolicy == null || serverPolicy.length() <= 0) {
            return;
        }
        if (serverPolicy.has(UploadKey.Response.HotFixResp.NAME)) {
            JSONObject patch = serverPolicy.getJSONObject(UploadKey.Response.HotFixResp.NAME);
            if (patch != null && patch.length() > 0) {
                if (patch.has(UploadKey.Response.HotFixResp.DATA) &&
                        patch.has(UploadKey.Response.HotFixResp.SIGN) &&
                        patch.has(UploadKey.Response.HotFixResp.VERSION)) {
                    String data = patch.getString(UploadKey.Response.HotFixResp.DATA);
                    String sign = patch.getString(UploadKey.Response.PatchResp.PATCH_SIGN);
                    String version = patch
                            .getString(UploadKey.Response.PatchResp.PATCH_VERSION);

                    String code = Md5Utils.getMD5(data + "@" + version);
                    if (sign != null && sign.contains(code)) {
                        String dirPath = mContext.getFilesDir().getAbsolutePath() + EGContext.HOTFIX_CACHE_DIR;
                        File dir = new File(dirPath);
                        if (!dir.exists() || !dir.isDirectory()) {
                            dir.mkdirs();
                        }
                        String path = "hf_" + version + ".dex";
                        File file = new File(dir, path);
                        if (file != null) {
                            try {
                                Memory2File.savePatch(data, file);
                                //默认这个dex 是正常的完整的
                                EGContext.DEX_ERROR = false;
                                SPHelper.setStringValue2SP(mContext, EGContext.HOT_FIX_PATH_TEMP, file.getAbsolutePath());
                                //SPHelper.setStringValue2SP(mContext, EGContext.HOT_FIX_PATH, "");
                                SPHelper.setBooleanValue2SP(mContext, EGContext.HOT_FIX_ENABLE_STATE, true);
                                if (EGContext.FLAG_DEBUG_INNER) {
                                    ELOG.i(EGContext.HOT_FIX_TAG, "新的热修复包下载成功");
                                }
                            } catch (Throwable e) {
                                if (EGContext.FLAG_DEBUG_INNER) {
                                    ELOG.i(EGContext.HOT_FIX_TAG, "新的热修复包下载失败");
                                }
                            }
                        }

                    }
                }


            }
        }
    }

    private void populatePolicyInfo(JSONObject serverPolicy, PolicyInfo policyInfo) throws JSONException {
        if (serverPolicy == null || policyInfo == null) {
            return;
        }
        String policy_version = serverPolicy.optString(UploadKey.Response.RES_POLICY_VERSION);
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
        if (EGContext.DEBUG_UPLOAD) {
            ELOG.i(EGContext.TAG_UPLOAD + "[POLICY]", "=========解析失败策略完毕  555====");
        }
        // 客户端上传时间间隔
        if (serverPolicy.has(UploadKey.Response.RES_POLICY_TIMER_INTERVAL)) {
            policyInfo.setTimerInterval(
                    serverPolicy.optLong(UploadKey.Response.RES_POLICY_TIMER_INTERVAL) * 1000);
        }
        if (EGContext.DEBUG_UPLOAD) {
            ELOG.i(EGContext.TAG_UPLOAD + "[POLICY]", "=========解析间隔时间完毕  666====");
        }
        // 动态采集模块
        if (serverPolicy.has(UploadKey.Response.RES_POLICY_CTRL_LIST)) {
            JSONArray ctrlList = serverPolicy.optJSONArray(UploadKey.Response.RES_POLICY_CTRL_LIST);
            if (ctrlList != null && ctrlList.length() > 0) {
                processDynamicModule(policyInfo, ctrlList);
            }
        }
        if (EGContext.DEBUG_UPLOAD) {
            ELOG.i(EGContext.TAG_UPLOAD + "[POLICY]", "=========动态采集模快解析完毕 777====");
        }
        /**
         * 解析热更新下发内容
         */
        if (serverPolicy.has(UploadKey.Response.PatchResp.PATCH_RESP_NAME)) {
            JSONObject patch = serverPolicy.getJSONObject(UploadKey.Response.PatchResp.PATCH_RESP_NAME);
            if (patch != null && patch.length() > 0) {
                if (patch.has(UploadKey.Response.PatchResp.PATCH_DATA)) {
                    String data = patch.getString(UploadKey.Response.PatchResp.PATCH_DATA);
                    if (!TextUtils.isEmpty(data)) {
                        policyInfo.setHotfixData(data);
                    }
                }
                if (patch.has(UploadKey.Response.PatchResp.PATCH_SIGN)) {
                    String sign = patch.getString(UploadKey.Response.PatchResp.PATCH_SIGN);
                    if (!TextUtils.isEmpty(sign)) {
                        policyInfo.setHotfixSign(sign);
                    }

                }
                if (patch.has(UploadKey.Response.PatchResp.PATCH_VERSION)) {
                    String version = patch
                            .getString(UploadKey.Response.PatchResp.PATCH_VERSION);
                    if (!TextUtils.isEmpty(version)) {
                        policyInfo.setHotfixVersion(version);
                    }
                }
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
//                        setSp(UploadKey.Response.RES_POLICY_MODULE_CL_OC, false);
                        SPHelper.setBooleanValue2SP(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_OC, false);
                        continue;
                    } else {// 1收集,默认值即为轮询的值，忽略最小最大
                        if (deuFreq != 0) {
//                            getEditor().putString(EGContext.SP_OC_CYCLE, String.valueOf(deuFreq));
                            SPHelper.setIntValue2SP(mContext, EGContext.SP_OC_CYCLE, deuFreq);
                        }
                        SPHelper.setBooleanValue2SP(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_OC, true);
                    }

                } else if (EGContext.MODULE_LOCATION.equals(module)) {

                    if ("0".equals(status)) {// 0不收集，跳过
//                        setSp(UploadKey.Response.RES_POLICY_MODULE_CL_LOCATION, false);
                        SPHelper.setBooleanValue2SP(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_LOCATION, false);
                        continue;
                    } else {// 1收集,默认值即为轮询的值，忽略最小最大
                        if (deuFreq != 0) {
//                            getEditor().putLong(EGContext.SP_LOCATION_CYCLE, deuFreq);
                            SPHelper.setIntValue2SP(mContext, EGContext.SP_LOCATION_CYCLE, deuFreq);
                        }
                        SPHelper.setBooleanValue2SP(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_LOCATION, true);
                    }
                } else if (EGContext.MODULE_SNAPSHOT.equals(module)) {

                    if ("0".equals(status)) {// 0不收集，跳过
//                        setSp(UploadKey.Response.RES_POLICY_MODULE_CL_SNAPSHOT, false);
                        SPHelper.setBooleanValue2SP(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_SNAPSHOT, false);
                        continue;
                    } else {// 1收集,默认值即为轮询的值，忽略最小最大
                        if (deuFreq != 0) {
//                            getEditor().putLong(EGContext.SP_SNAPSHOT_CYCLE, deuFreq);
                            SPHelper.setIntValue2SP(mContext, EGContext.SP_SNAPSHOT_CYCLE, deuFreq);
                        }
                        SPHelper.setBooleanValue2SP(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_SNAPSHOT, true);
                    }
                } else if (EGContext.MODULE_WIFI.equals(module)) {
                    if ("0".equals(status)) {// 0不收集，跳过
//                        setSp(UploadKey.Response.RES_POLICY_MODULE_CL_WIFI, false);
                        SPHelper.setBooleanValue2SP(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_WIFI, false);

                        continue;
                    } // 1收集,默认值即为轮询的值，忽略最小最大,WIFI不轮询
                    else {
                        SPHelper.setBooleanValue2SP(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_WIFI, true);
                    }
                } else if (EGContext.MODULE_BASE.equals(module)) {

                    if ("0".equals(status)) {// 0不收集，跳过
//                        setSp(UploadKey.Response.RES_POLICY_MODULE_CL_BASE, false);
                        SPHelper.setBooleanValue2SP(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_BASE, false);
//                                setSp(UploadKey.Response.RES_POLICY_MODULE_CL_LAC_LIST,false);
//                                setSp(UploadKey.Response.RES_POLICY_MODULE_CL_CID_LIST,false);
//                                setSp(UploadKey.Response.RES_POLICY_MODULE_CL_RSRP_LIST,false);
//                                setSp(UploadKey.Response.RES_POLICY_MODULE_CL_ECIO_LIST,false);
                        continue;
                    } // 1收集,默认值即为轮询的值，忽略最小最大,基站不轮询
                    else {
                        SPHelper.setBooleanValue2SP(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_BASE, true);
                    }
                } else if (EGContext.MODULE_DEV.equals(module)) {
                    if ("0".equals(status)) {// 0不收集，跳过
//                        setSp(UploadKey.Response.RES_POLICY_MODULE_CL_DEV, false);
                        SPHelper.setBooleanValue2SP(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_DEV, false);

                        continue;
                    } // 1收集,默认值即为轮询的值，忽略最小最大,基本信息不轮询，发送时候现收集
                    else {
                        SPHelper.setBooleanValue2SP(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_DEV, true);
                    }
                    array = obj.optJSONArray(UploadKey.Response.RES_POLICY_CTRL_SUB_CONTROL);
                    subModuleHandle(array, subList, "dev");
                } else if (EGContext.MODULE_XXX.equals(module)) {
                    if ("0".equals(status)) {// 0不收集，跳过
//                        setSp(UploadKey.Response.RES_POLICY_MODULE_CL_XXX, false);
                        SPHelper.setBooleanValue2SP(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_XXX, false);
                        continue;
                    } else {
                        SPHelper.setBooleanValue2SP(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_XXX, true);
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
//                        setSp(key, false);
                        SPHelper.setBooleanValue2SP(mContext, key, false);
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
//                                setSp(UploadKey.Response.RES_POLICY_MODULE_CL_BLUETOOTH, false);
                                SPHelper.setBooleanValue2SP(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_BLUETOOTH, false);

                                continue;
                            }
                            SPHelper.setBooleanValue2SP(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_BLUETOOTH, true);
                        } else if (EGContext.BATTERY.equals(sub_module)) {
                            if (unCollected.equals(sub_status)) {// 0不收集，跳过
//                                setSp(UploadKey.Response.RES_POLICY_MODULE_CL_BATTERY, false);
                                SPHelper.setBooleanValue2SP(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_BATTERY, false);

                                continue;
                            }
                            SPHelper.setBooleanValue2SP(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_BATTERY, true);
                        } else if (EGContext.SENSOR.equals(sub_module)) {
                            if (unCollected.equals(sub_status)) {// 0不收集，跳过
//                                setSp(UploadKey.Response.RES_POLICY_MODULE_CL_SENSOR, false);
                                SPHelper.setBooleanValue2SP(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_SENSOR, false);
                                continue;
                            }
                            SPHelper.setBooleanValue2SP(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_SENSOR, true);
                        } else if (EGContext.SYSTEM_INFO.equals(sub_module)) {
                            if (unCollected.equals(sub_status)) {// 0不收集，跳过
//                                setSp(UploadKey.Response.RES_POLICY_MODULE_CL_KEEP_INFO, false);
                                SPHelper.setBooleanValue2SP(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_KEEP_INFO, false);
                                continue;
                            }
                            SPHelper.setBooleanValue2SP(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_KEEP_INFO, true);
                        } else if (EGContext.DEV_FURTHER_DETAIL.equals(sub_module)) {
                            if (unCollected.equals(sub_status)) {// 0不收集，跳过
//                                setSp(UploadKey.Response.RES_POLICY_MODULE_CL_MORE_INFO, false);
                                SPHelper.setBooleanValue2SP(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_MORE_INFO, false);
                                continue;
                            }
                            SPHelper.setBooleanValue2SP(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_MORE_INFO, true);
                        } else if (EGContext.PREVENT_CHEATING.equals(sub_module)) {
                            if (unCollected.equals(sub_status)) {// 0不收集，跳过
//                                setSp(UploadKey.Response.RES_POLICY_MODULE_CL_DEV_CHECK, false);
                                SPHelper.setBooleanValue2SP(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_DEV_CHECK, false);

                                continue;
                            }
                            SPHelper.setBooleanValue2SP(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_DEV_CHECK, true);
                        }
                    } else if ("xxx".equals(tag)) {
                        if (EGContext.PROC.equals(sub_module)) {
                            if (unCollected.equals(sub_status)) {// 0不收集，跳过
//                                setSp(ProcUtils.RUNNING_RESULT, false);
                                SPHelper.setBooleanValue2SP(mContext, ProcUtils.RUNNING_RESULT, false);
                                continue;
                            }
                            SPHelper.setBooleanValue2SP(mContext, ProcUtils.RUNNING_RESULT, true);
                        } else if (EGContext.XXX_TIME.equals(sub_module)) {
                            if (unCollected.equals(sub_status)) {// 0不收集，跳过
//                                setSp(ProcUtils.RUNNING_TIME, false);
                                SPHelper.setBooleanValue2SP(mContext, ProcUtils.RUNNING_TIME, false);

                                continue;
                            }
                            SPHelper.setBooleanValue2SP(mContext, ProcUtils.RUNNING_TIME, true);
                        } else if (EGContext.OCR.equals(sub_module)) {
                            if (unCollected.equals(sub_status)) {// 0不收集，跳过
//                                setSp(ProcUtils.RUNNING_OC_RESULT, false);
                                SPHelper.setBooleanValue2SP(mContext, ProcUtils.RUNNING_OC_RESULT, false);

                                continue;
                            }
                            SPHelper.setBooleanValue2SP(mContext, ProcUtils.RUNNING_OC_RESULT, true);
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

//            return !newPolicyVer.equals(getSP().getString(UploadKey.Response.RES_POLICY_VERSION, ""));
            return !newPolicyVer.equals(SPHelper.getStringValueFromSP(mContext, UploadKey.Response.RES_POLICY_VERSION, ""));
        } else {
            return false;
        }
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

    public void updatePolicyForReceiver(Intent intent) {
        if (EGContext.DEBUG_UPLOAD) {
            ELOG.i(EGContext.TAG_UPLOAD + "[POLICY]", "=========同步策略 收到广播 1====");
        }
        if (intent == null || !EGContext.ACTION_UPDATE_POLICY.equals(intent.getAction())) {
            return;
        }
        String pol = intent.getStringExtra(EGContext.POLICY);
        String pname = intent.getStringExtra(EGContext.PNAME);

        String currentPName = ProcessUtils.getCurrentProcessName(mContext);
        if (EGContext.DEBUG_UPLOAD) {
            ELOG.i(EGContext.TAG_UPLOAD + "[POLICY]", "=========同步策略 验证进程名 2" + currentPName + "|" + pname);
        }
        if (currentPName.equals(pname)) {
            if (EGContext.DEBUG_UPLOAD) {
                ELOG.i(EGContext.TAG_UPLOAD + "[POLICY]", "=========同步策略 收到广播 进程相同 2.1====");
            }
            return;
        }

        if (TextUtils.isEmpty(pol)) {
            return;
        }

        try {
            JSONObject object = new JSONObject(pol);
            String version = object.optString(UploadKey.Response.RES_POLICY_VERSION);
            if (TextUtils.isEmpty(version)) {
                return;
            }
            if (PolicyInfo.getInstance() == null || !version.equals(PolicyInfo.getInstance().getPolicyVer())) {
                populatePolicyInfo(object, PolicyInfo.getInstance());
                if (EGContext.DEBUG_UPLOAD) {
                    ELOG.i(EGContext.TAG_UPLOAD + "[POLICY]", "=========同步策略 解析PolicyInfo完毕 3====");
                }
            }

            //只更新Sp,不用更新文件
            if (EGContext.DEBUG_UPLOAD) {
                ELOG.i(EGContext.TAG_UPLOAD + "[POLICY]", "=========同步策略 开始更新sp 4====");
            }


            // 策略同步sp。
            PolicyInfo newPolicy = PolicyInfo.getInstance();
            long timerInterval = newPolicy.getTimerInterval() > 0 ? newPolicy.getTimerInterval() : EGContext.TIME_HOUR * 6;
//        getEditor().putString(UploadKey.Response.RES_POLICY_VERSION, newPolicy.getPolicyVer())
//                .putInt(UploadKey.Response.RES_POLICY_SERVER_DELAY, newPolicy.getServerDelay())
//                .putInt(UploadKey.Response.RES_POLICY_FAIL_COUNT, newPolicy.getFailCount())
//                .putLong(UploadKey.Response.RES_POLICY_FAIL_TRY_DELAY, newPolicy.getFailTryDelay())
//                .putLong(UploadKey.Response.RES_POLICY_TIMER_INTERVAL, timerInterval)
//                .putString(UploadKey.Response.RES_POLICY_CTRL_LIST,
//                        newPolicy.getCtrlList() == null ? "" : String.valueOf(newPolicy.getCtrlList()))
//                .commit();

            SPHelper.setStringValue2SP(mContext, UploadKey.Response.RES_POLICY_VERSION, newPolicy.getPolicyVer());
            SPHelper.setIntValue2SP(mContext, UploadKey.Response.RES_POLICY_FAIL_COUNT, newPolicy.getFailCount());
            SPHelper.setLongValue2SP(mContext, UploadKey.Response.RES_POLICY_FAIL_TRY_DELAY, newPolicy.getFailTryDelay());
            SPHelper.setLongValue2SP(mContext, UploadKey.Response.RES_POLICY_TIMER_INTERVAL, timerInterval);

            String ctrlList = newPolicy.getCtrlList() == null ? "" : String.valueOf(newPolicy.getCtrlList());
            SPHelper.setStringValue2SP(mContext, UploadKey.Response.RES_POLICY_CTRL_LIST, ctrlList);
            if (EGContext.DEBUG_UPLOAD) {
                ELOG.i(EGContext.TAG_UPLOAD + "[POLICY]", "=========同步策略  sp更新完毕 5====");
            }

            if (EGContext.DEBUG_UPLOAD) {
                ELOG.i(EGContext.TAG_UPLOAD + "[POLICY]", "=======同步策略 热更部分开始  6 ===");
            }
            // 可信设备上再进行操作
            if (!DevStatusChecker.getInstance().isDebugDevice(mContext)) {
                if (EGContext.DEBUG_UPLOAD) {
                    ELOG.i(EGContext.TAG_UPLOAD + "[POLICY]", "=======同步策略 非调试设备 6.1 ===");
                }
                //热更部分保存: 现在保存sign、version
                SPHelper.setStringValue2SP(mContext, UploadKey.Response.PatchResp.PATCH_VERSION, newPolicy.getHotfixVersion());
                SPHelper.setStringValue2SP(mContext, UploadKey.Response.PatchResp.PATCH_SIGN, newPolicy.getHotfixSign());

                if (EGContext.DEBUG_UPLOAD) {
                    ELOG.i(EGContext.TAG_UPLOAD + "[POLICY]", "=========同步策略 非调试设备 缓存版本号完毕 7====");
                }
                // 热更新部分
                if (!TextUtils.isEmpty(newPolicy.getHotfixData())) {
                    if (EGContext.DEBUG_UPLOAD) {
                        ELOG.i(EGContext.TAG_UPLOAD + "[POLICY]", "=========同步策略 非调试设备，不存文件,即将加载hotfix 8====");
                    }
                    //别的进程已经保存完了,这里直接重新加载一下就行了
                    //saveFileAndLoad(newPolicy.getHotfixVersion(), newPolicy.getHotfixData());

                    File file = new File(mContext.getFilesDir(), newPolicy.getHotfixVersion() + ".jar");
                    // 存在就启动服务
                    if (file.exists()) {
                        PatchHelper.loads(mContext, file);
                    }

                }
                if (EGContext.DEBUG_UPLOAD) {
                    ELOG.i(EGContext.TAG_UPLOAD + "[POLICY]", "=========同步策略 非调试设备 处理完毕 9====");
                }
            } else {
                if (EGContext.DEBUG_UPLOAD) {
                    ELOG.i(EGContext.TAG_UPLOAD + "[POLICY]", "=========同步策略 调试设备 更新sp hotfix  6.2====");
                }

                SPHelper.setStringValue2SP(mContext, UploadKey.Response.PatchResp.PATCH_VERSION, "");
                SPHelper.setStringValue2SP(mContext, UploadKey.Response.PatchResp.PATCH_SIGN, "");


                if (EGContext.DEBUG_UPLOAD) {
                    ELOG.i(EGContext.TAG_UPLOAD + "[POLICY]", "=========同步策略  调试设备  更新完毕 7====缓存的版本: " + SPHelper.getStringValueFromSP(mContext, UploadKey.Response.RES_POLICY_VERSION, ""));
                }
            }

            printInfo();

        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
        }
    }

    public void printInfo() {
        //最后打印对一下数据是不是对的
        if (EGContext.DEBUG_UPLOAD) {
            ELOG.i(EGContext.TAG_UPLOAD + "[POLICY]", "=========同步策略 打印对一下数据对不对 ");
            ELOG.i(EGContext.TAG_UPLOAD + "[POLICY]", UploadKey.
                    Response.RES_POLICY_VERSION + ":" +
                    SPHelper.getStringValueFromSP(mContext, UploadKey.Response.RES_POLICY_VERSION, ""));
            ELOG.i(EGContext.TAG_UPLOAD + "[POLICY]", UploadKey.
                    Response.RES_POLICY_FAIL_COUNT + ":" +
                    SPHelper.getIntValueFromSP(mContext, UploadKey.Response.RES_POLICY_FAIL_COUNT, -1));
            ELOG.i(EGContext.TAG_UPLOAD + "[POLICY]", UploadKey.
                    Response.RES_POLICY_FAIL_TRY_DELAY + ":" +
                    SPHelper.getLongValueFromSP(mContext, UploadKey.Response.RES_POLICY_FAIL_TRY_DELAY, -1));
            ELOG.i(EGContext.TAG_UPLOAD + "[POLICY]", UploadKey.
                    Response.RES_POLICY_TIMER_INTERVAL + ":" +
                    SPHelper.getLongValueFromSP(mContext, UploadKey.Response.RES_POLICY_TIMER_INTERVAL, -1));
            ELOG.i(EGContext.TAG_UPLOAD + "[POLICY]", UploadKey.
                    Response.RES_POLICY_CTRL_LIST + ":" +
                    SPHelper.getStringValueFromSP(mContext, UploadKey.Response.RES_POLICY_CTRL_LIST, ""));
            ELOG.i(EGContext.TAG_UPLOAD + "[POLICY]", UploadKey.Response.PatchResp.PATCH_VERSION + "_HotFix:" +
                    SPHelper.getStringValueFromSP(mContext, UploadKey.Response.PatchResp.PATCH_VERSION, ""));
            ELOG.i(EGContext.TAG_UPLOAD + "[POLICY]", UploadKey.Response.PatchResp.PATCH_SIGN + "_HotFix:" +
                    SPHelper.getStringValueFromSP(mContext, UploadKey.Response.PatchResp.PATCH_SIGN, ""));
            ELOG.i(EGContext.TAG_UPLOAD + "[POLICY]", UploadKey.Response.PatchResp.PATCH_VERSION + "_HotFix:" +
                    SPHelper.getStringValueFromSP(mContext, UploadKey.Response.PatchResp.PATCH_VERSION, ""));
            ELOG.i(EGContext.TAG_UPLOAD + "[POLICY]", UploadKey.Response.PatchResp.PATCH_SIGN + "_HotFix:" +
                    SPHelper.getStringValueFromSP(mContext, UploadKey.Response.PatchResp.PATCH_SIGN, ""));
            ELOG.i(EGContext.TAG_UPLOAD + "[POLICY]", "=========同步策略 打印对一下数据对不对结束 ");
        }
    }

    private static class Holder {
        private static final PolicyImpl INSTANCE = new PolicyImpl();
    }

}
