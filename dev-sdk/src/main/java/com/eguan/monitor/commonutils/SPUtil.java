package com.eguan.monitor.commonutils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.eguan.monitor.Constants;

/**
 * 本地XML格式字段缓存存储工具
 */
@SuppressLint("CommitPrefEdits")
public class SPUtil {


    private static SharedPreferences sp;
    private static Editor editor;
    private static SPUtil instance = null;




    private SPUtil() {
    }

    @SuppressLint("WorldReadableFiles")
    @SuppressWarnings("deprecation")
    public static synchronized SPUtil getInstance(Context context) {
        if (context == null) {
            return instance;
        }
        if (instance == null) {
            instance = new SPUtil();
//            sp = context.getApplicationContext().getSharedPreferences(Constants.SPUTIL, Context.MODE_PRIVATE);
            sp = SharedPreferencesUtils.getSharedPreferences(context, Constants.SPUTIL);
            editor = sp.edit();
        }
        return instance;
    }

    /**
     * 网络请求当前状态（0：请求完成，可以进行下次请求；1：开始请求；2：正在请求）
     *
     * @return
     */

    public int getRequestState() {
        return sp.getInt(Constants.REQUEST_STATE, 0);
    }

    public void setRequestState(int requestState) {
        editor.putInt(Constants.REQUEST_STATE, requestState);
        editor.commit();
    }

    /**
     * 用于对比卸载应用的列表缓存信息
     *
     * @return
     */
    public String getAllAppForUninstall() {
        return sp.getString(Constants.ALLAPPFORUNINSTALL, "");
    }

    public void setAllAppForUninstall(String allAppForUninstall) {
        editor.putString(Constants.ALLAPPFORUNINSTALL, allAppForUninstall);
        editor.commit();
    }

    /**
     * 最后打开的应用包名
     */
    public void setLastOpenPackgeName(String packageName) {
        editor.putString(Constants.LASTPACKAGENAME, packageName);
        editor.commit();
    }

    public String getLastOpenPackgeName() {
        return sp.getString(Constants.LASTPACKAGENAME, "");
    }

    /**
     * 应用的打开时间
     *
     * @return
     */
    public String getLastOpenTime() {
        return sp.getString(Constants.LASTOPENTIME, "");
    }

    public void setLastOpenTime(String lastOpenTime) {
        editor.putString(Constants.LASTOPENTIME, lastOpenTime);
        editor.commit();
    }

    /**
     * 打开的应用名称
     *
     * @return
     */
    public String getLastAppName() {
        return sp.getString(Constants.LASTAPPNAME, "");
    }

    public void setLastAppName(String lastAppName) {
        editor.putString(Constants.LASTAPPNAME, lastAppName);
        editor.commit();
    }

    /**
     * 打开的应用版本号
     *
     * @return
     */
    public String getLastAppVerison() {
        return sp.getString(Constants.LASTAPPVERSION, "");
    }

    public void setLastAppVerison(String lastAppVerison) {
        editor.putString(Constants.LASTAPPVERSION, lastAppVerison);
        editor.commit();
    }

    /**
     * 设备信息JSON
     *
     * @return
     */
    public String getDiverInfoJson() {
        return sp.getString(Constants.DIVINFOJSON, "");
    }

    public void setDiverInfoJson(String diverInfoJson) {
        editor.putString(Constants.DIVINFOJSON, diverInfoJson);
        editor.commit();
    }

    /**
     * 地理位置获取缓存间隔时间，该值大于1小时的毫秒数，即获取地理位置经纬度
     *
     * @return
     */
    public long getLongTime() {
        return sp.getLong(Constants.LONGTIME, 0);
    }

    public void setLongTime(long longTime) {
        editor.putLong(Constants.LONGTIME, longTime);
        editor.commit();
    }

    /**
     * 上一次请求成功的时间戳
     *
     * @return
     */
    public long getLastQuestTime() {
        return sp.getLong(Constants.LASTQUESTTIME, 0);
    }

    public void setLastQuestTime(long lastQuestTime) {
        editor.putLong(Constants.LASTQUESTTIME, lastQuestTime);
        editor.commit();
    }

    /**
     * 网络状态变化缓存值
     *
     * @return
     */
    public String getNetTypeChange() {
        return sp.getString(Constants.NETTYPE, "");
    }

