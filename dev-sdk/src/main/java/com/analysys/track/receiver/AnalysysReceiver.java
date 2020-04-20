package com.analysys.track.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.analysys.track.BuildConfig;
import com.analysys.track.impl.CusHotTransform;
import com.analysys.track.internal.AnalysysInternal;
import com.analysys.track.utils.ELOG;


/**
 * @Copyright © 2019 sanbo Inc. All rights reserved.
 * @Description: 广播接收器
 * @Version: 1.0
 * @Create: 2019-08-07 18:15:12
 * @author: sanbo
 * @mail: xueyongfu@analysys.com.cn
 */
public class AnalysysReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        try {
            if (BuildConfig.logcat) {
                ELOG.d(BuildConfig.tag_recerver, " 收到广播: " + intent);
            }
            if (BuildConfig.enableHotFix && CusHotTransform.getInstance(context).isCanWork(AnalysysReceiver.class.getName(), "onReceive")) {
                CusHotTransform.getInstance(context).transform(true, AnalysysReceiver.class.getName(), "onReceive", context, intent);
                return;
            }
        } catch (Throwable e) {
        }

        if (intent == null) {
            return;
        }
        AnalysysInternal.getInstance(context).parseIntent(intent);
    }

}
