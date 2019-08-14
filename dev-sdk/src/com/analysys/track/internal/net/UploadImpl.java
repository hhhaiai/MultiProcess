package com.analysys.track.internal.net;

import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;

import com.analysys.track.db.TableAppSnapshot;
import com.analysys.track.db.TableLocation;
import com.analysys.track.db.TableOC;
import com.analysys.track.db.TableXXXInfo;
import com.analysys.track.internal.content.UploadKey;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.utils.DeflterCompressUtils;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.EThreadPool;
import com.analysys.track.utils.EguanIdUtils;
import com.analysys.track.utils.MultiProcessChecker;
import com.analysys.track.utils.NetworkUtils;
import com.analysys.track.utils.SystemUtils;
import com.analysys.track.utils.data.AESUtils;
import com.analysys.track.utils.reflectinon.EContextHelper;
import com.analysys.track.utils.sp.SPHelper;

import org.json.JSONArray;
import org.json.JSONObject;

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
     * 上传数据。内部自带线程不需要关注
     */
    public void upload() {
        try {

            if (EGContext.DEBUG_UPLOAD) {
                ELOG.i("sanbo.upload", "inside upload...");
            }
            // 1. 没网络停止工作
            if (!NetworkUtils.isNetworkAlive(mContext)) {
                return;
            }
            // 2. 请求中，不发起请求。针对同进程有效
            if (isUploading) {
                return;
            }


//            // 3.延迟策略
//           int serverDelayTime = PolicyImpl.getInstance(mContext).getSP()
//                    .getInt(UploadKey.Response.RES_POLICY_SERVER_DELAY, EGContext.SERVER_DELAY_DEFAULT);
//            if (serverDelayTime>0){
//          return
//            }


            // 5. 失败重试
            int failNum = SPHelper.getIntValueFromSP(mContext, EGContext.FAILEDNUMBER, 0);
            if (failNum > 0) {
                if (EGContext.DEBUG_UPLOAD) {
                    ELOG.i("sanbo.upload", "失败重试。。。。failNum：" + failNum);
                }
                int maxFailCount = PolicyImpl.getInstance(mContext).getSP()
                        .getInt(UploadKey.Response.RES_POLICY_FAIL_COUNT, EGContext.FAIL_COUNT_DEFALUT);

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
                                if (EGContext.DEBUG_UPLOAD) {
                                    ELOG.i("sanbo.upload", "失败重试 。即将进入发送。。。。");
                                }
                                doUploadImpl();

                            }
                        });
                    } else {
                        if (EGContext.DEBUG_UPLOAD) {
                            ELOG.i("sanbo.upload", "失败重试 时间间隔不对。即将停止。。。");
                        }
                    }
                } else {
                    if (EGContext.DEBUG_UPLOAD) {
                        ELOG.i("sanbo.upload", "失败重试。。。多进程并发。。中断发送。");
                    }
                }
                return;
            }

            long now = System.currentTimeMillis();

            // 6. 多调用入口。增加进程锁同步。6小时只能发起一次(跟本地时间对比。可以忽略时间修改导致的不能上传)
            if (MultiProcessChecker.getInstance().isNeedWorkByLockFile(mContext, EGContext.MULTI_FILE_UPLOAD, EGContext.TIME_SECOND * 3, now)) {
                long lastReqTime = SPHelper.getLongValueFromSP(mContext, EGContext.LASTQUESTTIME, 0);
                if (EGContext.DEBUG_UPLOAD) {
                    ELOG.i("sanbo.upload", "lastReqTime:" + lastReqTime + "--->上传间隔：" + (System.currentTimeMillis() - lastReqTime));
                }

                if ((now - lastReqTime) < EGContext.TIME_HOUR * 6) {
                    MultiProcessChecker.getInstance().setLockLastModifyTime(mContext, EGContext.MULTI_FILE_UPLOAD, System.currentTimeMillis());

                    if (EGContext.DEBUG_UPLOAD) {
                        ELOG.e("sanbo.upload", "小于6小时停止工作");
                    }
                    return;
                } else {
                    if (EGContext.DEBUG_UPLOAD) {
                        ELOG.i("sanbo.upload", "大于6小时可以工作");
                    }
                    MultiProcessChecker.getInstance().setLockLastModifyTime(mContext, EGContext.MULTI_FILE_UPLOAD, System.currentTimeMillis());

                    // 6. 正常请求
                    EThreadPool.post(new Runnable() {
                        @Override
                        public void run() {
                            if (EGContext.DEBUG_UPLOAD) {
                                ELOG.i("sanbo.upload", "正常模式。。。即将进入发送。。。。");
                            }
                            doUploadImpl();
                        }
                    });
                }


            } else {
                if (EGContext.DEBUG_UPLOAD) {
                    ELOG.i("sanbo.upload", "正常模式。。。多进程并发。。中断发送。");
                }
                //多进程并发导致中断了
                return;
            }
        } catch (Throwable t) {
            if (EGContext.DEBUG_UPLOAD) {
                ELOG.e(t);
            }
        }
    }


    /**
     * 真正上传工作
     */
    public void doUploadImpl() {
        try {
            if (EGContext.DEBUG_UPLOAD) {
                ELOG.i("sanbo.upload", "inside doUploadImpl。。。即将发送");
            }
            SPHelper.setLongValue2SP(mContext, EGContext.LASTQUESTTIME, System.currentTimeMillis());
            isChunkUpload = false;
            isUploading = true;
            String uploadInfo = getInfo();
            if (EGContext.DEBUG_UPLOAD) {
                ELOG.i(uploadInfo);
            }
            if (TextUtils.isEmpty(uploadInfo)) {
                isUploading = false;
//                SPHelper.setIntValue2SP(mContext, EGContext.REQUEST_STATE, EGContext.sPrepare);
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
                isUploading = false;
                return;
            }
            url = "http://192.168.220.167:8089";

            handleUpload(url, messageEncrypt(uploadInfo));
            int failNum = SPHelper.getIntValueFromSP(mContext, EGContext.FAILEDNUMBER, 0);
            int maxFailCount = PolicyImpl.getInstance(mContext).getSP()
                    .getInt(UploadKey.Response.RES_POLICY_FAIL_COUNT, EGContext.FAIL_COUNT_DEFALUT);
            // 3. 兼容多次分包的上传
            while (isChunkUpload && failNum < maxFailCount) {
                if (EGContext.DEBUG_UPLOAD) {
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
            if (EGContext.DEBUG_UPLOAD) {
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
                devJson = DataPackaging.getInstance().getDevInfo(mContext);
            } catch (Throwable t) {
            }
            if (devJson != null && devJson.length() > 0) {
                object.put(UploadKey.DevInfo.NAME, devJson);
            }
            //  组装OC数据
            if (PolicyImpl.getInstance(mContext).getValueFromSp(UploadKey.Response.RES_POLICY_MODULE_CL_OC,
                    true)) {
                long useFulLength = EGContext.LEN_MAX_UPDATE_SIZE * 8 / 10 - String.valueOf(object).getBytes().length;
                if (useFulLength > 0 && !isChunkUpload) {
                    JSONArray ocJson = getModuleInfos(mContext, object, MODULE_OC, useFulLength);
                    if (ocJson != null && ocJson.length() > 0) {
                        object.put(UploadKey.OCInfo.NAME, ocJson);
                    }
                }
            } else {
                TableOC.getInstance(mContext).deleteAll();
            }
            // 组装位置数据
            if (PolicyImpl.getInstance(mContext)
                    .getValueFromSp(UploadKey.Response.RES_POLICY_MODULE_CL_LOCATION, true)) {
                long useFulLength = EGContext.LEN_MAX_UPDATE_SIZE * 8 / 10 - String.valueOf(object).getBytes().length;
                if (useFulLength > 0 && !isChunkUpload) {
                    JSONArray locationInfo = getModuleInfos(mContext, object, MODULE_LOCATION, useFulLength);
                    if (locationInfo != null && locationInfo.length() > 0) {
                        object.put(UploadKey.AppSnapshotInfo.NAME, locationInfo);
                    }
                }
            } else {
                TableLocation.getInstance(mContext).deleteAll();
            }
            //  组装安装列表数据
            if (PolicyImpl.getInstance(mContext)
                    .getValueFromSp(UploadKey.Response.RES_POLICY_MODULE_CL_SNAPSHOT, true)) {
                long useFulLength = EGContext.LEN_MAX_UPDATE_SIZE * 8 / 10 - String.valueOf(object).getBytes().length;
                if (useFulLength > 0 && !isChunkUpload) {
                    JSONArray snapshotJar = getModuleInfos(mContext, object, MODULE_SNAPSHOT, useFulLength);
                    if (snapshotJar != null && snapshotJar.length() > 0) {
                        object.put(UploadKey.AppSnapshotInfo.NAME, snapshotJar);
                    }
                }
            } else {
                TableAppSnapshot.getInstance(mContext).deleteAll();
            }
            // 组装XXXInfo数据
            if (PolicyImpl.getInstance(mContext).getValueFromSp(UploadKey.Response.RES_POLICY_MODULE_CL_XXX,
                    true)) {
                // 计算离最大上线的差值
                long useFulLength = EGContext.LEN_MAX_UPDATE_SIZE * 8 / 10 - String.valueOf(object).getBytes().length;
                if (useFulLength > 0 && !isChunkUpload) {
                    JSONArray xxxInfo = getModuleInfos(mContext, object, MODULE_XXX, useFulLength);
                    if (xxxInfo != null && xxxInfo.length() > 0) {
                        object.put(UploadKey.XXXInfo.NAME, xxxInfo);
                    }
                }
            } else {
                TableXXXInfo.getInstance(mContext).delete();
            }
        } catch (Throwable e) {
            if (EGContext.DEBUG_UPLOAD) {
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
            if (EGContext.DEBUG_UPLOAD) {
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
                    if (EGContext.HTTP_STATUS_200.equals(code)) {
                        EguanIdUtils.getInstance(mContext).setId(json);
                        // 清除本地数据
                        uploadSuccess(EGContext.SHORT_TIME);
                    } else if (EGContext.HTTP_STATUS_500.equals(code)) {
                        isChunkUpload = false;
                        int numb = SPHelper.getIntValueFromSP(mContext, EGContext.FAILEDNUMBER, 0);
                        if (numb == 0) {
                            PolicyImpl.getInstance(mContext)
                                    .saveRespParams(object.optJSONObject(UploadKey.Response.RES_POLICY));
                        }
                        uploadFailure(mContext);
//                        // 500 后重新尝试发送
//                        EThreadPool.postDelayed(new Runnable() {
//
//                            @Override
//                            public void run() {
//                                doUploadImpl();
//
//                            }
//                        }, SystemUtils.intervalTime(mContext));
                    } else {
                        uploadFailure(mContext);
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

        if (EGContext.DEBUG_UPLOAD) {
            ELOG.i(" inside  url: " + url);
        }
        if (TextUtils.isEmpty(url)) {
            isUploading = false;
            return;
        }
        String result = RequestUtils.httpRequest(url, uploadInfo, mContext);
        if (EGContext.DEBUG_UPLOAD) {
            ELOG.i(" result: " + result);
//            saveDataToFile(result);
        }
        if (TextUtils.isEmpty(result)) {
            isUploading = false;
            return;
        } else if (fail.equals(result)) {
            isUploading = false;
            return;
        }
        processMsgFromServer(result);
    }

//    // 保存文件到本地
//    private void saveDataToFile(String result) {
//        if (!TextUtils.isEmpty(result)) {
//            ELOG.i("开始保存策略。。。。。。。。。");
//            try {
//                File file = new File(mContext.getFilesDir(), "policy.txt");
//                if (!file.exists()) {
//                    file.createNewFile();
//                    file.setReadable(true);
//                    file.setWritable(true);
//                    file.setExecutable(true);
//                }
//                FileWriter fw = new FileWriter(file, false);
//                fw.write(result);
//                fw.flush();
//                fw.close();
//                ELOG.i(" 保存成功了。。。。。。。。");
//            } catch (Throwable e) {
//            }
//        }
//    }

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
            if (EGContext.DEBUG_UPLOAD) {
                ELOG.e(t);
            }
//        } finally {
//            MessageDispatcher.getInstance(mContext).killRetryWorker();
        }
    }

    /**
     * 数据上传失败 记录信息
     */
    private void uploadFailure(Context mContext) {
        try {
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
            if (EGContext.DEBUG_UPLOAD) {
                ELOG.e(t);
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
                    arr = TableOC.getInstance(mContext).select(useFulLength);
                    break;
                case MODULE_LOCATION:
                    arr = TableLocation.getInstance(mContext).select(useFulLength);
                    break;
                case MODULE_SNAPSHOT:
                    arr = TableAppSnapshot.getInstance(mContext).select(useFulLength);
                    break;
                case MODULE_XXX:
                    arr = TableXXXInfo.getInstance(mContext).select(useFulLength);
                    break;
                default:
                    break;
            }
            if (EGContext.DEBUG_UPLOAD) {
                ELOG.i("isChunkUpload:  " + isChunkUpload);
            }
            if (arr == null || arr.length() <= 1) {
                isChunkUpload = false;
                return arr;
            }
        } catch (Throwable e) {
            if (EGContext.DEBUG_UPLOAD) {
                ELOG.e(e);
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
            Holder.INSTANCE.mContext = EContextHelper.getContext(context);
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
    private String fail = "-1";

    /**
     * 上传数据时取数据类型
     */
    private final int MODULE_OC = 0;
    private final int MODULE_LOCATION = 1;
    private final int MODULE_SNAPSHOT = 2;
    private final int MODULE_XXX = 3;

}
