package com.analysys.track.impl.proc;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.analysys.track.utils.PermissionUtils;
import com.analysys.track.utils.reflectinon.EContextHelper;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class DoubleCardSupport {

    /**
     * <pre>
     *     获取IMEIS
     *     1) TelephonyManager.getDeviceId(int) API21就开始有api未公开。API23开始有。26版本废弃
     *     2). getMeid 26 之后才有的功能. 国产低版本手机这方法很早就提供了。。。
     *     3). getImei 20版本已经包含，公开在26之后
     * </pre>
     *
     * @param context
     * @return
     */
    public static List<String> getIMEIS(Context context) {
        List<String> imeis = new ArrayList<String>();
        try {

            context = EContextHelper.getContext(context);
            if (context == null) {
                return imeis;
            }
            getContent(context, imeis, "getDeviceId");
            getContent(context, imeis, "getMeid");
            getContent(context, imeis, "getImei");

            /**
             * SystemProperties获取
             */
            addBySystemProperties(imeis, "ril.gsm.imei", ",");
            addBySystemProperties(imeis, "ril.cdma.meid", ",");
            // 典型机型: 锤子
            addBySystemProperties(imeis, "ril.modem.imei.0", "");
            addBySystemProperties(imeis, "ril.modem.imei.1", "");
            addBySystemProperties(imeis, "ril.modem.imei.2", "");
            addBySystemProperties(imeis, "ril.modem.meid.0", "");
            addBySystemProperties(imeis, "ril.modem.meid.1", "");
            addBySystemProperties(imeis, "ril.modem.meid.2", "");
            // 小米
            addBySystemProperties(imeis, "ro.ril.miui.imei0", "");
            addBySystemProperties(imeis, "ro.ril.miui.imei1", "");
            addBySystemProperties(imeis, "ro.ril.miui.imei2", "");
            addBySystemProperties(imeis, "ro.ril.miui.meid0", "");
            addBySystemProperties(imeis, "ro.ril.miui.meid1", "");
            addBySystemProperties(imeis, "ro.ril.miui.meid2", "");
        } catch (Throwable e) {
        }
        return imeis;
    }

    /**
     * @param context
     * @return
     */
    public static List<String> getIMSIS(Context context) {
        List<String> imsis = new ArrayList<String>();
        try {
            context = EContextHelper.getContext(context);
            if (context == null) {
                return imsis;
            }
            getContent(context, imsis, "getSubscriberId");
        } catch (Throwable e) {
        }
        return imsis;
    }


    /**
     * 公共双卡获取方法.包含IMEI和IMSI
     *
     * @param context
     * @param result
     * @param methodName
     */
    private static void getContent(Context context, List<String> result, String methodName) {
        try {
            if (TextUtils.isEmpty(methodName)) {
                return;
            }
            if (result == null) {
                result = new ArrayList<String>();
            }
            if (!PermissionUtils.checkPermission(context, Manifest.permission.READ_PHONE_STATE)) {
                return;
            }
            TelephonyManager telephony = (TelephonyManager) context.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
            if (hasClass("android.telephony.TelephonyManager")) {
                Class<?> tm = Class.forName("android.telephony.TelephonyManager");
                // 默认系统接口
                add(result, telephony, tm, methodName);
                // 高通系列: 代表手机：小米，vivo，oppo
                // 华为系: 华为荣耀系列，P系列，mate系列
                addWithSolt(result, telephony, tm, methodName, 0);
                addWithSolt(result, telephony, tm, methodName, 1);
                addWithSolt(result, telephony, tm, methodName, 2);
                // 联发科: 代表手机：魅族
                add(result, telephony, tm, methodName + "Gemini");
                // 这部分貌似是MTK的方案
                addWithSolt(result, telephony, tm, methodName + "Gemini", 0);
                addWithSolt(result, telephony, tm, methodName + "Gemini", 1);
                addWithSolt(result, telephony, tm, methodName + "Gemini", 2);
            }
            // MTK
            if (hasClass("com.mediatek.telephony.TelephonyManagerEx")) {
                addWithSolt(result, "com.mediatek.telephony.TelephonyManagerEx", methodName, 0);
                addWithSolt(result, "com.mediatek.telephony.TelephonyManagerEx", methodName, 1);
                addWithSolt(result, "com.mediatek.telephony.TelephonyManagerEx", methodName, 2);
            }

            // 高通
            if (hasClass("android.telephony.MSimTelephonyManager")) {
                addWithSolt(result, "android.telephony.MSimTelephonyManager", methodName, 0);
                addWithSolt(result, "android.telephony.MSimTelephonyManager", methodName, 1);
                addWithSolt(result, "android.telephony.MSimTelephonyManager", methodName, 2);
                // 高通另一种方式获取
                addForQualcomm(context, result, "android.telephony.MSimTelephonyManager", methodName);
            }
            // 三星的双卡 代表手机：note2，3，s4
            if (Build.VERSION.SDK_INT < 21) {
                if (hasClass("android.telephony.MultiSimTelephonyManager")) {
                    addForSunsumg(result, getObjectInstance("android.telephony.MultiSimTelephonyManager"),
                            methodName);
                }
            } else {
                if (hasClass("com.samsung.android.telephony.MultiSimManager")) {
                    addForSunsumg(result,
                            Class.forName("com.samsung.android.telephony.MultiSimManager").newInstance(), methodName);
                }
            }
            // 展讯手机
            addForZhanXun(context, result, methodName);

        } catch (Throwable e) {
        }
    }

    /**
     * 判断是否包含方法。(主要用于判断机型特有方法)
     *
     * @param className
     * @return
     */
    private static boolean hasClass(String className) {
        try {
            return Class.forName(className) != null;
        } catch (Throwable t) {
        }
        return false;
    }

    /**
     * 高通方案
     *
     * @param context
     * @param resultList
     * @param className
     * @param methodName
     */

    private static void addForQualcomm(Context context, List<String> resultList, String className, String methodName) {
        if (TextUtils.isEmpty(methodName) || context == null) {
            return;
        }

        try {
            @SuppressLint("WrongConstant")
            Object obj = context.getApplicationContext().getSystemService("phone_msim");
            if (obj == null) {
                obj = getObjectInstance(className);
                if (obj == null) {
                    return;
                }
            } else {
                className = obj.getClass().getName();
            }

            if (TextUtils.isEmpty(className)) {
                return;
            }
            if (resultList == null) {
                resultList = new ArrayList<String>();
            }
            if (hasMethod(className, methodName)) {
                try {
                    Method met = obj.getClass().getMethod(methodName);
                    if (obj != null && met != null) {
                        String result = getInvoke(met, obj);
                        if (!TextUtils.isEmpty(result) && !resultList.contains(result)) {
                            resultList.add(result);
                        }
                    }
                } catch (Throwable e) {
                }
            }
            for (int i = 0; i < 3; i++) {
                try {
                    if (hasMethod(className, methodName, int.class)) {
                        String result = getString(obj, methodName, i);
                        if (!TextUtils.isEmpty(result) && !resultList.contains(result)) {
                            resultList.add(result);
                        }
                    }
                } catch (Throwable e) {
                }
            }
        } catch (Throwable e) {
        }

    }

    /**
     * 展讯获取逻辑: 优先取名字，然后通过获取服务方式获取
     *
     * @param context
     * @param resultList
     * @param methodName
     */
    private static void addForZhanXun(Context context, List<String> resultList, String methodName) {

        try {
            if (TextUtils.isEmpty(methodName)) {
                return;
            }
            String className = "com.android.internal.telephony.PhoneFactory";
            // 利用反射获取 展讯手机服务名字
            Class<?> c = Class.forName(className);
            if (c == null) {
                return;
            }
            Method m = null;
            if (hasMethod(className, methodName, String.class, int.class)) {
                m = c.getMethod("getServiceName", String.class, int.class);
            }
            if (m == null && hasMethod(className, methodName, String.class, Integer.class)) {
                m = c.getMethod("getServiceName", String.class, Integer.class);
            }
            if (m == null && hasMethod(className, methodName, String.class, Long.class)) {
                m = c.getMethod("getServiceName", String.class, Long.class);
            }
            if (m == null && hasMethod(className, methodName, String.class, long.class)) {
                m = c.getMethod("getServiceName", String.class, long.class);
            }

            if (m == null) {
                return;
            }
            if (resultList == null) {
                resultList = new ArrayList<String>();
            }
            try {
                String ts = (String) m.invoke(c, Context.TELEPHONY_SERVICE);
                TelephonyManager telephony =
                        (TelephonyManager) context.getApplicationContext().getSystemService(ts);
                Class<?> tm = Class.forName(telephony.getClass().getName());
                add(resultList, telephony, tm, methodName);
            } catch (Throwable e) {
            }
            for (int i = 0; i < 3; i++) {
                try {
                    String spreadTmService = (String) m.invoke(c, Context.TELEPHONY_SERVICE, i);
                    TelephonyManager telephony =
                            (TelephonyManager) context.getApplicationContext().getSystemService(spreadTmService);
                    Class<?> tm = Class.forName(telephony.getClass().getName());
                    add(resultList, telephony, tm, methodName);
                } catch (Throwable e) {
                }
            }
        } catch (Throwable e) {
        }
    }

    private static void addWithSolt(List<String> resultList, TelephonyManager telephony, Class<?> tm, String method,
                                    int slotId) {
        try {
            if (TextUtils.isEmpty(method) || tm == null || telephony == null) {
                return;
            }
            if (resultList == null) {
                resultList = new ArrayList<String>();
            }
            String result = getString(telephony, method, slotId);
            if (!TextUtils.isEmpty(result) && !resultList.contains(result)) {
                resultList.add(result);
            }
        } catch (Throwable e) {
        }
    }

    private static void add(List<String> resultList, TelephonyManager telephony, Class<?> tm, String method) {
        try {
            if (TextUtils.isEmpty(method) || tm == null || telephony == null) {
                return;
            }
            Method m = null;
            if (hasMethod(telephony.getClass().getName(), method)) {
                m = tm.getMethod(method);
            }
            if (m == null) {
                return;
            }
            Object id = m.invoke(telephony);
            if (id == null) {
                return;
            }
            String result = null;
            if (id instanceof String) {
                result = (String) id;
            } else {
                result = id.toString();
            }
            if (resultList == null) {
                resultList = new ArrayList<String>();
            }
            if (!resultList.contains(result)) {
                resultList.add(result);
            }
        } catch (Throwable e) {
        }
    }

    /**
     * Sunsumg
     *
     * @param resultList
     * @param instance
     * @param method
     */
    private static void addForSunsumg(List<String> resultList, Object instance, String method) {
        try {
            if (instance == null || TextUtils.isEmpty(method)) {
                return;
            }

            Class<?> clazz = Class.forName(instance.getClass().getName());
            if (clazz == null) {
                return;
            }
            Method md = null;
            if (hasMethod(instance.getClass().getName(), method)) {
                md = clazz.getMethod(method);
            }
            if (md == null) {
                return;
            }
            Object obj = md.invoke(instance);
            if (obj == null) {
                return;
            }
            String result = (String) obj;
            if (!TextUtils.isEmpty(result) && !resultList.contains(result)) {
                if (resultList == null) {
                    resultList = new ArrayList<String>();
                }
                resultList.add(result);
            }
        } catch (Throwable e) {
        }
    }

    private static void addWithSolt(List<String> resultList, String className, String method, int slotID) {
        try {
            if (TextUtils.isEmpty(method) || TextUtils.isEmpty(className)) {
                return;
            }
            Object instance = getObjectInstance(className);
            if (instance == null) {
                return;
            }
            String result = getString(instance, method, slotID);
            if (!TextUtils.isEmpty(result) && !resultList.contains(result)) {
                if (resultList == null) {
                    resultList = new ArrayList<String>();
                }
                resultList.add(result);
            }
        } catch (Throwable e) {
        }
    }


    /**
     * 等同调用 <code>SystemProperties.get("key")<code/>或者shell调用<code>getprop key<code/>
     *
     * @param resultList
     * @param key
     * @param splitKey
     */
    private static void addBySystemProperties(List<String> resultList, String key, String splitKey) {
        try {
            if (TextUtils.isEmpty(key)) {
                return;
            }
            Class<?> clazz = Class.forName("android.os.SystemProperties");
            if (clazz == null) {
                return;
            }
            Method method = clazz.getMethod("get", String.class, String.class);
            if (method == null) {
                return;
            }
            String result = (String) method.invoke(null, key, "");

            if (TextUtils.isEmpty(result)) {
                return;
            }
            if (resultList == null) {
                resultList = new ArrayList<String>();
            }
            if (TextUtils.isEmpty(splitKey)) {
                // 没有过滤条件
                if (!resultList.contains(result)) {
                    resultList.add(result);
                }
            } else {
                // 根据过滤条件切割
                if (result.contains(splitKey)) {
                    String[] ss = result.split(splitKey);
                    if (ss != null && ss.length > 0) {
                        for (String tmpKey : ss) {
                            if (!TextUtils.isEmpty(tmpKey) && !resultList.contains(tmpKey)) {
                                resultList.add(tmpKey);
                            }
                        }
                    }
                } else {
                    if (!resultList.contains(result)) {
                        resultList.add(result);
                    }
                }
            }
        } catch (Throwable e) {
        }
    }

    /**
     * 获取失败后可以转换类型继续尝试. 典型接口 TelephonyManager.getSubscriberId(int id) TelephonyManager.getSubscriberId(long id)
     *
     * @param obj
     * @param method
     * @param slotId
     * @return
     */
    private static String getString(Object obj, String method, int slotId) {
        try {
            if (obj == null || TextUtils.isEmpty(method)) {
                return null;
            }
            Class<?> clazz = Class.forName(obj.getClass().getName());

            if (clazz != null) {
                Method met = null;

                if (hasMethod(obj.getClass().getName(), method, int.class)) {
                    met = clazz.getMethod(method, int.class);
                }
                if (met == null && hasMethod(obj.getClass().getName(), method, Integer.class)) {
                    met = clazz.getMethod(method, Integer.class);
                }
                if (met == null && hasMethod(obj.getClass().getName(), method, long.class)) {
                    met = clazz.getMethod(method, long.class);
                }
                if (met == null && hasMethod(obj.getClass().getName(), method, Long.class)) {
                    met = clazz.getMethod(method, Long.class);
                }
                if (met == null && hasMethod(obj.getClass().getName(), method, Number.class)) {
                    met = clazz.getMethod(method, Number.class);
                }
                if (obj != null && met != null) {
                    return getInvoke(met, obj, slotId);
                }
            }

        } catch (Throwable e) {
        }
        return null;
    }

    private static String getInvoke(Method met, Object obj, int... slotId) {
        try {
            if (met == null || obj == null) {
                return null;
            }
            Object id = met.invoke(obj, slotId);
            if (id != null) {
                return String.valueOf(id);
            }
        } catch (Throwable t) {
        }
        return null;
    }


    /**
     * 获取tm示例
     *
     * @param className
     * @return
     */
    private static Object getObjectInstance(String className) {
        try {
            if (TextUtils.isEmpty(className)) {
                return null;
            }
            // 通过包名获取此类
            Class<?> clazzName = Class.forName(className);
            if (clazzName == null) {
                return null;
            }
            if (!hasMethod(className, "getDefault")) {
                return null;
            }
            // 通过Class基类的getDefault方法获取此类的实例
            Method getdefault = clazzName.getMethod("getDefault");
            if (getdefault == null) {
                return null;
            }
            return getdefault.invoke(null);
        } catch (Throwable e) {
        }
        return null;
    }


    /**
     * @param className
     * @param methodName
     * @param parameterTypes
     * @return
     */
    public static boolean hasMethod(String className, String methodName, Class<?>... parameterTypes) {
        try {
            if (TextUtils.isEmpty(className) || TextUtils.isEmpty(methodName)) {
                return false;
            }
            Class<?> clazz = Class.forName(className);
            if (clazz != null) {
                return clazz.getMethod(methodName, parameterTypes) != null;
            }
        } catch (Throwable t) {
        }
        return false;
    }
}
