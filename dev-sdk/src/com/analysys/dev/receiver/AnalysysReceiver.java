package com.analysys.dev.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.analysys.dev.internal.Content.EGContext;
import com.analysys.dev.utils.ELOG;
import com.analysys.dev.utils.reflectinon.Reflecer;
import com.analysys.dev.internal.work.MessageDispatcher;

public class AnalysysReceiver extends BroadcastReceiver {
    Context mContext;
    String PACKAGE_ADDED = "android.intent.action.PACKAGE_ADDED";
    String PACKAGE_REMOVED = "android.intent.action.PACKAGE_REMOVED";
    String PACKAGE_REPLACED = "android.intent.action.PACKAGE_REPLACED";
    public static AnalysysReceiver getInstance() {
        return AnalysysReceiver.Holder.INSTANCE;
    }
    private static class Holder {
        private static final AnalysysReceiver INSTANCE = new AnalysysReceiver();
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        Reflecer.init();
        String packageName = intent.getDataString().substring(8);
        mContext = context.getApplicationContext();

        if (PACKAGE_ADDED.equals(intent.getAction())) {
            ELOG.d("接收到应用安装广播：" + packageName);
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
    }
}
