package com.analysys.track;

import android.content.Context;

import com.analysys.track.internal.AnalysysInternal;
import com.analysys.track.internal.Content.EGContext;
import com.analysys.track.utils.sp.SPHelper;

public class AnalysysTracker {
   /**
   * 初始化SDK
   */
  public static void init(final Context context, final String key, final String channel) {
      AnalysysInternal.getInstance(context).initEguan(key, channel);
  }
  /**
   * 设置Debug模式
   * @param isDebug
   */
  public static void setDebugMode(Context ctx ,boolean isDebug) {
    EGContext.FLAG_DEBUG_USER = isDebug;
    SPHelper.setDebugMode( ctx, isDebug);
  }
}
