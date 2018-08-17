package com.eguan.monitor.fangzhou.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.PowerManager;

import com.eguan.monitor.Constants;
import com.eguan.monitor.cache.InnerProcessCacheManager;
import com.eguan.monitor.commonutils.EgLog;
import com.eguan.monitor.commonutils.MyThread;
import com.eguan.monitor.commonutils.ReceiverUtils;
import com.eguan.monitor.commonutils.SPUtil;
import com.eguan.monitor.dbutils.device.DeviceTableOperation;
import com.eguan.monitor.imp.AppProcessManager;
import com.eguan.monitor.imp.InstalledAPPInfoManager;
import com.eguan.monitor.imp.InstalledAppInfo;
import com.eguan.monitor.imp.OCInfoManager;
import com.eguan.monitor.manager.AccessibilityOCManager;
import com.eguan.monitor.thread.EGQueue;
import com.eguan.monitor.thread.SafeRunnable;

import java.util.List;

/**
 * 设备监测主服务程序
 */
public class MonitorService extends Service {

    Context context = MonitorService.this;

    //--------------地理位置信息--------------
    private SPUtil spUtil = null;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        if (Constants.FLAG_DEBUG_INNER) {
            EgLog.v("MonitorService.onCreate");
        }
        EGQueue.execute(new SafeRunnable() {
            @Override
            public void safeRun() {
                try {
                    //网络未发生变化前,获取NT信息
                    InnerProcessCacheManager.getInstance().dealAppNetworkType(context);
                    OCInfoManager.getInstance(context).filterInsertOCInfo(Constants.SERVCICE_RESTART, true);
                    AccessibilityOCManager.getInstance(context).updateServiceBootOCInfo();
                    //处理5.0的proc数据
                    AppProcessManager.getInstance(context).dealRestartService();
                    DeviceTableOperation.getInstance(context).initDB();
                } catch (Throwable e) {
                    if (Constants.FLAG_DEBUG_INNER) {
                        EgLog.e(e);
                    }
                }
                initInfo();
                //同步应用的URL到设备，确保地址为最新
                Constants.setNormalUploadUrl();
                Constants.setRTLUploadUrl();
            }
        });

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        getKeyAndChannel();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        EGQueue.execute(new SafeRunnable() {
            @Override
            public void safeRun() {
                try {
                    String str = spUtil.getDeviceTactics();
                    if (!str.equals(Constants.TACTICS_STATE)) {
                        ReceiverUtils.getInstance().unRegistAllReceiver(context, true);
                        MyThread.getInstance(context).stopThread();
                        startService(new Intent(context, MonitorService.class));
                    }
                } catch (Throwable e) {
                    if (Constants.FLAG_DEBUG_INNER) {
                        EgLog.e(e);
                    }
                }
            }
        });
        super.onDestroy();
    }

    @SuppressWarnings("deprecation")
    private void initInfo() {
        try {
            spUtil = SPUtil.getInstance(this);
//            ProcessTimeManager.getInstance().setProcessTime(context);
            /*------------------ 初始化OCInfo存储信息 -----------------*/
            InitializationOCSP();
            /*---------------缓存最新应用列表信息，对于对比卸载变化情况---------------------*/
            InstalledAPPInfoManager manager = new InstalledAPPInfoManager();
            List<InstalledAppInfo> list = InstalledAPPInfoManager.getAllApps(this);
            spUtil.setAllAppForUninstall(manager.getAppInfoToJson(list));
            /*---------------设置本次启动，允许进行网络请求---------------------*/
            spUtil.setRequestState(0);
            /*--------------- 注销所有存活着的广播 -----------------------*/
//			ReceiverUtils.getInstance().unRegistAllReceiver(context, true);
            /*--------------- 注册所有广播 -----------------------*/
            ReceiverUtils.getInstance().registerScreenReceiver(context);
            /*-----------------判断是否获取地理位置信息----------------*/
            LocationInfo();
            /*----- 五秒定时器启动，获取打开关闭数据 获取，判断是否需要上传数据----*/
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            boolean isScreenOn = pm.isScreenOn();
            // 如果为true，则表示屏幕正在使用，false则屏幕关闭。
            if (isScreenOn) {

                if (Constants.FLAG_DEBUG_INNER) {
                    EgLog.v("----------------ScreenOn will  registAllReceiver--------------");
                }
                /*--------------- 注册所有存活着的广播 -----------------------*/
                ReceiverUtils.getInstance().registAllReceiver(context);
//                GlobalTimer.getInstance(MonitorService.this).startAlarm();
            }
        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        }
    }

    /**
     * 初始化OCInfo存储信息
     */
    private void InitializationOCSP() {
        spUtil.setLastOpenPackgeName("");
        spUtil.setLastOpenTime("");
        spUtil.setLastAppName("");
        spUtil.setLastAppVerison("");
        spUtil.setAppProcess("");
    }

    /**
     * 读取配置信息是否获取地理位置信息
     */
    private void LocationInfo() {
        ApplicationInfo appInfo = null;
        try {
            appInfo = getPackageManager().getApplicationInfo(
                    getPackageName(), PackageManager.GET_META_DATA);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        SPUtil.getInstance(MonitorService.this).setLocation(appInfo.metaData.getString(Constants.LI));
    }

    private void getKeyAndChannel() {
        try {
            Constants.APP_KEY_VALUE = SPUtil.getInstance(context).getKey();
            Constants.APP_CHANNEL_VALUE = SPUtil.getInstance(context).getChannel();
        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        }
    }
}
