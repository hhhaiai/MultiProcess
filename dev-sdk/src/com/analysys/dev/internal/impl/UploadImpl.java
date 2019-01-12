package com.analysys.dev.internal.impl;

import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.analysys.dev.database.TableAppSnapshot;
import com.analysys.dev.database.TableLocation;
import com.analysys.dev.database.TableOC;
import com.analysys.dev.database.TableOCCount;
import com.analysys.dev.internal.Content.EGContext;
import com.analysys.dev.utils.DeflterCompressUtils;
import com.analysys.dev.utils.EThreadPool;
import com.analysys.dev.utils.RequestUtils;
import com.analysys.dev.utils.Utils;
import com.analysys.dev.utils.reflectinon.EContextHelper;
import com.analysys.dev.internal.work.MessageDispatcher;

import org.json.JSONArray;
import org.json.JSONObject;
import com.analysys.dev.internal.impl.DeviceImpl;
import com.analysys.dev.utils.sp.SPHelper;

/**
 * @Copyright © 2018 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2018/10/20 14:57
 * @Author: Wang-X-C
 */
public class UploadImpl {
    Context mContext;
    private final String DI = "DevInfo";
    private final String ASI = "AppSnapshotInfo";
    private final String LI = "LocationInfo";
    private final String OCC = "OCCount";
    private final String OCI = "OCIount";

    private static class Holder {
        private static final UploadImpl INSTANCE = new UploadImpl();
    }

    public static UploadImpl getInstance(Context context) {
        if (Holder.INSTANCE.mContext == null) {
            Holder.INSTANCE.mContext = EContextHelper.getContext(context);
        }
        return Holder.INSTANCE;
    }

