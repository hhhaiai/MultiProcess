package com.eguan.monitor.netutils;

import java.util.List;

import org.json.JSONObject;

import com.eguan.monitor.Constants;
import com.eguan.monitor.aesutils.DataDealUtils;
import com.eguan.monitor.commonutils.AppSPUtils;
import com.eguan.monitor.commonutils.EgLog;
import com.eguan.monitor.commonutils.EguanIdUtils;
import com.eguan.monitor.commonutils.SPUtil;
import com.eguan.monitor.commonutils.SystemUtils;
import com.eguan.monitor.commonutils.TimeUtils;
import com.eguan.monitor.dbutils.device.DeviceTableOperation;
import com.eguan.monitor.imp.IUUInfo;
import com.eguan.monitor.imp.InstalledAPPInfoManager;
import com.eguan.monitor.imp.OCInfo;
import com.eguan.monitor.json.DeviceJsonFactory;

import android.content.Context;
import android.text.TextUtils;

public class DevInfoUpload {

    private DevInfoUpload() {
    }

    public static DevInfoUpload getInstance() {
        return Holder.INSTANCE;
    }

    private static class Holder {
        private static final DevInfoUpload INSTANCE = new DevInfoUpload();
    }

    /**
     * 判断是否需要上传数据
     *
     * @param context
     */
    public void StartpostData(Context context) {

        SPUtil spUtil = SPUtil.getInstance(context);
        if ((System.currentTimeMillis() - spUtil.getLastQuestTime()) > Constants.LONG_INVALIED_TIME) {

            if (spUtil.getRequestState() != 0) {

                return;
            }
            if (!SystemUtils.isNetworkAvailable(context)) {

                return;
            }
            if (spUtil.getFailedNumb() == 0) {

                upload(context);
                if (Constants.FLAG_DEBUG_INNER) {
                    EgLog.v("--------------First upload--------------");
                }

            } else if (spUtil.getFailedNumb() == 1
                    && System.currentTimeMillis() - spUtil.getFailedTime() > spUtil.getRetryTime()) {

                upload(context);
                if (Constants.FLAG_DEBUG_INNER) {
                    EgLog.v("--------------second upload	--------------");
                }

            } else if (spUtil.getFailedNumb() == 2
                    && System.currentTimeMillis() - spUtil.getFailedTime() > spUtil.getRetryTime()) {

                upload(context);
                if (Constants.FLAG_DEBUG_INNER) {
                    EgLog.v("--------------Third upload--------------");
                }

            } else if (spUtil.getFailedNumb() == 3) {

                spUtil.setFailedNumb(0);
                spUtil.setLastQuestTime(System.currentTimeMillis());
                spUtil.setFailedTime(0);
            } else if (spUtil.getFailedNumb() == -1) {
                return;
            }
        }
    }

    /**
     * 开始上传
     *
     * @param context
     */

