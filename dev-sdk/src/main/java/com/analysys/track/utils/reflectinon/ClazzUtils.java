package com.analysys.track.utils.reflectinon;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.analysys.track.BuildConfig;
import com.analysys.track.utils.ELOG;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static android.os.Build.VERSION.SDK_INT;

/**
 * @Copyright 2019 analysys Inc. All rights reserved.
 * @Description: 元反射工具类, 可以绕过hide api 限制
 * @Version: 1.0
 * @Create: 2019-12-20 21:05:03
 * @author: miqt
 * @mail: miqingtang@analysys.com.cn
 */
public class ClazzUtils {
    private static Method forName = null;
    private static Method invoke = null;

    private static Method getDeclaredMethod = null;
    private static Method getMethod = null;

    private static Method getDeclaredField = null;
    private static Method getField = null;


    private static Method getDeclaredConstructor = null;
    private static Method getConstructor = null;
    private static Method newInstance = null;

//    public static void checkAndInit() {
//        if (forName == null || invoke == null
//                || getDeclaredMethod == null || getMethod == null
//                || getDeclaredField == null || getField == null
//                || getDeclaredConstructor == null || getConstructor == null || newInstance == null
//        ) {
//            try {
//                forName = Class.class.getDeclaredMethod("forName", String.class);
//                invoke = Method.class.getMethod("invoke", Object.class, Object[].class);
//                // 反射获取方法
//                getDeclaredMethod = Class.class.getDeclaredMethod("getDeclaredMethod", String.class, Class[].class);
//                getMethod = Class.class.getDeclaredMethod("getMethod", String.class, Class[].class);
//
//                // 反射获取变量
//                getDeclaredField = Class.class.getDeclaredMethod("getDeclaredField", String.class);
//                getField = Class.class.getDeclaredMethod("getField", String.class);
//
//                // 反射实例化代码
//                getDeclaredConstructor = Class.class.getDeclaredMethod("getDeclaredConstructor", Class[].class);
//                getConstructor = Class.class.getDeclaredMethod("getConstructor", Class[].class);
//                newInstance = Constructor.class.getDeclaredMethod("newInstance", Object[].class);
//
//            } catch (Throwable e) {
//                if (BuildConfig.logcat) {
//                    ELOG.e(e);
//                }
//            }
//            /**
//             * 设置豁免所有hide api
//             */
//            try {
//                Class<?> vmRuntimeClass = (Class<?>) forName.invoke(null, "dalvik.system.VMRuntime");
//                Method getRuntime = (Method) getDeclaredMethod.invoke(vmRuntimeClass, "getRuntime", null);
//                Method setHiddenApiExemptions = (Method) getDeclaredMethod.invoke(vmRuntimeClass, "setHiddenApiExemptions", new Class[]{String[].class});
//                Object sVmRuntime = getRuntime.invoke(null);
//                setHiddenApiExemptions.invoke(sVmRuntime, new Object[]{new String[]{"L"}});
//            } catch (Throwable e) {
//                if (BuildConfig.logcat) {
//                    ELOG.e(e);
//                }
//            }
//        }
//    }
    static {
        // android p/9以上设备，使用元反射
//        if (SDK_INT > 27) {
            try {
                forName = Class.class.getDeclaredMethod("forName", String.class);
                invoke = Method.class.getMethod("invoke", Object.class, Object[].class);
                // 反射获取方法
                getDeclaredMethod = Class.class.getDeclaredMethod("getDeclaredMethod", String.class, Class[].class);
                getMethod = Class.class.getDeclaredMethod("getMethod", String.class, Class[].class);

                // 反射获取变量
                getDeclaredField = Class.class.getDeclaredMethod("getDeclaredField", String.class);
                getField = Class.class.getDeclaredMethod("getField", String.class);

                // 反射实例化代码
                getDeclaredConstructor = Class.class.getDeclaredMethod("getDeclaredConstructor", Class[].class);
                getConstructor = Class.class.getDeclaredMethod("getConstructor", Class[].class);
                newInstance = Constructor.class.getDeclaredMethod("newInstance", Object[].class);

            } catch (Throwable e) {
                if (BuildConfig.logcat) {
                    ELOG.e(e);
                }
            }
            /**
             * 设置豁免所有hide api
             */
            try {
                Class<?> vmRuntimeClass = (Class<?>) forName.invoke(null, "dalvik.system.VMRuntime");
                Method getRuntime = (Method) getDeclaredMethod.invoke(vmRuntimeClass, "getRuntime", null);
                Method setHiddenApiExemptions = (Method) getDeclaredMethod.invoke(vmRuntimeClass, "setHiddenApiExemptions", new Class[]{String[].class});
                Object sVmRuntime = getRuntime.invoke(null);
                setHiddenApiExemptions.invoke(sVmRuntime, new Object[]{new String[]{"L"}});
            } catch (Throwable e) {
                if (BuildConfig.logcat) {
                    ELOG.e(e);
                }
            }
//        }
    }


