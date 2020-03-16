package com.device.tripartite;


import android.content.Context;
import android.text.TextUtils;

import com.device.BuildConfig;
import com.device.utils.DemoClazzUtils;
import com.device.utils.EL;
import com.tencent.bugly.Bugly;
import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;

import org.json.JSONArray;

import java.lang.reflect.Field;

/**
 * @Copyright © 2020 sanbo Inc. All rights reserved.
 * @Description: 所有的三方SDK的调用
 * @Version: 1.0
 * @Create: 2020/3/12 17:22
 * @author: sanbo
 */
public class Abu {

    /**
     * 初始化bugly。 track-sdk-demo
     *
     * @param context
     */
    public static void initBugly(Context context) {
        Bugly.init(context, "8b5379e3bc", false);
    }

    /**
     * 初始化统计
     *
     * @param context
     */
    public static void initAnalysys(Context context) {
        initEg(context);
        initUmeng(context);
    }


    public static void onResume(Context ctx, String pn) {
        MobclickAgent.onResume(ctx);
        MobclickAgent.onPageStart(pn);
    }


    public static void onPause(Context ctx, String pn) {
        MobclickAgent.onPause(ctx);
        MobclickAgent.onPageEnd(pn);
    }

    public static void onEvent(Context ctx, String eventName) {
        MobclickAgent.onEvent(ctx, eventName);
    }

    public static JSONArray getUSMInfo(Context ctx, long begin, long end) {
        return (JSONArray) DemoClazzUtils.invokeStaticMethod("com.analysys.track.internal.impl.usm.USMImpl", "getUSMInfo",
                new Class[]{Context.class, long.class, long.class}, new Object[]{ctx, begin, end});
    }

    // 初始化接口:第二个参数填写您在平台申请的appKey,第三个参数填写
    private static void initEg(Context context) {


        try {
//        DemoClazzUtils.invokeStaticMethod("com.analysys.track.AnalysysTracker", "init",
//                new Class[]{Context.class, String.class, String.class}, new Object[]{context, "7773661000888540d", "WanDouJia"});
            Field fd = DemoClazzUtils.getField("com.analysys.track.BuildConfig", "DEMO_APPKEY");
            EL.i("fd: " + fd);
            if (fd != null) {
                String appkey = (String) fd.get(null);
                EL.i("appkey: " + appkey);
                if (TextUtils.isEmpty(appkey)) {
                    appkey = BuildConfig.DEMO_APPKEY;
                }
                if (TextUtils.isEmpty(appkey)) {
                    appkey = "test_appkey";
                }
                DemoClazzUtils.invokeStaticMethod("com.analysys.track.AnalysysTracker", "init",
                        new Class[]{Context.class, String.class, String.class}, new Object[]{context, appkey, "WanDouJia"});
            }

        } catch (Throwable e) {
        }
    }

    //init umeng
    private static void initUmeng(Context context) {

        MobclickAgent.setSessionContinueMillis(10);
        MobclickAgent.setCatchUncaughtExceptions(true);
        UMConfigure.setProcessEvent(true);
        UMConfigure.setEncryptEnabled(true);
        UMConfigure.setLogEnabled(true);
        UMConfigure.init(context, "5b4c140cf43e4822b3000077", "track-demo-dev", UMConfigure.DEVICE_TYPE_PHONE, "99108ea07f30c2afcafc1c5248576bc5");
    }

}
