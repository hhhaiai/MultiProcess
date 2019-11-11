package com.analysys.track.hotfix;

import android.content.Context;
import android.text.TextUtils;

import com.analysys.track.BuildConfig;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.utils.BuglyUtils;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.ProcessUtils;
import com.analysys.track.utils.reflectinon.EContextHelper;
import com.analysys.track.utils.sp.SPHelper;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * @Copyright 2019 analysys Inc. All rights reserved.
 * @Description: 控制热修复转向的类, 主要控制, 初始化, 转向短路, dex包管理的一些逻辑
 * @Version: 1.0
 * @Create: 2019-11-06 11:28:37
 * @author: miqt
 * @mail: miqingtang@analysys.com.cn
 */
public class HotFixTransform {
    private final static HashMap<Class, String> mapMemberClass = new HashMap<Class, String>();
    //放入入口类路径,用于判断dex包是不是损坏
    private final static HashSet<String> MYCLASS_NAME = new HashSet<String>();

    static {
        mapMemberClass.put(Integer.class, "int");
        mapMemberClass.put(Double.class, "double");
        mapMemberClass.put(Float.class, "float");
        mapMemberClass.put(Character.class, "char");
        mapMemberClass.put(Boolean.class, "boolean");
        mapMemberClass.put(Short.class, "short");
        mapMemberClass.put(Long.class, "long");
        mapMemberClass.put(Byte.class, "byte");

        MYCLASS_NAME.add("com.analysys.track.AnalysysTracker");
        MYCLASS_NAME.add("com.analysys.track.service.AnalysysAccessibilityService");
        MYCLASS_NAME.add("com.analysys.track.service.AnalysysJobService");
        MYCLASS_NAME.add("com.analysys.track.service.AnalysysService");
        MYCLASS_NAME.add("com.analysys.track.receiver.AnalysysReceiver");
    }

    private static volatile ClassLoader loader = EContextHelper.getContext(null).getClassLoader();

    public static void init(Context context) {
        if (!isInit()) {
            synchronized (HotFixTransform.class) {
                if (!isInit()) {
                    String path = SPHelper.getStringValueFromSP(context, EGContext.HOT_FIX_PATH, "");
                    boolean enable = SPHelper.getBooleanValueFromSP(context, EGContext.HOT_FIX_ENABLE_STATE, false);
                    if (enable && hasDexFile(path)) {
                        setAnalClassloader(context, path);
                    } else {
                        SPHelper.setBooleanValue2SP(context, EGContext.HOT_FIX_ENABLE_STATE, false);
                        setPathClassLoader();
                    }
                    isinit = true;
                    //主进程进行清理旧的dex文件
                    deleteOldDex(context, path);
                }
            }
        }
    }

    private static void setPathClassLoader() {
        loader = EContextHelper.getContext(null).getClassLoader();
    }

    private static void setAnalClassloader(final Context context, String path) {
        loader = new AnalysysClassLoader(path, context.getCacheDir().getAbsolutePath(), null, context.getClassLoader(), new AnalysysClassLoader.Callback() {
            @Override
            public void onSelfNotFound(String name) {
                //入口类一定能自己找到,如果找不到,则一定是这个dex损坏了
                if (MYCLASS_NAME.contains(name)) {
                    dexError(context);
                    if (EGContext.DEBUG_HF) {
                        ELOG.v(EGContext.HOT_FIX_TAG, "[DEX损坏]:" + name + "[not found]");
                    }
                }
            }

            @Override
            public void onLoadBySelf(String name) {
            }

            @Override
            public void onLoadByCache(String name) {

            }

            @Override
            public void onLoadByParent(String name) {

            }

            @Override
            public void onNotFound(String name) {

            }
        });
    }

