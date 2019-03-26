package com.analysys.track.impl;

import java.io.File;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.analysys.track.database.TableAppSnapshot;
import com.analysys.track.database.TableLocation;
import com.analysys.track.database.TableOC;
import com.analysys.track.database.TableXXXInfo;
import com.analysys.track.internal.Content.DeviceKeyContacts;
import com.analysys.track.internal.Content.EGContext;
import com.analysys.track.impl.proc.DataPackaging;
import com.analysys.track.impl.proc.ProcParser;
import com.analysys.track.utils.EguanIdUtils;
import com.analysys.track.work.CheckHeartbeat;
import com.analysys.track.work.MessageDispatcher;
import com.analysys.track.model.PolicyInfo;
import com.analysys.track.utils.AESUtils;
import com.analysys.track.utils.DeflterCompressUtils;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.EThreadPool;
import com.analysys.track.utils.NetworkUtils;
import com.analysys.track.utils.RequestUtils;
import com.analysys.track.utils.SystemUtils;

import com.analysys.track.utils.reflectinon.EContextHelper;
import com.analysys.track.utils.sp.SPHelper;

import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;

public class UploadImpl {
    Context mContext;
    public final String DI = "DevInfo";
    private final String ASI = "AppSnapshotInfo";
    private final String LI = "LocationInfo";
    private final String OCI = "OCInfo";
    private final String XXXInfo = "XXXInfo";
    // 是否分包上传
    private boolean isChunkUpload = false;
    // 本条记录的时间
    private static List<String> timeList = new ArrayList<String>();

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
        try {
            //TODO 测试下线程名字，判断子线程or主线程，应该子线程，测试是否会导致同级别的卡顿
            if(EGContext.NETWORK_TYPE_NO_NET.equals(NetworkUtils.getNetworkType(mContext))){
                return;
            }
            if (SPHelper.getRequestState(mContext) != 0) {
                return;
            }
            File dir = mContext.getFilesDir();
            File f = new File(dir, EGContext.DEV_UPLOAD_PROC_NAME);
            long now = System.currentTimeMillis();
            if (f.exists()) {
                long time = f.lastModified();
                long dur = now - time;
                if (Math.abs(dur) <= PolicyImpl.getInstance(mContext).getSP().getInt(DeviceKeyContacts.Response.RES_POLICY_TIMER_INTERVAL,EGContext.UPLOAD_CYCLE)) {
                    return;
                }
            } else {
                f.createNewFile();
                f.setLastModified(now);
            }
            boolean isDurOK = (now - SPHelper.getLastQuestTime(mContext)) > PolicyImpl.getInstance(mContext).getSP().getInt(DeviceKeyContacts.Response.RES_POLICY_TIMER_INTERVAL,EGContext.UPLOAD_CYCLE);
            // L.i("---------即将发送数据isDurOK：" + isDurOK);
            if (isDurOK) {
                f.setLastModified(now);
                reTryAndUpload(true);
            }
        } catch (Throwable t) {
            ELOG.i("EThreadPool upload has an exception:::" + t.getMessage());
        }
    }
    public void reTryAndUpload(boolean isNormalUpload){
        if (isNormalUpload) {
            doUpload();
        }else if (!isNormalUpload && SPHelper.getFailedNumb(mContext) > 0 && (SPHelper.getFailedNumb(mContext) < PolicyImpl.getInstance(mContext).getSP().getInt(DeviceKeyContacts.Response.RES_POLICY_FAIL_COUNT,EGContext.FAIL_COUNT_DEFALUT))
                && (System.currentTimeMillis() - SPHelper.getFailedTime(mContext) > SPHelper.getRetryTime(mContext))) {
            doUpload();
        } else if(!isNormalUpload && SPHelper.getFailedNumb(mContext) > 0 && (SPHelper.getFailedNumb(mContext) == PolicyImpl.getInstance(mContext).getSP().getInt(DeviceKeyContacts.Response.RES_POLICY_FAIL_COUNT,EGContext.FAIL_COUNT_DEFALUT))){
            doUpload();
            SPHelper.setFailedNumb(mContext, 0);
            SPHelper.setLastQuestTime(mContext, System.currentTimeMillis());
            SPHelper.setFailedTime(mContext ,0);
        }else {
            return;
        }
    }
    private void doUpload(){
        isChunkUpload = false;
        try {
            // 如果时间超过一天，并且当前是可网络请求状态，则先上传开发者配置请求，然后上传数据
            SPHelper.setRequestState(mContext,EGContext.sBeginResuest);
            final int serverDelayTime = PolicyImpl.getInstance(mContext).getSP().getInt(DeviceKeyContacts.Response.RES_POLICY_SERVER_DELAY,EGContext.SERVER_DELAY_DEFAULT);
            if (SystemUtils.isMainThread()) {
                // 策略处理
                EThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if(serverDelayTime > 0){
                                Thread.sleep(serverDelayTime);
                            }
                            String uploadInfo = getInfo();
                            ELOG.i("uploadInfo ::::  " + uploadInfo);
                            if (TextUtils.isEmpty(uploadInfo)) {
                                return;
                            }
                            boolean isDebugMode = SPHelper.getDebugMode(mContext);
                            boolean userRTP = PolicyInfo.getInstance().isUseRTP() == 0 ? true : false;
                            String url = "";
                            if (isDebugMode) {
                                url = EGContext.TEST_URL;
                            } else {
                                if (userRTP) {
                                    boolean userRTL = PolicyInfo.getInstance().isUseRTL() == 1 ? true : false;
                                    if(userRTL){
                                        url = EGContext.USERTP_URL;
                                    }
                                }
                            }
                            handleUpload(url, messageEncrypt(uploadInfo));
                            // 3. 兼容多次分包的上传
                            while (isChunkUpload) {
                                uploadInfo = getInfo();
                                handleUpload(url, messageEncrypt(uploadInfo));
                            }
                        } catch (Throwable t) {
                            ELOG.i("EThreadPool upload has an exception:::" + t.getMessage());
                        }
                        MessageDispatcher.getInstance(mContext).uploadInfo(PolicyImpl.getInstance(mContext).getSP().getInt(DeviceKeyContacts.Response.RES_POLICY_TIMER_INTERVAL,EGContext.UPLOAD_CYCLE), false);
                    }
                });
            } else {
                try {
                    if(serverDelayTime > 0){
                        Thread.sleep(serverDelayTime);
                    }
                    if(EGContext.NETWORK_TYPE_NO_NET.equals(NetworkUtils.getNetworkType(mContext))){
                        return;
                    }
                    String uploadInfo = getInfo();
                    ELOG.i("uploadInfo ::::  " + uploadInfo);
                    if (TextUtils.isEmpty(uploadInfo)) {
                        return;
                    }
                    boolean isDebugMode = SPHelper.getDebugMode(mContext);
                    boolean userRTP = PolicyInfo.getInstance().isUseRTP() == 0 ? true : false;
                    String url = PolicyImpl.getInstance(mContext).getSP().getString(EGContext.APP_URL_SP,EGContext.NORMAL_APP_URL);
                    if (isDebugMode) {
                        url = EGContext.TEST_URL;
                    } else {
                        if (userRTP) {
                            url = EGContext.USERTP_URL;
                        }
                    }
                    handleUpload(url, messageEncrypt(uploadInfo));
                    // 3. 兼容多次分包的上传
                    while (isChunkUpload) {
                        uploadInfo = getInfo();
                        handleUpload(url, messageEncrypt(uploadInfo));
                    }
                } catch (Throwable t) {
                    ELOG.i("EThreadPool upload has an exception:::" + t.getMessage());
                }
                MessageDispatcher.getInstance(mContext).uploadInfo(PolicyImpl.getInstance(mContext).getSP().getInt(DeviceKeyContacts.Response.RES_POLICY_TIMER_INTERVAL,EGContext.UPLOAD_CYCLE), false);
            }
        } catch (Throwable t) {
        }
    }
    /**
     * 获取各个模块数据组成json
     */
    private String getInfo() {
        JSONObject object = null;
        try {
            object = new JSONObject();
            //发送的时候，临时组装devInfo,有大模块控制的优先控制大模块，大模块收集，针对字段级别进行控制
            JSONObject devJson = DataPackaging.getDevInfo(mContext);
            if (devJson != null) {
                object.put(DI, devJson);
            }
            //从oc表查询closeTime不为空的整条信息，组装上传
            JSONArray ocJson = TableOC.getInstance(mContext).select();
            if (ocJson != null) {
                object.put(OCI, ocJson);
            }

            JSONArray snapshotJar = TableAppSnapshot.getInstance(mContext).select();
            if (snapshotJar != null) {
                object.put(ASI, snapshotJar);
            }

            JSONArray xxxInfo = getUploadXXXInfos(mContext,object,isChunkUpload);
            ELOG.i(isChunkUpload+" :::::::isChunkUpload");
            if (xxxInfo != null) {
                object.put(XXXInfo, xxxInfo);
            }
            JSONArray locationJar = TableLocation.getInstance(mContext).select();
            if (locationJar != null) {
                object.put(LI, locationJar);
            }


        } catch (Throwable e) {
            // Log.getStackTraceString(e);
            ELOG.e(e+"getInfo()");
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
            // new Thread(new Runnable() {
            // @Override
            // public void run() {
            // try{
            // ELOG.i("==========================="+Environment.getExternalStorageDirectory()+"/origin.txt");
            // FileUtils.write(Environment.getExternalStorageDirectory()+"/origin.txt",msg);
            // }catch (Throwable t){
            // ELOG.i("THREAD HAS AN EXCEPTION "+t.getMessage());
            // }
            // }
            // }).start();
            String key_inner = SystemUtils.getAppKey(mContext);
            if (null == key_inner) {
                key_inner = EGContext.ORIGINKEY_STRING;
            }
            key = DeflterCompressUtils.makeSercretKey(key_inner, mContext);
            ELOG.i("key：：：：：：" + key);

            byte[] def = DeflterCompressUtils.compress(URLEncoder.encode(URLEncoder.encode(msg)).getBytes("UTF-8"));
            byte[] encryptMessage = AESUtils.encrypt(def, key.getBytes("UTF-8"));
            if (encryptMessage != null) {
                byte[] returnData = Base64.encode(encryptMessage, Base64.DEFAULT);
                // ELOG.i("returnData :::::::::::::"+ new String(returnData));
                // new Thread(new Runnable() {
                // @Override
                // public void run() {
                // try{
                // ELOG.i("==========================="+Environment.getExternalStorageDirectory()+"/encode.txt");
                // FileUtils.write(Environment.getExternalStorageDirectory()+"/encode.txt",new
                // String(returnData).replace("\n",""));
                // }catch (Throwable t){
                // ELOG.i("THREAD HAS AN EXCEPTION "+t.getMessage());
                // }
                // }
                // }).start();
                return new String(returnData).replace("\n", "");
            }
        } catch (Throwable t) {
            ELOG.i("messageEncrypt has an exception." + t.getMessage());
        }

        return null;
    }

    /**
     * 判断是否上传成功.200和413都是成功。策略（即500）的时候失败，需要重发.
     *
     * @param json
     * @return
     */
    private void analysysReturnJson(String json) {
        try {
            ELOG.i("json   :::::::::" + json);
            if (!TextUtils.isEmpty(json)) {
                // 返回413，表示包太大，大于1M字节，本地直接删除
                if (EGContext.HTTP_DATA_OVERLOAD.equals(json)) {
                    // 删除源数据
                    uploadSuccess(mContext, SPHelper.getIntervalTime(mContext));
                    return;
                }
                JSONObject object = new JSONObject(json);
                String code = object.opt(DeviceKeyContacts.Response.RES_CODE).toString();
                if (code != null) {
                    if (EGContext.HTTP_SUCCESS.equals(code)) {
                        EguanIdUtils.getInstance(mContext).setId(json);
                        // 清除本地数据
                        uploadSuccess(mContext,TimerTime(code));
                    }
                    if (EGContext.HTTP_RETRY.equals(code)) {
                        PolicyImpl.getInstance(mContext)
                            .saveRespParams(object.optJSONObject(DeviceKeyContacts.Response.RES_POLICY));
                        uploadFailure(mContext);
                        CheckHeartbeat.getInstance(mContext).checkRetry();
                    }else {
                        uploadFailure(mContext);
                    }
                }else {
                    uploadFailure(mContext);
                }

            } else {
                uploadFailure(mContext);
            }
        } catch (Throwable e) {
            uploadFailure(mContext);
        }
    }

    private void handleUpload(final String url, final String uploadInfo) {

        String result = RequestUtils.httpRequest(url, uploadInfo, mContext);
        if (TextUtils.isEmpty(result)) {
            return;
        }
        analysysReturnJson(result);
    }

