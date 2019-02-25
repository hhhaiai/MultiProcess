package com.analysys.dev;

import android.content.Context;
import com.analysys.dev.internal.AnalysysInternal;
import com.analysys.dev.service.AnalysysAccessibilityService;
import com.analysys.dev.utils.AccessibilityHelper;
import com.analysys.dev.utils.EThreadPool;
import com.analysys.dev.utils.TPUtils;
import com.analysys.dev.utils.sp.SPHelper;

public class EguanMonitorAgent {
  private EguanMonitorAgent() {
  }

  private static class Holder {
    private static final EguanMonitorAgent INSTANCE = new EguanMonitorAgent();
  }

  public static EguanMonitorAgent getInstance() {
    return Holder.INSTANCE;
  }

  /**
   * 初始化SDK
   */
  public void initEguan(final Context context, final String key, final String channel, final boolean isDebug) {
    if (TPUtils.isMainThread()) {
      EThreadPool.execute(new Runnable() {
        @Override
        public void run() {
          AnalysysInternal.getInstance(context).initEguan(key, channel, isDebug);
        }
      });
    } else {
      AnalysysInternal.getInstance(context).initEguan(key, channel, isDebug);
    }

  }
  /**
   * 设置Debug模式
   * @param isDebug
   */
  public void setDebugMode(Context ctx ,boolean isDebug) {
    SPHelper.setDebugMode( ctx, isDebug);
  }
}
