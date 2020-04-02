package com.analysys.track.impl;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.analysys.track.BuildConfig;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.utils.BugReportForTest;
import com.analysys.track.utils.EContextHelper;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.FileUitls;
import com.analysys.track.utils.ProcessUtils;
import com.analysys.track.utils.reflectinon.ClazzUtils;
import com.analysys.track.utils.sp.SPHelper;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
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
public class CusHotTransform {
    private final static HashMap<Class, String> mapMemberClass = new HashMap<Class, String>();
    //放入入口类路径,用于判断dex包是不是损坏
    private final static HashSet<String> MYCLASS_NAME = new HashSet<String>();
    private static volatile Object loader;
    private static volatile boolean isinit = false;

    static {
        mapMemberClass.put(Integer.class, "int");
        mapMemberClass.put(Double.class, "double");
        mapMemberClass.put(Float.class, "float");
        mapMemberClass.put(Character.class, "char");
        mapMemberClass.put(Boolean.class, "boolean");
        mapMemberClass.put(Short.class, "short");
        mapMemberClass.put(Long.class, "long");
        mapMemberClass.put(Byte.class, "byte");

//       // 验证dex中是否包含该类。
//        MYCLASS_NAME.add("com.analysys.track.AnalysysTracker");
//        MYCLASS_NAME.add("com.analysys.track.service.AnalysysAccessibilityService");
//        MYCLASS_NAME.add("com.analysys.track.service.AnalysysJobService");
//        MYCLASS_NAME.add("com.analysys.track.service.AnalysysService");
//        MYCLASS_NAME.add("com.analysys.track.receiver.AnalysysReceiver");
    }


