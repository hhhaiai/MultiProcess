package com.eguan;

import com.eguan.utils.EGetHelper;
import android.content.Context;

/**
 * @Copyright © 2018 Analysys Inc. All rights reserved.
 * @Description: 设备SDK入口
 * @Version: 1.0
 * @Create: 2018年8月30日 上午11:45:43
 * @Author: sanbo
 */
public class AnalysysInternal {
    private Context mContext = null;

    private AnalysysInternal() {}

    private static class Holder {
        private static AnalysysInternal instance = new AnalysysInternal();
    }

    public static AnalysysInternal getInstance(Context context) {
        if (Holder.instance.mContext == null) {
            if (context != null) {
                Holder.instance.mContext = context.getApplicationContext();
            } else {
                Holder.instance.mContext = EGetHelper.getContext();
            }
        }
        return Holder.instance;
    }

    public void initEguan(String key, String channel) {

    }

    public void setDebugMode(boolean flag) {

    }
}
