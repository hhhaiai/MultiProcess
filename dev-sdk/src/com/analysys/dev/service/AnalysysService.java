package com.analysys.dev.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Process;
import com.analysys.dev.internal.utils.LL;
import com.analysys.dev.internal.work.MessageDispatcher;

public class AnalysysService extends Service {

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  @Override public void onCreate() {
    super.onCreate();
    LL.d("服务启动 进程ID：< " + Process.myPid() + " >");
    MessageDispatcher.getInstance(this).initModule();
  }
}
