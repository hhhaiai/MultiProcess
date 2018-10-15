package com.analysys.dev.internal.utils;

import android.app.ActivityManager;
import android.content.Context;
import java.util.List;

/**
 * @Copyright © 2018 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2018/10/12 11:29
 * @Author: Wang-X-C
 */
public class Utils {

  /**
   * 判断服务是否启动
   */
  public static boolean isServiceWork(Context mContext, String serviceName) {
    boolean isWork = false;
    try {
      ActivityManager myAM = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
      List<ActivityManager.RunningServiceInfo> myList = myAM.getRunningServices(Integer.MAX_VALUE);
      if (myList.size() <= 0) {
        return false;
      }
      for (int i = 0; i < myList.size(); i++) {
        String mName = myList.get(i).service.getClassName();
        if (mName.equals(serviceName)) {
          isWork = true;
          break;
        }
      }
    } catch (Throwable e) {
    }
    return isWork;
  }
}
