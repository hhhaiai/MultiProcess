package com.analysys.track.internal.net;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Base64;

import com.analysys.track.db.TableAppSnapshot;
import com.analysys.track.db.TableLocation;
import com.analysys.track.db.TableOC;
import com.analysys.track.db.TableXXXInfo;
import com.analysys.track.internal.Content.DeviceKeyContacts;
import com.analysys.track.internal.Content.EGContext;
import com.analysys.track.internal.work.MessageDispatcher;
import com.analysys.track.utils.data.AESUtils;
import com.analysys.track.utils.DeflterCompressUtils;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.EThreadPool;
import com.analysys.track.utils.EguanIdUtils;
import com.analysys.track.utils.MultiProcessChecker;
import com.analysys.track.utils.NetworkUtils;
import com.analysys.track.utils.RequestUtils;
import com.analysys.track.utils.SystemUtils;
import com.analysys.track.utils.reflectinon.EContextHelper;
import com.analysys.track.utils.sp.SPHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.net.URLEncoder;
import java.util.ArrayList;
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
     * 是否分包上传
     */
    public static boolean isChunkUpload = false;
    /**
     * 本条记录的时间
     */
    public static List<String> idList = new ArrayList<String>();
    //    public final String DI = "DevInfo";
//    private final String ASI = "AppSnapshotInfo";
//    private final String LI = "LocationInfo";
//    private final String OCI = "OCInfo";
//    private final String XXXInfo = "XXXInfo";
    Context mContext;
    String fail = "-1";

    private UploadImpl() {
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

            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.i("inside upload...");
            }
            if (!NetworkUtils.isNetworkAlive(mContext)) {
                if (EGContext.FLAG_DEBUG_INNER) {
                    ELOG.i("has not network ...will return...");
                }
                return;
            }
            if (SPHelper.getIntValueFromSP(mContext, EGContext.REQUEST_STATE, 0) != 0) {
                if (EGContext.FLAG_DEBUG_INNER) {
                    ELOG.i("already requesting... will break...");
                }
                return;
            }
            long currentTime = System.currentTimeMillis();
            long upLoadCycle = PolicyImpl.getInstance(mContext).getSP()
                    .getLong(DeviceKeyContacts.Response.RES_POLICY_TIMER_INTERVAL, EGContext.UPLOAD_CYCLE);
            MessageDispatcher.getInstance(mContext).uploadInfo(upLoadCycle);
