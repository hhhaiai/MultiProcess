package com.analysys.track.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.analysys.track.internal.AnalysysInternal;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.internal.impl.ReceiverImpl;
import com.analysys.track.utils.sp.SPHelper;


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
        if (intent == null) {
            return;
        }
        
        //没初始化并且开屏了10次,就初始化,否则+1返回不处理
        if (!AnalysysInternal.isInit() && Intent.ACTION_USER_PRESENT.equals(intent.getAction())) {
            int size = SPHelper.getIntValueFromSP(context, EGContext.KEY_ACTION_SCREEN_ON_SIZE, 0);
            if (size > 10) {
                AnalysysInternal.getInstance(context).initEguan(null, null);
            } else {
                SPHelper.setIntValue2SP(context, EGContext.KEY_ACTION_SCREEN_ON_SIZE, size + 1);
                return;
            }
        }
        ReceiverImpl.getInstance().process(context, intent);

    }


}
