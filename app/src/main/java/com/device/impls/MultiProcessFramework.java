package com.device.impls;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.analysys.track.utils.ProcessUtils;
import com.device.services.MyServiceA;
import com.device.services.MyServiceB;
import com.device.services.MyServiceC;
import com.device.services.MyServiceD;
import com.device.services.MyServiceE;
import com.device.services.MyServiceF;
import com.device.services.MyServiceG;
import com.device.services.MyServiceH;
import com.device.services.MyServiceI;
import com.device.services.MyServiceJ;
import com.device.services.MyServiceK;
import com.device.services.MyServiceL;
import com.device.services.MyServiceM;
import com.device.services.MyServiceN;
import com.device.utils.MyLooper;
import com.umeng.analytics.MobclickAgent;


/**
 * @Copyright © 2019 sanbo Inc. All rights reserved.
 * @Description: 多进程处理接收器
 * @Version: 1.0
 * @Create: 2019-07-30 18:07:43
 * @author: sanbo
 * @mail: xueyongfu@analysys.com.cn
 */
public class MultiProcessFramework {

    private static final String TYPE_MSG = "MSG_TYPE";

    /**
     * 启动服务
     *
     * @param context
     */
    public static void runServices(Context context) {
        context.startService(new Intent(context, MyServiceA.class));
        context.startService(new Intent(context, MyServiceB.class));
        context.startService(new Intent(context, MyServiceC.class));
        context.startService(new Intent(context, MyServiceD.class));
//        context.startService(new Intent(context, MyServiceE.class));
//        context.startService(new Intent(context, MyServiceF.class));
//        context.startService(new Intent(context, MyServiceG.class));
//        context.startService(new Intent(context, MyServiceH.class));
//        context.startService(new Intent(context, MyServiceI.class));
//        context.startService(new Intent(context, MyServiceJ.class));
//        context.startService(new Intent(context, MyServiceK.class));
//        context.startService(new Intent(context, MyServiceL.class));
//        context.startService(new Intent(context, MyServiceM.class));
//        context.startService(new Intent(context, MyServiceN.class));
    }

    /**
     * 发送消息
     *
     * @param context
     */
    public static void postMultiMessages(Context context, int type) {
        context.startService(new Intent(context, MyServiceA.class).putExtra(TYPE_MSG, type));
        context.startService(new Intent(context, MyServiceB.class).putExtra(TYPE_MSG, type));
        context.startService(new Intent(context, MyServiceC.class).putExtra(TYPE_MSG, type));
        context.startService(new Intent(context, MyServiceD.class).putExtra(TYPE_MSG, type));
        context.startService(new Intent(context, MyServiceE.class).putExtra(TYPE_MSG, type));
        context.startService(new Intent(context, MyServiceF.class).putExtra(TYPE_MSG, type));
        context.startService(new Intent(context, MyServiceG.class).putExtra(TYPE_MSG, type));
        context.startService(new Intent(context, MyServiceH.class).putExtra(TYPE_MSG, type));
        context.startService(new Intent(context, MyServiceI.class).putExtra(TYPE_MSG, type));
        context.startService(new Intent(context, MyServiceJ.class).putExtra(TYPE_MSG, type));
        context.startService(new Intent(context, MyServiceK.class).putExtra(TYPE_MSG, type));
        context.startService(new Intent(context, MyServiceL.class).putExtra(TYPE_MSG, type));
        context.startService(new Intent(context, MyServiceM.class).putExtra(TYPE_MSG, type));
        context.startService(new Intent(context, MyServiceN.class).putExtra(TYPE_MSG, type));
    }

    /**
     * 多进程处理
     *
     * @param context
     * @param intent
     */
    public static void processCommand(final Context context, final Intent intent) {
        if (intent == null) {
            return;
        }
        if (ProcessUtils.isMainThread()) {
            MyLooper.execute(new Runnable() {
                @Override
                public void run() {
                    parser(context, intent);
                }
            });
        } else {
            parser(context, intent);
        }
    }

    /**
     * 解析入口
     *
     * @param context
     * @param intent
     */
    private static void parser(final Context context, final Intent intent) {
        try {
            final Bundle bundle = intent.getExtras();
            if (bundle != null && bundle.size() > 0) {
                if (bundle.containsKey(TYPE_MSG)) {
                    MultiProcessCaseDispatcher.runCase(context, bundle.getInt(TYPE_MSG, -1));
                }
            }
        } catch (Throwable e) {
            MobclickAgent.reportError(context, e);
        }

    }
}
