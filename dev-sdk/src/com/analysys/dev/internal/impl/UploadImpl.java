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
            }});
                // 策略处理
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
//
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

}
