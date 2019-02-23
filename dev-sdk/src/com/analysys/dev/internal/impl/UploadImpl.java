package com.analysys.dev.internal.impl;

import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;

import com.analysys.dev.database.TableAppSnapshot;
import com.analysys.dev.database.TableLocation;
import com.analysys.dev.database.TableOCCount;
import com.analysys.dev.database.TableXXXInfo;
import com.analysys.dev.internal.Content.DeviceKeyContacts;
import com.analysys.dev.internal.Content.EGContext;
import com.analysys.dev.internal.impl.proc.DataPackaging;
import com.analysys.dev.internal.impl.proc.ProcessManager;
import com.analysys.dev.model.PolicyInfo;
import com.analysys.dev.utils.AESUtils;
import com.analysys.dev.utils.DeflterCompressUtils;
import com.analysys.dev.utils.ELOG;
import com.analysys.dev.utils.EThreadPool;
import com.analysys.dev.utils.NetworkUtils;
import com.analysys.dev.utils.RequestUtils;
import com.analysys.dev.utils.Utils;
import com.analysys.dev.utils.reflectinon.EContextHelper;
import com.analysys.dev.internal.work.MessageDispatcher;

import org.json.JSONArray;
import org.json.JSONObject;

import com.analysys.dev.utils.sp.SPHelper;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class UploadImpl {
    Context mContext;
    public final String DI = "DevInfo";
    private final String ASI = "AppSnapshotInfo";
    private final String LI = "LocationInfo";
    private final String OCI = "OCInfo";
    private final String XXXInfo = "XXXInfo";

    private int count = 0;

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
                try {
                    String uploadInfo = getInfo();
                    ELOG.i("uploadInfo ::::  "+uploadInfo);
                    if (TextUtils.isEmpty(uploadInfo)) {
                        return;
                    }
                    boolean isDebugMode = SPHelper.getDebugMode(mContext);
                    boolean userRTP = PolicyInfo.getInstance().isUseRTP() == 0 ?true:false;
                    String url = "";
                    if (isDebugMode) {
                        url  = EGContext.TEST_URL;
                    } else {
                        if (userRTP) {
                            url = EGContext.USERTP_URL;
                        } else {
                            url = EGContext.RT_URL;//?哪个接口
                        }
                    }
                    handleUpload(url, messageEncrypt(uploadInfo));
                }catch (Throwable t){
                 ELOG.i("EThreadPool upload has an exception:::"+t.getMessage());
                }
            }
        });
                // 策略处理
        MessageDispatcher.getInstance(mContext).uploadInfo(EGContext.UPLOAD_CYCLE);
    }


    /**
     * 获取各个模块数据组成json
     */
    private String getInfo() {
        JSONObject object = null;
        try {
            object = new JSONObject();
            JSONObject devJson = DataPackaging.getDevInfo(mContext);
            if (devJson != null) {
                object.put(DI, devJson);
            }
            JSONArray ocJson = TableOCCount.getInstance(mContext).select();
            if(ocJson != null ){
                object.put(OCI,ocJson);
            }

            JSONArray xxxInfo = TableXXXInfo.getInstance(mContext).select();
//            ELOG.i(xxxInfo+"   :::::::xxxInfo");
            if(xxxInfo != null){
                object.put(XXXInfo, xxxInfo);
            }

            JSONArray snapshotJar = TableAppSnapshot.getInstance(mContext).select();
            if (snapshotJar != null) {
                object.put(ASI, snapshotJar);
            }
            JSONArray locationJar = TableLocation.getInstance(mContext).select();
            if (locationJar != null) {
                object.put(LI, locationJar);
            }
//            JSONArray ocCountJar = TableOCCount.getInstance(mContext).select();
//            if (ocCountJar != null) {
//                object.put(OCC, ocCountJar);
//            }
//            JSONArray ocJar = TableOC.getInstance(mContext).select();
//            if (ocJar != null) {
//                object.put(OCI, ocJar);
//            }
        } catch (Throwable e) {
            ELOG.e(e.getMessage()+" getInfo has an exception");
        }
        return object.toString();
    }
