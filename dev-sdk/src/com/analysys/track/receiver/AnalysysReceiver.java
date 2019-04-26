package com.analysys.track.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;

import com.analysys.track.impl.OCImpl;
import com.analysys.track.internal.Content.DataController;
import com.analysys.track.impl.DeviceImpl;
import com.analysys.track.utils.FileUtils;
import com.analysys.track.utils.sp.SPHelper;
import com.analysys.track.work.CheckHeartbeat;
import com.analysys.track.work.MessageDispatcher;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.EThreadPool;
import com.analysys.track.utils.ReceiverUtils;
import com.analysys.track.utils.SystemUtils;

import com.analysys.track.internal.Content.EGContext;

public class AnalysysReceiver extends BroadcastReceiver {
    Context mContext;
    String PACKAGE_ADDED = "android.intent.action.PACKAGE_ADDED";
    String PACKAGE_REMOVED = "android.intent.action.PACKAGE_REMOVED";
    String PACKAGE_REPLACED = "android.intent.action.PACKAGE_REPLACED";

    String SCREEN_ON = "android.intent.action.SCREEN_ON";
    String SCREEN_OFF = "android.intent.action.SCREEN_OFF";
    String CONNECTIVITY_CHANGE = "android.net.conn.CONNECTIVITY_CHANGE";
    String BATTERY_CHANGED = "android.intent.action.BATTERY_CHANGED";
    String BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";
    //上次结束时间
    private static long mLastCloseTime = 0;
    private AnalysysReceiver(){}
    public static AnalysysReceiver getInstance() {
        return AnalysysReceiver.Holder.INSTANCE;
    }
    private static class Holder {
        private static final AnalysysReceiver INSTANCE = new AnalysysReceiver();
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            if (intent == null){
                return;
            }
            String data = intent.getDataString();
            String packageName = "";
            if(!TextUtils.isEmpty(data)){
                packageName = data.substring(8);
            }
            mContext = context.getApplicationContext();

            if (PACKAGE_ADDED.equals(intent.getAction())) {
                ELOG.d("接收到应用安装广播：" + packageName);
                //TODO 多测试
                MessageDispatcher.getInstance(mContext).appChangeReceiver(packageName, Integer.parseInt(EGContext.SNAP_SHOT_INSTALL),System.currentTimeMillis());
            }
            if (PACKAGE_REMOVED.equals(intent.getAction())) {
                ELOG.d("接收到应用卸载广播：" + packageName);
                MessageDispatcher.getInstance(mContext).appChangeReceiver(packageName, Integer.parseInt(EGContext.SNAP_SHOT_UNINSTALL),System.currentTimeMillis());
            }
            if (PACKAGE_REPLACED.equals(intent.getAction())) {
                ELOG.d("接收到应用更新广播：" + packageName);
                MessageDispatcher.getInstance(mContext).appChangeReceiver(packageName, Integer.parseInt(EGContext.SNAP_SHOT_UPDATE),System.currentTimeMillis());
            }
            if (CONNECTIVITY_CHANGE.equals(intent.getAction())) {
                ELOG.d("接收网络变化广播");
                //TODO 网络变化操作确认
//                WifiImpl.getInstance(mContext).getWifiInfo();
//                MessageDispatcher.getInstance(mContext).startService();
            }
            if (SCREEN_ON.equals(intent.getAction())) {
                ELOG.e("接收开启屏幕广播");
                //设置开锁屏的flag 用于补数逻辑
                EGContext.SCREEN_ON = true;
//                ProcessManager.setIsCollected(true);
                processScreenOnOff(true);
                CheckHeartbeat.getInstance(mContext).sendMessages();
            }
            if (SCREEN_OFF.equals(intent.getAction())) {
                EGContext.SCREEN_ON = false;
//                ProcessManager.setIsCollected(false);
                processScreenOnOff(false);

                ELOG.e("接收关闭屏幕广播::::"+System.currentTimeMillis());
            }
            if (BATTERY_CHANGED.equals(intent.getAction())) {
                if(DataController.SWITCH_OF_MODULE_CL_BATTERY){
                    DeviceImpl.getInstance(mContext).processBattery(intent);
                }
                ELOG.e("电池变化广播");
            }
            if (BOOT_COMPLETED.equals(intent.getAction())) {
                ELOG.e("接收到开机广播");
                MessageDispatcher.getInstance(mContext).startService();
            }
        }catch (Throwable t){
            if(EGContext.FLAG_DEBUG_INNER){
                ELOG.e(t.getMessage());
            }
        }
    }

    private void processScreenOnOff(final boolean on) {
        try {
            if (SystemUtils.isMainThread()) {
                EThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        screenOnOffHandle(on);
                    }
                });

            } else {
                screenOnOffHandle(on);
            }
        } catch (Throwable e) {
            if(EGContext.FLAG_DEBUG_INNER){
                ELOG.e(e.getMessage());
            }
        }
    }
    /**
     * 锁屏补时间
     */
    private void screenOnOffHandle(boolean on){
        // 补充时间
        if(mLastCloseTime == 0){//第一次时间为空，则取sp时间
            long spLastVisitTime = FileUtils.getLockFileLastModifyTime(mContext,EGContext.FILES_SYNC_SP_WRITER);
            if(System.currentTimeMillis() - spLastVisitTime > EGContext.TIME_SYNC_SP){//即便频繁开关屏也不能频繁操作sp
                mLastCloseTime = SPHelper.getLongValueFromSP(mContext,EGContext.LAST_AVAILABLE_TIME, 0);
                FileUtils.setLockLastModifyTime(mContext,EGContext.FILES_SYNC_SP_WRITER,System.currentTimeMillis());
            }else {
                return;
            }

        }
        if(mLastCloseTime == 0){//取完sp时间后依然为空，则为第一次锁屏，设置closeTime,准备入库
            mLastCloseTime = System.currentTimeMillis();
            OCImpl.mLastAvailableOpenOrCloseTime = mLastCloseTime;
            SPHelper.setLongValue2SP(mContext,EGContext.LAST_AVAILABLE_TIME, mLastCloseTime);
            ELOG.i("接收关闭屏幕广播后存入sp的时间::::"+ mLastCloseTime);
            if(on){
               return;
            }
            OCImpl.getInstance(mContext).closeOC(false, mLastCloseTime);
        }else {//sp里取到了数据，即，非第一次锁屏，则判断是否有效数据来设置closeTime,准备入库
            ELOG.i("非第一次锁屏，接收关闭屏幕广播后存入sp的时间::::"+ mLastCloseTime);
            OCImpl.mLastAvailableOpenOrCloseTime = System.currentTimeMillis();
            SPHelper.setLongValue2SP(mContext,EGContext.LAST_AVAILABLE_TIME, OCImpl.mLastAvailableOpenOrCloseTime);
            if(on){
                return;
            }
            OCImpl.getInstance(mContext).closeOC(true, mLastCloseTime);
        }
        ReceiverUtils.getInstance().unRegistAllReceiver(mContext);
    }