    public void upload(final Context context) {
        final SPUtil spUtil = SPUtil.getInstance(context);
        // 如果时间超过一天，并且当前是可网络请求状态，则先上传开发者配置请求，然后上传数据
        spUtil.setRequestState(1);
        try {
            String url;
            String postRsult = "";
            String data = getUpPostDataes(context);
            boolean boo = spUtil.getDebugMode();
            Constants.changeUrlNormal(boo);
            if (boo) {
                url = Constants.TEST_CALLBACK_URL;
            } else {
                url = Constants.DEVIER_URL;
            }
            postRsult = ConnectionClient.sendPost(context, url, Constants.DEVICE_UPLOAD_KEY,
                    DataDealUtils.dealUploadData(context, data), AppSPUtils.getInstance(context).getPolicyVer());
            handlerAfterPost(context, postRsult);

            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.v("DevInfoUpload.upload() 上传完毕.... process handlerAfterPost over");
            }
        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        }
    }

    /**
     * 组织上传数据
     *
     * @return
     */
    private String getUpPostDataes(Context mContext) {
        JSONObject jsonObject = new JSONObject();
        try {
            // 设备信息
            jsonObject.put("DevInfo", DeviceJsonFactory.getDeviceJson(mContext));

            // OCInfo打开／关闭信息(OC是Open、Close的首字母缩写)
            jsonObject.put("OCInfo", DeviceJsonFactory.getOCInfo(mContext));
            // OCTimes(OC是Open、Close的首字母缩写)
            jsonObject.put("OCTimes", DeviceJsonFactory.getOCTimesJson(mContext));

            // InstalledAppInfo已安装应用列表信息
            jsonObject.put("InstalledAppInfo", DeviceJsonFactory.getInstalledAPPInfos(mContext));
            // IUUInfo安装、卸载、更新信息
            jsonObject.put("IUUInfo", DeviceJsonFactory.getIUUInfo(mContext));

            // WBGInfo热点信息
            jsonObject.put("WBGInfo", DeviceJsonFactory.getBaseStationInfos(mContext));

            // jsonObject.put("WebInfo", new JSONArray());
            // jsonObject.put("PLInfo", DeviceJsonFactory.getPLInfo(mContext));

            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.d("上传数据结果：----:" + jsonObject);
            }

        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
            return "{}";
        }
        return jsonObject.toString();
    }

    private void handlerAfterPost(Context mContext, String response) {

        if (Constants.FLAG_DEBUG_INNER) {
            EgLog.d("handlerAfterPost：" + response);
        }
        if (mContext == null)
            return;// 如果Context为空的话,不处理
        SPUtil spUtil = SPUtil.getInstance(mContext);
        try {
            String returnCode = "";
            if (response == null || response.equals("")) {
                uploadFailure(mContext);
                return;
            }
            // 返回413，表示包太大，大于1M字节，本地直接删除
            if (response.equals("413")) {
                uploadSuccess(mContext, spUtil.getIntervalTime());
                return;
            }

            JSONObject jsonObject = new JSONObject(response);

            if (jsonObject == null || jsonObject.equals("")) {
                uploadFailure(mContext);
                return;
            }
            EguanIdUtils.getInstance(mContext).setId(response);

            if (jsonObject.has("code")) {
                returnCode = jsonObject.getString("code");
            }

            // 策略处理
            TacticsManager.getInstance(mContext).devTacticsProcess(jsonObject);

            if (TextUtils.isEmpty(returnCode) || returnCode.equals("500")) {
                uploadFailure(mContext);
                return;
            }
            long lon = TimerTime(returnCode);
            uploadSuccess(mContext, lon);

        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        }
    }

    /**
     * 数据上传成功 本地数据处理
     */
    private void uploadSuccess(Context mContext, long time) {

        // if (Constants.FLAG_DEBUG_INNER) {
        // EgLog.v("uploadSuccess() <<< 数据长传成功，处理本地逻辑 >>>");
        // }

        SPUtil spUtil = SPUtil.getInstance(mContext);
        spUtil.setRequestState(0);
        if (time != spUtil.getIntervalTime()) {
            spUtil.setIntervalTime(time);
        }
        // 上传成功，更改本地缓存
        /*-----------------缓存这次上传成功的时间-------------------------*/
        spUtil.setLastQuestTime(System.currentTimeMillis());
        /*-----------------清除本地打开关闭应用行为信息缓存记录-------------------------*/
        List<OCInfo> list = DeviceTableOperation.getInstance(mContext).selectOCInfo();
        DeviceTableOperation.getInstance(mContext).deleteOCInfo(list);
        /*-----------------清除本地应用安装卸载行为信息缓存记录------------------------*/
        List<IUUInfo> list2 = DeviceTableOperation.getInstance(mContext).selectIUUInfo();
        DeviceTableOperation.getInstance(mContext).deleteIUUInfo(list2);
        /*-----------------重新缓存当前设备的所有应用列表信息------------------------*/
        InstalledAPPInfoManager appManager = new InstalledAPPInfoManager();
        spUtil.setInstallAppJson(appManager.getAppInfoToJson(InstalledAPPInfoManager.getAllApps(mContext)));
        /*-----------------清除OCTimeTable------------------------*/
        DeviceTableOperation.getInstance(mContext).deleteOCTimeTable();
        SPUtil.getInstance(mContext).setNetTypeChange("");
        SPUtil.getInstance(mContext).setRetryTime(0);
        DeviceTableOperation.getInstance(mContext).deleteWBGInfo();
        // spUtil.setProcessLifecycle(0);
        // if (Constants.FLAG_DEBUG_INNER) {
        // EgLog.v("end uploadSuccess !");
        // }
    }

    /**
     * 数据上传失败 记录信息
     */
    private void uploadFailure(Context mContext) {
        SPUtil spUtil = SPUtil.getInstance(mContext);
        /*--------------------------------上传失败记录上传次数-------------------------------------*/
        if (Constants.FLAG_DEBUG_INNER) {
            EgLog.w("----uploadFailure()--------数据上传失败  开始重试---------");
        }
        spUtil.setRequestState(0);
        int numb = spUtil.getFailedNumb() + 1;
        spUtil.setFailedNumb(numb);
        spUtil.setFailedTime(System.currentTimeMillis());
        long time = TimeUtils.intervalTime();
        if (Constants.FLAG_DEBUG_INNER) {
            EgLog.d("---uploadFailure()----重试上传间隔时间-------" + time);
        }
        spUtil.setRetryTime(time);
    }

    private long TimerTime(String str) {
        long time = 0;
        switch (Integer.valueOf(str)) {
        case 200:
        case 700:
            time = Constants.SHORT_TIME;
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