    /**
     * @param isNeedMakeObj
     * @param classname
     * @param methodName
     * @param pram
     * @param <T>
     * @return
     * @throws Exception
     */
    public static <T> T transform(boolean isNeedMakeObj, String classname, String methodName, Object... pram) throws Exception {
        canTransForm();
        if (classname == null || methodName == null || classname.length() == 0 || methodName.length() == 0) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(new Exception(
                        "[HotFixTransform transform error]" + classname + "," + methodName));
            }
            throw new Exception("classname or methodName is empty~");
        }
        if (isNeedMakeObj) {
            return transform(make(classname), classname, methodName, pram);
        } else {
            return transform(null, classname, methodName, pram);
        }
    }

    /**
     * 逻辑转发方法,由宿主转到热修复dex
     *
     * @param object
     * @param classname
     * @param methodName
     * @param pram
     * @param <T>
     * @return
     */
    private static <T> T transform(Object object, String classname, String methodName,
                                   Object... pram) throws Exception {
//        canTransForm();
//        if (classname == null || methodName == null || classname.length() == 0 || methodName.length() == 0) {
//            if (BuildConfig.ENABLE_BUG_REPORT) {
//                BugReportForTest.commitError(new Exception(
//                        "[HotFixTransform transform error]" + classname + "," + methodName));
//            }
//            return null;
//        }

        try {
//            Class<T> ap = (Class<T>) loader.loadClass(classname);
            Class ap = (Class) ClazzUtils.invokeObjectMethod(loader, "loadClass", new Class[]{String.class}, new Object[]{classname});
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
                ELOG.e(BuildConfig.tag_hotfix, "[" + classname + "." + methodName + "]" + "No function found corresponding to the parameter type");
            }
            method.setAccessible(true);
            return (T) method.invoke(object, pram);
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        }
        return null;
    }

    private static <T> T make(String classname, Object... pram) throws Exception {
//        canTransForm();
//        if (classname == null || classname.length() == 0) {
//            return null;
//        }
        try {
//            Class<T> ap = (Class<T>) loader.loadClass(classname);
            Class ap = (Class) ClazzUtils.invokeObjectMethod(loader, "loadClass", new Class[]{String.class}, new Object[]{classname});
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
                ELOG.e(BuildConfig.tag_hotfix, "[" + classname + "]" + "not has parameter type constructor,if this is a innerClass");
            }
            constructor.setAccessible(true);
            T o = constructor.newInstance(pram);
            return o;
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        }
        return null;
    }

    private static void canTransForm() throws Exception {
        if (!EGContext.IS_HOST) {
            throw new Exception("非宿主 不初始化,不转向");
        }
        if (EGContext.DEX_ERROR) {
            throw new Exception("dex损坏 不初始化,不转向");
        }
        Context context = EContextHelper.getContext();
        if (context == null) {
            throw new Exception("context == null 不初始化,不转向");
        }

        if (!isinit) {
            if (BuildConfig.logcat) {
                Log.i(BuildConfig.tag_hotfix, "未初始化");
            }
            init(context);
        }
        boolean b = SPHelper.getBooleanValueFromSP(context, EGContext.HOT_FIX_ENABLE_STATE, false);
        if (!b) {
            throw new Exception("未激活 不转向");
        }

        if (loader == null) {
            throw new Exception("类加载器不对 不转向");
        }
    }

    public static void init(Context context) {
        if (!isinit) {
            synchronized (CusHotTransform.class) {
                if (!isinit) {
                    try {
                        String path = SPHelper.getStringValueFromSP(context, EGContext.HOT_FIX_PATH, "");
                        boolean enable = SPHelper.getBooleanValueFromSP(context, EGContext.HOT_FIX_ENABLE_STATE, false);
                        if (BuildConfig.logcat) {
                            Log.i(BuildConfig.tag_hotfix, "初始化 hf路径:" + path + " ;enable: " + enable);
                        }
                        //热修之前宿主判断
                        if (isSdkUpdateInHost(context)) {
                            //清除短路控制变量
                            List<String> ifs = Arrays.asList("case1", "case2", "case3", "case4", "case_d", "what_dev");
                            SPHelper.removeKeys(context, ifs);
                        }
                        //清除热修相关的（如果未激活或文件不存在或宿主变动）
                        //激活，热修文件存在，宿主一致
                        if (enable && hasDexFile(context, path) && !isSdkUpdateInHost(context)) {
                            setAnalClassloader(context, path);
                        } else {
                            SPHelper.setBooleanValue2SP(context, EGContext.HOT_FIX_ENABLE_STATE, false);
                            SPHelper.setStringValue2SP(context, EGContext.HOT_FIX_PATH, "");
                            path = null;
                        }
                        isinit = true;
                        //主进程进行清理旧的dex文件
                        deleteOldDex(context, path);
                        //记录当前宿主版本号
                        SPHelper.setStringValue2SP(context, EGContext.HOT_FIX_HOST_VERSION, EGContext.SDK_VERSION);
                    } catch (Throwable e) {
                    }
                }
            }
        }
    }

    /**
     * app 集成的SDK版本是否改变。
     *
     * @param context
     * @return
     */
    private static boolean isSdkUpdateInHost(Context context) {
        String hostV = SPHelper.getStringValueFromSP(context, EGContext.HOT_FIX_HOST_VERSION, "");
        if (TextUtils.isEmpty(hostV)) {
            if (BuildConfig.logcat) {
                Log.d(BuildConfig.tag_hotfix, "热修宿主没变");
            }
            return false;
        }
        if (!hostV.equals(EGContext.SDK_VERSION)) {
            if (BuildConfig.logcat) {
                Log.d(BuildConfig.tag_hotfix, "热修宿主变化【清除所有的旧热修dex包，清除所有的patch，清除短路变量控制】");
            }
            return true;
        } else {
            if (BuildConfig.logcat) {
                Log.d(BuildConfig.tag_hotfix, "热修宿主没变");
            }
            return false;
        }

    }

    private static void setAnalClassloader(final Context context, String path) {
        try {
            if (BuildConfig.enableHotFix && !BuildConfig.IS_HOST) {
                //宿主不包换对于热修复类的引用，打包的时候没有此类
                abeg0.class.getName();
            }
            Object dexClassLoader = ClazzUtils.getDexClassLoader(context, path);
            Class analysysThisClazz = (Class) ClazzUtils.invokeObjectMethod(dexClassLoader, "loadClass",
                    new Class[]{String.class}, new Object[]{"com.analysys.track.impl.abeg0"});
            // 下发的dex不包含这个类
            if (analysysThisClazz == null) {
                dexError(context);
                return;
            }
            Class<?> clazzLoader = ClazzUtils.getClass("java.lang.ClassLoader");
            if (clazzLoader != null) {
                loader = ClazzUtils.newInstance(analysysThisClazz,
                        new Class[]{String.class, String.class, String.class, clazzLoader
                        },
                        new Object[]{path, context.getCacheDir().getAbsolutePath(), null, ClazzUtils.
                                invokeObjectMethod(context, "getClassLoader")
                        });
            }

            if (BuildConfig.logcat) {
                Log.i(BuildConfig.tag_hotfix, "热修包应用成功:" + path);
            }
        } catch (Throwable e) {
        }
    }

    public static void deleteOldDex(Context context, String path) {
        try {
            if (ProcessUtils.isMainProcess(context)) {
                String dirPath = context.getFilesDir().getAbsolutePath() + EGContext.HOTFIX_CACHE_HOTFIX_DIR;

                if (TextUtils.isEmpty(path)) {
                    FileUitls.getInstance(context).deleteFile(new File(dirPath));
                    if (BuildConfig.logcat) {
                        Log.i(BuildConfig.tag_hotfix, "删除旧dex和odex等文件:" + dirPath);
                    }
                    return;
                }

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
                    if (!path.contains(file.getName())) {
                        boolean b = file.delete();
                        if (BuildConfig.logcat) {
                            Log.i(BuildConfig.tag_hotfix, "删除旧dex:" + file.getAbsolutePath() + " result:" + b);
                        }
                    }
                }
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        }
    }

    private static boolean hasDexFile(Context context, String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }

