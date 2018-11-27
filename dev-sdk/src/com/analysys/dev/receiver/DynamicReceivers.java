package com.analysys.dev.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.analysys.dev.internal.utils.LL;
import com.analysys.dev.internal.work.MessageDispatcher;

/**
 * @Copyright © 2018 Analysys Inc. All rights reserved.
 * @Description: 动态注册的广播
 * @Version: 1.0
 * @Create: 2018年10月8日 下午5:54:14
 * @Author: sanbo
 */
public class DynamicReceivers extends BroadcastReceiver {
  String SCREEN_ON = "android.intent.action.SCREEN_ON";
  String SCREEN_OFF = "android.intent.action.SCREEN_OFF";
  String CONNECTIVITY_CHANGE = "android.net.conn.CONNECTIVITY_CHANGE";

  Context mContext;

  @Override
  public void onReceive(Context context, Intent intent) {
    mContext = context.getApplicationContext();
    if (CONNECTIVITY_CHANGE.equals(intent.getAction())) {
      LL.d("接收网络变化广播");
      MessageDispatcher.getInstance(mContext).startService(0);
    }
    if (SCREEN_ON.equals(intent.getAction())) {
      LL.e("接收开启屏幕广播");
      MessageDispatcher.getInstance(mContext).startService(0);
    }
    if (SCREEN_OFF.equals(intent.getAction())) {
      MessageDispatcher.getInstance(mContext).killWorker();
      LL.e("接收关闭屏幕广播");
    }
  }
}
