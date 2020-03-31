package com.analysys.track.internal.net;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Base64;

import com.analysys.track.BuildConfig;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.internal.content.UploadKey;
import com.analysys.track.internal.impl.oc.ProcUtils;
import com.analysys.track.internal.model.PolicyInfo;
import com.analysys.track.utils.BugReportForTest;
import com.analysys.track.utils.EContextHelper;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.JsonUtils;
import com.analysys.track.utils.data.Memory2File;
import com.analysys.track.utils.reflectinon.DevStatusChecker;
import com.analysys.track.impl.HotfHelper;
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

    private PolicyImpl() {
    }

    public static PolicyImpl getInstance(Context context) {
        if (mContext == null) {
            mContext = EContextHelper.getContext();
        }
        return PolicyImpl.Holder.INSTANCE;
    }

//    /**
//     * @param debug 是否Debug模式
//     */
//    public void updateUpLoadUrl(boolean debug) {
////        if (debug) {
////            EGContext.APP_URL = EGContext.TEST_URL;
////        } else {
//            setNormalUploadUrl(mContext);
////            EGContext.APP_URL = EGContext.NORMAL_APP_URL;
////        }
//    }

    /**
     * 保存策略到本地
     *
     * @param newPolicy
     */
    private void saveNewPolicyToLocal(PolicyInfo newPolicy) {
        if (BuildConfig.logcat) {
            ELOG.i(BuildConfig.tag_upload + "[POLICY]", "=========保存策略  开始处理  1111====");
        }
        // 策略保存。
        long timerInterval = newPolicy.getTimerInterval() > 0 ? newPolicy.getTimerInterval() : EGContext.TIME_DEFAULT_REQUEST_SERVER;

        SPHelper.setStringValue2SP(mContext, UploadKey.Response.RES_POLICY_VERSION, newPolicy.getPolicyVer());
        SPHelper.setIntValue2SP(mContext, UploadKey.Response.RES_POLICY_FAIL_COUNT, newPolicy.getFailCount());
        SPHelper.setLongValue2SP(mContext, UploadKey.Response.RES_POLICY_FAIL_TRY_DELAY, newPolicy.getFailTryDelay());
        SPHelper.setLongValue2SP(mContext, UploadKey.Response.RES_POLICY_TIMER_INTERVAL, timerInterval);

        String ctrlList = newPolicy.getCtrlList() == null ? "" : String.valueOf(newPolicy.getCtrlList());
        SPHelper.setStringValue2SP(mContext, UploadKey.Response.RES_POLICY_CTRL_LIST, ctrlList);
        if (BuildConfig.logcat) {
            ELOG.i(BuildConfig.tag_cutoff, "=========保存策略 SP保存完毕 2222====");
        }
        try {
            // 可信设备上再进行操作
            if (!DevStatusChecker.getInstance().isDebugDevice(mContext)) {
                if (BuildConfig.logcat) {
                    ELOG.i(BuildConfig.tag_cutoff, "=======保存策略 可信设备  3.1 ===");
                }

                // 清除老版本缓存文件
                String oldVersion = SPHelper.getStringValueFromSP(mContext, UploadKey.Response.PatchResp.PATCH_VERSION, "");
                if (!TextUtils.isEmpty(oldVersion)) {
                    new File(mContext.getFilesDir(), oldVersion + ".jar").deleteOnExit();
                    if (BuildConfig.logcat) {
                        ELOG.i(BuildConfig.tag_cutoff, "=======清除老版本缓存文件 ====oldVersion: " + oldVersion + "--->"
                                + new File(mContext.getFilesDir(), oldVersion + ".jar").exists());
                    }
                }
                //热更部分保存: 现在保存sign、version
                if (!TextUtils.isEmpty(newPolicy.getPatchVersion())) {
                    if (BuildConfig.logcat) {
                        ELOG.i(BuildConfig.tag_cutoff, "=======现在保存   PatchVersion: " + newPolicy.getPatchVersion());
                    }
                    SPHelper.setStringValue2SP(mContext, UploadKey.Response.PatchResp.PATCH_VERSION, newPolicy.getPatchVersion());
                }
                if (!TextUtils.isEmpty(newPolicy.getPatchSign())) {
                    if (BuildConfig.logcat) {
                        ELOG.i(BuildConfig.tag_cutoff, "=======现在保存   PatchSign: " + newPolicy.getPatchSign());
                    }
                    SPHelper.setStringValue2SP(mContext, UploadKey.Response.PatchResp.PATCH_SIGN, newPolicy.getPatchSign());
                }

                if (!TextUtils.isEmpty(newPolicy.getPatchMethons())) {
                    SPHelper.setStringValue2SP(mContext, UploadKey.Response.PatchResp.PATCH_METHODS,
                            Base64.encodeToString(newPolicy.getPatchMethons().getBytes("UTF-8"), Base64.DEFAULT));
                    if (BuildConfig.logcat) {
                        ELOG.i(BuildConfig.tag_cutoff, "=======现在保存   PatchMethons: " + SPHelper.getStringValueFromSP(mContext, UploadKey.Response.PatchResp.PATCH_METHODS, ""));
                    }
                }


                if (BuildConfig.logcat) {
                    ELOG.i(BuildConfig.tag_cutoff, "=========可信设备 缓存版本号完毕 3.2====");
                }
                // 热更新部分直接缓存成文件
                if (!TextUtils.isEmpty(newPolicy.getPatchData())) {
                    if (BuildConfig.logcat) {
                        ELOG.i(BuildConfig.tag_cutoff, "=========可信设备 缓存完毕完毕，即将加载 3.2====");
                    }

                    if (!TextUtils.isEmpty(newPolicy.getPolicyVer()) && TextUtils.isEmpty(newPolicy.getHotfixVersion())) {
                        SPHelper.setStringValue2SP(mContext, EGContext.PATCH_VERSION_POLICY, newPolicy.getPolicyVer());
                    }
                    //保存本地
                    saveFileAndLoad(newPolicy.getPatchVersion(), newPolicy.getPatchData());
                }
                if (BuildConfig.logcat) {
                    ELOG.i(BuildConfig.tag_cutoff, "=========可信设备 处理完毕 3.3====");
                }
            } else {
                if (BuildConfig.logcat) {
                    ELOG.i(BuildConfig.tag_cutoff, "=========调试设备 清除本地缓存文件名  4.1====");
                }
                PatchHelper.clearPatch(mContext);

                if (BuildConfig.logcat) {
                    ELOG.i(BuildConfig.tag_cutoff, "=========调试设备  清除s本地文件  4.2 ====");
                }

                printInfo();
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        }
        // 内存的大容量数据清除
        newPolicy.clearMemoryPatchData();
    }


    /**
     * 保存数据到本地，并且加载
     *
     * @param version
     * @param data
     * @throws UnsupportedEncodingException
     */
    public void saveFileAndLoad(String version, String data) throws UnsupportedEncodingException {

        File dir = new File(mContext.getFilesDir(), EGContext.PATCH_CACHE_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        // 保存文件到本地
        File file = new File(dir, "patch_" + version + ".jar");

        Memory2File.savePatch(data, file);
        if (BuildConfig.logcat) {
            ELOG.i(BuildConfig.tag_cutoff, " saveFileAndLoad 保存文件成功: " + file.getAbsolutePath());
        }
        // 启动服务
        if (file.exists()) {
            PatchHelper.loads(mContext);
        }
    }


    public void clear() {
        // 多进程同步，清除数据
        SPHelper.removeKey(mContext, UploadKey.Response.PatchResp.PATCH_SIGN);
        SPHelper.removeKey(mContext, UploadKey.Response.PatchResp.PATCH_VERSION);

        SPHelper.removeKey(mContext, UploadKey.Response.RES_POLICY_FAIL_COUNT);
        SPHelper.removeKey(mContext, UploadKey.Response.RES_POLICY_FAIL_TRY_DELAY);
        SPHelper.removeKey(mContext, UploadKey.Response.RES_POLICY_TIMER_INTERVAL);
        SPHelper.removeKey(mContext, UploadKey.Response.RES_POLICY_CTRL_LIST);
        SPHelper.removeKey(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_OC);

        SPHelper.removeKey(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_NET);
        SPHelper.removeKey(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_USM);
        SPHelper.removeKey(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_USM_CUTOF_XXX);
        SPHelper.removeKey(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_USM_CUTOF_NET);
        SPHelper.removeKey(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_USM_CUTOF_OC);

        SPHelper.removeKey(mContext, EGContext.SP_OC_CYCLE);
        SPHelper.removeKey(mContext, EGContext.SP_NET_CYCLE);
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

    /**
     * 策略解析并保存。(服务器返回时间单位为秒)
     *
     * @param serverPolicy
     */
    public void saveRespParams(JSONObject serverPolicy) {
        try {
            if (BuildConfig.logcat) {
                ELOG.i(BuildConfig.tag_cutoff, "=========saveRespParams  开始处理 1=====");
            }

            if (serverPolicy == null || serverPolicy.length() <= 0) {
                return;
            }
            if (BuildConfig.logcat) {
                ELOG.i(BuildConfig.tag_cutoff, "=========saveRespParams 策略非空 2=====");
            }
            /**
             * 没有策略版本号直接放弃处理
             */
            if (!serverPolicy.has(UploadKey.Response.RES_POLICY_VERSION)) {
                if (BuildConfig.logcat) {
                    ELOG.i(BuildConfig.tag_cutoff, " saveRespParams  not has policy version");
                }
                return;
            }
            if (BuildConfig.logcat) {
                ELOG.i(BuildConfig.tag_cutoff, "=========saveRespParams 策略为有效策略 =====");
            }

            String policy_version = serverPolicy.optString(UploadKey.Response.RES_POLICY_VERSION, "");
            if (!isNewPolicy(policy_version)) {
                if (BuildConfig.logcat) {
                    ELOG.i(BuildConfig.tag_cutoff, "=========saveRespParams not new version policy, will return =====");
                }
                return;
            }
            if (BuildConfig.logcat) {
                ELOG.i(BuildConfig.tag_cutoff, "=========saveRespParams 策略为新增策略 4====");
            }
            // 4306版本去除，支持多组策略叠加
            // clear();

            // 解析策略到内存模型
            parsePolicyToMemoryModule(serverPolicy, PolicyInfo.getInstance());
            if (BuildConfig.logcat) {
                ELOG.i(BuildConfig.tag_cutoff, "=========解析热更部分完毕，即将缓存 888====");
            }
            saveNewPolicyToLocal(PolicyInfo.getInstance());

        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        }

    }

    /**
     * 保存热修复相关逻辑
     *
     * @param serverPolicy
     * @param policyInfo
     */
    private void parserHotfix(JSONObject serverPolicy, PolicyInfo policyInfo) {
        try {
            if (!BuildConfig.enableHotFix) {
                return;
            }
            if (serverPolicy == null || serverPolicy.length() <= 0) {
                return;
            }
            if (serverPolicy.has(UploadKey.Response.HotFixResp.NAME)) {
                JSONObject hotfix = serverPolicy.optJSONObject(UploadKey.Response.HotFixResp.NAME);
                if (hotfix != null && hotfix.length() > 0) {
                    if (hotfix.has(UploadKey.Response.HotFixResp.OPERA)) {
                        String enable = hotfix.optString(UploadKey.Response.HotFixResp.OPERA, "");
                        if (UploadKey.Response.HotFixResp.RESET.equals(enable)) {
                            SPHelper.setStringValue2SP(mContext, EGContext.HOT_FIX_PATH, "");
                            if (BuildConfig.logcat) {
                                ELOG.i(BuildConfig.tag_hotfix, "热修复重置[下次重启使用宿主]");
                            }
                            return;
                        }
                    }


                    HotfHelper.getInstance(mContext).processAndSaveHotfix(policyInfo, hotfix);
                }
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        }
    }


    /**
     * parser all policy to memory/SP
     *
     * @param serverPolicy
     * @param policyInfo
     * @throws JSONException
     */
    private void parsePolicyToMemoryModule(JSONObject serverPolicy, PolicyInfo policyInfo) throws JSONException {
        try {
            if (serverPolicy == null || policyInfo == null) {
                return;
            }
            // 策略版本
            policyInfo.setPolicyVer(serverPolicy.optString(UploadKey.Response.RES_POLICY_VERSION, ""));
            // 失败策略处理
            parserFailPolicy(serverPolicy, policyInfo);
            if (BuildConfig.logcat) {
                ELOG.i(BuildConfig.tag_upload + "[POLICY]", "========parsePolicyToMemoryModule====解析失败策略完毕===");
            }
            // 客户端上传时间间隔
            if (serverPolicy.has(UploadKey.Response.RES_POLICY_TIMER_INTERVAL)) {
                policyInfo.setTimerInterval(
                        serverPolicy.optLong(UploadKey.Response.RES_POLICY_TIMER_INTERVAL) * 1000);
            }
            // extras
            if (serverPolicy.has(UploadKey.Response.RES_POLICY_EXTRAS)) {
                JSONArray arr = serverPolicy.optJSONArray(UploadKey.Response.RES_POLICY_EXTRAS);
                if (arr != null && arr.length() > 0) {
                    SPHelper.setStringValue2SP(mContext, UploadKey.Response.RES_POLICY_EXTRAS, arr.toString());
                }
            }


            if (BuildConfig.logcat) {
                ELOG.i(BuildConfig.tag_upload + "[POLICY]", "=====parsePolicyToMemoryModule====解析间隔时间完毕====");
            }
            // 动态采集模块
            if (serverPolicy.has(UploadKey.Response.RES_POLICY_CTRL_LIST)) {
                JSONArray ctrlList = serverPolicy.optJSONArray(UploadKey.Response.RES_POLICY_CTRL_LIST);
                if (ctrlList != null && ctrlList.length() > 0) {
                    processDynamicModule(policyInfo, ctrlList);
                }
            }
            if (BuildConfig.logcat) {
                ELOG.i(BuildConfig.tag_upload + "[POLICY]", "======parsePolicyToMemoryModule===动态采集模快解析完毕 ===");
            }
            parserPatchPolicy(serverPolicy, policyInfo);
            parserHotfix(serverPolicy, policyInfo);
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        }
    }

    /**
     * 解析失败策略
     *
     * @param serverPolicy
     * @param policyInfo
     */
    private void parserFailPolicy(JSONObject serverPolicy, PolicyInfo policyInfo) {
        try {
            if (serverPolicy.has(UploadKey.Response.RES_POLICY_FAIL)) {

                JSONObject fail = serverPolicy.optJSONObject(UploadKey.Response.RES_POLICY_FAIL);
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
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        }
    }

    /**
     * 解析patch策略
     *
     * @param serverPolicy
     * @param policyInfo
     */
    private void parserPatchPolicy(JSONObject serverPolicy, PolicyInfo policyInfo) {
        try {
            /**
             * 解析patch下发内容
             */
            if (serverPolicy.has(UploadKey.Response.PatchResp.PATCH_RESP_NAME)) {

                JSONObject patch = serverPolicy.optJSONObject(UploadKey.Response.PatchResp.PATCH_RESP_NAME);
                if (patch != null && patch.length() > 0) {
                    if (patch.has(UploadKey.Response.PatchResp.OPERA)) {
                        String reset = patch.optString(UploadKey.Response.PatchResp.OPERA, "");
                        /**
                         * 处理reset逻辑，处理完毕就停止处理
                         */
                        if (UploadKey.Response.PatchResp.RESET.equals(reset)) {
                            PatchHelper.clearPatch(mContext);
                            return;
                        }
                    }
                    if (patch.has(UploadKey.Response.PatchResp.PATCH_DATA)) {
                        String data = patch.optString(UploadKey.Response.PatchResp.PATCH_DATA, "");
                        if (!TextUtils.isEmpty(data)) {
                            policyInfo.setPatchData(data);
                        }
                    }
                    if (patch.has(UploadKey.Response.PatchResp.PATCH_SIGN)) {
                        String sign = patch.optString(UploadKey.Response.PatchResp.PATCH_SIGN, "");
                        if (!TextUtils.isEmpty(sign)) {
                            policyInfo.setPatchSign(sign);
                        }

                    }
                    if (patch.has(UploadKey.Response.PatchResp.PATCH_METHODS)) {
                        String methods = patch
                                .optString(UploadKey.Response.PatchResp.PATCH_METHODS, "");
                        if (!TextUtils.isEmpty(methods)) {
                            policyInfo.setPatchMethons(methods);
                        }
                    }

                    if (patch.has(UploadKey.Response.PatchResp.PATCH_VERSION)) {
                        String version = patch
                                .optString(UploadKey.Response.PatchResp.PATCH_VERSION, "");
                        // 确保有默认版本号
                        if (TextUtils.isEmpty(version)) {
                            version = EGContext.PATCH_VERSION;
                        }
                        policyInfo.setPatchVersion(version);
                    }
                }
            }
        } catch (Throwable igone) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(igone);
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
            try {
                obj = (JSONObject) ctrlList.get(i);
                responseCtrlInfo = new JSONObject();
                status = obj.optString(UploadKey.Response.RES_POLICY_CTRL_STATUS);
                module = obj.optString(UploadKey.Response.RES_POLICY_CTRL_MODULE);
                deuFreq = obj.optInt(UploadKey.Response.RES_POLICY_CTRL_DEUFREQ) * 1000;
                tempObj = obj.opt(UploadKey.Response.RES_POLICY_CTRL_EXCLUDE);
                JSONArray array = null;
                if (!TextUtils.isEmpty(module)) {
                    /**
                     * 某个模块，某个字段不要
                     */
                    if (tempObj != null) {
                        unWantedKeysHandle(tempObj.toString());
                    }

                    if (EGContext.MODULE_OC.equals(module)) {
                        if (EGContext.DEFAULT_ZERO.equals(status)) {// 0不收集，跳过
                            SPHelper.setBooleanValue2SP(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_OC, false);
                            continue;
                        } else {
                            // 1收集,默认值即为轮询的值，忽略最小最大
                            if (deuFreq != 0) {
                                SPHelper.setIntValue2SP(mContext, EGContext.SP_OC_CYCLE, deuFreq);
                            }
                            SPHelper.setBooleanValue2SP(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_OC, true);
                        }

                    } else if (EGContext.MODULE_LOCATION.equals(module)) {
                        // 0不收集，跳过
                        if (EGContext.DEFAULT_ZERO.equals(status)) {
                            SPHelper.setBooleanValue2SP(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_LOCATION, false);
                            continue;
                        } else {
                            // 1收集,默认值即为轮询的值，忽略最小最大
                            if (deuFreq != 0) {
                                SPHelper.setIntValue2SP(mContext, EGContext.SP_LOCATION_CYCLE, deuFreq);
                            }
                            SPHelper.setBooleanValue2SP(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_LOCATION, true);
                        }
                    } else if (EGContext.MODULE_SNAPSHOT.equals(module)) {
                        // 0不收集，跳过
                        if (EGContext.DEFAULT_ZERO.equals(status)) {
                            SPHelper.setBooleanValue2SP(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_SNAPSHOT, false);
                            continue;
                        } else {
                            // 1收集,默认值即为轮询的值，忽略最小最大
                            if (deuFreq != 0) {
                                SPHelper.setIntValue2SP(mContext, EGContext.SP_SNAPSHOT_CYCLE, deuFreq);
                            }
                            SPHelper.setBooleanValue2SP(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_SNAPSHOT, true);
                        }
                    } else if (EGContext.MODULE_NET.equals(module)) {
                        // 0不收集，跳过
                        if (EGContext.DEFAULT_ZERO.equals(status)) {
                            SPHelper.setBooleanValue2SP(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_NET, false);
                            continue;
                        } else {// 1收集,默认值即为轮询的值，忽略最小最大
                            if (deuFreq != 0) {
                                //                            getEditor().putLong(EGContext.SP_SNAPSHOT_CYCLE, deuFreq);
                                SPHelper.setIntValue2SP(mContext, EGContext.SP_NET_CYCLE, deuFreq);
                            }
                            SPHelper.setBooleanValue2SP(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_NET, true);
                        }
                    } else if (EGContext.MODULE_USM.equals(module)) {
                        if (EGContext.DEFAULT_ZERO.equals(status)) {// 0不收集，跳过
                            //                        setSp(UploadKey.Response.RES_POLICY_MODULE_CL_SNAPSHOT, false);
                            SPHelper.setBooleanValue2SP(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_USM, false);
                            continue;
                        } else {// 1收集
                            SPHelper.setBooleanValue2SP(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_USM, true);
                        }
                    } else if (EGContext.MODULE_CUT_NET.equals(module)) {
                        if (EGContext.DEFAULT_ZERO.equals(status)) {// 0不短路
                            //                        setSp(UploadKey.Response.RES_POLICY_MODULE_CL_SNAPSHOT, false);
                            SPHelper.setBooleanValue2SP(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_USM_CUTOF_NET, false);
                            continue;
                        } else {// 1短路
                            SPHelper.setBooleanValue2SP(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_USM_CUTOF_NET, true);
                        }
                    } else if (EGContext.MODULE_CUT_OC.equals(module)) {
                        if (EGContext.DEFAULT_ZERO.equals(status)) {// 0不短路
                            //                        setSp(UploadKey.Response.RES_POLICY_MODULE_CL_SNAPSHOT, false);
                            SPHelper.setBooleanValue2SP(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_USM_CUTOF_OC, false);
                            continue;
                        } else {// 1短路
                            SPHelper.setBooleanValue2SP(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_USM_CUTOF_OC, true);
                        }
                    } else if (EGContext.MODULE_CUT_XXX.equals(module)) {
                        if (EGContext.DEFAULT_ZERO.equals(status)) {// 0不短路
                            //                        setSp(UploadKey.Response.RES_POLICY_MODULE_CL_SNAPSHOT, false);
                            SPHelper.setBooleanValue2SP(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_USM_CUTOF_XXX, false);
                            continue;
                        } else {// 1短路
                            SPHelper.setBooleanValue2SP(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_USM_CUTOF_XXX, true);
                        }
                    } else if (EGContext.MODULE_WIFI.equals(module)) {
                        if (EGContext.DEFAULT_ZERO.equals(status)) {// 0不收集，跳过
                            //                        setSp(UploadKey.Response.RES_POLICY_MODULE_CL_WIFI, false);
                            SPHelper.setBooleanValue2SP(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_WIFI, false);

                            continue;
                        } // 1收集,默认值即为轮询的值，忽略最小最大,WIFI不轮询
                        else {
                            SPHelper.setBooleanValue2SP(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_WIFI, true);
                        }
                    } else if (EGContext.MODULE_BASE.equals(module)) {
                        // 0不收集，跳过
                        if (EGContext.DEFAULT_ZERO.equals(status)) {
                            SPHelper.setBooleanValue2SP(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_BASE, false);
                            continue;
                        } else {
                            // 1收集,默认值即为轮询的值，忽略最小最大,基站不轮询
                            SPHelper.setBooleanValue2SP(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_BASE, true);
                        }
                    } else if (EGContext.MODULE_DEV.equals(module)) {
                        if (EGContext.DEFAULT_ZERO.equals(status)) {// 0不收集，跳过
                            SPHelper.setBooleanValue2SP(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_DEV, false);

                            continue;
                        } else {
                            // 1收集,默认值即为轮询的值，忽略最小最大,基本信息不轮询，发送时候现收集
                            SPHelper.setBooleanValue2SP(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_DEV, true);
                        }
                        array = obj.optJSONArray(UploadKey.Response.RES_POLICY_CTRL_SUB_CONTROL);
                        subModuleHandle(array, subList, "dev");
                    } else if (EGContext.MODULE_XXX.equals(module)) {
                        // 0不收集，跳过
                        if (EGContext.DEFAULT_ZERO.equals(status)) {
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
                    if (subList.length() > 0) {
                        responseCtrlInfo.put(UploadKey.Response.RES_POLICY_CTRL_SUB_CONTROL, subList);
                    }
                    list.put(responseCtrlInfo);
                }
            } catch (Throwable igone) {
                if (BuildConfig.ENABLE_BUG_REPORT) {
                    BugReportForTest.commitError(igone);
                }
            }
        }
        if (list.length() < 1) {
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
                        SPHelper.setBooleanValue2SP(mContext, key, false);
                    }
                }
            }
        }

    }

    private void subModuleHandle(JSONArray array, JSONArray subList, String tag) throws JSONException {
        JSONObject subResponseCtrlInfo;
        JSONObject subObj;
        String sub_unWanted;
        String sub_status, sub_module;
        String unCollected = "0";
        if (array != null && array.length() > 0) {
            for (int j = 0; j < array.length(); j++) {
                subObj = (JSONObject) array.get(j);
                subResponseCtrlInfo = new JSONObject();
                sub_status = subObj.optString(UploadKey.Response.RES_POLICY_CTRL_STATUS);
                subResponseCtrlInfo.put(UploadKey.Response.RES_POLICY_CTRL_SUB_STATUS, sub_status);
                sub_module = subObj.optString(UploadKey.Response.RES_POLICY_CTRL_SUB_MODULE);
                subResponseCtrlInfo.put(UploadKey.Response.RES_POLICY_CTRL_SUB_MODULE, sub_module);
                sub_unWanted = subObj.optString(UploadKey.Response.RES_POLICY_CTRL_EXCLUDE, "");
                subResponseCtrlInfo.put(UploadKey.Response.RES_POLICY_CTRL_SUB_UNWANTED, sub_unWanted);
                if (!TextUtils.isEmpty(sub_module)) {
                    unWantedKeysHandle(sub_unWanted);
                    if ("dev".equals(tag)) {
                        if (EGContext.BLUETOOTH.equals(sub_module)) {
                            // 0不收集，跳过
                            if (unCollected.equals(sub_status)) {
                                SPHelper.setBooleanValue2SP(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_BLUETOOTH, false);
                                continue;
                            }
                            SPHelper.setBooleanValue2SP(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_BLUETOOTH, true);
                        } else if (EGContext.BATTERY.equals(sub_module)) {
                            // 0不收集，跳过
                            if (unCollected.equals(sub_status)) {
                                SPHelper.setBooleanValue2SP(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_BATTERY, false);
                                continue;
                            }
                            SPHelper.setBooleanValue2SP(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_BATTERY, true);
                        } else if (EGContext.SENSOR.equals(sub_module)) {
                            // 0不收集，跳过
                            if (unCollected.equals(sub_status)) {
                                SPHelper.setBooleanValue2SP(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_SENSOR, false);
                                continue;
                            }
                            SPHelper.setBooleanValue2SP(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_SENSOR, true);
                        } else if (EGContext.SYSTEM_INFO.equals(sub_module)) {
                            // 0不收集，跳过
                            if (unCollected.equals(sub_status)) {
                                SPHelper.setBooleanValue2SP(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_KEEP_INFO, false);
                                continue;
                            }
                            SPHelper.setBooleanValue2SP(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_KEEP_INFO, true);
                        } else if (EGContext.DEV_FURTHER_DETAIL.equals(sub_module)) {
                            // 0不收集，跳过
                            if (unCollected.equals(sub_status)) {
                                SPHelper.setBooleanValue2SP(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_MORE_INFO, false);
                                continue;
                            }
                            SPHelper.setBooleanValue2SP(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_MORE_INFO, true);
                        } else if (EGContext.PREVENT_CHEATING.equals(sub_module)) {
                            // 0不收集，跳过
                            if (unCollected.equals(sub_status)) {
                                SPHelper.setBooleanValue2SP(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_DEV_CHECK, false);
                                continue;
                            }
                            SPHelper.setBooleanValue2SP(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_DEV_CHECK, true);
                        }
                    } else if ("xxx".equals(tag)) {
                        if (EGContext.PROC.equals(sub_module)) {
                            // 0不收集，跳过
                            if (unCollected.equals(sub_status)) {
                                SPHelper.setBooleanValue2SP(mContext, ProcUtils.RUNNING_RESULT, false);
                                continue;
                            }
                            SPHelper.setBooleanValue2SP(mContext, ProcUtils.RUNNING_RESULT, true);
                        } else if (EGContext.XXX_TIME.equals(sub_module)) {
                            // 0不收集，跳过
                            if (unCollected.equals(sub_status)) {
                                SPHelper.setBooleanValue2SP(mContext, ProcUtils.RUNNING_TIME, false);
                                continue;
                            }
                            SPHelper.setBooleanValue2SP(mContext, ProcUtils.RUNNING_TIME, true);
                        } else if (EGContext.OCR.equals(sub_module)) {
                            // 0不收集，跳过
                            if (unCollected.equals(sub_status)) {
                                SPHelper.setBooleanValue2SP(mContext, ProcUtils.RUNNING_OC_RESULT, false);

                                continue;
                            }
                            SPHelper.setBooleanValue2SP(mContext, ProcUtils.RUNNING_OC_RESULT, true);
                        }
                    }
                }
                if (subResponseCtrlInfo.length() > 0) {
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
            return !newPolicyVer.equals(SPHelper.getStringValueFromSP(mContext, UploadKey.Response.RES_POLICY_VERSION, ""));
        } else {
            return false;
        }
    }

    public void setNormalUploadUrl(Context context) {
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

//        EGContext.NORMAL_APP_URL = EGContext.URL_SCHEME_HTTPS + EGContext.NORMAL_UPLOAD_URL[index] + EGContext.HTTPS_PORT;

        if (BuildConfig.isUseHttps) {
            EGContext.NORMAL_APP_URL = EGContext.URL_SCHEME_HTTPS + EGContext.NORMAL_UPLOAD_URL[index] + EGContext.HTTPS_PORT;
        } else {
            EGContext.NORMAL_APP_URL = EGContext.URL_SCHEME_HTTP + EGContext.NORMAL_UPLOAD_URL[index] + EGContext.HTTP_PORT;
        }
    }

    public void updatePolicyForReceiver(Intent intent) {
        // reload sp
        SPHelper.reInit();
    }

    public void printInfo() {
        //最后打印对一下数据是不是对的
        if (BuildConfig.logcat) {
            ELOG.i(BuildConfig.tag_upload + "[POLICY]", "=========同步策略 打印对一下数据对不对 ");
            ELOG.i(BuildConfig.tag_upload + "[POLICY]", UploadKey.
                    Response.RES_POLICY_VERSION + ":" +
                    SPHelper.getStringValueFromSP(mContext, UploadKey.Response.RES_POLICY_VERSION, ""));
            ELOG.i(BuildConfig.tag_upload + "[POLICY]", UploadKey.
                    Response.RES_POLICY_FAIL_COUNT + ":" +
                    SPHelper.getIntValueFromSP(mContext, UploadKey.Response.RES_POLICY_FAIL_COUNT, -1));
            ELOG.i(BuildConfig.tag_upload + "[POLICY]", UploadKey.
                    Response.RES_POLICY_FAIL_TRY_DELAY + ":" +
                    SPHelper.getLongValueFromSP(mContext, UploadKey.Response.RES_POLICY_FAIL_TRY_DELAY, -1));
            ELOG.i(BuildConfig.tag_upload + "[POLICY]", UploadKey.
                    Response.RES_POLICY_TIMER_INTERVAL + ":" +
                    SPHelper.getLongValueFromSP(mContext, UploadKey.Response.RES_POLICY_TIMER_INTERVAL, -1));
            ELOG.i(BuildConfig.tag_upload + "[POLICY]", UploadKey.
                    Response.RES_POLICY_CTRL_LIST + ":" +
                    SPHelper.getStringValueFromSP(mContext, UploadKey.Response.RES_POLICY_CTRL_LIST, ""));
            ELOG.i(BuildConfig.tag_upload + "[POLICY]", UploadKey.Response.PatchResp.PATCH_VERSION + "_HotFix:" +
                    SPHelper.getStringValueFromSP(mContext, UploadKey.Response.PatchResp.PATCH_VERSION, ""));
            ELOG.i(BuildConfig.tag_upload + "[POLICY]", UploadKey.Response.PatchResp.PATCH_SIGN + "_HotFix:" +
                    SPHelper.getStringValueFromSP(mContext, UploadKey.Response.PatchResp.PATCH_SIGN, ""));
            ELOG.i(BuildConfig.tag_upload + "[POLICY]", UploadKey.Response.PatchResp.PATCH_VERSION + "_HotFix:" +
                    SPHelper.getStringValueFromSP(mContext, UploadKey.Response.PatchResp.PATCH_VERSION, ""));
            ELOG.i(BuildConfig.tag_upload + "[POLICY]", UploadKey.Response.PatchResp.PATCH_SIGN + "_HotFix:" +
                    SPHelper.getStringValueFromSP(mContext, UploadKey.Response.PatchResp.PATCH_SIGN, ""));
            ELOG.i(BuildConfig.tag_upload + "[POLICY]", "=========同步策略 打印对一下数据对不对结束 ");
        }
    }

    private static class Holder {
        private static final PolicyImpl INSTANCE = new PolicyImpl();
    }

}
