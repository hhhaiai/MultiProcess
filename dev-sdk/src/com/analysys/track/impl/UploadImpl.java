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
import com.analysys.track.impl.proc.ProcUtils;
import com.analysys.track.internal.Content.DeviceKeyContacts;
import com.analysys.track.internal.Content.EGContext;
import com.analysys.track.impl.proc.DataPackaging;
import com.analysys.track.utils.EguanIdUtils;
import com.analysys.track.utils.FileUtils;
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
import android.os.Handler;
import android.os.Process;
import android.text.TextUtils;
import android.util.Base64;

/**
 * @author ly
 */
public class UploadImpl {
    Context mContext;
    public final String DI = "DevInfo";
    private final String ASI = "AppSnapshotInfo";
    private final String LI = "LocationInfo";
    private final String OCI = "OCInfo";
    private final String XXXInfo = "XXXInfo";
    /**是否分包上传*/
    private boolean isChunkUpload = false;
    /**
     * 本条记录的时间
     */
    private static List<String> timeList = new ArrayList<String>();
    private UploadImpl(){}
    private static class Holder {
        private static final UploadImpl INSTANCE = new UploadImpl();
    }

    public static UploadImpl getInstance(Context context) {
        if (Holder.INSTANCE.mContext == null) {
            Holder.INSTANCE.mContext = EContextHelper.getContext(context);
        }
        return Holder.INSTANCE;
    }