//            L.info(mContext, " 多进程测试...");
            if (MultiProcessChecker.isNeedWorkByLockFile(mContext, EGContext.FILES_SYNC_UPLOAD, upLoadCycle, currentTime)) {
                MultiProcessChecker.setLockLastModifyTime(mContext, EGContext.FILES_SYNC_UPLOAD, currentTime);
            } else {
                if (EGContext.FLAG_DEBUG_INNER) {
                    ELOG.i(" 多进程测试------即将停止...");
                }
                return;
            }
            File dir = mContext.getFilesDir();
            File f = new File(dir, EGContext.DEV_UPLOAD_PROC_NAME);
            long now = System.currentTimeMillis();
            if (f.exists()) {
                long time = f.lastModified();
                long dur = now - time;
                // Math.abs(dur)
                if (dur <= upLoadCycle) {
                    if (EGContext.FLAG_DEBUG_INNER) {
                        ELOG.i(" 文件间隔不对。即将停止....");
                    }
                    return;
                }
            } else {
                f.createNewFile();
                f.setLastModified(now);
            }
            boolean isDurOK = (now - SPHelper.getLongValueFromSP(mContext, EGContext.LASTQUESTTIME, 0)) > upLoadCycle;

            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.i("---------isDurOK------" + isDurOK);
            }
            if (isDurOK) {
                f.setLastModified(now);
                reTryAndUpload(true);
            }
        } catch (Throwable t) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(t);
            }

        }
    }

    public void reTryAndUpload(boolean isNormalUpload) {
        int failNum = SPHelper.getIntValueFromSP(mContext, EGContext.FAILEDNUMBER, 0);
        int maxFailCount = PolicyImpl.getInstance(mContext).getSP()
                .getInt(DeviceKeyContacts.Response.RES_POLICY_FAIL_COUNT, EGContext.FAIL_COUNT_DEFALUT);
        long faildTime = SPHelper.getLongValueFromSP(mContext, EGContext.FAILEDTIME, 0);
        long retryTime = SystemUtils.intervalTime(mContext);
        if (isNormalUpload) {
            doUpload();
        } else if (!isNormalUpload && failNum > 0 && (failNum < maxFailCount)
                && (System.currentTimeMillis() - faildTime > retryTime)) {
            doUpload();
        } else if (!isNormalUpload && failNum > 0 && (failNum == maxFailCount)
                && (System.currentTimeMillis() - faildTime > retryTime)) {
            doUpload();
            // 上传失败次数
            MessageDispatcher.getInstance(mContext).killRetryWorker();
            SPHelper.setIntValue2SP(mContext, EGContext.FAILEDNUMBER, 0);
            SPHelper.setLongValue2SP(mContext, EGContext.LASTQUESTTIME, System.currentTimeMillis());
            SPHelper.setLongValue2SP(mContext, EGContext.FAILEDTIME, 0);
        } else {
            return;
        }
    }

    private void doUpload() {
        isChunkUpload = false;
        try {
            // 如果时间超过一天，并且当前是可网络请求状态，则先上传开发者配置请求，然后上传数据
            SPHelper.setIntValue2SP(mContext, EGContext.REQUEST_STATE, EGContext.sBeginResuest);
            final int serverDelayTime = PolicyImpl.getInstance(mContext).getSP()
                    .getInt(DeviceKeyContacts.Response.RES_POLICY_SERVER_DELAY, EGContext.SERVER_DELAY_DEFAULT);
            if (SystemUtils.isMainThread()) {
                if (serverDelayTime > 0) {
                    // 策略处理
                    EThreadPool.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            doUploadImpl();
                        }
                    }, serverDelayTime);
                } else {
                    // 策略处理
                    EThreadPool.post(new Runnable() {
                        @Override
                        public void run() {
                            doUploadImpl();
                        }
                    });
                }
            } else {
                if (serverDelayTime > 0) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            doUploadImpl();
                        }
                    }, serverDelayTime);
                } else {
                    doUploadImpl();
                }
            }
        } catch (Throwable t) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(t);
            }
        }
    }

    public void doUploadImpl() {
        try {
            String uploadInfo = getInfo();
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.i("uploadInfo: " + uploadInfo);
            }
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.i(uploadInfo);
            }
            if (TextUtils.isEmpty(uploadInfo)) {
                SPHelper.setIntValue2SP(mContext, EGContext.REQUEST_STATE, EGContext.sPrepare);
                return;
            }
            // boolean isDebugMode = SPHelper.getBooleanValueFromSP(mContext,EGContext.DEBUG, false);
            // 重置url
            PolicyImpl.getInstance(mContext).updateUpLoadUrl(EGContext.FLAG_DEBUG_USER);
            String url = EGContext.NORMAL_APP_URL;
            if (EGContext.FLAG_DEBUG_USER) {
                url = EGContext.TEST_URL;
            }
            if (TextUtils.isEmpty(url)) {
                SPHelper.setIntValue2SP(mContext, EGContext.REQUEST_STATE, EGContext.sPrepare);
                return;
            }
