package com.analysys.track.utils.reflectinon;

import android.content.Context;
import android.text.TextUtils;

import com.analysys.track.BuildConfig;
import com.analysys.track.utils.BugReportForTest;

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
    private static Method forName;
    private static Method getDeclaredMethod;
    private static Method getMethod;
    private static Method getField;
    private static Method invoke;
    private static Method getDeclaredField;

    public static boolean rawReflex = false;

    static {
        // android  9 10 版本
        if (SDK_INT > 27 && SDK_INT <= 29) {
            try {
                forName = Class.class.getDeclaredMethod("forName", String.class);
                getDeclaredMethod = Class.class.getDeclaredMethod("getDeclaredMethod", String.class, Class[].class);
                getMethod = Class.class.getDeclaredMethod("getMethod", String.class, Class[].class);
                getDeclaredField = Class.class.getDeclaredMethod("getDeclaredField", String.class);
                getField = Class.class.getDeclaredMethod("getField", String.class);
                invoke = Method.class.getMethod("invoke", Object.class, Object[].class);
                rawReflex = true;
            } catch (Throwable e) {
                rawReflex = false;
            }
        }
    }

    /**
     * 设置豁免所有hide api
     */
    public static void unseal() {
        // android  9 10 版本
//        if (SDK_INT > 27 && SDK_INT <= 29) {
        if (SDK_INT > 27) {
            try {
                Class<?> vmRuntimeClass = (Class<?>) forName.invoke(null, "dalvik.system.VMRuntime");
                Method getRuntime = (Method) getDeclaredMethod.invoke(vmRuntimeClass, "getRuntime", null);
                Method setHiddenApiExemptions = (Method) getDeclaredMethod.invoke(vmRuntimeClass, "setHiddenApiExemptions", new Class[]{String[].class});
                Object sVmRuntime = getRuntime.invoke(null);
                setHiddenApiExemptions.invoke(sVmRuntime, new Object[]{new String[]{"L"}});
            } catch (Throwable e) {
            }
        }
    }

    public static Method getMethod(String clazzName, String methodName, Class<?>... parameterTypes) {
        Class<?> c = getClass(clazzName);
        if (c != null) {
            return getMethod(c, methodName, parameterTypes);
        } else {
            return null;
        }
    }

    public static Method getMethod(Class clazz, String methodName, Class<?>... parameterTypes) {
        if (clazz == null || TextUtils.isEmpty(methodName)) {
            return null;
        }

        Method method = (Method) invokeMethod(invoke, getDeclaredMethod, clazz, methodName, parameterTypes);
        if (method == null) {
            method = (Method) invokeMethod(invoke, getMethod, clazz, methodName, parameterTypes);
        }
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
        } else {
            if (BuildConfig.ENABLE_BUGLY) {
                BugReportForTest.commitError(new Exception(clazz.getName() + methodName + "not found !"));
            }
        }
        return method;
    }

    public static Field getField(Class clazz, String fieldName) {
        if (clazz == null || TextUtils.isEmpty(fieldName)) {
            return null;
        }

        Field field = (Field) invokeMethod(invoke, getDeclaredField, clazz, fieldName);
        if (field == null) {
            field = (Field) invokeMethod(invoke, getField, clazz, fieldName);
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
        } else {
            if (BuildConfig.ENABLE_BUGLY) {
                BugReportForTest.commitError(new Exception(clazz.getName() + fieldName + "not found !"));
            }
        }
        return null;
    }



    public static Object invokeObjectMethod(Object o, String methodName) {
        return invokeObjectMethod(o, methodName, (Class<?>[]) null, (Object[]) null);
    }

    public static Object invokeObjectMethod(Object o, String methodName, Class<?>[] argsClass, Object[] args) {
        if (o == null || methodName == null) {
            return null;
        }
        Object returnValue = null;
        try {
            Class<?> c = o.getClass();
            Method method;
            method = getMethod(c, methodName, argsClass);
            returnValue = method.invoke(o, args);
        } catch (Throwable e) {
        }

        return returnValue;
    }

    public static Object invokeObjectMethod(Object o, String methodName, String[] argsClassNames, Object[] args) {
        if (o == null || methodName == null) {
            return null;
        }
        if (argsClassNames != null) {
            Class[] argsClass = new Class[argsClassNames.length];
            for (int i = 0; i < argsClassNames.length; i++) {
                try {
                    argsClass[i] = Class.forName(argsClassNames[i]);
                } catch (Throwable e) {
                }
            }
            return invokeObjectMethod(o, methodName, argsClass, args);
        }
        return null;
    }


    public static Object getObjectFieldObject(Object o, String fieldName) {
        if (o == null || fieldName == null) {
            return null;
        }
        Field field = getField(o.getClass(), fieldName);
        try {
            return field.get(o);
        } catch (Throwable e) {

        }
        return null;
    }

    public static void setObjectFieldObject(Object o, String fieldName, Object value) {
        if (o == null || fieldName == null) {
            return;
        }
        Field field = getField(o.getClass(), fieldName);
        try {
            field.set(o, value);
        } catch (Throwable e) {

        }
    }

    public static Object getStaticFieldObject(Class clazz, String fieldName) {
        if (clazz == null || fieldName == null) {
            return null;
        }
        Field field = getField(clazz.getClass(), fieldName);
        try {
            return field.get(null);
        } catch (Throwable e) {

        }
        return null;
    }

    public static Object invokeStaticMethod(String clazzName, String methodName, Class<?>[] argsClass, Object[] args) {

        Class<?> c = getClass(clazzName);
        if (c != null) {
            return invokeStaticMethod(c, methodName, argsClass, args);
        } else {
            return null;
        }
    }

    public static Object invokeStaticMethod(Class clazz, String methodName, Class<?>[] argsClass, Object[] args) {
        if (clazz == null || methodName == null) {
            return null;
        }
        Object returnValue = null;
        try {
            Method method;
            if (argsClass == null) {
                // 木有参数的直接准确调用
                method = getMethod(clazz, methodName);
                returnValue = method.invoke(null);
            } else {
                if (argsClass.length == args.length) {
                    method = getMethod(clazz, methodName, argsClass);
                    returnValue = method.invoke(null, args);
                }
            }

        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BugReportForTest.commitError(e);
            }
        }

        return returnValue;
    }

    /**
     * 是否包含方法
     *
     * @param clazz
     * @param methodName
     * @param parameterTypes
     * @return
     */
    public static boolean hasMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        try {
            if (clazz == null || TextUtils.isEmpty(methodName)) {
                return false;
            }
            if (parameterTypes == null || parameterTypes.length == 0) {
                return clazz.getMethod(methodName) != null || clazz.getDeclaredMethod(methodName) != null;
            } else {
                return clazz.getMethod(methodName, parameterTypes) != null || clazz.getDeclaredMethod(methodName, parameterTypes) != null;
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BugReportForTest.commitError(e);
            }
        }
        return false;
    }


    public static Object getDexClassLoader(Context context, String path) {
        String baseStr = "dalvik.system.DexClassLoader";
        Class c = getClass("java.lang.ClassLoader");
        if (c != null) {
            Class[] types = new Class[]{String.class, String.class, String.class, c};
            Object[] values = new Object[]{path, context.getCacheDir().getAbsolutePath(), null, ClazzUtils.invokeObjectMethod(context, "getClassLoader")};
            return ClazzUtils.newInstance(baseStr, types, values);
        } else {
            return null;
        }

    }


    /**
     * 获取构造函数
     *
     * @param clazzName
     * @return
     */
    public static Object newInstance(String clazzName) {
        if (TextUtils.isEmpty(clazzName)) {
            return null;
        }
        return newInstance(clazzName, new Class[]{}, new Object[]{});
    }

    /**
     * 获取构造函数
     *
     * @param clazzName
     * @param types
     * @param values
     * @return
     */
    private static Object newInstance(String clazzName, Class[] types, Object[] values) {
        Class<?> c = getClass(clazzName);
        if (c != null) {
            return newInstance(c, types, values);
        } else {
            return null;
        }
    }

    public static Object newInstance(Class clazz, Class[] types, Object[] values) {

        try {
            Constructor ctor = clazz.getDeclaredConstructor(types);
            ctor.setAccessible(true);
            return ctor.newInstance(values);

        } catch (Throwable igone) {
            try {
                Constructor ctor = clazz.getConstructor(types);
                ctor.setAccessible(true);
                return ctor.newInstance(values);
            } catch (Throwable e) {
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
            result = (Class) invokeMethod(forName, null, name);
            if (result == null) {
                result = Class.forName(name);
            }
        } catch (Throwable e) {
        }

        return result;
    }

    /**
     * 执行invoke方法
     *
     * @param methodName
     * @param obj        若静态则为null,非静态则为对象
     * @param argsValue
     * @return
     */
    private static Object invokeMethod(Method methodName, Object obj, Object... argsValue) {
        try {
            if (methodName != null) {
                return methodName.invoke(obj, argsValue);
            }
        } catch (Throwable e) {
        }
        return null;
    }

}
