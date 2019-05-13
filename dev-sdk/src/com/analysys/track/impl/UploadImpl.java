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
import com.analysys.track.work.MessageDispatcher;
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
    private static List<String> idList = new ArrayList<String>();
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
                if(EGContext.FLAG_DEBUG_INNER){
                    ELOG.i("upload return");
                }
                return;
            }
            if (SPHelper.getIntValueFromSP(mContext,EGContext.REQUEST_STATE,0) != 0) {
                if(EGContext.FLAG_DEBUG_INNER) {
                    ELOG.i("upload return");
                }
                return;
            }
            long currentTime = System.currentTimeMillis();
            long upLoadCycle = PolicyImpl.getInstance(mContext).getSP().getLong(DeviceKeyContacts.Response.RES_POLICY_TIMER_INTERVAL,EGContext.UPLOAD_CYCLE);
            MessageDispatcher.getInstance(mContext).uploadInfo(upLoadCycle);
            if(FileUtils.isNeedWorkByLockFile(mContext,EGContext.FILES_SYNC_UPLOAD,upLoadCycle,currentTime)){
                FileUtils.setLockLastModifyTime(mContext,EGContext.FILES_SYNC_UPLOAD,currentTime);
            }else {
                return;
            }
            File dir = mContext.getFilesDir();
            File f = new File(dir, EGContext.DEV_UPLOAD_PROC_NAME);
            long now = System.currentTimeMillis();
            if (f.exists()) {
                long time = f.lastModified();
                long dur = now - time;
                //Math.abs(dur)
                if ( dur <= upLoadCycle) {
                    return;
                }
            } else {
                f.createNewFile();
                f.setLastModified(now);
            }
            boolean isDurOK = (now - SPHelper.getLongValueFromSP(mContext,EGContext.LASTQUESTTIME,0)) > upLoadCycle;
            if (isDurOK) {
                f.setLastModified(now);
                reTryAndUpload(true);
            }
        } catch (Throwable t) {
            if(EGContext.FLAG_DEBUG_INNER){
                ELOG.e(t);
            }

        }
    }
    public void reTryAndUpload(boolean isNormalUpload){
        int failNum = SPHelper.getIntValueFromSP(mContext,EGContext.FAILEDNUMBER,0);
        int maxFailCount = PolicyImpl.getInstance(mContext).getSP().getInt(DeviceKeyContacts.Response.RES_POLICY_FAIL_COUNT,EGContext.FAIL_COUNT_DEFALUT);
        long faildTime = SPHelper.getLongValueFromSP(mContext,EGContext.FAILEDTIME,0);
        long retryTime = SystemUtils.intervalTime(mContext);
        if (isNormalUpload) {
            doUpload();
        }else if (!isNormalUpload && failNum > 0 && (failNum < maxFailCount)
                && (System.currentTimeMillis() - faildTime> retryTime)) {
            doUpload();
        } else if(!isNormalUpload && failNum> 0 && (failNum == maxFailCount)&& (System.currentTimeMillis() - faildTime> retryTime)){
            doUpload();
            //上传失败次数
            MessageDispatcher.getInstance(mContext).killRetryWorker();
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
                ELOG.e(t);
            }
        }
    }
    private void doUploadImpl(){
        try {
            String uploadInfo = getInfo();
            if(EGContext.FLAG_DEBUG_INNER){
                ELOG.i(uploadInfo);
            }
            if (TextUtils.isEmpty(uploadInfo)) {
                return;
            }
//            boolean isDebugMode = SPHelper.getBooleanValueFromSP(mContext,EGContext.DEBUG, false);
            // 重置url
            PolicyImpl.getInstance(mContext).updateUpLoadUrl(EGContext.FLAG_DEBUG_USER);
            String url = EGContext.NORMAL_APP_URL;
            if (EGContext.FLAG_DEBUG_USER) {
                url = EGContext.TEST_URL;
            }
            handleUpload(url, messageEncrypt(uploadInfo));
            int failNum = SPHelper.getIntValueFromSP(mContext,EGContext.FAILEDNUMBER,0);
            int maxFailCount = PolicyImpl.getInstance(mContext).getSP().getInt(DeviceKeyContacts.Response.RES_POLICY_FAIL_COUNT,EGContext.FAIL_COUNT_DEFALUT);
            // 3. 兼容多次分包的上传
            while (isChunkUpload && failNum < maxFailCount) {
                uploadInfo = getInfo();
                handleUpload(url, messageEncrypt(uploadInfo));
            }
        } catch (Throwable t) {
            if(EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(t);
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
            JSONObject devJson = null;
            try {
                devJson = DataPackaging.getDevInfo(mContext);
            }catch (Throwable t){}
            if (devJson != null && devJson.length() > 0) {
                object.put(DI, devJson);
            }
            //从oc表查询closeTime不为空的整条信息，组装上传
            if(PolicyImpl.getInstance(mContext).getValueFromSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_OC,true)){
                JSONArray ocJson = TableOC.getInstance(mContext).select();
                if (ocJson != null && ocJson.length() > 0 ) {
                    object.put(OCI, ocJson);
                }
            }else {
                TableOC.getInstance(mContext).deleteAll();
            }
            //策略控制大模块收集则进行数据组装，不收集则删除数据
            if(PolicyImpl.getInstance(mContext).getValueFromSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_SNAPSHOT,true)) {
                JSONArray snapshotJar = TableAppSnapshot.getInstance(mContext).select();
                if (snapshotJar != null && snapshotJar.length() > 0 ) {
                    object.put(ASI, snapshotJar);
                }
            }else {
                TableAppSnapshot.getInstance(mContext).deleteAll();
            }
            //策略控制大模块收集则进行数据组装，不收集则删除数据
            if(PolicyImpl.getInstance(mContext).getValueFromSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_LOCATION,true)){
                JSONArray locationInfo = TableLocation.getInstance(mContext).select();
                if (locationInfo != null && locationInfo.length() > 0) {
                    object.put(LI, locationInfo);
                }
            }else {
                TableLocation.getInstance(mContext).deleteAll();
            }
            //策略控制大模块收集则进行数据组装，不收集则删除数据
            if(PolicyImpl.getInstance(mContext).getValueFromSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_XXX,true)){
                JSONArray xxxInfo = getUploadXXXInfos(mContext,object);
                if (xxxInfo != null && xxxInfo.length() > 0) {
                    object.put(XXXInfo, xxxInfo);
                }
            }else {
                TableXXXInfo.getInstance(mContext).delete();
            }
        } catch (Throwable e) {
            if(EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(e);
            }
        }
        return String.valueOf(object);
    }