//
    /**
     * 上传数据加密
     */
    public String messageEncrypt(String msg) {
        try {
            String key = "";
            if (TextUtils.isEmpty(msg)) {
                return null;
            }
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    try{
//                        ELOG.i("==========================="+Environment.getExternalStorageDirectory()+"/origin.txt");
//                        FileUtils.write(Environment.getExternalStorageDirectory()+"/origin.txt",msg);
//                    }catch (Throwable t){
//                        ELOG.i("THREAD HAS AN EXCEPTION  "+t.getMessage());
//                    }
//                }
//            }).start();
            String key_inner = SPHelper.getDefault(mContext).getString(EGContext.USERKEY,"");
            if (null == key_inner) {
                key_inner = EGContext.ORIGINKEY_STRING;
            }
            key = DeflterCompressUtils.makeSercretKey(key_inner , mContext);
            ELOG.i("key：：：：：："+key);

            byte[] def = DeflterCompressUtils.compress(URLEncoder.encode(URLEncoder.encode(msg)).getBytes("UTF-8"));
            byte[] encryptMessage = AESUtils.encrypt(def,key.getBytes("UTF-8"));
            if (encryptMessage != null) {
                  byte[]  returnData = Base64.encode(encryptMessage,Base64.DEFAULT);
//                ELOG.i("returnData :::::::::::::"+ new String(returnData));
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        try{
//                            ELOG.i("==========================="+Environment.getExternalStorageDirectory()+"/encode.txt");
//                            FileUtils.write(Environment.getExternalStorageDirectory()+"/encode.txt",new String(returnData).replace("\n",""));
//                        }catch (Throwable t){
//                            ELOG.i("THREAD HAS AN EXCEPTION  "+t.getMessage());
//                        }
//                    }
//                }).start();
                return new String(returnData).replace("\n","");
            }
        }catch (Throwable t){
           ELOG.i("messageEncrypt has an exception."+ t.getMessage());
        }

        return null;
    }
    /**
     * 判断是否上传成功.200和413都是成功。策略（即500）的时候失败，需要重发.
     *
     * @param json
     * @return
     */
    private boolean analysysReturnJson(String json) {
        boolean result = false;
        try {
            ELOG.i("json   :::::::::"+json);
            if (!TextUtils.isEmpty(json)) {
                //返回413，表示包太大，大于1M字节，本地直接删除
                if (EGContext.HTTP_DATA_OVERLOAD.equals(json)) {
                    //删除源数据
                    cleanData();
                    return true;
                }
                JSONObject object = new JSONObject(json);
                String code = object.get(DeviceKeyContacts.Response.RES_CODE).toString();
                if(code != null){
                    if(EGContext.HTTP_SUCCESS.equals(code)){
                        Utils.setId(json , mContext);
                        //清除本地数据
                        cleanData();
                        result = true;
                    }
                    if(EGContext.HTTP_RETRY.equals(code)){
                        PolicyImpl.getInstance(mContext).saveRespParams(object.getJSONObject(DeviceKeyContacts.Response.RES_POLICY));
                        result = false;
                    }
                }

            }else{
                result = false;
            }
        } catch (Throwable e) {
            result = false;
        }
        return result;
    }
    private void handleUpload(final String url,final String uploadInfo){

        String result = RequestUtils.httpRequest(url, uploadInfo,mContext);
        if (TextUtils.isEmpty(result)) {
            return;
        }
        if (!analysysReturnJson(result)) {
            count++;
            //加入网络判断,如果无网情况下,不进行失败上传
            if (count <= PolicyImpl.getInstance(mContext).getFailCount()&& !NetworkUtils.getNetworkType(mContext).equals(EGContext.NETWORK_TYPE_NO_NET)) {
                long dur = (long)((Math.random() + 1) * PolicyInfo.getInstance().getTimerInterval());
                EThreadPool.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        handleUpload(url, uploadInfo);
                    }
                }, dur);

            } else if (PolicyInfo.getInstance().isUseRTP() == 0) {//使用实时策略
                long failTryDelayInterval = System.currentTimeMillis() +
                        PolicyInfo.getInstance().getFailTryDelay();
                PolicyInfo.getInstance().setFailTryDelay
                        (failTryDelayInterval);
                PolicyImpl.getInstance(mContext).savePermitForFailTimes(failTryDelayInterval);
            }
        } else {
            count = 0;
        }
    }
    private void cleanData(){
      TableAppSnapshot.getInstance(mContext).delete();
      TableLocation.getInstance(mContext).delete();
    }
}
