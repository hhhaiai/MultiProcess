package com.analysys.dev.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.analysys.dev.internal.utils.LL;
import com.analysys.dev.internal.utils.reflectinon.Reflecer;
import com.analysys.dev.internal.work.MessageDispatcher;

public class AnalysysReceiver extends BroadcastReceiver {
    Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        Reflecer.init();
        String packageName = intent.getDataString().substring(8);
        mContext = context.getApplicationContext();

        if (intent.getAction().equals("android.intent.action.PACKAGE_ADDED")) {
            LL.d("接收到应用安装广播：" + packageName);
            MessageDispatcher.getInstance(mContext).appChangeReceiver(packageName, 0);
        }
        if (intent.getAction().equals("android.intent.action.PACKAGE_REMOVED")) {
            LL.d("接收到应用卸载广播：" + packageName);
            MessageDispatcher.getInstance(mContext).appChangeReceiver(packageName, 1);
        }
        if (intent.getAction().equals("android.intent.action.PACKAGE_REPLACED")) {
            LL.d("接收到应用更新广播：" + packageName);
            MessageDispatcher.getInstance(mContext).appChangeReceiver(packageName, 2);
        }

        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            LL.e("接收到开机广播");
            MessageDispatcher.getInstance(mContext).startService(0);
        }
    }
}