    public void setNetTypeChange(String netTypeChange) {
        editor.putString(Constants.NETTYPE, netTypeChange);
        editor.commit();
    }

//    /**
//     * 地理位置变化缓存值
//     *
//     * @return
//     */
//    public String getLocationChange() {
//        return sp.getString(Constants.LOCATION, "");
//    }
//
//    public void setLocationChange(String locationChange) {
//        editor.putString(Constants.LOCATION, locationChange);
//        editor.commit();
//    }

//	/**
//	 * 进程生命周期变化缓存值
//	 * 
//	 * @return
//	 */
//	public String getProcessLife() {
//		return sp.getString(Constants.PROCESSLIFE, "");
//	}
//
//	public void setProcessLife(String processLife) {
//		editor.putString(Constants.PROCESSLIFE, processLife);
//		editor.commit();
//	}

    /**
     * 应用列表信息JSON
     *
     * @return
     */
    public String getInstallAppJson() {
        return sp.getString(Constants.APPJSON, "");
    }

    public void setInstallAppJson(String installAppJson) {
        editor.putString(Constants.APPJSON, installAppJson);
        editor.commit();
    }

    /**
     * 用户渠道号
     *
     * @param channel
     */
    public void setchannel(String channel) {
        editor.putString(Constants.USERCHANNEL, channel);
        editor.commit();
    }

    public String getChannel() {
        return sp.getString(Constants.USERCHANNEL, "");
    }

    /**
     * 用户Key
     */
    public void setKey(String key) {
        editor.putString(Constants.USERKEY, key);
        editor.commit();
    }

    public String getKey() {
        return sp.getString(Constants.USERKEY, "");
    }

    /**
     * 上传失败次数
     */
    public void setFailedNumb(int numb) {
        editor.putInt(Constants.FAILEDNUMBER, numb);
        editor.commit();
    }

    public int getFailedNumb() {
        return sp.getInt(Constants.FAILEDNUMBER, 0);
    }

    /**
     * 上传失败时间
     */
    public void setFailedTime(long time) {
        editor.putLong(Constants.FAILEDTIME, time);
        editor.commit();
    }

    public long getFailedTime() {
        return sp.getLong(Constants.FAILEDTIME, 0);
    }

    /**
     * 数据上传间隔时间
     *
     * @param time
     */
    public void setRetryTime(long time) {
        editor.putLong(Constants.RETRYTIME, time);
        editor.commit();
    }

    public long getRetryTime() {
        return sp.getLong(Constants.RETRYTIME, 0);
    }

    /**
     * 是否为测试模式
     */
    public boolean getDebugMode() {
        return sp.getBoolean(Constants.DEBUGURL, false);
    }

    public void setDebugMode(boolean debug) {
        editor.putBoolean(Constants.DEBUGURL, debug);
        editor.commit();
    }

    /**
     * 定时器 时间间隔
     *
     * @param numb
     */
    public void setIntervalTime(long numb) {
        editor.putLong(Constants.INTERVALTIME, numb);
        editor.commit();
    }

    public long getIntervalTime() {
        return sp.getLong(Constants.INTERVALTIME, 0);
    }

    /**
     * 是否获取地理位置信息
     *
     * @param str
     */
    public void setLocation(String str) {
        editor.putString(Constants.LOCATIONINFO, str);
        editor.commit();
    }

    public String getLocation() {
        return sp.getString(Constants.LOCATIONINFO, "");
    }

    /**
     * 获取应用列表的时间
     */
    public void setAppList(long time) {
        editor.putLong(Constants.APPLIST, time);
        editor.commit();
    }

    /**
     * 获取应用列表的时间
     */
    public long getAppList() {
        return sp.getLong(Constants.APPLIST, 0);
    }

    /**
     * 存储 get APP Process
     */
    public void setAppProcess(String json) {
        editor.putString(Constants.APPPROCESS, json);
        editor.commit();
    }

    /**
     * 存储 get APP Process
     */
    public String getAppProcess() {
        return sp.getString(Constants.APPPROCESS, "");
    }

    /**
     * 存储APPprocess 时间
     */
    public void setAppProcessTime(long time) {
        editor.putLong(Constants.APPPROCESSTIME, time);
        editor.commit();
    }

    /**
     * 获取APPprocess 时间
     */
    public long getAppProcessTime() {
        return sp.getLong(Constants.APPPROCESSTIME, 0);
    }

    /**
     * 获取wbg时间暂存
     *
     * @param time
     */
    public void setWBGInfoTime(long time) {
        editor.putLong(Constants.WBG_INFO, time);
        editor.commit();
    }

    public long getWBGInfoTime() {
        return sp.getLong(Constants.WBG_INFO, 0);
    }

    /**
     * 进程存活时长
     */
    public void setProcessLifecycle(long data) {
        editor.putLong(Constants.DATATIME, data);
        editor.commit();
    }

    /**
     * 进程存活时长
     */
    public long getProcessLifecycle() {
        return sp.getLong(Constants.DATATIME, 0);
    }

