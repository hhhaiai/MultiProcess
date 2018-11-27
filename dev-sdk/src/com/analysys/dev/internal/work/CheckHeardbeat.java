package com.analysys.dev.internal.work;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import com.analysys.dev.internal.Content.EDContext;
import com.analysys.dev.internal.utils.EContextHelper;
import com.analysys.dev.internal.utils.LL;

/**
 * @Copyright © 2018 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2018/10/22 18:34
 * @Author: Wang-X-C
 */
public class CheckHeardbeat {
  Context mContext;
  Handler mHandler;

  public CheckHeardbeat() {
    mHandler = startWorkHandler();
  }

  private static class Holder {
    private static final CheckHeardbeat INSTANCE = new CheckHeardbeat();
  }

  public static CheckHeardbeat getInstance(Context context) {
    if (Holder.INSTANCE.mContext == null) {
      if (context != null) {
        Holder.INSTANCE.mContext = context;
      } else {
        Holder.INSTANCE.mContext = EContextHelper.getContext();
      }
    }
    return Holder.INSTANCE;
  }

  private Handler startWorkHandler() {
    final HandlerThread thread = new HandlerThread("com.analysys", android.os.Process.THREAD_PRIORITY_BACKGROUND);
    thread.start();
    final Handler ret = new CheckHandler(thread.getLooper());
    return ret;
  }

  public void sendMessages() {
    Message msg = new Message();
    msg.what = MSG_CHECK;
    mHandler.sendMessageDelayed(msg, EDContext.CHECK_HEARTBEAT_CYCLE);
  }

  class CheckHandler extends Handler {
    public CheckHandler(Looper looper) {
      super(looper);
    }

    @Override public void handleMessage(Message msg) {
      super.handleMessage(msg);
      switch (msg.what) {
        case MSG_CHECK:

          //LL.i("心跳检查,进程："+Process.myPid());
          MessageDispatcher.getInstance(mContext).checkHeartbeat();
          sendMessages();
          break;
        default:
          break;
      }
    }
  }

  private static final int MSG_CHECK = 0x0c;
}
