package com.eguan.monitor.receiver.device;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.eguan.Constants;
import com.eguan.db.DBPorcesser;
import com.eguan.imp.AppProcessManager;
import com.eguan.imp.OCInfoManager;
import com.eguan.imp.ScreenTime;
import com.eguan.utils.commonutils.EgLog;
import com.eguan.utils.commonutils.ReceiverUtils;
import com.eguan.utils.netutils.DevInfoUpload;
import com.eguan.utils.thread.EGQueue;
import com.eguan.utils.thread.SafeRunnable;

public class ScreenReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        EGQueue.execute(new SafeRunnable() {
            @Override
            public void safeRun() {
                try {
                    String action = intent.getAction();
                    if (action != null) {
                        // TODO Auto-generated method stub
                        /* 点亮屏幕 */
                        if (action.equals(Intent.ACTION_SCREEN_ON)) {
                            ScreenTime.addOnOffTime(true);
                            DataUpload(context);
                            /* 注册广播 */
                            ReceiverUtils.getInstance().registAllReceiver(context);
                            /* 关闭屏幕 */
                        } else if (action.equals(Intent.ACTION_SCREEN_OFF)) {

                            AppProcessManager.resetCounter();
                            ScreenTime.addOnOffTime(false);
                            /* 注销广播 */
                            AppProcessManager.getInstance(context).dealScreenOff();
                            ReceiverUtils.getInstance().unRegistAllReceiver(context, false);
                            DataUpload(context);
                            OCInfoManager.getInstance(context).filterInsertOCInfo(Constants.CLOSE_SCREEN, true);
                        }
                    }
                } catch (Throwable e) {
                    if (Constants.FLAG_DEBUG_INNER) {
                        EgLog.e(e);
                    }
                }
            }
        });
    }

    /**
     * upload
     *
     * @param context
     * @return
     */
    private void DataUpload(final Context context) {
        // EGThreadPool.pushDB(new Runnable() {
        // @Override
        // public void run() {
        try {
            int number = DBPorcesser.getInstance(context).DataQuantity();
            if (number >= Constants.DATA_NUMBER) {
                DevInfoUpload.getInstance().upload(context);
            }
        } catch (Throwable e) {
            // if (Config.EG_DEBUG) {
            EgLog.e("DataUpload", e.toString());
            // }
        }
        // }
        // });

    }
}