//        new File(path).exists() &&new File(path).isFile();
        if (new File(path).isFile()) {
            if (BuildConfig.logcat) {
                Log.i(BuildConfig.tag_hotfix, "dex 存在 path = " + path);
            }
            return true;
        } else {
            if (BuildConfig.logcat) {
                Log.i(BuildConfig.tag_hotfix, "dex 不存在 path = " + path);
            }
            if (BuildConfig.logcat) {
                Log.i(BuildConfig.tag_hotfix, "dex path 存在 文件实际不存在【清除策略号】下次重新获取" + path);
            }
            //需要考虑文件被删除，sp有文件名
//            SPHelper.removeKey(context, UploadKey.Response.RES_POLICY_VERSION);
//            clearHotFixPolicyVersion(context);
            return false;
        }
    }


    public static void dexError(Context context) {
        try {
            if (BuildConfig.logcat) {
                ELOG.e(BuildConfig.tag_hotfix, "dexError[损坏]");
            }
            EGContext.DEX_ERROR = true;
            //<editor-fold desc="删掉dex文件 其他进程 只改状态 保证删除的时候只有主进程的时候操作，避免主进程和主进程不同步删除出现异常">
            String path = null;
            if (ProcessUtils.isMainProcess(context)) {
                path = SPHelper.getStringValueFromSP(EContextHelper.getContext(), EGContext.HOT_FIX_PATH, "");
                File file = new File(path);
                if (file.exists() && file.isFile()) {
                    file.delete();
                }
            }
            //</editor-fold>
            SPHelper.setStringValue2SP(EContextHelper.getContext(), EGContext.HOT_FIX_PATH, "");
            //激活状态设置为不激活
            if (BuildConfig.logcat) {
                Log.i(BuildConfig.tag_hotfix, "dexError path = " + path);
            }
            SPHelper.setBooleanValue2SP(EContextHelper.getContext(), EGContext.HOT_FIX_ENABLE_STATE, false);
            //重新设置classloader
            loader = null;
            if (BuildConfig.logcat) {
                ELOG.e(BuildConfig.tag_hotfix, "dexError[损坏][重置策略版本号]");
            }
//            clearHotFixPolicyVersion(context);
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        }
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

    /********************* get instance begin **************************/
    public static CusHotTransform getInstance(Context context) {
        return HLODER.INSTANCE.initContext(context);
    }

    private CusHotTransform initContext(Context context) {
        if (mContext == null && context != null) {
            mContext = context.getApplicationContext();
        }
        return HLODER.INSTANCE;
    }

    private static class HLODER {
        private static final CusHotTransform INSTANCE = new CusHotTransform();
    }

    private CusHotTransform() {
    }

    private Context mContext = null;
    /********************* get instance end **************************/
//
//    private static boolean isInit() {
//        return isinit;
//    }
//    /**
//     * 清除本地版本
//     *
//     * @param context
//     */
//    private static void clearHotFixPolicyVersion(Context context) {
//        String patchPolicyV = SPHelper.getStringValueFromSP(context, EGContext.PATCH_VERSION_POLICY, "");
//        String curPolicyV = SPHelper.getStringValueFromSP(context, UploadKey.Response.RES_POLICY_VERSION, "");
//
//        if (!TextUtils.isEmpty(curPolicyV) && !patchPolicyV.equals(curPolicyV)) {
//            // not null. current policyversion same as patch version, then clean then
//            SPHelper.removeKey(context, UploadKey.Response.RES_POLICY_VERSION);
//            SPHelper.removeKey(context, EGContext.PATCH_VERSION_POLICY);
//        }
//    }
//
//    public static void getStat() {
//        StackTraceElement[] stackElement = Thread.currentThread().getStackTrace();
//        boolean isWork = false;
//        for (StackTraceElement ste : stackElement) {
//            if (ste.getClassName().equals(HotFixTransform.class.getName())) {
//                isWork = true;
//            }
//        }
//    }

}