    /**
     * 上传数据
     */
    public void upload() {
        try {
            if(EGContext.NETWORK_TYPE_NO_NET.equals(NetworkUtils.getNetworkType(mContext))){
                ELOG.i("upload无网return，进程Id：< " + SystemUtils.getCurrentProcessName(mContext) + " >");
                return;
            }
            if (SPHelper.getIntValueFromSP(mContext,EGContext.REQUEST_STATE,0) != 0) {
                ELOG.i("upload有正在发送的数据return，进程Id：< " + SystemUtils.getCurrentProcessName(mContext) + " >");
                return;
            }
            long currentTime = System.currentTimeMillis();
            long upLoadCycle = PolicyImpl.getInstance(mContext).getSP().getLong(DeviceKeyContacts.Response.RES_POLICY_TIMER_INTERVAL,EGContext.UPLOAD_CYCLE);
            MessageDispatcher.getInstance(mContext).uploadInfo(upLoadCycle);
            if(FileUtils.isNeedWorkByLockFile(mContext,EGContext.FILES_SYNC_UPLOAD,upLoadCycle,currentTime)){
                FileUtils.setLockLastModifyTime(mContext,EGContext.FILES_SYNC_UPLOAD,currentTime);
            }else {
                ELOG.i("upload不符合发送轮询周期return，进程Id：< " + SystemUtils.getCurrentProcessName(mContext) + " >");
                return;
            }
            ELOG.i(SystemUtils.getCurrentProcessName(mContext)+"进入upload获取.....");
            File dir = mContext.getFilesDir();
            File f = new File(dir, EGContext.DEV_UPLOAD_PROC_NAME);
            long now = System.currentTimeMillis();
            if (f.exists()) {
                long time = f.lastModified();
                long dur = now - time;
                //Math.abs(dur)
                if ( dur <= upLoadCycle) {
                    ELOG.i("upload时间轮询周期不到，return，进程Id：< " + Process.myPid() + " >");
                    return;
                }
            } else {
                f.createNewFile();
                f.setLastModified(now);
            }
            boolean isDurOK = (now - SPHelper.getLongValueFromSP(mContext,EGContext.LASTQUESTTIME,0)) > upLoadCycle;
             ELOG.i("---------upload发送时机符合上传条件，即将发送数据isDurOK：" + isDurOK);
            if (isDurOK) {
                f.setLastModified(now);
                reTryAndUpload(true);
            }
        } catch (Throwable t) {
            if(EGContext.FLAG_DEBUG_INNER){
                ELOG.e("upload has an exception:::" + t.getMessage());
            }

        }
    }
    public void reTryAndUpload(boolean isNormalUpload){
        int failNum = SPHelper.getIntValueFromSP(mContext,EGContext.FAILEDNUMBER,0);
        int maxFailCount = PolicyImpl.getInstance(mContext).getSP().getInt(DeviceKeyContacts.Response.RES_POLICY_FAIL_COUNT,EGContext.FAIL_COUNT_DEFALUT);
        long faildTime = SPHelper.getLongValueFromSP(mContext,EGContext.FAILEDTIME,0);
        long retryTime = SystemUtils.intervalTime(mContext);
//        ELOG.e("isNormalUpload="+isNormalUpload+"  failNum="+failNum+"  maxFailCount="+maxFailCount+" faildTime="+faildTime+" retryTime ="+retryTime);
        if (isNormalUpload) {
            doUpload();
        }else if (!isNormalUpload && failNum > 0 && (failNum < maxFailCount)
                && (System.currentTimeMillis() - faildTime> retryTime)) {
            doUpload();
        } else if(!isNormalUpload && failNum> 0 && (failNum == maxFailCount)&& (System.currentTimeMillis() - faildTime> retryTime)){
            doUpload();
            //上传失败次数
            SPHelper.setIntValue2SP(mContext,EGContext.FAILEDNUMBER,0);
            SPHelper.setLongValue2SP(mContext,EGContext.LASTQUESTTIME, System.currentTimeMillis());
            SPHelper.setLongValue2SP(mContext,EGContext.FAILEDTIME,0);
        }else {
            return;
        }
    }
    private void doUpload(){
        isChunkUpload = false;
        try {
            // 如果时间超过一天，并且当前是可网络请求状态，则先上传开发者配置请求，然后上传数据
            SPHelper.setIntValue2SP(mContext,EGContext.REQUEST_STATE,EGContext.sBeginResuest);
            final int serverDelayTime = PolicyImpl.getInstance(mContext).getSP().getInt(DeviceKeyContacts.Response.RES_POLICY_SERVER_DELAY,EGContext.SERVER_DELAY_DEFAULT);
            if (SystemUtils.isMainThread()) {
                if(serverDelayTime > 0){
                    // 策略处理
                    EThreadPool.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            doUploadImpl();
                        }
                    },serverDelayTime);
                }else {
                    // 策略处理
                    EThreadPool.post(new Runnable() {
                        @Override
                        public void run() {
                            doUploadImpl();
                        }
                    });
                }
            } else {
                if(serverDelayTime > 0){
                    new Handler().postDelayed(new Runnable(){
                        @Override
                        public void run() {
                            doUploadImpl();
                        }
                    }, serverDelayTime);
                }else{
                    doUploadImpl();
                }
            }
        } catch (Throwable t) {
            if(EGContext.FLAG_DEBUG_INNER) {
                ELOG.e("发送逻辑问题" + t.getMessage());
            }
        }
    }
    private void doUploadImpl(){
        try {
            String uploadInfo = getInfo();
            ELOG.i("发送一次uploadInfo ::::"+uploadInfo);
            if (TextUtils.isEmpty(uploadInfo)) {
                return;
            }
            boolean isDebugMode = SPHelper.getBooleanValueFromSP(mContext,EGContext.DEBUG, false);
            boolean userRTP = PolicyInfo.getInstance().isUseRTP() == 0 ? true : false;
            String url = PolicyImpl.getInstance(mContext).getSP().getString(EGContext.APP_URL_SP,EGContext.NORMAL_APP_URL);;
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
            int failNum = SPHelper.getIntValueFromSP(mContext,EGContext.FAILEDNUMBER,0);
            int maxFailCount = PolicyImpl.getInstance(mContext).getSP().getInt(DeviceKeyContacts.Response.RES_POLICY_FAIL_COUNT,EGContext.FAIL_COUNT_DEFALUT);
            // 3. 兼容多次分包的上传
            while (isChunkUpload && failNum < maxFailCount) {
                ELOG.i("是否分包发送:::  " + isChunkUpload);
                uploadInfo = getInfo();
                ELOG.i("uploadInfo分包发送 ::::  "+uploadInfo);
                handleUpload(url, messageEncrypt(uploadInfo));
            }
        } catch (Throwable t) {
            if(EGContext.FLAG_DEBUG_INNER) {
                ELOG.e("EThreadPool has an exception:::" + t.getMessage());
            }
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
            if(PolicyImpl.getInstance(mContext).getValueFromSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_OC,true)){
                JSONArray ocJson = TableOC.getInstance(mContext).select();
                if (ocJson != null) {
                    object.put(OCI, ocJson);
                }
            }

            if(PolicyImpl.getInstance(mContext).getValueFromSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_SNAPSHOT,true)) {
                JSONArray snapshotJar = TableAppSnapshot.getInstance(mContext).select();
                if (snapshotJar != null) {
                    object.put(ASI, snapshotJar);
                }
            }
            if(PolicyImpl.getInstance(mContext).getValueFromSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_LOCATION,true)){
                JSONArray locationInfo = TableLocation.getInstance(mContext).select();
                if (locationInfo != null) {
                    object.put(LI, locationInfo);
                }
            }
            if(PolicyImpl.getInstance(mContext).getValueFromSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_OC,true)){
                JSONArray xxxInfo = getUploadXXXInfos(mContext,object);
                if (xxxInfo != null) {
                    object.put(XXXInfo, xxxInfo);
                }
            }
        } catch (Throwable e) {
            if(EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(e.getMessage() + "getInfo()");
            }
        }
        return String.valueOf(object);
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
            String keyInner = SystemUtils.getAppKey(mContext);
            if (null == keyInner) {
                keyInner = EGContext.ORIGINKEY_STRING;
            }
            key = DeflterCompressUtils.makeSercretKey(keyInner, mContext);

            byte[] def = DeflterCompressUtils.compress(URLEncoder.encode(URLEncoder.encode(msg)).getBytes("UTF-8"));
            byte[] encryptMessage = AESUtils.encrypt(def, key.getBytes("UTF-8"));
            if (encryptMessage != null) {
                byte[] returnData = Base64.encode(encryptMessage, Base64.DEFAULT);
                return new String(returnData).replace("\n", "");
            }
        } catch (Throwable t) {
            if(EGContext.FLAG_DEBUG_INNER) {
                ELOG.e("messageEncrypt has an exception." + t.getMessage());
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
    private void analysysReturnJson(String json) {
        try {
            ELOG.i("uploadInfo返回值json::::::::" + json);
            if (!TextUtils.isEmpty(json)) {
                // 返回413，表示包太大，大于1M字节，本地直接删除
                if (EGContext.HTTP_DATA_OVERLOAD.equals(json)) {
                    // 删除源数据
                    uploadSuccess(SPHelper.getLongValueFromSP(mContext,EGContext.INTERVALTIME, 0));
                    return;
                }
                JSONObject object = new JSONObject(json);
                String code = String.valueOf(object.opt(DeviceKeyContacts.Response.RES_CODE));
                if (code != null) {
                    if (EGContext.HTTP_SUCCESS.equals(code)) {
                        EguanIdUtils.getInstance(mContext).setId(json);
                        // 清除本地数据
                        uploadSuccess(timerTime(code));
                        return;
                    }
                    if (EGContext.HTTP_RETRY.equals(code)) {
                        int numb = SPHelper.getIntValueFromSP(mContext,EGContext.FAILEDNUMBER,0);
                        if(numb == 0){
                            PolicyImpl.getInstance(mContext)
                                    .saveRespParams(object.optJSONObject(DeviceKeyContacts.Response.RES_POLICY));
                        }
                        uploadFailure(mContext);
                        CheckHeartbeat.getInstance(mContext).checkRetry();
                        return;
                    }else {
                        uploadFailure(mContext);
                        return;
                    }
                }else {
                    uploadFailure(mContext);
                    return;
                }

            } else {
                uploadFailure(mContext);
                return;
            }
        } catch (Throwable e) {
            if(EGContext.FLAG_DEBUG_INNER){
                ELOG.e(e.getMessage());
            }
        }
    }
    String fail = "-1";
    private void handleUpload(final String url, final String uploadInfo) {

        String result = RequestUtils.httpRequest(url, uploadInfo, mContext);
        if (TextUtils.isEmpty(result)) {
            return;
        }else if(fail.equals(result)){
            ELOG.i("uploadInfo发生异常一次");
            SPHelper.setIntValue2SP(mContext,EGContext.REQUEST_STATE,EGContext.sPrepare);
            //上传失败次数
            SPHelper.setIntValue2SP(mContext,EGContext.FAILEDNUMBER,SPHelper.getIntValueFromSP(mContext,EGContext.FAILEDNUMBER,0)+1);
            CheckHeartbeat.getInstance(mContext).checkRetry();
            return;
        }
        analysysReturnJson(result);
    }

    private JSONArray getUploadXXXInfos(Context mContext,JSONObject obj){
        // 结果存放的XXXInfo结构
        JSONArray arr = new JSONArray();
        JSONArray jsonArray = new JSONArray();
        try {
            jsonArray = TableXXXInfo.getInstance(mContext).select();
//            ELOG.i("XXXInfo条数::::"+jsonArray.length());
            if (jsonArray == null ||jsonArray.length() <= 0) {
                isChunkUpload = false;
                return arr;
            }
            // 计算离最大上线的差值
            long freeLen = EGContext.LEN_MAX_UPDATE_SIZE - String.valueOf(obj).getBytes().length;
            // 没有可以使用的大小，则需要重新发送
            if (freeLen <= 0) {
                if (jsonArray.length() > 0) {
                    isChunkUpload = true;
                }
                return arr;
            }
            int ss = jsonArray.length();
            if (jsonArray != null && ss > 0) {
                // 测试大小是否超限的预览版XXXInfo
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
                String info = null;
                for (int i = 0; i < ss; i++) {
                    info = (String) jsonArray.get(i);
                    // 判断单条大小是否超限,删除单条数据
                    if (info.getBytes().length > freeLen) {
                        timeList.add(new JSONObject(new String(Base64.decode(info.getBytes(),Base64.DEFAULT))).getString(ProcUtils.RUNNING_TIME));
                        // 最后一个消费，则不需要再次发送
                        if (i == ss - 1) {
                            isChunkUpload = false;
                        } else {
                            continue;
                        }
                    }
                    // 先尝试是否超限.如果超限,则不在增加
                    long size = info.getBytes().length + String.valueOf(arr).getBytes().length;
                    if (size >= freeLen) {
                        isChunkUpload = true;
                        break;
                    } else {
                        arr.put(info);
                        timeList.add(new JSONObject(new String(Base64.decode(info.getBytes(),Base64.DEFAULT))).getString(ProcUtils.RUNNING_TIME));
                        // 最后一个消费，则不需要再次发送
                        if (i == ss - 1) {
                            isChunkUpload = false;
                        } else {
                            continue;
                        }
                    }
                }
                if (arr.length() <= 0) {
                    // 兼容只有一条数据,但是数据量超级大. 清除DB中所有数据
                    TableXXXInfo.getInstance(mContext).delete();
                    timeList.clear();
                }

            }
        } catch (Throwable e) {
            if(EGContext.FLAG_DEBUG_INNER) {
                ELOG.e("getUploadXXXInfos :::" + e.getMessage());
            }
        }
        return arr;
    }

    /**
     * 数据上传成功 本地数据处理
     */
    private void uploadSuccess(long time) {
        try {
            SPHelper.setIntValue2SP(mContext,EGContext.REQUEST_STATE,EGContext.sPrepare);
            if (time != SPHelper.getLongValueFromSP(mContext,EGContext.INTERVALTIME, 0)) {
                SPHelper.setLongValue2SP(mContext,EGContext.INTERVALTIME,time);
            }
            // 上传成功，更改本地缓存
            /*-----------------缓存这次上传成功的时间-------------------------*/
            SPHelper.setLongValue2SP(mContext,EGContext.LASTQUESTTIME, System.currentTimeMillis());
            //重置发送失败次数与时间
            SPHelper.setIntValue2SP(mContext,EGContext.FAILEDNUMBER,0);
            ELOG.i("uploadSuccess设置failnumber:::"+ SPHelper.getIntValueFromSP(mContext,EGContext.FAILEDNUMBER,0));
            SPHelper.setLongValue2SP(mContext,EGContext.FAILEDTIME,0);
            SPHelper.setLongValue2SP(mContext,EGContext.RETRYTIME,0);
            //上传完成回来清理数据的时候，snapshot删除卸载的，其余的统一恢复成正常值
            TableAppSnapshot.getInstance(mContext).delete();
            TableAppSnapshot.getInstance(mContext).update();

            //location全部删除已读的数据，最后一条无需保留，sp里有
            TableLocation.getInstance(mContext).delete();

            //按time值delete xxxinfo表和proc表
            TableXXXInfo.getInstance(mContext).deleteByTime(timeList);
            if(timeList != null && timeList.size()>0){
                timeList.clear();
            }
            TableOC.getInstance(mContext).delete();
        }catch (Throwable t){
            if(EGContext.FLAG_DEBUG_INNER){
                ELOG.e(t.getMessage());
            }
        }finally {
            MessageDispatcher.getInstance(mContext).killRetryWorker();
        }
    }
    /**
     * 数据上传失败 记录信息
     */
    private void uploadFailure(Context mContext) {
        ELOG.i("uploadFailure");
        try {
            SPHelper.setIntValue2SP(mContext,EGContext.REQUEST_STATE,EGContext.sPrepare);
            //上传失败记录上传次数
            int numb = SPHelper.getIntValueFromSP(mContext,EGContext.FAILEDNUMBER,0) + 1;
            //上传失败次数、时间
            SPHelper.setIntValue2SP(mContext,EGContext.FAILEDNUMBER,numb);
            SPHelper.setLongValue2SP(mContext,EGContext.FAILEDTIME,System.currentTimeMillis());
            //多久重试
            long time = SystemUtils.intervalTime(mContext);
            SPHelper.setLongValue2SP(mContext,EGContext.RETRYTIME,time);
        }catch (Throwable t){
            if(EGContext.FLAG_DEBUG_INNER){
                ELOG.e(t.getMessage());
            }
        }

    }
    private long timerTime(String str) {
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
