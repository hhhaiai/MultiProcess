package com.analysys.track.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.analysys.track.hotfix.HotFixTransformCancel;
import com.analysys.track.hotfix.HotFixTransform;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.internal.impl.ReceiverImpl;
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
    public void onReceive(Context context, Intent intent) {

        try {
            HotFixTransform.transform(
                    HotFixTransform.make(AnalysysReceiver.class.getName())
                    , AnalysysReceiver.class.getName()
                    , "onReceive", context, intent);
            return;
        } catch (HotFixTransformCancel e) {

        }
        if (EGContext.FLAG_DEBUG_INNER) {
            ELOG.i("AnalysysReceiver onReceive");
        }
        if (intent == null) {
            return;
        }
        ReceiverImpl.getInstance().process(context, intent);

    }


}
