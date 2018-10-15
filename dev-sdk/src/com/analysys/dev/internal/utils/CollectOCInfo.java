package com.analysys.dev.internal.utils;

import android.content.Context;
import android.os.Build;

/**
 * @Copyright © 2018 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2018/10/13 17:03
 * @Author: Wang-X-C
 */
public class CollectOCInfo {

  private static Context mContext = null;

  private static class Holder {
    private static final CollectOCInfo INSTANCE = new CollectOCInfo();
  }

  public static CollectOCInfo getInstance(Context context) {
    if (context == null) {
      return null;
    } else {
      mContext = context.getApplicationContext();
    }
    mContext = context;
    return Holder.INSTANCE;
  }

  public void getAppInfo() {

    if (Build.VERSION.SDK_INT < 26) {
      // getRunningTask
    } else if (Build.VERSION.SDK_INT < 28) {
      // 读proc文件获取
    }
  }
}
