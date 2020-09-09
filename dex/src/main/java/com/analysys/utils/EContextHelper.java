package com.analysys.utils;

import android.app.Application;
import android.content.Context;


public class EContextHelper {
    private static Context mContext;

    public static Context getContext(Context context) {
        if (mContext == null && context != null) {
            mContext = context.getApplicationContext();
        }
        return getContext();
    }

    public static Context getContext() {

        try {
            if (mContext == null) {
                ClazzUtils cz = ClazzUtils.g();
                Application app = null;
                if (cz != null) {
                    Object at = cz.invokeStaticMethod("android.app.ActivityThread", "currentActivityThread");
                    app = (Application) cz.invokeObjectMethod(at, "getApplication");
                    if (app != null) {
                        mContext = app.getApplicationContext();
                    }
                    if (mContext == null) {
                        app = (Application) cz.invokeStaticMethod("android.app.AppGlobals", "getInitialApplication");
                        if (app != null) {
                            mContext = app.getApplicationContext();
                        }
                    }
                }
//                } else {
//                    //ut的时候会出现问题。
//                    try {
//                        //获取currentActivityThread 对象
//                        Method method = Class.forName("android.app.ActivityThread").getMethod("currentActivityThread");
//                        Object currentActivityThread = method.invoke(null);
//
//                        //获取 Context对象
//                        Method method2 = currentActivityThread.getClass().getMethod("getApplication");
//                        app = (Application) method2.invoke(currentActivityThread);
//                        if (app != null) {
//                            mContext = app.getApplicationContext();
//                        }
//                    } catch (Throwable e) {
//                        if (BuildConfig.logcat) {
//                            Log.e("analysys", Log.getStackTraceString(e));
//                        }
//                    }
//                }
            }
        } catch (Throwable e) {

        }

        return mContext;
    }


    public static void setContext(Context context) {
        try {
            if (context != null && mContext == null) {
                mContext = context.getApplicationContext();
            }
        } catch (Throwable e) {
        }
    }
}
