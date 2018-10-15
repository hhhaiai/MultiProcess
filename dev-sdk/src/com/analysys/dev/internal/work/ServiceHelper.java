package com.analysys.dev.internal.work;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Message;
import com.analysys.dev.internal.Content.EDContext;
import com.analysys.dev.internal.utils.EContextHelper;
import com.analysys.dev.internal.utils.LL;
import com.analysys.dev.internal.utils.PermissionUtils;
import com.analysys.dev.internal.utils.Utils;
import com.analysys.dev.service.AnalysysService;
import java.lang.reflect.Method;

/**
 * @Copyright © 2018 Analysys Inc. All rights reserved.
 * @Description: 服务启动
 * @Version: 1.0
 * @Create: 2018年9月12日 下午2:26:29
 * @Author: sanbo
 */
public class ServiceHelper {
  private Context mContext = null;

  private ServiceHelper() {

  }

  private static class Holder {
    private static ServiceHelper instance = new ServiceHelper();
  }

  public static ServiceHelper getInstance(Context context) {
    if (Holder.instance.mContext == null) {
      if (context != null) {
        Holder.instance.mContext = context;
      } else {
        Holder.instance.mContext = EContextHelper.getContext();
      }
    }
    return Holder.instance;
  }

  /**
   * 启动服务任务接入
   */
  public void startService(int delay) {
    Message msg = new Message();
    msg.what = MessageDispatcher.MSG_START_SERVICE_SELF;
    MessageDispatcher.getInstance(mContext).sendMessage(msg, delay);
  }

  /**
   * 官方api方式打开服务
   */
  public void startSelfService() {
    LL.d("是否启动服务："+isStartService());
    if (isStartService()) {
      boolean isWork = Utils.isServiceWork(mContext, EDContext.SERVICE_NAME);
      if (!isWork) {
        try {
          ComponentName cn = new ComponentName(mContext, AnalysysService.class);
          Intent intent = new Intent();
          intent.setComponent(cn);
          if (Build.VERSION.SDK_INT >= 26) {
            Class<?> clazz = Class.forName("android.content.Context");
            Method startForegroundService = clazz.getMethod("startForegroundService", Intent.class);
            startForegroundService.invoke(mContext, intent);
          } else {
            mContext.startService(intent);
          }
        } catch (Throwable e) {
        }
      }
    } else {
      startWork();
    }
  }

  /**
   * 判断是否可以启动服务
   */
  private boolean isStartService() {
    if (Build.VERSION.SDK_INT < 26) {
      return true;
    }
    if (EDContext.FLAG_SHOW_NOTIFY) {
      if (Build.VERSION.SDK_INT >= 28) {
        if (PermissionUtils.checkPermission(mContext,
            "android.permission.FOREGROUND_SERVICE")) {
          return true;
        }
      } else {
        return true;
      }
    }
    return false;
  }

  public void startWork() {
    Message msg = new Message();
    msg.what = MessageDispatcher.MSG_WORK;
    MessageDispatcher.getInstance(mContext).sendMessage(msg, 5 * 1000);
  }
  /**
   * 通过系统接口启动服务，用作拉活使用
   */
  protected void startServiceByCode(Intent intent) {

  }

  /**
   * 通过shell方式启动服务，用作拉活使用
   */
  protected void startServiceByShell(Intent intent) {

  }
}