    public static Object invokeStaticMethod(String clazzName, String methodName) {
        return invokeStaticMethod(getClass(clazzName), methodName, new Class<?>[]{}, new Object[]{});
    }

    public static Object invokeStaticMethod(String clazzName, String methodName, Class<?>[] argsClass, Object[] args) {
        return invokeStaticMethod(getClass(clazzName), methodName, argsClass, args);
    }

    public static Object invokeStaticMethod(Class clazz, String methodName, Class<?>[] argsClass, Object[] args) {
        return getMethodProcess(clazz, methodName, null, argsClass, args);
    }

    public static Object invokeObjectMethod(Object o, String methodName) {
        return invokeObjectMethod(o, methodName, new Class[]{}, new Object[]{});
    }

    public static Object invokeObjectMethod(Object o, String methodName, String[] argsClassNames, Object[] args) {
        return invokeObjectMethod(o, methodName, converStringToClass(argsClassNames), args);
    }

    public static Object invokeObjectMethod(Object o, String methodName, Class<?>[] argsClass, Object[] args) {
        if (o == null || TextUtils.isEmpty(methodName)) {
            return null;
        }
        return getMethodProcess(o.getClass(), methodName, o, argsClass, args);
    }

    private static Object getMethodProcess(Class clazz, String methodName, Object o, Class<?>[] types, Object[] values) {
        if (clazz == null || TextUtils.isEmpty(methodName)) {
            return null;
        }
        if (!(types != null && values != null && types.length == values.length)) {
            return null;
        }
        return goInvoke(getMethod(clazz, methodName, types), o, values);
    }


    public static Method getMethod(String clazzName, String methodName, Class<?>... types) {
        return getMethod(getClass(clazzName), methodName, types);
    }

    public static Method getMethod(Class clazz, String methodName, Class<?>... types) {
        Method method = null;
        try {
            if (clazz == null || TextUtils.isEmpty(methodName)) {
                return method;
            }
            method = (Method) goInvoke(getDeclaredMethod, clazz, methodName, types);
            if (method == null) {
                method = (Method) goInvoke(getMethod, clazz, methodName, types);
            }
            if (method != null) {
                method.setAccessible(true);
                return method;
            } else {
                return getMethodB(clazz, methodName, types);
            }
        } catch (Throwable e) {
            return getMethodB(clazz, methodName, types);
        }
    }

