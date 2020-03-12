package com.device.impls.cases;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.device.utils.DemoClazzUtils;
import com.device.utils.EL;

import org.json.JSONException;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;

public class PolicTestY {
    /********************************** 功能实现区 ************************************/
    public static void loadStatic2(Context context, File file, String className, String methodName, Class[] pareTyples,
                                   Object[] pareVaules) throws InvocationTargetException, IllegalAccessException, ClassNotFoundException {
        EL.i("inside loadStatic. will load [%s.%s]", className, methodName);
        if (TextUtils.isEmpty(className) || TextUtils.isEmpty(methodName)) {
            return;
        }
        try {
            //1. get DexClassLoader
            // need hide ClassLoader
            Object ca = DemoClazzUtils.getDexClassLoader(context, file.getPath());
            EL.i(" loadStatic DexClassLoader over. result: " + ca);
            // 2. load class
            Class<?> c = (Class<?>) DemoClazzUtils.invokeObjectMethod(ca, "loadClass", new Class[]{String.class}, new Object[]{className});
            if (c != null) {
                // 2. invoke method
                DemoClazzUtils.invokeStaticMethod(c, methodName, pareTyples, pareVaules);
            } else {
                EL.i(" loadStatic failed[get class load failed]......");

            }

        } catch (Throwable igone) {
            EL.e(igone);
        }
        EL.i(" loadStatic over......");
    }

    public static void loadStatic(Context context, File file, String className, String methodName, Class[] pareTyples,
                                  Object[] pareVaules) throws InvocationTargetException, IllegalAccessException, ClassNotFoundException {
        EL.i(String.format("inside loadStatic. will load [%s.%s]", className, methodName));
        if (TextUtils.isEmpty(className) || TextUtils.isEmpty(methodName)) {
            return;
        }
        String dexpath = file.getPath();
        // 0 表示Context.MODE_PRIVATE
        File fileRelease = context.getDir("dex", 0);
        DexClassLoader classLoader = null;
        try {
            classLoader = new DexClassLoader(dexpath, fileRelease.getAbsolutePath(), null, context.getClassLoader());
        } catch (Throwable e) {
            EL.e(Log.getStackTraceString(e));
        }
        Class<?> c = classLoader.loadClass(className);

        Method method = null; // 在指定类中获取指定的方法
        try {
            method = c.getMethod(methodName, pareTyples);
        } catch (Throwable e) {
            try {
                method = c.getDeclaredMethod(methodName, pareTyples); // 在指定类中获取指定的方法
            } catch (Throwable e1) {
                EL.i(" loadStatic error......");
            }
        }

        if (method != null) {
            method.setAccessible(true);
            method.invoke(null, pareVaules);
            EL.i(" loadStatic success......");
        } else {
            EL.i(" loadStatic failed......");
        }
        EL.i(" loadStatic over......");

    }

    /**
     * 测试解析策略 部分内容
     *
     * @param context
     * @throws JSONException
     */
    public static void testSavePolicy(Context context) throws JSONException {
//        PolicyImpl.getInstance(context).clear();
//        JSONObject obj = new JSONObject(AssetsHelper.getFromAssetsToString(context, "policy_body.txt"));
//        PolicyImpl.getInstance(context).saveRespParams(obj);
    }
}