//    private void cleanData() {
//        TableAppSnapshot.getInstance(mContext).delete();
//        TableLocation.getInstance(mContext).delete();
//        TableXXXInfo.getInstance(mContext).delete();
//        TableOC.getInstance(mContext).delete();
//    }
    private JSONArray getUploadXXXInfos(Context mContext,JSONObject obj,boolean isNeedMultiPackageUpload){
        // 结果存放的XXXInfo结构
        JSONArray arr = new JSONArray();
        JSONArray jsonArray = new JSONArray();
        try {
            jsonArray = TableXXXInfo.getInstance(mContext).select();
            // L.i("获取XXXInfo数量。。。" + infos.size());
            if (jsonArray == null ||jsonArray.length() <= 0) {
                isNeedMultiPackageUpload = false;
                return arr;
            }
            // L.i("getUploadXXXInfos -----------1111");

            // 计算离最大上线的差值
            long freeLen = EGContext.LEN_MAX_UPDATE_SIZE - obj.toString().getBytes().length;
            // 没有可以使用的大小，则需要重新发送
            if (freeLen <= 0) {
                if (jsonArray.length() > 0) {
                    isNeedMultiPackageUpload = true;
                }
                return arr;
            }
            // L.i("getUploadXXXInfos -----------22222");

            int ss = jsonArray.length();
            if (jsonArray != null && ss > 0) {
                // 测试大小是否超限的预览版XXXInfo
                JSONArray test = new JSONArray();
                // 挨个遍历,使用JSON如果不超限，则
                /**
                 * 遍历逻辑:
                 * </p>
                 * 1. 判断单条超限问题(非最后一个跳过处理下一个，加入待清除列表)
                 * </p>
                 * 2. 测试JSON如超限，退出组装
                 * </p>
                 * 3. 测试JSON不超限, 则加入使用数据，id计入清除列表中
                 */
                for (int i = 0; i < ss; i++) {
                    JSONObject info = (JSONObject)jsonArray.get(i);
                    // base64的值
                    String xx = info.toString();
                    // 判断单条大小是否超限,删除单条数据
                    if (xx.getBytes().length > freeLen) {
                        // L.i("getUploadXXXInfos -----------3333");
                        timeList.add(new JSONObject(new String(Base64.decode(xx.getBytes(),Base64.DEFAULT))).getString(ProcParser.RUNNING_TIME));
                        // 最后一个消费，则不需要再次发送
                        if (i == ss - 1) {
                            isNeedMultiPackageUpload = false;
                        } else {
                            continue;
                        }
                    }
                    // 先尝试是否超限.如果超限,则不在增加
                    test.put(info);
                    long size = test.toString().getBytes().length;
                    if (size >= freeLen) {
                        // L.i("getUploadXXXInfos -----------4444");
                        isNeedMultiPackageUpload = true;
                        break;
                    } else {
                        // L.i("getUploadXXXInfos -----------5555");
                        arr.put(info);
                        timeList.add(new JSONObject(new String(Base64.decode(xx.getBytes(),Base64.DEFAULT))).getString(ProcParser.RUNNING_TIME));
                        // 最后一个消费，则不需要再次发送
                        if (i == ss - 1) {
                            isNeedMultiPackageUpload = false;
                        }
                    }
                }
                test = null;
                // L.i("getUploadXXXInfos ---------> " + arr.length());
                if (arr.length() <= 0) {
                    // 兼容只有一条数据,但是数据量超级大. 清除DB中所有数据
                    TableXXXInfo.getInstance(mContext).delete();
                    timeList.clear();
                }

            }
        } catch (Throwable e) {
        }
        return arr;
    }

    /**
     * 数据上传成功 本地数据处理
     */
    private void uploadSuccess(Context context, long time) {
        SPHelper.setRequestState(mContext,EGContext.sPrepare);
        if (time != SPHelper.getIntervalTime(context)) {
            SPHelper.setIntervalTime(context,time);
        }
        // 上传成功，更改本地缓存
        /*-----------------缓存这次上传成功的时间-------------------------*/
        SPHelper.setLastQuestTime(context,System.currentTimeMillis());

        //上传完成回来清理数据的时候，snapshot删除卸载的，其余的统一恢复成正常值
        TableAppSnapshot.getInstance(mContext).delete();
        TableAppSnapshot.getInstance(mContext).update();

        //location全部删除已读的数据，最后一条无需保留，sp里有
        //TODO SP里的数据和敏感数据 做加密处理  wifi的也加密
        TableLocation.getInstance(mContext).delete();

        //按time值delete xxxinfo表和proc表
        TableXXXInfo.getInstance(mContext).deleteByTime(timeList);
        timeList.clear();
        TableOC.getInstance(context).delete();
        SPHelper.setRetryTime(mContext,0);
    }
    /**
     * 数据上传失败 记录信息
     */
    private void uploadFailure(Context mContext) {

        SPHelper.setRequestState(mContext,EGContext.sPrepare);
        //上传失败记录上传次数
        int numb = SPHelper.getFailedNumb(mContext) + 1;
        SPHelper.setFailedNumb(mContext, numb);
        SPHelper.setFailedTime(mContext, System.currentTimeMillis());
        long time = SystemUtils.intervalTime(mContext);//多久重试
        SPHelper.setRetryTime(mContext,time);
    }
    private long TimerTime(String str) {
        long time = 0;
        switch (Integer.valueOf(str)) {
            case 200:
            case 700:
                time = EGContext.SHORT_TIME;
                break;
            case 701:
                time = 5 * 1000;
                break;
            case 702:
                time = 10 * 1000;
                break;
            case 703:
                time = 15 * 1000;
                break;
            case 704:
                time = 30 * 1000;
                break;
            case 705:
                time = 60 * 1000;
                break;
            case 706:
                time = 2 * 60 * 1000;
                break;
            case 707:
                time = 5 * 60 * 1000;
                break;
            case 708:
                time = 10 * 60 * 1000;
                break;
            case 709:
                time = 15 * 60 * 1000;
                break;
            case 710:
                time = 30 * 60 * 1000;
                break;
            case 711:
                time = 60 * 60 * 1000;
                break;
            case 712:
                time = 2 * 60 * 60 * 1000;
                break;
            case 713:
                time = 3 * 60 * 60 * 1000;
                break;
            case 714:
                time = 4 * 60 * 60 * 1000;
                break;
            case 715:
                time = 6 * 60 * 60 * 1000;
                break;
            case 716:
                time = 8 * 60 * 60 * 1000;
                break;
            case 717:
                time = 12 * 60 * 60 * 1000;
                break;
            case 718:
                time = 24 * 60 * 60 * 1000;
                break;
            case 719:
                time = 48 * 60 * 60 * 1000;
                break;
            default:
                break;
        }
        return time;
    }
}