    private static void deleteOldDex(Context context, String path) {
        try {
            if (ProcessUtils.getCurrentProcessName(context).equals(context.getPackageName())) {
                String dirPath = context.getFilesDir().getAbsolutePath() + EGContext.HOTFIX_CACHE_DIR;
                File[] files = new File(dirPath).listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return name.endsWith(".dex");
                    }
                });
                if (files == null) {
                    return;
                }
                for (File file : files) {
                    if (TextUtils.isEmpty(path)) {
                        boolean b = file.delete();
                        ELOG.i(EGContext.HOT_FIX_TAG, "删除旧dex:" + file.getAbsolutePath() + " result:" + b);
                        continue;
                    }
                    if (!path.contains(file.getName())) {
                        boolean b = file.delete();
                        ELOG.i(EGContext.HOT_FIX_TAG, "删除旧dex:" + file.getAbsolutePath() + " result:" + b);
                    }

                }
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
        }
    }

    private static boolean hasDexFile(String path) {
        boolean hasdex = path != null && !path.equals("") && new File(path).isFile();
        if (hasdex) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.i(EGContext.HOT_FIX_TAG, "dex 存在 path = " + path);
            }
        } else {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.i(EGContext.HOT_FIX_TAG, "dex 不存在 path = " + path);
            }
        }
        return hasdex;
    }

    private static volatile boolean isinit = false;

    private static boolean isInit() {
        return isinit;
    }

    private static void dexError(Context context) {
        try {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(EGContext.HOT_FIX_TAG, "dexError[损坏]");
            }
            EGContext.DEX_ERROR = true;
            //<editor-fold desc="删掉dex文件 其他进程 只改状态 保证删除的时候只有主进程的时候操作，避免主进程和主进程不同步删除出现异常">
            String path = null;
            if (ProcessUtils.getCurrentProcessName(context).equals(context.getPackageName())) {
                path = SPHelper.getStringValueFromSP(EContextHelper.getContext(null), EGContext.HOT_FIX_PATH, "");
                File file = new File(path);
                if (file.exists() && file.isFile()) {
                    file.delete();
                }
            }
            //</editor-fold>
            SPHelper.setStringValue2SP(EContextHelper.getContext(null), EGContext.HOT_FIX_PATH, "");
            //激活状态设置为不激活
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.i(EGContext.HOT_FIX_TAG, "dexError path = " + path);
            }
            SPHelper.setBooleanValue2SP(EContextHelper.getContext(null), EGContext.HOT_FIX_ENABLE_STATE, false);
            //重新设置classloader
            setPathClassLoader();
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
        }
    }

    /**
     * 只能宿主调用该方法
     *
     * @param object
     * @param classname
     * @param methodName
     * @param pram
     * @param <T>
     * @return
     */
    public static <T> T transform(Object object, String classname, String methodName,
                                  Object... pram) throws HotFixTransformCancel {
        canTransForm();

        if (classname == null || methodName == null || classname.length() == 0 || methodName.length() == 0) {
            return null;
        }

        try {
            Class<T> ap = (Class<T>) loader.loadClass(classname);
            Method[] methods = ap.getDeclaredMethods();
            Method method = null;
            if (pram == null || pram.length == 0) {
                method = ap.getDeclaredMethod(methodName);
            } else {
                for (int i = 0; i < methods.length; i++) {
                    if (methods[i].getName().equals(methodName)
                            && isFound(methods[i].getParameterTypes(), pram)) {
                        method = methods[i];
                        break;
                    }
                }
            }

            if (method == null) {
                ELOG.e(EGContext.HOT_FIX_TAG, "[" + classname + "." + methodName + "]" + "No function found corresponding to the parameter type");
            }
            method.setAccessible(true);
            return (T) method.invoke(object, pram);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void canTransForm() throws HotFixTransformCancel {

        if (!EGContext.IS_HOST) {
            throw new HotFixTransformCancel("非宿主 不初始化,不转向");
        }
        if (EGContext.DEX_ERROR) {
            throw new HotFixTransformCancel("dex损坏 不初始化,不转向");
        }
        Context context = EContextHelper.getContext(null);
        if (context == null) {
            throw new HotFixTransformCancel("context == null 不初始化,不转向");
        }

        if (!isInit()) {
            init(context);
        }
        boolean b = SPHelper.getBooleanValueFromSP(context, EGContext.HOT_FIX_ENABLE_STATE, false);
        if (!b) {
            throw new HotFixTransformCancel("未激活 不转向");
        }

        if (!(loader instanceof AnalysysClassLoader)) {
            throw new HotFixTransformCancel("类加载器不对 不转向");
        }
    }

    public static <T> T make(String classname, Object... pram) throws HotFixTransformCancel {

        canTransForm();

        if (classname == null || classname.length() == 0) {
            return null;
        }
        try {
            Class<T> ap = (Class<T>) loader.loadClass(classname);
            Constructor<T>[] constructors = (Constructor<T>[]) ap.getDeclaredConstructors();
            Constructor<T> constructor = null;
            if (pram == null || pram.length == 0) {
                constructor = ap.getConstructor();
            } else {
                for (Constructor<T> constructor1 : constructors) {
                    Class[] aClass = constructor1.getParameterTypes();
                    //识别是不是正确的构造方法
                    if (isFound(aClass, pram)) {
                        constructor = constructor1;
                        break;
                    }
                }
            }

            if (constructor == null) {
                ELOG.e(EGContext.HOT_FIX_TAG, "[" + classname + "]" + "not has parameter type constructor,if this is a innerClass");
            }
            constructor.setAccessible(true);
            T o = constructor.newInstance(pram);
            return o;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据参数类型判断是否跟Class[] 是从属关系
     *
     * @param aClass
     * @param pram
     * @return
     */
    private static boolean isFound(Class[] aClass, Object[] pram) {
        if (aClass == null || pram == null) {
            return false;
        }
        if (aClass.length != pram.length) {
            return false;
        }
        for (int j = 0; j < aClass.length; j++) {
            if (pram[j] == null) {
                continue;
            }

            List<String> baseClassList = getBaseClass(pram[j].getClass());
            if (!baseClassList.contains(aClass[j].getName())) {
                return false;
            }
        }
        return true;
    }

    private static List<String> getBaseClass(Class clazz) {
        List<String> result = new LinkedList<>();
        result.addAll(getSuperClass(clazz));
        result.addAll(getInterfaces(clazz));
        return result;
    }

    private static List<String> getInterfaces(Class clazz) {
        List<String> result = new LinkedList<>();
        if (clazz == null) {
            return result;
        }
        Class[] classes = clazz.getInterfaces();
        for (int i = 0; i < classes.length; i++) {
            result.add(classes[i].getName());
        }
        for (int i = 0; i < classes.length; i++) {
            result.addAll(getInterfaces(classes[i]));
        }
        return result;
    }

    private static List<String> getSuperClass(Class clazz) {
        List<String> result = new LinkedList<>();
        while (clazz != null) {
            String name = mapMemberClass.get(clazz);
            if (name != null) {
                result.add(name);
            }
            result.add(clazz.getName());
            clazz = clazz.getSuperclass();
        }
        return result;
    }
}
