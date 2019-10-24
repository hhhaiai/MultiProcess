package com.analysys.track.utils;

import android.content.Context;

import com.analysys.track.utils.reflectinon.EContextHelper;

import java.lang.reflect.Method;

public class BuglyUtils {
    private static boolean tag = false;

    public static void commitError(Throwable throwable) {
        try {
            Class clazz = Class.forName("com.tencent.bugly.crashreport.CrashReport");
            if (!tag) {
                setTag(clazz);
            }
            postException(throwable, clazz);
        } catch (Throwable e) {

        }
    }

    private static void postException(Throwable throwable, Class<?> clazz) {
        try {
            Method postCatchedException = clazz.getMethod("postCatchedException");
            postCatchedException.invoke(null, throwable);
        } catch (Throwable e) {

        }
    }

    private static void setTag(Class<?> clazz) {
        try {
            Method setUserSceneTag = clazz.getMethod("setUserSceneTag", Context.class, Integer.class);
            setUserSceneTag.invoke(null, EContextHelper.getContext(null), 9999);
        } catch (Throwable e) {

        }
        tag = true;
    }
}