//    /**
//     * 补数入库
//     * @param needCalculateTime
//     * @param closeTime
//     */
//    public void saveData(boolean needCalculateTime,long closeTime){
//        try {
//            //        String lastAvailableTime = "";
//            if(Build.VERSION.SDK_INT < 21){//4.x
//                if(needCalculateTime && (System.currentTimeMillis() - closeTime < EGContext.OC_CYCLE)){//两次时间间隔如果小于5s,则无效
//                    ELOG.i("锁屏广播针对本次oc无效::::");
//                    if(OCImpl.getInstance(mContext).mCache != null){
//                        OCImpl.getInstance(mContext).mCache.remove(EGContext.LAST_OPEN_TIME);
//                    }
//                    return;
//                }else {//有效入库
//                    if(OCImpl.getInstance(mContext).mCache != null){
//                        OCImpl.getInstance(mContext).mCache.put(EGContext.END_TIME,OCImpl.mLastAvailableOpenOrCloseTime);
//                    }else {
//                        return;
//                    }
//                    OCImpl.getInstance(mContext).filterInsertOCInfo(EGContext.CLOSE_SCREEN);
//                }
//            }else if(Build.VERSION.SDK_INT > 20 && Build.VERSION.SDK_INT < 24){//5/6
//                if(needCalculateTime && (System.currentTimeMillis() - closeTime < EGContext.OC_CYCLE_OVER_5)){//无效
//                    ELOG.i("锁屏广播针对本次oc无效::::");
//                    return;
//                }else {
//                    ELOG.i("接收关闭屏幕广播后的入库时间::::"+closeTime);
//                    /**
//                     * 读取数据库数据入缓存，只读取一次，缓存先改然后与数据库同步
//                     */
////                    lastAvailableTime = SPHelper.getStringValueFromSP(mContext,EGContext.LAST_OPEN_TIME, "");
//                    OCImpl.getInstance(mContext).fillData();//批量入库补数
//                }
//            }
//        }catch (Throwable t){
//            ELOG.e(t);
//        }
//
//    }
}