//            url = "http://192.168.220.167:8089";
            // L.info(mContext,"url: "+url);

            handleUpload(url, messageEncrypt(uploadInfo));
            int failNum = SPHelper.getIntValueFromSP(mContext, EGContext.FAILEDNUMBER, 0);
            int maxFailCount = PolicyImpl.getInstance(mContext).getSP()
                    .getInt(DeviceKeyContacts.Response.RES_POLICY_FAIL_COUNT, EGContext.FAIL_COUNT_DEFALUT);
            // 3. 兼容多次分包的上传
            while (isChunkUpload && failNum < maxFailCount) {
                if (EGContext.FLAG_DEBUG_INNER) {
                    ELOG.i("开始分包上传...");
                }
                isChunkUpload = false;
                uploadInfo = getInfo();
                if (TextUtils.isEmpty(url)) {
                    SPHelper.setIntValue2SP(mContext, EGContext.REQUEST_STATE, EGContext.sPrepare);
                    return;
                }
                handleUpload(url, messageEncrypt(uploadInfo));
            }
        } catch (Throwable t) {
            if (EGContext.FLAG_DEBUG_INNER) {
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
            // 组装DevInfo数据
            JSONObject devJson = null;
            try {
                devJson = DataPackaging.getDevInfo(mContext);
            } catch (Throwable t) {
            }
            if (devJson != null && devJson.length() > 0) {
                object.put(DeviceKeyContacts.DevInfo.NAME, devJson);
            }
            //  组装OC数据
            if (PolicyImpl.getInstance(mContext).getValueFromSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_OC,
                    true)) {
                long useFulLength = EGContext.LEN_MAX_UPDATE_SIZE * 8 / 10 - String.valueOf(object).getBytes().length;
                if (useFulLength > 0 && !isChunkUpload) {
                    JSONArray ocJson = getModuleInfos(mContext, object, "OC", useFulLength);
                    if (ocJson != null && ocJson.length() > 0) {
                        object.put(DeviceKeyContacts.OCInfo.NAME, ocJson);
                    }
                }
            } else {
                TableOC.getInstance(mContext).deleteAll();
            }
            // 组装位置数据
            if (PolicyImpl.getInstance(mContext)
                    .getValueFromSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_LOCATION, true)) {
                long useFulLength = EGContext.LEN_MAX_UPDATE_SIZE * 8 / 10 - String.valueOf(object).getBytes().length;
                if (useFulLength > 0 && !isChunkUpload) {
                    JSONArray locationInfo = getModuleInfos(mContext, object, "LOCATION", useFulLength);
                    if (locationInfo != null && locationInfo.length() > 0) {
                        object.put(DeviceKeyContacts.AppSnapshotInfo.NAME, locationInfo);
                    }
                }
            } else {
                TableLocation.getInstance(mContext).deleteAll();
            }
            //  组装安装列表数据
            if (PolicyImpl.getInstance(mContext)
                    .getValueFromSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_SNAPSHOT, true)) {
                long useFulLength = EGContext.LEN_MAX_UPDATE_SIZE * 8 / 10 - String.valueOf(object).getBytes().length;
                if (useFulLength > 0 && !isChunkUpload) {
                    JSONArray snapshotJar = getModuleInfos(mContext, object, "SNAPSHOT", useFulLength);
                    if (snapshotJar != null && snapshotJar.length() > 0) {
                        object.put(DeviceKeyContacts.AppSnapshotInfo.NAME, snapshotJar);
                    }
                }
            } else {
                TableAppSnapshot.getInstance(mContext).deleteAll();
            }
            // 组装XXXInfo数据
            if (PolicyImpl.getInstance(mContext).getValueFromSp(DeviceKeyContacts.Response.RES_POLICY_MODULE_CL_XXX,
                    true)) {
                // 计算离最大上线的差值
                long useFulLength = EGContext.LEN_MAX_UPDATE_SIZE * 8 / 10 - String.valueOf(object).getBytes().length;
                if (useFulLength > 0 && !isChunkUpload) {
                    JSONArray xxxInfo = getModuleInfos(mContext, object, "XXX", useFulLength);
                    if (xxxInfo != null && xxxInfo.length() > 0) {
                        object.put(DeviceKeyContacts.XXXInfo.NAME, xxxInfo);
                    }
                }
            } else {
                TableXXXInfo.getInstance(mContext).delete();
            }
        } catch (Throwable e) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(e);
            }
        }
        return String.valueOf(object);
    }

    /**
     * 上传数据加密
     */
    @SuppressWarnings("deprecation")
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
            if (EGContext.FLAG_DEBUG_INNER) {
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
    private void processMsgFromServer(String json) {
        try {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.i(json);
            }
            if (!TextUtils.isEmpty(json)) {
                // 返回413，表示包太大，大于1M字节，本地直接删除
                if (EGContext.HTTP_STATUS_413.equals(json)) {
                    // 删除源数据
                    uploadSuccess(SPHelper.getLongValueFromSP(mContext, EGContext.INTERVALTIME, 0));
                    return;
                }
                JSONObject object = new JSONObject(json);
                String code = String.valueOf(object.opt(DeviceKeyContacts.Response.RES_CODE));
                if (code != null) {
                    if (EGContext.HTTP_STATUS_200.equals(code)) {
                        EguanIdUtils.getInstance(mContext).setId(json);
                        // 清除本地数据
                        uploadSuccess(EGContext.SHORT_TIME);
                        return;
                    }
                    if (EGContext.HTTP_STATUS_500.equals(code)) {
                        isChunkUpload = false;
                        int numb = SPHelper.getIntValueFromSP(mContext, EGContext.FAILEDNUMBER, 0);
                        if (numb == 0) {
                            PolicyImpl.getInstance(mContext)
                                    .saveRespParams(object.optJSONObject(DeviceKeyContacts.Response.RES_POLICY));
                        }
                        uploadFailure(mContext);
                        MessageDispatcher.getInstance(mContext).checkRetry();
                        return;
                    } else {
                        uploadFailure(mContext);
                        return;
                    }
                } else {
                    // 接收消息中没有code值
                    uploadFailure(mContext);
                    return;
                }

            } else {
                // 返回值为空
                uploadFailure(mContext);
                return;
            }
        } catch (Throwable e) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(e);
            }
        }
    }

    //{"code": 500,"policy": {"policyVer": "20190725185335","patch": {"version": "002","sign": "1245ac90db2fc1cb2106172559657804","data": "UEsDBBQACAgIAO6O+U4AAAAAAAAAAAAAAAAUAAQATUVUQS1JTkYvTUFOSUZFU1QuTUb+ygAA803My0xLLS7RDUstKs7Mz7NSMNQz4OVySa3Q9clPTiwBCyXnJBYXpxbrpaRW8HI5F6UmlqSm6DpVWimkVACVG5rxcvFyAQBQSwcI8N6zmEcAAABJAAAAUEsDBBQACAgIAO6O+U4AAAAAAAAAAAAAAAALAAAAY2xhc3Nlcy5kZXidlE1oE0EUx9/MbnaTWtPY2prmtJprSQrqxYhYKX7Aig1CQHvatmvYkmxC3Jb05MfBu3jTCoKChXrRg149VulFvRSh0EsVQUHwLPp/M9MPqyc3+c2bffPezHsz83Ym7PaMHj1ODx/9TH4sr18Yma+tyqf0tbSZ3L97b3VlySZqE1G3dqyfzJOGrkBa3wPWgMMDQv3pPJp9kBPmfQnNhiRagXwB+Ra8Bx/ButRjm+AL+AZsiygPRsA5MAnmwQK4AW6DO+ABeAyWwXPwErwGbwD+lOJYQcbEyTH1gv0gBzgh20AmB+4vIgbX+D+R2jZtbA6YPuu3+s8k+0oaUltgqZwFZsoqmaJBoz8EKaHPE8en7VLbUtKAWXOYeH1b6V20B/XWKiwThzTyu1EI9SP6YOkc2jmeKYv5hPJZQ8PxHcGMBRi2vQzyysv6u4uvPrlXTsejPeRZvTSFsdjjtbNqHvbd+A9fzodj/GzpfKbYX6QQl4PxguyDpUuHqVewHGOZS2OE323MnjX5mmulYhHqLIeKxeKIoVTcfsg5GcVRcoqcsdJsMB+QGCfhk+WPVUj648CnQZ9HylGrPNGJ4uRy0gmDZoX6tboRxPXypanZcDr5Uwe7KK5XaPgv3Zm5qDETdvaYL1xPwua2eRJ2k/J4OB01g8bZVqcZYHZRJbtarZ4gUSNZ82lg8h/LOUG7HcYz5FxTXmQ3gygmqzWXULrNJn6rTi73kkZM6aSlHXFkLvZfoP11FfdeiT5+Ffk+PlFBt27aK5bMbFgis2gLsWbrmqA9e75V43JXnVu7an3rTLjeU7RT8w7t1L3wtB3Xvshpf64v6en5+XtgGRu+u+RpX3Wvc7rP35vfUEsHCFo9p8uGAgAAqAQAAFBLAQIUABQACAgIAO6O+U7w3rOYRwAAAEkAAAAUAAQAAAAAAAAAAAAAAAAAAABNRVRBLUlORi9NQU5JRkVTVC5NRv7KAABQSwECFAAUAAgICADujvlOWj2ny4YCAACoBAAACwAAAAAAAAAAAAAAAACNAAAAY2xhc3Nlcy5kZXhQSwUGAAAAAAIAAgB/AAAATAMAAAAA"}},"tmpid":"","egid":""}
    //
    public void handleUpload(final String url, final String uploadInfo) {

        if (EGContext.FLAG_DEBUG_INNER) {
            ELOG.i(" inside  url: " + url);
        }
        if (TextUtils.isEmpty(url)) {
            SPHelper.setIntValue2SP(mContext, EGContext.REQUEST_STATE, EGContext.sPrepare);
            return;
        }
        String result = RequestUtils.httpRequest(url, uploadInfo, mContext);
        if (EGContext.FLAG_DEBUG_INNER) {
            ELOG.i(" result: " + result);
        }
        if (TextUtils.isEmpty(result)) {
            SPHelper.setIntValue2SP(mContext, EGContext.REQUEST_STATE, EGContext.sPrepare);
            return;
        } else if (fail.equals(result)) {
            SPHelper.setIntValue2SP(mContext, EGContext.REQUEST_STATE, EGContext.sPrepare);
            // //上传失败次数
            // SPHelper.setIntValue2SP(mContext,EGContext.FAILEDNUMBER,SPHelper.getIntValueFromSP(mContext,EGContext.FAILEDNUMBER,0)+1);
            // MessageDispatcher.getInstance(mContext).checkRetry();
            return;
        }
        processMsgFromServer(result);
    }

    /**
     * 数据上传成功 本地数据处理
     */
    private void uploadSuccess(long time) {
        try {
            SPHelper.setIntValue2SP(mContext, EGContext.REQUEST_STATE, EGContext.sPrepare);
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
            TableOC.getInstance(mContext).delete();
            // 上传完成回来清理数据的时候，snapshot删除卸载的，其余的统一恢复成正常值
            TableAppSnapshot.getInstance(mContext).delete();
            TableAppSnapshot.getInstance(mContext).update();

            // location全部删除已读的数据，最后一条无需保留，sp里有
            TableLocation.getInstance(mContext).delete();

            // 按time值delete xxxinfo表和proc表
            TableXXXInfo.getInstance(mContext).deleteByID(idList);
            if (idList != null && idList.size() > 0) {
                idList.clear();
            }
        } catch (Throwable t) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(t);
            }
        } finally {
            MessageDispatcher.getInstance(mContext).killRetryWorker();
        }
    }

    /**
     * 数据上传失败 记录信息
     */
    private void uploadFailure(Context mContext) {
        try {
            SPHelper.setIntValue2SP(mContext, EGContext.REQUEST_STATE, EGContext.sPrepare);
            // 上传失败记录上传次数
            int numb = SPHelper.getIntValueFromSP(mContext, EGContext.FAILEDNUMBER, 0) + 1;
            // 上传失败次数、时间
            SPHelper.setIntValue2SP(mContext, EGContext.FAILEDNUMBER, numb);
            SPHelper.setLongValue2SP(mContext, EGContext.FAILEDTIME, System.currentTimeMillis());
            // 多久重试
            long time = SystemUtils.intervalTime(mContext);
            SPHelper.setLongValue2SP(mContext, EGContext.RETRYTIME, time);
        } catch (Throwable t) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(t);
            }
        }

    }

    private JSONArray getModuleInfos(Context mContext, JSONObject obj, String moduleName, long useFulLength) {
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
            if (TextUtils.isEmpty(moduleName)) {
                return arr;
            }
            if ("OC".equals(moduleName)) {
//                ELOG.e("OC模块数据读取开始：：：");
                arr = TableOC.getInstance(mContext).select(useFulLength);
            } else if ("LOCATION".equals(moduleName)) {
//                ELOG.e("LOCATION模块数据读取开始：：：");
                arr = TableLocation.getInstance(mContext).select(useFulLength);
            } else if ("SNAPSHOT".equals(moduleName)) {
//                ELOG.e("SNAPSHOT模块数据读取开始：：：");
                arr = TableAppSnapshot.getInstance(mContext).select(useFulLength);
            } else if ("XXX".equals(moduleName)) {
//                ELOG.e("XXX模块数据读取开始：：：");
                arr = TableXXXInfo.getInstance(mContext).select(useFulLength);
            }
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.i("isChunkUpload  :::: " + isChunkUpload);
            }
            if (arr == null || arr.length() <= 1) {
                isChunkUpload = false;
                return arr;
            }
        } catch (Throwable e) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(e);
            }
        }
        return arr;
    }

    private static class Holder {
        private static final UploadImpl INSTANCE = new UploadImpl();
    }
}
