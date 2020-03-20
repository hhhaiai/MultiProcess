package com.analysys.track.utils.reflectinon;

import android.content.Context;
import android.os.Build;
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
//        if (SDK_INT > 27 && SDK_INT <= 29) {
        if (SDK_INT > 27) {
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
            unseal();
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

    /**
     * get method
     *
     * @param clazzName
     * @param methodName
     * @param parameterTypes
     * @return
     */
    public static Method getMethod(String clazzName, String methodName, Class<?>... parameterTypes) {
        return getMethod(getClass(clazzName), methodName, parameterTypes);
    }

    public static Method getMethod(Class clazz, String methodName, Class<?>... parameterTypes) {
        Method method = null;
        try {
            if (clazz == null || TextUtils.isEmpty(methodName)) {
                return method;
            }

            method = (Method) invokeMethod(invoke, getDeclaredMethod, clazz, methodName, parameterTypes);
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
                if (BuildConfig.ENABLE_BUG_REPORT) {
                    BugReportForTest.commitError(new Exception(clazz.getName() + methodName + "not found !"));
                }
            }
        } catch (Throwable e) {
        }
        return method;
    }

    /**
     * get field
     *
     * @param className
     * @param fieldName
     * @return
     */
    public static Field getField(String className, String fieldName) {
        return getField(getClass(className), fieldName);
    }

    public static Field getField(Class clazz, String fieldName) {
        Field field = null;
        try {
            if (clazz == null || TextUtils.isEmpty(fieldName)) {
                return field;
            }

            field = (Field) invokeMethod(invoke, getDeclaredField, clazz, fieldName);
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
            }
        } catch (Throwable e) {
        }
        return field;
    }

    /**
     * get object's field
     *
     * @param o
     * @param fieldName
     * @return
     */
    public static Object getObjectFieldObject(Object o, String fieldName) {
        try {
            if (o == null) {
                return null;
            }
            Field field = getField(o.getClass(), fieldName);
            if (field != null) {
                return field.get(o);
            }
        } catch (Throwable e) {
        }
        return null;
    }

    public static void setObjectFieldObject(Object o, String fieldName, Object value) {

        try {
            if (o == null || fieldName == null) {
                return;
            }
            Field field = getField(o.getClass(), fieldName);
            if (field != null) {
                field.set(o, value);
            }
        } catch (Throwable e) {
        }
    }

    public static Object getStaticFieldObject(Class clazz, String fieldName) {

        try {
            if (clazz == null || fieldName == null) {
                return null;
            }
            Field field = getField(clazz.getClass(), fieldName);
            if (field != null) {
                return field.get(null);
            }
        } catch (Throwable e) {

        }
        return null;
    }

    /**
     * invoke  private/default/protected method
     *
     * @param o
     * @param methodName
     * @return
     */
    public static Object invokeObjectMethod(Object o, String methodName) {
        return invokeObjectMethod(o, methodName, new Class[]{}, new Object[]{});
    }

    public static Object invokeObjectMethod(Object o, String methodName, Class<?>[] argsClass, Object[] args) {

        Object returnValue = null;
        try {
            if (o == null || methodName == null) {
                return returnValue;
            }
            Class<?> c = o.getClass();
            Method method = getMethod(c, methodName, argsClass);
            returnValue = method.invoke(o, args);
        } catch (Throwable e) {
        }

        return returnValue;
    }

    public static Object invokeObjectMethod(Object o, String methodName, String[] argsClassNames, Object[] args) {
        try {
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
        } catch (Throwable e) {
        }
        return null;
    }


    public static Object invokeStaticMethod(String clazzName, String methodName) {
        return invokeStaticMethod(getClass(clazzName), methodName, new Class<?>[]{}, new Object[]{});
    }
    /**
     * call static method
     *
     * @param clazzName
     * @param methodName
     * @param argsClass
     * @param args
     * @return
     */
    public static Object invokeStaticMethod(String clazzName, String methodName, Class<?>[] argsClass, Object[] args) {
        return invokeStaticMethod(getClass(clazzName), methodName, argsClass, args);
    }

    public static Object invokeStaticMethod(Class clazz, String methodName, Class<?>[] argsClass, Object[] args) {
        Object returnValue = null;
        try {
            if (clazz == null || methodName == null) {
                return returnValue;
            }
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
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        }
        return returnValue;
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
            if (types == null) {
                types = new Class[]{};
                values = new Object[]{};
            }
            Constructor ctor = clazz.getDeclaredConstructor(types);
            if (ctor != null) {
                ctor.setAccessible(true);
                return ctor.newInstance(values);
            } else {
                ctor = clazz.getConstructor(types);
                if (ctor != null) {
                    ctor.setAccessible(true);
                    return ctor.newInstance(values);
                }
            }

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
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        }
        return false;
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
            Field fd = getField(Build.class, fieldName);
            if (fd != null) {
                fd.setAccessible(true);
                return (String) fd.get(null);
            }
        } catch (Throwable e) {
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
}
