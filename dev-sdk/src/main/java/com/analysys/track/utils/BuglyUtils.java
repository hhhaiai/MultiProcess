package com.analysys.track.utils;

import android.content.Context;

import com.analysys.track.utils.reflectinon.EContextHelper;

import java.lang.reflect.Method;

public class BuglyUtils {

    public static void commitError(Throwable throwable) {
        ELOG.i(throwable);
        try {
            Class clazz = Class.forName("com.tencent.bugly.crashreport.CrashReport");
            setTag(clazz, 1002);
            postException(throwable, clazz);
            setTag(clazz, 1001);
        } catch (Throwable e) {

        }
    }

    private static void postException(Throwable throwable, Class<?> clazz) {
        try {
            Method postCatchedException = clazz.getMethod("postCatchedException", Throwable.class);
            postCatchedException.invoke(null, throwable);
        } catch (Throwable e) {

        }
    }

    private static void setTag(Class<?> clazz, int tag) {
        try {
            Method setUserSceneTag = clazz.getMethod("setUserSceneTag", Context.class, int.class);
            setUserSceneTag.invoke(null, EContextHelper.getContext(null
            ), tag);
        } catch (Throwable e) {

        }
    }
}
