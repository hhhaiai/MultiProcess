package me.hhhaiai.multiprocess;

import android.app.Service;
import android.content.Context;
import android.content.Intent;

import java.util.concurrent.CopyOnWriteArrayList;

import me.hhhaiai.utils.EContext;
import me.hhhaiai.utils.Reflect;
import me.hhhaiai.utils.ServiceHelper;

/**
 * @Copyright © 2021 analsys Inc. All rights reserved.
 * @Description: multiprocess接口类
 * @Version: 1.0
 * @Create: 2021/04/104 10:12:42
 * @author: sanbo
 */
public class MultiprocessManager {


    /**
     * 启动服务
     *
     * @param context
     * @param count
     */
    private static void runServices(Context context, int count) {
        context = EContext.getContext(context);
        if (context != null && count > 0) {
            CopyOnWriteArrayList<Class<Service>> cp = new CopyOnWriteArrayList<Class<Service>>();
            for (int i = 1; i <= count; i++) {
                cp.add(Reflect.getClass("CService" + i));
            }
            ServiceHelper.startService(context, cp);

        }
    }
}
