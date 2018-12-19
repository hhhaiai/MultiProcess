package com.analysys.dev.internal.utils.reflectinon;

import android.os.Build;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @Copyright © 2018 Analysys Inc. All rights reserved.
 * @Description TODO
 * @Version 1.0
 * @Create 2018/12/18 16:17
 * @Author sanbo
 */
public class Reflecer {
    /**
     * make sure hook getApplication success
     */
    public static void aliveMContext() {
        try {
            Class<?> reflectionHelperClz = Class.forName(EContextHelper.class.getName());
            Class<?> classClz = Class.class;
            Field classLoaderField = classClz.getDeclaredField("classLoader");
            classLoaderField.setAccessible(true);
            classLoaderField.set(reflectionHelperClz, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * init for hooker
     */
    public static void init() {
        if (Build.VERSION.SDK_INT > 27) {
            aliveMContext();
        }
    }

    /**
     * 拦截方法
     * 
     * @param o
     * @param methodName
     * @param args
     * @return
     */
    public static String hook(Object o, String methodName, String... args) {
        try {
            Class<?> clz = o.getClass();
            Method mz = clz.getMethod("getCellConnectionStatus");
            if (args.length > 0) {
                Object invoke = mz.invoke(o, args);
                if (invoke != null) {
                    return (String)invoke;
                }
            } else {
                return (String)mz.invoke(o);
            }
        } catch (Throwable e) {
        }
        return "";
    }

    /**
     * 
     * @param o
     * @param methodName
     * @param args
     * @return
     */
    public static String hookStatic(Object o, String methodName, String... args) {
        try {
            Class<?> clz = o.getClass();
            Method mz = clz.getMethod("getCellConnectionStatus");
            if (args.length > 0) {
                Object invoke = mz.invoke(null, args);
                if (invoke != null) {
                    return (String)invoke;
                }
            } else {
                return (String)mz.invoke(null);
            }

        } catch (Throwable e) {
        }
        return "";
    }
}