    /**
     * 进程启动时间
     */
    public void setStartTime(long time) {
        editor.putLong(Constants.STARTTIME, time);
        editor.commit();
    }

    /**
     * 进程启动时间
     */
    public long getStartTime() {
        return sp.getLong(Constants.STARTTIME, 0);
    }

    /**
     * 进程关闭时间时间
     */
    public void setEndTime(long time) {

        editor.putLong(Constants.ENDTIME, time);
        editor.commit();
    }

    /**
     * 进程关闭时间时间
     */
    public long getEndTime() {
        return sp.getLong(Constants.ENDTIME, 0);
    }

    /**
     * 获取地理位置信息间隔时间
     */
    public void setGPSTime(long time) {
        editor.putLong(Constants.GETGPSTIME, time);
        editor.commit();
    }

    public Long getGPSTime() {
        return sp.getLong(Constants.GETGPSTIME, 0);
    }

    public void setLastLocation(String location) {
        editor.putString(Constants.LASTLOCATION, location);
        editor.commit();
    }

    public String getLastLocation() {
        return sp.getString(Constants.LASTLOCATION, "");
    }





    public void setEguanId(String id) {
        editor.putString(Constants.EGUANID, id);
        editor.commit();
    }

    public String getEguanId() {
        return sp.getString(Constants.EGUANID, "");
    }

    public void setTmpId(String id) {
        editor.putString(Constants.TMPID, id);
        editor.commit();
    }

    public String getTmpId() {
        return sp.getString(Constants.TMPID, "");
    }



    public void setNetworkState(String state) {
        editor.putString(Constants.NETWORK, state);
        editor.commit();
    }

    public String getNetworkState() {
        return sp.getString(Constants.NETWORK, "");
    }


    public void setNetworkInfo(String state) {
        editor.putString(Constants.NETWORK_INFO, state);
        editor.commit();
    }

    public String getNetworkInfo() {
        return sp.getString(Constants.NETWORK_INFO, "");
    }

    public void setDeviceTactics(String info) {
        editor.putString(Constants.DEVICE_TACTICS, info);
        editor.commit();
    }

    public String getDeviceTactics() {
        return sp.getString(Constants.DEVICE_TACTICS, "");
    }



    public void setNetIpTag(String str) {
        editor.putString(Constants.NET_IP_TAG, str);
        editor.commit();
    }

    public String getNetIpTag() {
        return sp.getString(Constants.NET_IP_TAG, "");
    }

    /**
     * 数据合并间隔时间
     */
    public void setMergeInterval(long s) {
        editor.putLong(Constants.MERGE_INTERVAL, s);
        editor.commit();
    }

    public long getMergeInterval() {
        return sp.getLong(Constants.MERGE_INTERVAL, 0);
    }

    public void setMinDuration(long info) {
        editor.putLong(Constants.MIN_DURATION, info);
        editor.commit();
    }

    public long getMinDuration() {
        return sp.getLong(Constants.MIN_DURATION, 0);
    }

    public void setMaxDuration(long info) {
        editor.putLong(Constants.MAX_DURATION, info);
        editor.commit();
    }
    public long getMaxDuration() {
        return sp.getLong(Constants.MAX_DURATION, 0);
    }


    public String getAppType(){
        return sp.getString(Constants.APP_TYPE,"");
    }
    public void setAppType(String appType){
        editor.putString(Constants.APP_TYPE,appType);
        editor.commit();
    }

    private static final String ACCESSIBILITY_PKG = "accessibility_packagename";
    private static final String ACCESSIBILITY_START_TIME = "accessibility_starttime";
    public void setAccessibilityOCPackageName(String name) {
        editor.putString(ACCESSIBILITY_PKG,name);
        editor.commit();
    }
    public String getAccessibilityOCPackageName(){
        String name = sp.getString(ACCESSIBILITY_PKG, "");
        return name;
    }

    public void setAccessibilityOCStartTime(String startTime){
        editor.putString(ACCESSIBILITY_START_TIME,startTime);
        editor.commit();
    }
    public String getAccessibilityOCStartTime(){
        String startTime = sp.getString(ACCESSIBILITY_START_TIME, "");
        return startTime;
    }

    public void setAppUpdate(int numb) {
        editor.putInt(Constants.APP_UPDATE, numb);
        editor.commit();
    }

    public int getAppUpdate() {
        return sp.getInt(Constants.APP_UPDATE, 0);
    }

    public void setPushProvider(String provider, String pushId) {
        editor.putString(provider,pushId);
        editor.commit();
    }

    public String getPushProvider(String provider){
        String pushId = sp.getString(provider,"");
        return pushId;
    }
}
