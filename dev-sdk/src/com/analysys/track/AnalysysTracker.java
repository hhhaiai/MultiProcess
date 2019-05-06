package com.analysys.track;

import android.content.Context;

import com.analysys.track.internal.AnalysysInternal;
import com.analysys.track.internal.Content.EGContext;
import com.analysys.track.utils.ELOG;

public class AnalysysTracker {

    /**
     * 初始化SDK
     * @param context
     * @param appKey
     * @param channel
     */
  public static void init(Context context, String appKey, String channel) {
      AnalysysInternal.getInstance(context).initEguan(appKey, channel);
  }

    /**
     * 设置Debug模式
     * @param isDebug
     */
  public static void setDebugMode(boolean isDebug) {
    EGContext.FLAG_DEBUG_USER = isDebug;
    ELOG.info("setDebugMode ::"+isDebug);
//    SPHelper.setBooleanValue2SP(ctx, EGContext.DEBUG, isDebug);
  }
}
