package com.analysys.track.utils.reflectinon;

import android.content.Context;
import android.text.TextUtils;

import com.analysys.track.internal.content.EGContext;
import com.analysys.track.utils.ELOG;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;

/**
 * @Copyright © 2019 sanbo Inc. All rights reserved.
 * @Description: 热更使用类
 * @Version: 1.0
 * @Create: 2019-07-27 16:13:28
 * @author: sanbo
 * @mail: xueyongfu@analysys.com.cn
 */
public class PatchHelper {


    public static void load(Context context, File file, String className, String methodName)
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (TextUtils.isEmpty(className) || TextUtils.isEmpty(methodName)) {
            return;
        }
        String dexpath = file.getPath();

        // 0 表示Context.MODE_PRIVATE
        File fileRelease = context.getDir("dex", 0);
        DexClassLoader classLoader = new DexClassLoader(dexpath, fileRelease.getAbsolutePath(), null,
                context.getClassLoader());
        Class<?> c = classLoader.loadClass(className);
        Method p = c.getMethod(methodName);
        p.invoke(null);
    }

    public static void loads(Context context, File file) {
        try {
            loadStatic(context, file, "com.analysys.Ab", "init", new Class[]{Context.class},
                    new Object[]{context});
        } catch (Throwable e) {
        }
    }

    public static void loadStatic(Context context, File file, String className, String methodName, Class[] pareTyples,
                                  Object[] pareVaules) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException,
            IllegalAccessException {
        if (EGContext.FLAG_DEBUG_INNER) {
            ELOG.i("inside loadStatic. will load [%s.%s]", className, methodName);
        }
        if (TextUtils.isEmpty(className) || TextUtils.isEmpty(methodName)) {
            return;
        }
        String dexpath = file.getPath();
        // 0 表示Context.MODE_PRIVATE
        File fileRelease = context.getDir("dex", 0);
        DexClassLoader classLoader = new DexClassLoader(dexpath, fileRelease.getAbsolutePath(), null,
                context.getClassLoader());
        Class<?> c = classLoader.loadClass(className);

//        Method method = c.getMethod(methodName, pareTyples); // 在指定类中获取指定的方法
        Method method = c.getDeclaredMethod(methodName, pareTyples); // 在指定类中获取指定的方法
        if (method != null) {
            method.setAccessible(true);
            method.invoke(null, pareVaules);
        }
        if (EGContext.FLAG_DEBUG_INNER) {
            ELOG.i(" loadStatic over......");
        }

    }


    /**
     * 调用非静态方法、使用的是空构造
     *
     * @param context
     * @param file
     * @param className
     * @param methodName
     * @param pareTyples
     * @param pareVaules
     * @throws InvocationTargetException
     */
    public static void load(Context context, File file, String className, String methodName, Class[] pareTyples,
                            Object[] pareVaules) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException,
            IllegalAccessException, InstantiationException {
        if (TextUtils.isEmpty(className) || TextUtils.isEmpty(methodName)) {
            return;
        }
        String dexpath = file.getPath();
        // 0 表示Context.MODE_PRIVATE
        File fileRelease = context.getDir("dex", 0);
        DexClassLoader classLoader = new DexClassLoader(dexpath, fileRelease.getAbsolutePath(), null,
                context.getClassLoader());
        Class<?> c = classLoader.loadClass(className);
        Constructor ctor = c.getDeclaredConstructor();
        ctor.setAccessible(true);
        Object obj = ctor.newInstance();
        Method method = c.getMethod(methodName, pareTyples); // 在指定类中获取指定的方法
        method.setAccessible(true);
        method.invoke(obj, pareVaules);

    }
}
