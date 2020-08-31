package com.analysys.track.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.analysys.track.BuildConfig;

import java.lang.reflect.Method;

/**
 * @Copyright 2019 analysys Inc. All rights reserved.
 * @Description: 注意:这个类是内部测试用的,这个类帮助把SDK出现的一些错误,报到Bugly上去,正常发包情况下是不可以启用的,会误报错误到对接方的bugly上
 * @Version: 1.0
 * @Create: 2019-11-06 11:34:15
 * @author: miqt
 * @mail: miqingtang@analysys.com.cn
 */
public class BugReportForTest {

    public static void commitError(Throwable throwable) {
        commitError(null, throwable);
    }

    public static void commitError(String tag, Throwable throwable) {
        try {
            if (!BuildConfig.ENABLE_BUG_REPORT) {
                return;
            }
            //Log
            if ((BuildConfig.BUG_REPORT_TYPE & 1) != 0) {
                if (!TextUtils.isEmpty(tag)) {
                    //使用log的原因是防止 ELOG 内部异常出现循环打印
                    Log.e(tag, Log.getStackTraceString(throwable));
                } else {
                    Log.e("analysys", Log.getStackTraceString(throwable));
                }
            }
            if ((BuildConfig.BUG_REPORT_TYPE & 1 << 1) != 0) {
                reportToBugly(throwable);
            }
            if ((BuildConfig.BUG_REPORT_TYPE & 1 << 2) != 0) {
                reportToUmeng(throwable);
            }


        } catch (Throwable e) {

        }
    }

    private static void reportToUmeng(Throwable throwable) {
//        initUmeng(EContextHelper.getContext());
        Context c = EContextHelper.getContext();
        if (c != null) {
            postExToServer(c, throwable);
        }
    }
//
//    private static void initUmeng(Context context) {
//        try {
//            Class c = Class.forName("com.umeng.analytics.MobclickAgent");
//            Method m = c.getDeclaredMethod("setSessionContinueMillis", new Class[]{long.class});
//            m.invoke(null, new Object[]{10});
//        } catch (Throwable e) {
//        }
//        try {
//            Class c = Class.forName("com.umeng.analytics.MobclickAgent");
//            Method m = c.getDeclaredMethod("setCatchUncaughtExceptions", new Class[]{boolean.class});
//            m.invoke(null, new Object[]{true});
//        } catch (Throwable e) {
//        }
////        try {
////            Class c = Class.forName("com.umeng.commonsdk.UMConfigure");
////            Method m = c.getDeclaredMethod("setLogEnabled", new Class[]{boolean.class});
////            m.invoke(null, new Object[]{true});
////        } catch (Throwable e) {
////        }
//        try {
//            Class c = Class.forName("com.umeng.commonsdk.UMConfigure");
//            Method m = c.getDeclaredMethod("init", new Class[]{Context.class, String.class, String.class, int.class, String.class});
//            m.invoke(null, new Object[]{context, "5b4c140cf43e4822b3000077", "track-demo-dev", 1, "99108ea07f30c2afcafc1c5248576bc5"});
//        } catch (Throwable e) {
//        }
//    }

    private static void postExToServer(Context context, Throwable throwable) {
        try {
            Class c = Class.forName("com.umeng.analytics.MobclickAgent");
            Method m = c.getDeclaredMethod("reportError", new Class[]{Context.class, Throwable.class});
            m.invoke(null, new Object[]{context, throwable});
        } catch (Throwable e) {
        }
    }

    private static void reportToBugly(Throwable throwable) throws ClassNotFoundException {
        try {
            Context c = EContextHelper.getContext();
            if (c != null) {
                initBugly(c);
            }
            setTag(202004);
            postException(throwable);
        } catch (Throwable e) {
        }
    }

    private static void initBugly(Context context) {
        try {
            Class c = Class.forName("com.tencent.bugly.crashreport.CrashReport");
            Method m = c.getDeclaredMethod("initCrashReport", new Class[]{Context.class, String.class, boolean.class});
            m.invoke(null, new Object[]{context, "8b5379e3bc", false});
        } catch (Throwable e) {
        }

//        try {
//            Class c = Class.forName("com.tencent.bugly.Bugly");
//            Method m = c.getDeclaredMethod("init", new Class[]{Context.class, String.class, boolean.class});
//            m.invoke(null, new Object[]{context, "8b5379e3bc", false});
//        } catch (Throwable e) {
//        }

    }

    private static void postException(Throwable throwable) {
        try {
            Class c = Class.forName("com.tencent.bugly.crashreport.CrashReport");
            Method m = c.getDeclaredMethod("postCatchedException", new Class[]{Throwable.class});
            m.invoke(null, new Object[]{throwable});
        } catch (Throwable e) {

        }
    }

    private static void setTag(int tag) {
        try {
            Class c = Class.forName("com.tencent.bugly.crashreport.CrashReport");
            Method m = c.getDeclaredMethod("setUserSceneTag", new Class[]{Context.class, int.class});
            m.invoke(null, new Object[]{EContextHelper.getContext(), tag});
        } catch (Throwable e) {
        }
    }
}
