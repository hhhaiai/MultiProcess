package me.hhhaiai.multiprocess;

import android.app.Service;
import android.content.Context;

import java.util.concurrent.CopyOnWriteArrayList;

import me.hhhaiai.ImpTask;
import me.hhhaiai.mptils.EContext;
import me.hhhaiai.mptils.MpLog;
import me.hhhaiai.mptils.Reflect;
import me.hhhaiai.mptils.MpServiceHelper;

/**
 * @Copyright © 2021 analsys Inc. All rights reserved.
 * @Description: multiprocess接口类
 * @Version: 1.0
 * @Create: 2021/04/104 10:12:42
 * @author: sanbo
 */
public class MultiprocessManager {


    public void postMultiMessages(int count, ImpTask task) {
        if (count > MpServiceHelper.MAX_SERVICES) {
            MpLog.e("超过最大支持进程数量，现阶段支持最大进程数:" + MpServiceHelper.MAX_SERVICES);
            return;
        }
        mContext = EContext.getContext();
        if (mContext != null && count > 0) {
            CopyOnWriteArrayList<Class<? extends Service>> cp = new CopyOnWriteArrayList<Class<? extends Service>>();
            for (int i = 1; i <= count; i++) {
                cp.add(Reflect.getClass("me.hhhaiai.services.CService" + i));
            }
            MpServiceHelper.startService(mContext, cp, task);
        }
    }


    /********************* get instance begin **************************/
    public static MultiprocessManager getInstance(Context context) {
        return HLODER.INSTANCE.initContext(context);
    }

    private MultiprocessManager initContext(Context context) {
        mContext = EContext.getContext(context);
        return HLODER.INSTANCE;
    }

    private static class HLODER {
        private static final MultiprocessManager INSTANCE = new MultiprocessManager();
    }

    private MultiprocessManager() {
    }

    private Context mContext = null;
    /********************* get instance end **************************/


}
