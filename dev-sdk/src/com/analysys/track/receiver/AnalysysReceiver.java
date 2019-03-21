package com.analysys.track.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.analysys.track.internal.Content.DataController;
import com.analysys.track.impl.DeviceImpl;
import com.analysys.track.impl.OCImpl;
import com.analysys.track.impl.WifiImpl;
import com.analysys.track.impl.proc.ProcessManager;
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
    public static AnalysysReceiver getInstance() {
        return AnalysysReceiver.Holder.INSTANCE;
    }
    private static class Holder {
        private static final AnalysysReceiver INSTANCE = new AnalysysReceiver();
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            if (intent == null)return;
            String data = intent.getDataString();
            String packageName = "";
            if(!TextUtils.isEmpty(data)){
                packageName = data.substring(8);
            }
            mContext = context.getApplicationContext();

            if (PACKAGE_ADDED.equals(intent.getAction())) {
                ELOG.d("接收到应用安装广播：" + packageName);
                //TODO 多测试
                MessageDispatcher.getInstance(mContext).appChangeReceiver(packageName, Integer.parseInt(EGContext.SNAP_SHOT_INSTALL));
            }
            if (PACKAGE_REMOVED.equals(intent.getAction())) {
                ELOG.d("接收到应用卸载广播：" + packageName);
                MessageDispatcher.getInstance(mContext).appChangeReceiver(packageName, Integer.parseInt(EGContext.SNAP_SHOT_UNINSTALL));
            }
            if (PACKAGE_REPLACED.equals(intent.getAction())) {
                ELOG.d("接收到应用更新广播：" + packageName);
                MessageDispatcher.getInstance(mContext).appChangeReceiver(packageName, Integer.parseInt(EGContext.SNAP_SHOT_UPDATE));
            }
            if (CONNECTIVITY_CHANGE.equals(intent.getAction())) {
                ELOG.d("接收网络变化广播");
                WifiImpl.getInstance(mContext).getWifiInfo();
//                MessageDispatcher.getInstance(mContext).startService();
            }
            if (SCREEN_ON.equals(intent.getAction())) {
                ELOG.e("接收开启屏幕广播");
                //设置开锁屏的flag 用于补数逻辑
                EGContext.SCREEN_ON = true;
                ProcessManager.setIsCollected(true);
                CheckHeartbeat.getInstance(mContext).sendMessages();
            }
            if (SCREEN_OFF.equals(intent.getAction())) {
                EGContext.SCREEN_ON = false;
                ProcessManager.setIsCollected(false);
                processScreenOff(context);

                ELOG.e("接收关闭屏幕广播");
            }
            if (BATTERY_CHANGED.equals(intent.getAction())) {
                if(DataController.SWITCH_OF_MODULE_CL_BATTERY) DeviceImpl.getInstance(mContext).processBattery(intent);
                ELOG.e("电池变化广播");
            }
            if (BOOT_COMPLETED.equals(intent.getAction())) {
                ELOG.e("接收到开机广播");
                MessageDispatcher.getInstance(mContext).startService();
            }
        }catch (Throwable t){
        }
    }

    private void processScreenOff(final Context ctx) {
        // L.e("--------processScreenOff");
        try {
            if (SystemUtils.isMainThread()) {
                EThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        // AppProcessManager.resetCounter();
                        /*注销广播*/
//                        ProcessManager.dealScreenOff(ctx);
                        // ReceiverUtils.getInstance().unRegistAllReceiver(mContext, false);
                        ReceiverUtils.getInstance().unRegistAllReceiver(mContext);

                    }
                });
                EThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        OCImpl.getInstance(mContext).filterInsertOCInfo(EGContext.CLOSE_SCREEN);
                    }
                });

            } else {
                // AppProcessManager.resetCounter();
                /*注销广播*/
//                ProcessManager.dealScreenOff(ctx);
                ReceiverUtils.getInstance().unRegistAllReceiver(mContext);
                OCImpl.getInstance(mContext).filterInsertOCInfo(EGContext.CLOSE_SCREEN);
            }
        } catch (Throwable e) {
        }
    }
}