//    private JSONArray uploadLocationInfo(){
//        JSONArray oldArray = null , newArray = null,locationArray = null,wifiFilterArray = null,baseStationFilterArray = null;
//        JSONObject locationObj = null,tempObject = null;
//        String sdkv = null,locationInfo = null;
//        try {
//           locationArray = TableLocation.getInstance(mContext).select();
//           if(locationArray == null || locationArray.length()<1){
//               return null;
//           }else{
//               oldArray = new JSONArray();
//               newArray = new JSONArray();
//               wifiFilterArray = new JSONArray();
//               baseStationFilterArray = new JSONArray();
//               for(int i = 0;i < locationArray.length();i++){
//                   locationObj = new JSONObject();
//                   locationObj = (JSONObject)locationArray.get(i);
//                   sdkv = locationObj.optString(EGContext.VERSION);
//                   //老版本则按原数据进行拼接
//                   if(TextUtils.isEmpty(sdkv) || sdkv.length() < 1){
//                       locationInfo = locationObj.optString(EGContext.LOCATION_INFO);
//                       if(oldArray != null && !TextUtils.isEmpty(locationInfo)){
//                           oldArray.put(new JSONObject(locationInfo));
//                       }
//                   }else{
//                       //获取到location值
//                        locationInfo = locationObj.optString(EGContext.LOCATION_INFO);
//                        if(newArray != null && !TextUtils.isEmpty(locationInfo) && locationInfo.length()>0){
//                            tempObject = new JSONObject(locationInfo);
//                            newArray = locationInfoFiltered(tempObject,wifiFilterArray,baseStationFilterArray,newArray);
//                        }
//                   }
//               }
//           }
//        }catch (Throwable t){
//            if(EGContext.FLAG_DEBUG_INNER){
//                ELOG.e(t);
//            }
//        }
//        if(oldArray != null && oldArray.length() > 0){
//            return oldArray;
//        }else {
//            return newArray;
//        }
//    }

