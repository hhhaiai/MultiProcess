package com.analysys.dev.internal.impl;

import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;
import com.analysys.dev.database.TableAppSnapshot;
import com.analysys.dev.database.TableLocation;
import com.analysys.dev.database.TableOC;
import com.analysys.dev.database.TableOCCount;
import com.analysys.dev.internal.Content.EDContext;
import com.analysys.dev.internal.utils.EContextHelper;
import com.analysys.dev.internal.utils.EThreadPool;
import com.analysys.dev.internal.utils.RequestUtils;
import com.analysys.dev.internal.utils.Utils;
import com.analysys.dev.internal.work.MessageDispatcher;
import org.json.JSONArray;
import org.json.JSONObject;

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
  private final String OCI = "OCCount";

  private static class Holder {
    private static final UploadImpl INSTANCE = new UploadImpl();
  }

  public static UploadImpl getInstance(Context context) {
    if (Holder.INSTANCE.mContext == null) {
      if (context != null) {
        Holder.INSTANCE.mContext = context;
      } else {
        Holder.INSTANCE.mContext = EContextHelper.getContext();
      }
    }
    return Holder.INSTANCE;
  }

  // 上传数据
  public void upload() {
    EThreadPool.execute(new Runnable() {
      @Override
      public void run() {
        String uploadInfo = getInfo();
        byte[] encryptInfo = null;
        if (TextUtils.isEmpty(uploadInfo)) {
          return;
        }
        //encryptInfo = messageEncrypt(uploadInfo, "");
        if (TextUtils.isEmpty(uploadInfo)) {
          return;
        }
        String url = URLGeneral.getURL();
        if (TextUtils.isEmpty(url)) {
          return;
        }
        String returnInfo = RequestUtils.httpRequest(url, encryptInfo);
        if (TextUtils.isEmpty(returnInfo)) {
          return;
        }
        // 策略处理
      }
    });
    MessageDispatcher.getInstance(mContext).uploadInfo(EDContext.UPLOAD_CYCLE);
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
  private String messageEncrypt(String msg, String pwd) {
    if (TextUtils.isEmpty(msg) || TextUtils.isEmpty(pwd)) {
      return null;
    }
    byte[] encryptMessage = Utils.aesEncrypt(msg.getBytes(), pwd.getBytes());
    if (encryptMessage == null) {
      return null;
    }
    byte[] compressGzip = Utils.compressGzip(encryptMessage);
    if (compressGzip == null) {
      return null;
    }
    byte[] baseData = Base64.encode(compressGzip, Base64.NO_WRAP);
    if (baseData != null) {
      return new String(baseData);
    }
    return null;
  }
}