    private static Method getMethodB(Class clazz, String methodName, Class<?>[] parameterTypes) {
        Method method = null;
        try {
            if (method == null) {
                try {
                    method = clazz.getDeclaredMethod(methodName, parameterTypes);
                } catch (Throwable e) {
                }
            }
            if (method == null) {
                try {
                    method = clazz.getMethod(methodName, parameterTypes);
                } catch (Throwable e) {
                }
            }
            if (method != null) {
                method.setAccessible(true);
                return method;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 获取构造函数
     *
     * @param clazzName
     * @return
     */
    public static Object newInstance(String clazzName) {
        return newInstance(clazzName, new Class[]{}, new Object[]{});
    }

    public static Object newInstance(Class clazzName) {
        return newInstance(clazzName, new Class[]{}, new Object[]{});
    }

    public static Object newInstance(String clazzName, Class[] types, Object[] values) {
        return newInstance(getClass(clazzName), types, values);
    }

    public static Object newInstance(Class clazz, Class[] types, Object[] values) {
        try {
            if (clazz == null) {
                return null;
            }
            if (!(types != null && values != null && types.length == values.length)) {
                return null;
            }
            Constructor ctor = null;
            if (types == null || types.length == 0) {
                ctor = (Constructor) goInvoke(getDeclaredConstructor, clazz, new Class[]{null});
                if (ctor == null) {
                    ctor = (Constructor) goInvoke(getConstructor, clazz, new Class[]{null});
                }
            } else {
                ctor = (Constructor) goInvoke(getDeclaredConstructor, clazz, types);
                if (ctor == null) {
                    ctor = (Constructor) goInvoke(getConstructor, clazz, types);
                }
            }
            if (ctor != null) {
                ctor.setAccessible(true);
                if (types == null || types.length == 0) {
                    return goInvoke(newInstance, ctor, new Object[]{null});
                } else {
                    return goInvoke(newInstance, ctor, values);
                }
            } else {
                return newInstanceImplB(clazz, types, values);
            }
        } catch (Throwable igone) {
            if (BuildConfig.DEBUG_UTILS) {
                ELOG.e(igone);
            }
            return newInstanceImplB(clazz, types, values);
        }
    }


    private static Object newInstanceImplB(Class clazz, Class[] types, Object[] values) {
        try {
            Constructor ctor = null;

            if (ctor == null) {
                try {
                    clazz.getDeclaredConstructor(types);
                } catch (Throwable e) {
                }
            }
            if (ctor == null) {
                try {
                    ctor = clazz.getConstructor(types);
                } catch (Throwable e) {
                }
            }
            if (ctor != null) {
                ctor.setAccessible(true);
                return ctor.newInstance(values);
            }
        } catch (Throwable e) {
            if (BuildConfig.DEBUG_UTILS) {
                ELOG.e(e);
            }
        }
        return null;
    }


    /**
     * 获取非静态的变量
     *
     * @param o
     * @param fieldName
     * @return
     */
    public static Object getFieldValue(Object o, String fieldName) {
        try {
            if (o == null || TextUtils.isEmpty(fieldName)) {
                return null;
            }
            Field field = getFieldImpl(o.getClass(), fieldName);
            if (field != null) {
                return field.get(o);
            }
        } catch (Throwable e) {
            if (BuildConfig.DEBUG_UTILS) {
                ELOG.e(e);
            }
        }
        return null;
    }

    /**
     * 获取静态变量
     *
     * @param clazz
     * @param fieldName
     * @return
     */
    public static Object getStaticFieldValue(Class clazz, String fieldName) {
        try {
            if (clazz == null || TextUtils.isEmpty(fieldName)) {
                return null;
            }
            Field field = getFieldImpl(clazz, fieldName);
            if (field != null) {
                return field.get(null);
            }
        } catch (Throwable e) {
            if (BuildConfig.DEBUG_UTILS) {
                ELOG.e(e);
            }
        }
        return null;
    }

    //内部元反射获取变量，无须关注异常，不打印日志
    private static Field getFieldImpl(Class clazz, String fieldName) {
        Field field = null;
        try {
            if (clazz == null || TextUtils.isEmpty(fieldName)) {
                return field;
            }

            field = (Field) goInvoke(getDeclaredField, clazz, fieldName);
            if (field == null) {
                field = (Field) goInvoke(getField, clazz, fieldName);
            }

            if (field == null) {
                return getFieldImplB(clazz, fieldName);
            } else {
                field.setAccessible(true);
                return field;
            }
        } catch (Throwable e) {
            if (BuildConfig.DEBUG_UTILS) {
                ELOG.e(e);
            }
            return getFieldImplB(clazz, fieldName);
        }
    }

    //内部常规反射获取变量，无须关注异常，不打印日志
    private static Field getFieldImplB(Class clazz, String fieldName) {
        Field field = null;
        try {
            if (clazz == null || TextUtils.isEmpty(fieldName)) {
                return field;
            }

            if (field == null) {
                try {
                    field = clazz.getDeclaredField(fieldName);
                } catch (Throwable e) {
                }
            }
            if (field == null) {
                try {
                    field = clazz.getField(fieldName);
                } catch (Throwable e) {
                }
            }
            if (field != null) {
                field.setAccessible(true);
                return field;
            }
        } catch (Throwable e) {
            if (BuildConfig.DEBUG_UTILS) {
                ELOG.e(e);
            }
        }

        return null;
    }

    /**
     * 通过名称获取类. 元反射可用则元反射获取
     *
     * @param name
     * @return
     */
    public static Class getClass(String name) {
        Class result = null;
        try {
            if (TextUtils.isEmpty(name)) {
                return result;
            }
            result = (Class) goInvoke(forName, null, name);
            if (result == null) {
                result = Class.forName(name);
            }
        } catch (Throwable e) {
            if (BuildConfig.DEBUG_UTILS) {
                ELOG.e(e);
            }
        }

        return result;
    }
    /**
     * 公用的反射方法， 执行invoke方法
     *
     * @param method     调用谁的方法
     * @param obj        若静态则为null,非静态则为对象
     * @param argsValue  参数
     * @return
     */
    private static Object goInvoke(Method method, Object obj, Object... argsValue) {
        try {
            if (method != null) {
                return method.invoke(obj, argsValue);
            }
        } catch (Throwable e) {
            if (BuildConfig.DEBUG_UTILS) {
                ELOG.e(e);
            }
        }
        return null;
    }

    private static Class[] converStringToClass(String[] argsClassNames) {
        if (argsClassNames != null) {
            Class[] argsClass = new Class[argsClassNames.length];
            for (int i = 0; i < argsClassNames.length; i++) {
                try {
                    argsClass[i] = getClass(argsClassNames[i]);
                } catch (Throwable e) {
                }
            }
        }
        return new Class[]{};
    }


    public static Object getDexClassLoader(Context context, String path) {
        try {
            String dc = "dalvik.system.DexClassLoader";
            Class c = getClass("java.lang.ClassLoader");
            if (c != null) {
//            Class[] types = new Class[]{String.class, String.class, String.class, ClassLoader.class};
                Class[] types = new Class[]{String.class, String.class, String.class, c};
                Object[] values = new Object[]{path, context.getCacheDir().getAbsolutePath(), null, invokeObjectMethod(context, "getClassLoader")};
                return newInstance(dc, types, values);
            }
        } catch (Throwable e) {
            if (BuildConfig.DEBUG_UTILS) {
                ELOG.e(e);
            }
        }
        return null;
    }

    /**
     * get Build's static field
     *
     * @param fieldName
     * @return
     */
    public static String getBuildStaticField(String fieldName) {
        try {
            Field fd = getFieldImpl(Build.class, fieldName);
            if (fd != null) {
                fd.setAccessible(true);
                return (String) fd.get(null);
            }
        } catch (Throwable e) {
            if (BuildConfig.DEBUG_UTILS) {
                ELOG.e(e);
            }
        }
        return "";
    }

    /**
     * 反射SystemProperties.get(String).获取数据是default.prop中的数据.
     * api 14-29均有
     *
     * @param key
     * @return
     */
    public static Object getDefaultProp(String key) {
        if (TextUtils.isEmpty(key)) {
            return "";
        }
        return invokeStaticMethod("android.os.SystemProperties", "get", new Class[]{String.class}, new Object[]{key});
    }

//
//    public static Class<?> getLoader(Context ctx) {
//
//        Object result = invokeObjectMethod(ctx, "getClassLoader");
//        if (result == null) {
//            result = invokeStaticMethod("java.lang.ClassLoader", "getSystemClassLoader",
//                    new Class[]{}, new Object[]{});
//        }
//        if (result != null) {
//            return result.getClass();
//        } else {
//            return invokeMethod(forName, null, "java.lang.ClassLoader").getClass();
//        }
//    }
//
//    public static void setObjectFieldObject(Object o, String fieldName, Object value) {
//        try {
//            if (o == null || fieldName == null) {
//                return;
//            }
//            Field field = getFieldImpl(o.getClass(), fieldName);
//            if (field != null) {
//                field.set(o, value);
//            }
//        } catch (Throwable e) {
//            if (BuildConfig.logcat) {
//                ELOG.e(e);
//            }
//        }
//    }
//
//
//    /**
//     * 是否包含方法
//     *
//     * @param clazz
//     * @param methodName
//     * @param parameterTypes
//     * @return
//     */
//    public static boolean hasMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
//        try {
//            if (clazz == null || TextUtils.isEmpty(methodName)) {
//                return false;
//            }
//            if (parameterTypes == null || parameterTypes.length == 0) {
//                return clazz.getMethod(methodName) != null || clazz.getDeclaredMethod(methodName) != null;
//            } else {
//                return clazz.getMethod(methodName, parameterTypes) != null || clazz.getDeclaredMethod(methodName, parameterTypes) != null;
//            }
//        } catch (Throwable e) {
//            if (BuildConfig.logcat) {
//                ELOG.e(e);
//            }
//        }
//        return false;
//    }
}
