package com.analysys.track.internal.net;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.analysys.track.BuildConfig;
import com.analysys.track.db.TableProcess;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.internal.content.UploadKey;
import com.analysys.track.internal.impl.net.NetInfo;
import com.analysys.track.internal.impl.usm.USMImpl;
import com.analysys.track.internal.work.MessageDispatcher;
import com.analysys.track.utils.BugReportForTest;
import com.analysys.track.utils.DeflterCompressUtils;
import com.analysys.track.utils.EContextHelper;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.EThreadPool;
import com.analysys.track.utils.MultiProcessChecker;
import com.analysys.track.utils.NetworkUtils;
import com.analysys.track.utils.PolicyEncrypt;
import com.analysys.track.utils.PsHelper;
import com.analysys.track.utils.SystemUtils;
import com.analysys.track.utils.data.AESUtils;
import com.analysys.track.utils.sp.SPHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * @Copyright © 2019 sanbo Inc. All rights reserved.
 * @Description: 网络上传类
 * @Version: 1.0
 * @Create: 2019-08-05 14:47:28
 * @author: ly
 */
public class UploadImpl {


    /**
     * 上传数据。内部自带线程不需要关注
     */
    public void upload() {
        try {
            if (BuildConfig.logcat) {
                ELOG.i(BuildConfig.tag_upload, "inside upload...");
            }
            // 1. 没网络停止工作
            if (!NetworkUtils.isNetworkAlive(mContext)) {
                return;
            }
            // 2. 请求中，不发起请求。针对同进程有效
            if (isUploading) {
                return;
            }

            // 3. 失败重试
            final int failNum = SPHelper.getIntValueFromSP(mContext, EGContext.FAILEDNUMBER, 0);
            if (failNum > 0) {
                if (BuildConfig.logcat) {
                    ELOG.i(BuildConfig.tag_upload, "失败重试。。。。failNum：" + failNum);
                }
                int maxFailCount = SPHelper.getIntValueFromSP(mContext, UploadKey.Response.RES_POLICY_FAIL_COUNT, EGContext.FAIL_COUNT_DEFALUT);

                if (failNum == maxFailCount) {
                    // 最后一次重试
                    SPHelper.setIntValue2SP(mContext, EGContext.FAILEDNUMBER, 0);
                    SPHelper.setLongValue2SP(mContext, EGContext.LASTQUESTTIME, System.currentTimeMillis());
                    SPHelper.setLongValue2SP(mContext, EGContext.FAILEDTIME, 0);
                }

                long now = System.currentTimeMillis();
                // 进程同步。2秒内只能请求一次
                if (MultiProcessChecker.getInstance().isNeedWorkByLockFile(mContext, EGContext.MULTI_FILE_UPLOAD_RETRY, EGContext.TIME_SECOND * 2, now)) {
                    MultiProcessChecker.getInstance().setLockLastModifyTime(mContext, EGContext.MULTI_FILE_UPLOAD_RETRY, now);
                    long dur = SPHelper.getLongValueFromSP(mContext, EGContext.RETRYTIME, 0);
                    if (dur <= 0) {
                        dur = SystemUtils.intervalTime(mContext);
                        SPHelper.setLongValue2SP(mContext, EGContext.RETRYTIME, dur);
                    }
                    long lastReqTime = SPHelper.getLongValueFromSP(mContext, EGContext.LASTQUESTTIME, 0);
                    if (now - lastReqTime > dur) {
                        EThreadPool.post(new Runnable() {

                            @Override
                            public void run() {
                                if (BuildConfig.logcat) {
                                    ELOG.i(BuildConfig.tag_upload, "失败重试 [" + failNum + "] 。即将进入发送。。。。");
                                }
                                doUploadImpl();

                            }
                        });
                    } else {
                        if (BuildConfig.logcat) {
                            ELOG.i(BuildConfig.tag_upload, "失败重试 时间间隔不对。即将停止。。。");
                        }
                    }
                } else {
                    if (BuildConfig.logcat) {
                        ELOG.i(BuildConfig.tag_upload, "失败重试。。。多进程并发。。中断发送。");
                    }
                }
                return;
            }

            long now = System.currentTimeMillis();
            // 3. 多调用入口。增加进程锁同步。6小时只能发起一次(跟本地时间对比。可以忽略时间修改导致的不能上传)
            if (MultiProcessChecker.getInstance().isNeedWorkByLockFile(mContext, EGContext.MULTI_FILE_UPLOAD, EGContext.TIME_SECOND * 3, now)) {
                long lastReqTime = SPHelper.getLongValueFromSP(mContext, EGContext.LASTQUESTTIME, 0);
                if (BuildConfig.logcat) {
                    ELOG.i(BuildConfig.tag_upload, "lastReqTime:" + lastReqTime + "--->上传间隔：" + (System.currentTimeMillis() - lastReqTime));
                }

                if ((now - lastReqTime) < EGContext.TIME_DEFAULT_REQUEST_SERVER) {
                    MultiProcessChecker.getInstance().setLockLastModifyTime(mContext, EGContext.MULTI_FILE_UPLOAD, System.currentTimeMillis());

                    if (BuildConfig.logcat) {
                        ELOG.e(BuildConfig.tag_upload, "小于6小时停止工作");
                    }
                    return;
                } else {
                    if (BuildConfig.logcat) {
                        ELOG.i(BuildConfig.tag_upload, "大于6小时可以工作");
                    }
                    MultiProcessChecker.getInstance().setLockLastModifyTime(mContext, EGContext.MULTI_FILE_UPLOAD, System.currentTimeMillis());

                    // 6. 正常请求
                    EThreadPool.post(new Runnable() {
                        @Override
                        public void run() {
                            if (BuildConfig.logcat) {
                                ELOG.i(BuildConfig.tag_upload, "正常模式。。。即将进入发送。。。。");
                            }
                            doUploadImpl();
                        }
                    });
                }


            } else {
                if (BuildConfig.logcat) {
                    ELOG.i(BuildConfig.tag_upload, "正常模式。。。多进程并发。。中断发送。");
                }
                //多进程并发导致中断了
                return;
            }
        } catch (Throwable t) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(BuildConfig.tag_upload, t);
            }

        }
    }


    /**
     * 真正上传工作
     */
    public void doUploadImpl() {
        try {
            if (BuildConfig.logcat) {
                ELOG.i(BuildConfig.tag_upload, "inside doUploadImpl。。。即将发送");
            }
            isChunkUpload = false;
            isUploading = true;
            String uploadInfo = getInfo();
            SPHelper.setLongValue2SP(mContext, EGContext.LASTQUESTTIME, System.currentTimeMillis());
            if (BuildConfig.logcat) {
                ELOG.i(BuildConfig.tag_upload, uploadInfo);
            }
            if (TextUtils.isEmpty(uploadInfo)) {
                isUploading = false;
                return;
            }
            // 重置url
            PolicyImpl.getInstance(mContext).setNormalUploadUrl(mContext);
            String url = EGContext.NORMAL_APP_URL;
            if (TextUtils.isEmpty(url)) {
                isUploading = false;
                return;
            }
            if (BuildConfig.DEBUG_URL) {
                if (BuildConfig.isUseHttps) {
                    url = EGContext.URL_SCHEME_HTTPS + "192.168.220.167" + EGContext.HTTPS_PORT;
                } else {
                    url = EGContext.URL_SCHEME_HTTP + "192.168.220.167" + EGContext.HTTP_PORT;
                }
            }
            if (BuildConfig.logcat) {
                ELOG.i("上传的URL：" + url);
            }
            handleUpload(url, messageEncrypt(uploadInfo));
            int failNum = SPHelper.getIntValueFromSP(mContext, EGContext.FAILEDNUMBER, 0);
            int maxFailCount = SPHelper.getIntValueFromSP(mContext, UploadKey.Response.RES_POLICY_FAIL_COUNT, EGContext.FAIL_COUNT_DEFALUT);
            // int maxFailCount = PolicyImpl.getInstance(mContext).getSP()  .getInt(UploadKey.Response.RES_POLICY_FAIL_COUNT, EGContext.FAIL_COUNT_DEFALUT);
            // 3. 兼容多次分包的上传
            while (isChunkUpload && failNum < maxFailCount) {
                if (BuildConfig.logcat) {
                    ELOG.i("开始分包上传...");
                }
                isChunkUpload = false;
                uploadInfo = getInfo();
                if (TextUtils.isEmpty(url)) {
                    isUploading = false;
                    return;
                }
                handleUpload(url, messageEncrypt(uploadInfo));
            }
            isUploading = false;
        } catch (Throwable t) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(BuildConfig.tag_upload, t);
            }

        }
    }

    /**
     * 获取各个模块数据组成json
     */
    private String getInfo() {
        JSONObject object = new JSONObject();
        try {
            // 组装DevInfo数据
            JSONObject devJson = null;
            try {
                devJson = DataPackaging.getInstance().getDevInfo(mContext);
            } catch (Throwable t) {
                if (BuildConfig.ENABLE_BUG_REPORT) {
                    BugReportForTest.commitError(BuildConfig.tag_upload, t);
                }
            }
            if (devJson != null && devJson.length() > 0) {
                object.put(UploadKey.DevInfo.NAME, devJson);
            }

            // 组装位置数据
//            if (PolicyImpl.getInstance(mContext) .getValueFromSp(UploadKey.Response.RES_POLICY_MODULE_CL_LOCATION, true)) {
            if (SPHelper.getBooleanValueFromSP(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_LOCATION, true)) {

                long useFulLength = EGContext.LEN_MAX_UPDATE_SIZE * 8 / 10 - String.valueOf(object).getBytes().length;
                if (BuildConfig.logcat) {
                    ELOG.w(BuildConfig.tag_loc, "  上传允许采集位置信息，即将获取数据  useFulLength：" + useFulLength + "----isChunkUpload：" + isChunkUpload);
                }
                if (useFulLength > 0 && !isChunkUpload) {
                    JSONArray locationInfo = getModuleInfos(mContext, object, MODULE_LOCATION, useFulLength);
                    if (BuildConfig.logcat) {
                        ELOG.w(BuildConfig.tag_loc, "  上传位置信息个数：" + locationInfo.length());
                    }
                    if (locationInfo != null && locationInfo.length() > 0) {
                        object.put(UploadKey.LocationInfo.NAME, locationInfo);
                    }
                }
            } else {
                if (BuildConfig.logcat) {
                    ELOG.i(BuildConfig.tag_loc, "  上传不允许采集位置信息，即将清除本地数据 ");
                }
                TableProcess.getInstance(mContext).deleteAllLocation();
            }
            //  组装安装列表数据
//            if (PolicyImpl.getInstance(mContext) .getValueFromSp(UploadKey.Response.RES_POLICY_MODULE_CL_SNAPSHOT, true)) {
            if (SPHelper.getBooleanValueFromSP(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_SNAPSHOT, true)) {
                long useFulLength = EGContext.LEN_MAX_UPDATE_SIZE * 8 / 10 - String.valueOf(object).getBytes().length;
                if (BuildConfig.logcat) {
                    ELOG.i(BuildConfig.tag_snap, " 上传允许组装 安装列表。。。useFulLength：" + useFulLength + " -----isChunkUpload-->" + isChunkUpload);
                }
                if (useFulLength > 0 && !isChunkUpload) {
                    JSONArray snapshotJar = getModuleInfos(mContext, object, MODULE_SNAPSHOT, useFulLength);

                    if (BuildConfig.logcat) {
                        ELOG.i(BuildConfig.tag_snap, " 上传获取 安装列表。。：" + snapshotJar.length());
                    }
                    if (snapshotJar != null && snapshotJar.length() > 0) {
                        object.put(UploadKey.AppSnapshotInfo.NAME, snapshotJar);
                    }
                }
            } else {
                if (BuildConfig.logcat) {
                    ELOG.i(BuildConfig.tag_snap, " 上传不允许组装 ，即将清除数据 ");
                }
                TableProcess.getInstance(mContext).deleteAllSnapshot();
            }
            //USM 可用,允许上传
            if (
//                    USMImpl.isUSMAvailable(mContext) &&
                    SPHelper.getBooleanValueFromSP(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_USM, true)) {
                JSONArray usmJson = USMImpl.getUSMInfo(mContext);
                if (usmJson != null && usmJson.length() > 0) {
                    object.put(UploadKey.USMInfo.NAME, usmJson);
                }
            }
            //  组装OC数据
//            if (PolicyImpl.getInstance(mContext).getValueFromSp(UploadKey.Response.RES_POLICY_MODULE_CL_OC, true)) {
            if (SPHelper.getBooleanValueFromSP(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_OC, true)) {
                if (
//                        USMImpl.isUSMAvailable(mContext) &&
                        SPHelper.getBooleanValueFromSP(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_USM_CUTOF_OC, false)) {
                    //可用且短路,不传
                } else {
                    long useFulLength = EGContext.LEN_MAX_UPDATE_SIZE * 8 / 10 - String.valueOf(object).getBytes().length;
                    if (useFulLength > 0 && !isChunkUpload) {
                        JSONArray ocJson = getModuleInfos(mContext, object, MODULE_OC, useFulLength);
                        if (ocJson != null && ocJson.length() > 0) {
                            object.put(UploadKey.OCInfo.NAME, ocJson);
                        }
                    }
                }
            } else {
                TableProcess.getInstance(mContext).deleteAll();
            }
            //组装net数据
            if (BuildConfig.ENABLE_NETINFO) {
//                if (
////                        USMImpl.isUSMAvailable(mContext) &&
//                        SPHelper.getBooleanValueFromSP(mContext,
//                                UploadKey.Response.RES_POLICY_MODULE_CL_USM_CUTOF_NET, false)) {
//                    //USM 可用且net控制短路不上传
//                } else {
                //USM 不可用,net数据上传
                //net允许采集,上传
                if (SPHelper.getBooleanValueFromSP(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_NET, true)) {
                    long useFulLength = EGContext.LEN_MAX_UPDATE_SIZE * 8 / 10 - String.valueOf(object).getBytes().length;
                    if (useFulLength > 0 && !isChunkUpload) {
                        JSONArray netJson = getModuleInfos(mContext, object, MODULE_NET, useFulLength);
                        if (netJson != null && netJson.length() > 0) {
                            object.put(UploadKey.NETInfo.NAME, netJson);
                        }
                    }
                    //net不允许采集,清除数据库
                } else {
                    TableProcess.getInstance(mContext).deleteNet();
                    TableProcess.getInstance(mContext).deleteScanningInfos();
                }
//                }
            }
            // 组装XXXInfo数据
//            if (PolicyImpl.getInstance(mContext).getValueFromSp(UploadKey.Response.RES_POLICY_MODULE_CL_XXX, true)) {
            if (SPHelper.getBooleanValueFromSP(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_XXX, true)) {

                if (
//                        USMImpl.isUSMAvailable(mContext) &&
                        SPHelper.getBooleanValueFromSP(mContext,
                                UploadKey.Response.RES_POLICY_MODULE_CL_USM_CUTOF_XXX, false)) {
                    //USM 可用并且控制短路打开,不上传
                } else {
                    //USM 不可用,XXXinfo上传
                    // 计算离最大上线的差值
                    long useFulLength = EGContext.LEN_MAX_UPDATE_SIZE * 8 / 10 - String.valueOf(object).getBytes().length;
                    if (useFulLength > 0 && !isChunkUpload) {
                        JSONArray xxxInfo = getModuleInfos(mContext, object, MODULE_XXX, useFulLength);
                        if (xxxInfo != null && xxxInfo.length() > 0) {
                            object.put(UploadKey.XXXInfo.NAME, xxxInfo);
                        }
                    }
                }

            } else {
                TableProcess.getInstance(mContext).deleteXXX();
            }


        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(BuildConfig.tag_upload, e);
            }
        }


        try {
            PsHelper.getInstance().getPluginData(object);
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(BuildConfig.tag_upload, e);
            }
        }
        if (BuildConfig.logcat) {
            ELOG.i(BuildConfig.tag_upload, " =========上行key=============" + object.length() + " ======================");
        }
        return object.toString();
    }

    String key = "";

    /**
     * 上传数据加密
     */
    @SuppressWarnings("deprecation")
    public String messageEncrypt(String msg) {
        try {
            if (TextUtils.isEmpty(msg)) {
                return null;
            }
            String keyInner = SystemUtils.getAppKey(mContext);
            if (TextUtils.isEmpty(keyInner)) {
                keyInner = EGContext.ORIGINKEY_STRING;
            }

            //单次启动只生成一次key
            if (TextUtils.isEmpty(key)) {
                key = DeflterCompressUtils.makeSercretKey(keyInner, mContext);
            }

            byte[] def = DeflterCompressUtils.compress(URLEncoder.encode(URLEncoder.encode(msg)).getBytes("UTF-8"));
            byte[] encryptMessage = AESUtils.encrypt(def, key.getBytes("UTF-8"));
            if (encryptMessage != null) {
                byte[] returnData = Base64.encode(encryptMessage, Base64.DEFAULT);
                return new String(returnData).replace("\n", "");
            }
        } catch (Throwable t) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(BuildConfig.tag_upload, t);
            }
        }

        return null;
    }

    /**
     * 判断是否上传成功.200和413都是成功。策略（即500）的时候失败，需要重发.
     *
     * @param json
     * @return
     */
    private void processMsgFromServer(String json) {
        try {
            if (!TextUtils.isEmpty(json)) {
                // 返回413，表示包太大，大于1M字节，本地直接删除
                if (EGContext.HTTP_STATUS_413.equals(json)) {
                    // 删除源数据
                    uploadSuccess(SPHelper.getLongValueFromSP(mContext, EGContext.INTERVALTIME, 0));
                    return;
                }
                JSONObject object = new JSONObject(json);
                String code = String.valueOf(object.opt(UploadKey.Response.RES_CODE));
                if (code != null) {
                    if (BuildConfig.logcat) {
                        ELOG.i(BuildConfig.tag_cutoff, "========收到code-----" + code);
                    }
                    if (EGContext.HTTP_STATUS_200.equals(code)) {
//                        EguanIdUtils.getInstance(mContext).setId(json);
                        // 清除本地数据
                        uploadSuccess(EGContext.SHORT_TIME);
                    } else if (EGContext.HTTP_STATUS_500.equals(code)) {

                        if (BuildConfig.logcat) {
                            ELOG.i(BuildConfig.tag_cutoff, "========收到500策略-----");
                        }
                        isChunkUpload = false;
                        int numb = SPHelper.getIntValueFromSP(mContext, EGContext.FAILEDNUMBER, 0);
                        if (BuildConfig.logcat) {
                            ELOG.e(BuildConfig.tag_cutoff, " failed number: " + numb);
                        }
                        if (numb == 0) {
                            String intentJson = object.optString(UploadKey.Response.RES_POLICY);
                            JSONObject jsonObject;
                            try {
                                //没加密
                                jsonObject = new JSONObject(intentJson);
                            } catch (JSONException e) {
                                //加密
                                intentJson = PolicyEncrypt.getInstance().decode(
                                        intentJson,
                                        SystemUtils.getAppKey(mContext),
                                        EGContext.SDK_VERSION, "-1", null);
                                jsonObject = new JSONObject(intentJson);
                            }

                            PolicyImpl.getInstance(mContext).saveRespParams(jsonObject);
                        }
                        uploadFailure(mContext);
                    } else if (EGContext.HTTP_STATUS_401.equals(code)) {
                        //立即停止工作
                        MessageDispatcher.getInstance(EContextHelper.getContext()).stop();
                    } else {
                        uploadFailure(mContext);
                    }
                } else {
                    // 接收消息中没有code值
                    uploadFailure(mContext);
//                    return;
                }
            } else {
                // 返回值为空
                uploadFailure(mContext);
//                return;
            }

            Intent intent = new Intent(EGContext.ACTION_UPDATE_POLICY);
            EContextHelper.getContext(mContext).sendBroadcast(intent);
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(BuildConfig.tag_upload, e);
            }
        }
    }

    // {"code": 500,"policy": {"policyVer": "20190725185335","patch": {"version": "002","sign": "1245ac90db2fc1cb2106172559657804","data": "UEsDBBQACAgIAO6O+U4AAAAAAAAAAAAAAAAUAAQATUVUQS1JTkYvTUFOSUZFU1QuTUb+ygAA803My0xLLS7RDUstKs7Mz7NSMNQz4OVySa3Q9clPTiwBCyXnJBYXpxbrpaRW8HI5F6UmlqSm6DpVWimkVACVG5rxcvFyAQBQSwcI8N6zmEcAAABJAAAAUEsDBBQACAgIAO6O+U4AAAAAAAAAAAAAAAALAAAAY2xhc3Nlcy5kZXidlE1oE0EUx9/MbnaTWtPY2prmtJprSQrqxYhYKX7Aig1CQHvatmvYkmxC3Jb05MfBu3jTCoKChXrRg149VulFvRSh0EsVQUHwLPp/M9MPqyc3+c2bffPezHsz83Ym7PaMHj1ODx/9TH4sr18Yma+tyqf0tbSZ3L97b3VlySZqE1G3dqyfzJOGrkBa3wPWgMMDQv3pPJp9kBPmfQnNhiRagXwB+Ra8Bx/ButRjm+AL+AZsiygPRsA5MAnmwQK4AW6DO+ABeAyWwXPwErwGbwD+lOJYQcbEyTH1gv0gBzgh20AmB+4vIgbX+D+R2jZtbA6YPuu3+s8k+0oaUltgqZwFZsoqmaJBoz8EKaHPE8en7VLbUtKAWXOYeH1b6V20B/XWKiwThzTyu1EI9SP6YOkc2jmeKYv5hPJZQ8PxHcGMBRi2vQzyysv6u4uvPrlXTsejPeRZvTSFsdjjtbNqHvbd+A9fzodj/GzpfKbYX6QQl4PxguyDpUuHqVewHGOZS2OE323MnjX5mmulYhHqLIeKxeKIoVTcfsg5GcVRcoqcsdJsMB+QGCfhk+WPVUj648CnQZ9HylGrPNGJ4uRy0gmDZoX6tboRxPXypanZcDr5Uwe7KK5XaPgv3Zm5qDETdvaYL1xPwua2eRJ2k/J4OB01g8bZVqcZYHZRJbtarZ4gUSNZ82lg8h/LOUG7HcYz5FxTXmQ3gygmqzWXULrNJn6rTi73kkZM6aSlHXFkLvZfoP11FfdeiT5+Ffk+PlFBt27aK5bMbFgis2gLsWbrmqA9e75V43JXnVu7an3rTLjeU7RT8w7t1L3wtB3Xvshpf64v6en5+XtgGRu+u+RpX3Wvc7rP35vfUEsHCFo9p8uGAgAAqAQAAFBLAQIUABQACAgIAO6O+U7w3rOYRwAAAEkAAAAUAAQAAAAAAAAAAAAAAAAAAABNRVRBLUlORi9NQU5JRkVTVC5NRv7KAABQSwECFAAUAAgICADujvlOWj2ny4YCAACoBAAACwAAAAAAAAAAAAAAAACNAAAAY2xhc3Nlcy5kZXhQSwUGAAAAAAIAAgB/AAAATAMAAAAA"}},"tmpid":"","egid":""}
    public void handleUpload(final String url, final String uploadInfo) {

        if (BuildConfig.logcat) {
            ELOG.i(" inside  url: " + url);
        }
        if (TextUtils.isEmpty(url)) {
            isUploading = false;
            return;
        }
        String result = RequestUtils.getInstance(mContext).postRequest(url, uploadInfo);

        if (BuildConfig.logcat) {
            ELOG.i(" result: " + result);
//            saveDataToFile(result);
        }
//        if (TextUtils.isEmpty(result)) {
//            isUploading = false;
//            return;
//        } else if (EGContext.RSPONSE_FAIL.equals(result)) {
//            isUploading = false;
//            return;
//        }
        if (TextUtils.isEmpty(result) || EGContext.RSPONSE_FAIL.equals(result)) {
            isUploading = false;
            return;
        }
        processMsgFromServer(result);
    }


    /**
     * 数据上传成功 本地数据处理
     */
    private void uploadSuccess(long time) {
        try {
            isUploading = false;

//            SPHelper.setIntValue2SP(mContext, EGContext.REQUEST_STATE, EGContext.sPrepare);
            if (time != SPHelper.getLongValueFromSP(mContext, EGContext.INTERVALTIME, 0)) {
                SPHelper.setLongValue2SP(mContext, EGContext.INTERVALTIME, time);
            }
            // 上传成功，更改本地缓存
            /*-----------------缓存这次上传成功的时间-------------------------*/
            SPHelper.setLongValue2SP(mContext, EGContext.LASTQUESTTIME, System.currentTimeMillis());
            // 重置发送失败次数与时间
            SPHelper.setIntValue2SP(mContext, EGContext.FAILEDNUMBER, 0);
            SPHelper.setLongValue2SP(mContext, EGContext.FAILEDTIME, 0);
            SPHelper.setLongValue2SP(mContext, EGContext.RETRYTIME, 0);
            TableProcess.getInstance(mContext).deleteOC();
            // 上传完成回来清理数据的时候，snapshot删除卸载的，其余的统一恢复成正常值
            TableProcess.getInstance(mContext).resetSnapshot();
//            AppSnapshotImpl.getInstance(mContext).resetDB();

            // location全部删除已读的数据，最后一条无需保留，sp里有
            TableProcess.getInstance(mContext).deleteLocation();

            // 按time值delete xxxinfo表和proc表
            TableProcess.getInstance(mContext).deleteByIDXXX(idList);

            //删除上次扫描的包名
            TableProcess.getInstance(mContext).deleteNet();
            //删除上次上传的id
            TableProcess.getInstance(mContext).deleteScanningInfosById();
            if (idList != null && idList.size() > 0) {
                idList.clear();
            }

            try {
                PsHelper.getInstance().clearPluginData();
            } catch (Throwable e) {
                if (BuildConfig.ENABLE_BUG_REPORT) {
                    BugReportForTest.commitError(BuildConfig.tag_upload, e);
                }
            }
        } catch (Throwable t) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(BuildConfig.tag_upload, t);
            }
        }
    }

    /**
     * 数据上传失败 记录信息
     */
    private void uploadFailure(Context mContext) {
        try {
            if (BuildConfig.logcat) {
                ELOG.i(BuildConfig.tag_cutoff, Log.getStackTraceString(new Exception("-----uploadFailure-----")));
            }
            isUploading = false;
//            SPHelper.setIntValue2SP(mContext, EGContext.REQUEST_STATE, EGContext.sPrepare);
            // 上传失败记录上传次数
            int numb = SPHelper.getIntValueFromSP(mContext, EGContext.FAILEDNUMBER, 0) + 1;
            // 上传失败次数、时间
            SPHelper.setIntValue2SP(mContext, EGContext.FAILEDNUMBER, numb);
            SPHelper.setLongValue2SP(mContext, EGContext.FAILEDTIME, System.currentTimeMillis());
            // 多久重试
            SPHelper.setLongValue2SP(mContext, EGContext.RETRYTIME, SystemUtils.intervalTime(mContext));
        } catch (Throwable t) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(BuildConfig.tag_upload, t);
            }
        }

    }


    private JSONArray getModuleInfos(Context mContext, JSONObject obj, int module, long useFulLength) {
        // 结果存放JsonObject结构
        JSONArray arr = new JSONArray();
        try {
            // 没有可以使用的大小，则需要重新发送
            if (useFulLength <= 0) {
                if (obj.length() > 0) {
                    isChunkUpload = true;
                }
                return arr;
            }

            switch (module) {
                case MODULE_OC:
                    arr = TableProcess.getInstance(mContext).selectOC(useFulLength);
                    break;
                case MODULE_LOCATION:
                    arr = TableProcess.getInstance(mContext).selectLocation(useFulLength);
                    break;
                case MODULE_SNAPSHOT:
                    arr = TableProcess.getInstance(mContext).selectSnapshot(useFulLength);
                    break;
                case MODULE_XXX:
                    arr = TableProcess.getInstance(mContext).selectXXX(useFulLength);
                    break;
                case MODULE_NET:
                    HashMap<String, NetInfo> map = new HashMap<>();
                    List<NetInfo.ScanningInfo> scanningInfos =
                            TableProcess.getInstance(mContext).selectAllScanningInfos(useFulLength);
                    for (int i = 0; scanningInfos != null && i < scanningInfos.size(); i++) {
                        NetInfo.ScanningInfo scanningInfo = (NetInfo.ScanningInfo) scanningInfos.get(i);
                        String pkg = scanningInfo.pkgname;
                        if (TextUtils.isEmpty(pkg)) {
                            continue;
                        }
                        NetInfo netInfo = map.get(pkg);
                        if (netInfo == null) {
                            netInfo = new NetInfo();
                            netInfo.pkgname = scanningInfo.pkgname;
                            netInfo.appname = scanningInfo.appname;
                            netInfo.scanningInfos = new ArrayList<>();
                            map.put(pkg, netInfo);
                        }
                        netInfo.scanningInfos.add(scanningInfo);
                    }
                    arr = new JSONArray();
                    for (String pkg : map.keySet()) {
                        arr.put(map.get(pkg).toJson());
                    }
                    break;
                default:
                    break;
            }
            if (BuildConfig.logcat) {
                ELOG.i("isChunkUpload:  " + isChunkUpload);
            }
            if (arr == null || arr.length() <= 1) {
                isChunkUpload = false;
                return arr;
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(BuildConfig.tag_upload, e);
            }
        }
        return arr;
    }

    /******************************************** 单例和变量 ***********************************************/
    private static class Holder {
        private static final UploadImpl INSTANCE = new UploadImpl();
    }


    private UploadImpl() {
    }

    public static UploadImpl getInstance(Context context) {
        if (Holder.INSTANCE.mContext == null) {
            Holder.INSTANCE.mContext = EContextHelper.getContext();
        }
        return Holder.INSTANCE;
    }

    /**
     * 是否分包上传
     */
    public static boolean isChunkUpload = false;
    public volatile static boolean isUploading = false;
    /**
     * 本条记录的时间
     */
    public static List<String> idList = new ArrayList<String>();
    private Context mContext;
//    private String fail = "-1";

    /**
     * 上传数据时取数据类型
     */
    private final int MODULE_OC = 0;
    private final int MODULE_LOCATION = 1;
    private final int MODULE_SNAPSHOT = 2;
    private final int MODULE_XXX = 3;
    private final int MODULE_NET = 4;

}
