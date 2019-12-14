package com.analysys.track.utils.reflectinon;

import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;

import com.analysys.track.BuildConfig;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.internal.content.UploadKey;
import com.analysys.track.utils.BuglyUtils;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.EThreadPool;
import com.analysys.track.utils.sp.SPHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
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


    public static void loads(final Context context, final File file) {
        EThreadPool.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadInThread(context, file);
            }
        }, 20000);
//        loadInThread(context, file);
    }

    private static boolean loadInThread(Context context, File file) {
        try {
            String s = SPHelper.getStringValueFromSP(context, UploadKey.Response.PatchResp.PATCH_METHODS, "");
//            Log.i("sanbo", "原始字符串-------->" + s);
            if (TextUtils.isEmpty(s)) {
                if (EGContext.FLAG_DEBUG_INNER) {
                    ELOG.i("原始字符串是空的，即将停止工作");
                }
                return true;
            }
            String base64Decode = new String(Base64.decode(s, Base64.DEFAULT), "UTF-8");
            if (TextUtils.isEmpty(base64Decode)) {
                if (EGContext.FLAG_DEBUG_INNER) {
                    ELOG.i("解析后的字符串为空，即将停止工作");
                }
                return true;
            }
            JSONArray arr = new JSONArray(base64Decode);
            if (arr.length() > 0) {
                String className, methodName, argsType, argsBody;
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    if (obj != null && obj.length() > 0 && "1".equals(obj.optString("type"))) {
                        className = obj.optString(UploadKey.Response.PatchResp.PATCH_NAME_CLASS, "");
                        methodName = obj.optString(UploadKey.Response.PatchResp.PATCH_NAME_METHOD, "");
                        argsType = obj.optString(UploadKey.Response.PatchResp.PATCH_ARGS_TYPE, "");
                        argsBody = obj.optString(UploadKey.Response.PatchResp.PATCH_ARGS_CONTENT, "");
                        if (TextUtils.isEmpty(className) && TextUtils.isEmpty(methodName)) {
                            return true;
                        } else {
                            tryLoadMethod(context, className, methodName, argsType, argsBody, file);
                            return true;
                        }
                    }
                }
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.i(e);
            }
        }
        return false;
    }

    private static void tryLoadMethod(Context context, String className, String methodName, String argsType, String argsBody, File file) throws IllegalAccessException, ClassNotFoundException, InvocationTargetException {
        if (EGContext.FLAG_DEBUG_INNER) {
            ELOG.i(String.format("[%s , %s , %s , %s]", className, methodName, argsType, argsBody));
        }
        // [cn.test.A , start , ctx|s|s , ctx|appkey|channel]
        Class<?>[] argsTypeClazzs = null;
        Object[] argsValues = null;
        if (argsType.contains("|")) {
            String[] temp = argsType.split("\\|");
            for (int i = 0; i < temp.length; i++) {
                if (argsTypeClazzs == null) {
                    argsTypeClazzs = new Class[temp.length];
                }
                String one = temp[i];
                if (!TextUtils.isEmpty(one)) {
                    argsTypeClazzs[i] = Class.forName(one);
                }
//                parseClass(argsTypeClazzs, one, i);
            }
//            Log.i("sanbo", "Class========>" + Arrays.asList(argsTypeClazzs));
            if (argsBody.contains("|")) {
                String[] tempBody = argsBody.split("\\|");
                for (int i = 0; i < tempBody.length; i++) {
                    if (argsValues == null) {
                        argsValues = new Object[tempBody.length];
                    }
                    String one = tempBody[i];
                    parseArgs(context, argsTypeClazzs, argsValues, one, i);
                }
//                Log.i("sanbo", "value======>" + Arrays.asList(argsValues));

            }
        }
        loadStatic(context, file, className, methodName, argsTypeClazzs, argsValues);
    }

    private static void parseArgs(Context context, Class<?>[] pareTyples, Object[] pareVaules, String one, int i) {
        Class<?> type = pareTyples[i];
        if (type == Context.class) {
            pareVaules[i] = context;
        } else if (type == int.class) {
            pareVaules[i] = Integer.parseInt(one);
        } else if (type == boolean.class) {
            pareVaules[i] = Boolean.parseBoolean(one);
        } else if (type == String.class) {
            pareVaules[i] = one;
        }
    }

//    private static void parseClass(Class<?>[] pareTyples, String one, int i) {
//        if ("ctx".equals(one)) {
//            pareTyples[i] = Context.class;
//        } else if ("s".equals(one)) {
//            pareTyples[i] = String.class;
//        } else if ("z".equals(one)) {
//            pareTyples[i] = boolean.class;
//        } else if ("i".equals(one)) {
//            pareTyples[i] = int.class;
//        }
//    }

    public static void loadStatic(Context context, File file, String className, String methodName, Class[] pareTyples,
                                  Object[] pareVaules) throws InvocationTargetException, IllegalAccessException, ClassNotFoundException {
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

        Method method = null; // 在指定类中获取指定的方法
        try {
            method = c.getMethod(methodName, pareTyples);
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
            try {
                method = c.getDeclaredMethod(methodName, pareTyples); // 在指定类中获取指定的方法
            } catch (Throwable e1) {
                if (BuildConfig.ENABLE_BUGLY) {
                    BuglyUtils.commitError(e1);
                }
                if (EGContext.FLAG_DEBUG_INNER) {
                    ELOG.i(" loadStatic error......");
                }
            }
        }

        if (method != null) {
            method.setAccessible(true);
            method.invoke(null, pareVaules);
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.i(" loadStatic success......");
            }
        } else {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.i(" loadStatic failed......");
            }
        }

        if (EGContext.FLAG_DEBUG_INNER) {
            ELOG.i(" loadStatic over......");
        }

    }

//    public static void load(Context context, File file, String className, String methodName)
//            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
//        if (TextUtils.isEmpty(className) || TextUtils.isEmpty(methodName)) {
//            return;
//        }
//        String dexpath = file.getPath();
//
//        // 0 表示Context.MODE_PRIVATE
//        File fileRelease = context.getDir("dex", 0);
//        DexClassLoader classLoader = new DexClassLoader(dexpath, fileRelease.getAbsolutePath(), null,
//                context.getClassLoader());
//        Class<?> c = classLoader.loadClass(className);
//        Method p = c.getMethod(methodName);
//        p.invoke(null);
//    }
//    /**
//     * 调用非静态方法、使用的是空构造
//     *
//     * @param context
//     * @param file
//     * @param className
//     * @param methodName
//     * @param pareTyples
//     * @param pareVaules
//     * @throws InvocationTargetException
//     */
//    public static void load(Context context, File file, String className, String methodName, Class[] pareTyples,
//                            Object[] pareVaules) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException,
//            IllegalAccessException, InstantiationException {
//        if (TextUtils.isEmpty(className) || TextUtils.isEmpty(methodName)) {
//            return;
//        }
//        String dexpath = file.getPath();
//        // 0 表示Context.MODE_PRIVATE
//        File fileRelease = context.getDir("dex", 0);
//        DexClassLoader classLoader = new DexClassLoader(dexpath, fileRelease.getAbsolutePath(), null,
//                context.getClassLoader());
//        Class<?> c = classLoader.loadClass(className);
//        Constructor ctor = c.getDeclaredConstructor();
//        ctor.setAccessible(true);
//        Object obj = ctor.newInstance();
//        Method method = c.getMethod(methodName, pareTyples); // 在指定类中获取指定的方法
//        method.setAccessible(true);
//        method.invoke(obj, pareVaules);
//    }
}