//    /**
//     * locationInfo数据按照策略过滤不需要的模块或字段
//     * @param tempObject
//     * @param wifiFilterArray
//     * @param baseStationFilterArray
//     * @param newArray
//     * @return
//     */
//    private JSONArray locationInfoFiltered(JSONObject tempObject, JSONArray wifiFilterArray, JSONArray baseStationFilterArray, JSONArray newArray){
//        try {
//            JSONObject wifiFilterObject = null,baseStationFilterObject = null,filterObject = null;
//            //拆分location值，拆为经纬度、wifi、基站
//            String ct = tempObject.optString(DeviceKeyContacts.LocationInfo.CollectionTime);
//            String gl = tempObject.optString(DeviceKeyContacts.LocationInfo.GeographyLocation);
//            JSONArray wifiInfo = tempObject.optJSONArray(DeviceKeyContacts.LocationInfo.WifiInfo.NAME);
//            JSONArray baseStationInfo = tempObject.optJSONArray(DeviceKeyContacts.LocationInfo.BaseStationInfo.NAME);
//            //wifi不为空且需要获取
//            if(wifiInfo != null && wifiInfo.length() > 0 && PolicyImpl.getInstance(mContext).getValueFromSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_WIFI,DataController.SWITCH_OF_WIFI_NAME)){
//                for(int k = 0;k<wifiInfo.length();k++){
//                    tempObject = (JSONObject)wifiInfo.get(k);
//                    wifiFilterObject = new JSONObject();
//                    String ssid = tempObject.optString(DeviceKeyContacts.LocationInfo.WifiInfo.SSID);
//                    String bssid = tempObject.optString(DeviceKeyContacts.LocationInfo.WifiInfo.BSSID);
//                    int level = tempObject.optInt(DeviceKeyContacts.LocationInfo.WifiInfo.Level);
//                    String capabilities = tempObject.optString(DeviceKeyContacts.LocationInfo.WifiInfo.Capabilities);
//                    int frequency = tempObject.optInt(DeviceKeyContacts.LocationInfo.WifiInfo.Frequency);
//                    wifiFilterObject = WifiImpl.getInstance(mContext).getWifiInfoObj(wifiFilterObject,ssid,bssid,level,capabilities,frequency);
//                    if(wifiFilterObject != null && wifiFilterObject.length() > 0){
//                        wifiFilterArray.put(wifiFilterObject);
//                    }
//
//                }
//            }else {
//                wifiFilterArray = null;
//            }
//            if(baseStationInfo != null && baseStationInfo.length() > 0 && PolicyImpl.getInstance(mContext).getValueFromSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_BASE,DataController.SWITCH_OF_BS_NAME)){
//                for(int t = 0;t<wifiInfo.length();t++){
//                    tempObject = (JSONObject)wifiInfo.get(t);
//                    baseStationFilterObject = new JSONObject();
//                    int lac = tempObject.optInt(DeviceKeyContacts.LocationInfo.BaseStationInfo.LocationAreaCode);
//                    int cid = tempObject.optInt(DeviceKeyContacts.LocationInfo.BaseStationInfo.CellId);
//                    int level = tempObject.optInt(DeviceKeyContacts.LocationInfo.BaseStationInfo.Level);
//                    baseStationFilterObject = LocationImpl.getInstance(mContext).getBaseStationInfoObj(baseStationFilterObject,lac,cid,level);
//                    if(baseStationFilterObject != null && baseStationFilterObject.length() > 0){
//                        baseStationFilterArray.put(baseStationFilterObject);
//                    }
//                }
//            }else {
//                baseStationFilterArray = null;
//            }
//            filterObject = new JSONObject();
//            //数据重新组装
//            JsonUtils.pushToJSON(mContext,filterObject,DeviceKeyContacts.LocationInfo.CollectionTime,ct,DataController.SWITCH_OF_COLLECTION_TIME);
//            JsonUtils.pushToJSON(mContext,filterObject,DeviceKeyContacts.LocationInfo.GeographyLocation,gl,DataController.SWITCH_OF_GEOGRAPHY_LOCATION);
//            JsonUtils.pushToJSON(mContext,filterObject,DeviceKeyContacts.LocationInfo.WifiInfo.NAME,wifiFilterArray,DataController.SWITCH_OF_WIFI_NAME);
//            JsonUtils.pushToJSON(mContext,filterObject,DeviceKeyContacts.LocationInfo.BaseStationInfo.NAME,baseStationFilterArray,DataController.SWITCH_OF_BS_NAME);
//            if(filterObject.has(DeviceKeyContacts.LocationInfo.GeographyLocation) || filterObject.has(DeviceKeyContacts.LocationInfo.WifiInfo.NAME)
//                    || filterObject.has(DeviceKeyContacts.LocationInfo.BaseStationInfo.NAME)){
//                newArray.put(filterObject);
//            }
//        }catch (Throwable t){
//        }
//        return newArray;
//    }
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
            if (TextUtils.isEmpty(keyInner)) {
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
            if(EGContext.FLAG_DEBUG_INNER){
                ELOG.e(t);
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
            if(EGContext.FLAG_DEBUG_INNER){
                ELOG.i(json);
            }
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
                        MessageDispatcher.getInstance(mContext).checkRetry();
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
                ELOG.e(e);
            }
        }
    }
    String fail = "-1";
    private void handleUpload(final String url, final String uploadInfo) {

        String result = RequestUtils.httpRequest(url, uploadInfo, mContext);
        if (TextUtils.isEmpty(result)) {
            return;
        }else if(fail.equals(result)){
            SPHelper.setIntValue2SP(mContext,EGContext.REQUEST_STATE,EGContext.sPrepare);
//            //上传失败次数
//            SPHelper.setIntValue2SP(mContext,EGContext.FAILEDNUMBER,SPHelper.getIntValueFromSP(mContext,EGContext.FAILEDNUMBER,0)+1);
//            MessageDispatcher.getInstance(mContext).checkRetry();
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
                JSONObject jsonObject = null;
                for (int i = 0; i < ss; i++) {
                    info = (String) jsonArray.get(i);
                    // 判断单条大小是否超限,删除单条数据
                    if (info.getBytes().length > freeLen) {
                        jsonObject = new JSONObject(new String(Base64.decode(info.getBytes(),Base64.DEFAULT)));
                        idList.add(jsonObject.getString(ProcUtils.ID));
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
                        jsonObject = new JSONObject(new String(Base64.decode(info.getBytes(),Base64.DEFAULT)));
                        idList.add(jsonObject.getString(ProcUtils.ID));
                        jsonObject.remove(ProcUtils.ID);
                        arr.put(new String(Base64.encode(jsonObject.toString().getBytes(),Base64.DEFAULT)));
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
                    idList.clear();
                }

            }
        } catch (Throwable e) {
            if(EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(e);
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
            SPHelper.setLongValue2SP(mContext,EGContext.FAILEDTIME,0);
            SPHelper.setLongValue2SP(mContext,EGContext.RETRYTIME,0);
            //上传完成回来清理数据的时候，snapshot删除卸载的，其余的统一恢复成正常值
            TableAppSnapshot.getInstance(mContext).delete();
            TableAppSnapshot.getInstance(mContext).update();

            //location全部删除已读的数据，最后一条无需保留，sp里有
            TableLocation.getInstance(mContext).delete();

            //按time值delete xxxinfo表和proc表
            TableXXXInfo.getInstance(mContext).deleteByID(idList);
            if(idList != null && idList.size()>0){
                idList.clear();
            }
            TableOC.getInstance(mContext).delete();
        }catch (Throwable t){
            if(EGContext.FLAG_DEBUG_INNER){
                ELOG.e(t);
            }
        }finally {
            MessageDispatcher.getInstance(mContext).killRetryWorker();
        }
    }
    /**
     * 数据上传失败 记录信息
     */
    private void uploadFailure(Context mContext) {
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
                ELOG.e(t);
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