    // 上传数据
    public void upload() {
        EThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                String uploadInfo = getInfo();
//                byte[] encryptInfo = null;
                if (TextUtils.isEmpty(uploadInfo)) {
                    return;
                }
//                encryptInfo = messageEncrypt(uploadInfo);
                String url = URLGeneral.getURL();
                if (TextUtils.isEmpty(url)) {
                    return;
                }
                String returnInfo = RequestUtils.httpRequest(url, messageEncrypt(uploadInfo));
                if (TextUtils.isEmpty(returnInfo)) {
                    return;
                }
                // 策略处理
                if (!analysisReturnJson(result, id)) {
                    //L.i("upload response : " + result);
                    MyLog.ii(Constants.APP_TAG, "send message failed");
                    count++;
                    MyLog.d(Config.APP_TAG, "count:" + count + " server liminted count " +
                            "tiem:" + PolicyManger.getLocalPolicy(context).getFailCount());
                    //加入网络判断,如果无网情况下,不进行失败上传
                    if (count <= PolicyManger.getLocalPolicy(context).getFailCount()) {

                        long dur = (long)((Math.random() + 1) * delayInterval);
                        // 时间太久了，默认都是60秒以上
                        //  long dur = (long) (Math.random() * delayInterval);
                        //L.i("will retry dur====>" + dur);
                        EGQueue.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                upload();
                            }
                        }, dur);
                    } else if (PolicyManger.getLocalPolicy(context).isUserRTP()) {
                        long failTryDelayInterval = System.currentTimeMillis() +
                                PolicyManger.getLocalPolicy(context).getFailTryDelay();
                        AppSPUtils.getInstance(context).setFailTryDelay
                                (failTryDelayInterval);
                        Policy.getNativePolicy(context).savePermitForFailTime(context,
                                failTryDelayInterval);
                        MyLog.d(Config.APP_TAG, "failed delay:" + AppSPUtils.getInstance
                                (context).getFailTryDelay());
                    }
                } else {
                    //L.i("send message success");
                    MyLog.ii(Constants.APP_TAG, "send message success");
                    AppTableOperation.getInstance(context).deleteAppJson(id);
                    if (SystemUtils.isMainThread()) {
                        EGQueue.post(new SafeRunnable() {
                            @Override
                            public void safeRun() {
                                upload();

                            }
                        });
                    } else {
                        upload();
                    }
                    count = 0;
                }
            }
        });
        MessageDispatcher.getInstance(mContext).uploadInfo(EGContext.UPLOAD_CYCLE);
    }

    /**
     * 获取各个模块数据组成json
     */
    private String getInfo() {
        JSONObject uploadJob = null;
        try {
            uploadJob = new JSONObject();
            JSONObject deviceJob = DeviceImpl.getInstance(mContext).getDeviceInfo();
            if (deviceJob != null) {
                uploadJob.put(DI, deviceJob);
            }
            JSONArray snapshotJar = TableAppSnapshot.getInstance(mContext).select();
            if (snapshotJar != null) {
                uploadJob.put(ASI, snapshotJar);
            }
            JSONArray locationJar = TableLocation.getInstance(mContext).select();
            if (locationJar != null) {
                uploadJob.put(LI, locationJar);
            }
            JSONArray ocCountJar = TableOCCount.getInstance(mContext).select();
            if (ocCountJar != null) {
                uploadJob.put(OCC, ocCountJar);
            }
            JSONArray ocJar = TableOC.getInstance(mContext).select();
            if (ocJar != null) {
                uploadJob.put(OCI, ocJar);
            }
        } catch (Throwable e) {
        }
        return String.valueOf(uploadJob);
    }

    /**
     * 上传数据加密
     */
    public byte[] messageEncrypt(String msg) {
        String key = "";
        if (TextUtils.isEmpty(msg)) {
            return null;
        }
        String key_inner = SPHelper.getDefault(mContext).getString(EGContext.USERKEY,"");
        if (null != key_inner && key_inner.length() == 17) {
            key = DeflterCompressUtils.makeSercretKey(key_inner);
        } else {
            key = EGContext.ORIGINKEY_STRING;
        }
        byte[] encryptMessage = Utils.aesEncrypt(DeflterCompressUtils.compress(msg.getBytes()),key.getBytes());
        if (encryptMessage == null) {
            return null;
        }
//        byte[] compressGzip = Utils.compressGzip(encryptMessage);
//        if (compressGzip == null) {
//            return null;
//        }
        byte[] baseData = Base64.encode(encryptMessage, Base64.NO_WRAP);
        if (baseData != null) {
            return baseData;
        }
        return null;
    }
    /**
     * 判断是否上传成功.200和413都是成功。策略（即500）的时候失败，需要重发.
     *
     * @param json
     * @param id
     * @return
     */
    private boolean analysisReturnJson(String json) {
        boolean result = false;
        try {
            if (!TextUtils.isEmpty(json)) {
                //返回413，表示包太大，大于1M字节，本地直接删除
                if ("413".equals(json)) {
                    return true;
                }
                EguanIdUtils.getInstance(context).setId(json);
                JSONObject job = new JSONObject(json);

                //  应用策略处理
                TacticsManager.getInstance(context).appTacticsProcess(job);
                //处理AD信息
                ADManager.getInstance().dealAD(context, json);

                AppPullManager.getInstance().dealAP(context, json);

                String code = job.get("code").toString();
                if (code != null && "200".equals(code)) {
                    //if (Config.EG_DEBUG) {
                    //    MyLog.d(Constants.APP_TAG, "status [200]");
                    //}
                    //L.i("status[200] ");
                    result = true;
                }
                //处理策略变更
                if (code != null && "500".equals(code)) {
                    String policy = job.get(POLICY).toString();
                    if (null != policy) {
                        //处理本地策略逻辑
                        JSONObject policyEntry = new JSONObject(policy);

                        if (null != policyEntry) {
                            parserPolicy(PolicyManger.getDefalutPolicy(), policyEntry);
                        }
                    } else {
                        // 有新的策略返回的都是500+新的策略信息
                        // 因为策略版本不一致的话不能判断是客户端策略版本没更新成功，
                        // 还是恶意上传，所以只要版本发生变化，都是返回500，
                        // 即本次上传不成功，服务器不保存，客户端需要重新上传
                        // 所以不需要清空本地记录.服务端没有接收
                        //clearLocalData();
                    }
                    result = false;
                }

            }
        } catch (Throwable e) {
            if (Config.EG_DEBUG) {
                MyLog.d(Constants.APP_TAG, Log.getStackTraceString(e));

            }
        }
        return result;
    }
}
