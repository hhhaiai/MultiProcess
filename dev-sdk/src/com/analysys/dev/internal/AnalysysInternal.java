package com.analysys.dev.internal;

import android.content.Context;
import android.os.Bundle;
import android.os.Process;
import android.text.TextUtils;
import com.analysys.dev.internal.Content.EDContext;
import com.analysys.dev.internal.utils.AndroidManifestHelper;
import com.analysys.dev.internal.utils.EContextHelper;
import com.analysys.dev.internal.utils.LL;
import com.analysys.dev.internal.utils.sp.SPHelper;
import com.analysys.dev.internal.work.MessageDispatcher;

/**
 * @Copyright © 2018 Analysys Inc. All rights reserved.
 * @Description: 设备SDK入口
 * @Version: 1.0
 * @Create: 2018年8月30日 上午11:45:43
 * @Author: sanbo
 */
public class AnalysysInternal {
  private Context mContext = null;

  private AnalysysInternal() {
  }

  private static class Holder {
    private static AnalysysInternal instance = new AnalysysInternal();
  }

  public static AnalysysInternal getInstance(Context context) {
    if (Holder.instance.mContext == null) {
      if (context != null) {
        Holder.instance.mContext = context.getApplicationContext();
      } else {
        Holder.instance.mContext = EContextHelper.getContext();
      }
    }
    return Holder.instance;
  }

  /**
   * 初始化函数
   * key支持参数设置、XML文件设置，
   * 参数设置优先级大于XML设置
   *
   * @param isDebug 只保留日志控制
   */
  public void initEguan(String key, String channel, boolean isDebug) {

    LL.d("初始化，进程Id：< " + Process.myPid() + " >");

    if (TextUtils.isEmpty(key)) {
      Bundle bundle = AndroidManifestHelper.getMetaData(mContext);
      if (bundle == null) {
        LL.e(EDContext.LOGINFO.LOG_NOT_APPKEY);
      }
      key = bundle.getString(EDContext.XML_METADATA_APPID);
      channel = bundle.getString(EDContext.XML_METADATA_CHANNEL);
      if (TextUtils.isEmpty(key)) {
        LL.e(EDContext.LOGINFO.LOG_NOT_APPKEY);
      }
    }
    SPHelper.getDefault(mContext).edit().putString(EDContext.SP_APP_KEY, key).commit();
    SPHelper.getDefault(mContext).edit().putString(EDContext.SP_APP_CHANNEL, channel).commit();

    EDContext.FLAG_DEBUG_USER = isDebug;

    MessageDispatcher.getInstance(mContext).startService(0);
    // 4.启动页面监听相关的
    // PageViewHelper.getInstance(mContext).init();

  }
}
