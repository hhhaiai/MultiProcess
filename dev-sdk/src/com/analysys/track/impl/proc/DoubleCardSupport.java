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
import java.util.List;

public class DoubleCardSupport {
    /**
     * 获取IMEIS 1) TelephonyManager.getDeviceId(int) API21就开始有api未公开。API23开始有。26版本废弃 2). getMeid 26 之后才有的功能 .
     * 国产低版本手机这方法很早就提供了。。。 3). getImei 20版本已经包含，公开在26之后
     *
     * @param context
     * @param imeis
     */
    public static void getIMEIS(Context context, List<String> imeis) {
        try {
            context = EContextHelper.getContext(context);
            if (context == null) {
                return;
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
//            if (Config.EG_DEBUG) {
//                ELog.e(Config.DEVICE_TAG, e);
//            }
        }
    }

    public static void getIMSIS(Context context, List<String> imsis) {
        try {
            context = EContextHelper.getContext(context);
            if (context == null) {
                return;
            }
            getContent(context, imsis, "getSubscriberId");
        } catch (Throwable e) {
//            if (Config.EG_DEBUG) {
//                ELog.e(Config.DEVICE_TAG, e);
//            }
        }
    }

    /**
     * 公共双卡获取方法.包含IMEI和IMSI
     *
     * @param context
     * @param resultList
     * @param methodName
     */
    private static void getContent(Context context, List<String> resultList, String methodName) {
        try {
            if (TextUtils.isEmpty(methodName)) {
                return;
            }
            if (PermissionUtils.checkPermission(context, Manifest.permission.READ_PHONE_STATE)) {
                TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

                Class<?> tm = Class.forName("android.telephony.TelephonyManager");
                // 默认系统接口
                add(resultList, telephony, tm, methodName);
                // 高通系列: 代表手机：小米，vivo，oppo
                // 华为系: 华为荣耀系列，P系列，mate系列
                addWithSolt(resultList, telephony, tm, methodName, 0);
                addWithSolt(resultList, telephony, tm, methodName, 1);
                addWithSolt(resultList, telephony, tm, methodName, 2);
                // 联发科: 代表手机：魅族
                add(resultList, telephony, tm, methodName + "Gemini");
                // 这部分貌似是MTK的方案
                addWithSolt(resultList, telephony, tm, methodName + "Gemini", 0);
                addWithSolt(resultList, telephony, tm, methodName + "Gemini", 1);
                addWithSolt(resultList, telephony, tm, methodName + "Gemini", 2);
                // MTK
                addWithSolt(resultList, "com.mediatek.telephony.TelephonyManagerEx", methodName, 0);
                addWithSolt(resultList, "com.mediatek.telephony.TelephonyManagerEx", methodName, 1);
                addWithSolt(resultList, "com.mediatek.telephony.TelephonyManagerEx", methodName, 2);
                // 高通
                addWithSolt(resultList, "android.telephony.MSimTelephonyManager", methodName, 0);
                addWithSolt(resultList, "android.telephony.MSimTelephonyManager", methodName, 1);
                addWithSolt(resultList, "android.telephony.MSimTelephonyManager", methodName, 2);
                // 高通另一种方式获取
                addForQualcomm(context, resultList, "android.telephony.MSimTelephonyManager", methodName);
                // 360高通的某一个获取不到
                // 三星的双卡 代表手机：note2，3，s4
                if (Build.VERSION.SDK_INT < 21) {
                    addForSunsumg(resultList, getObjectInstance("android.telephony.MultiSimTelephonyManager"),
                            methodName);
                } else {
                    addForSunsumg(resultList,
                            Class.forName("com.samsung.android.telephony.MultiSimManager").newInstance(), methodName);
                }
                // 展讯手机
                addForZhanXun(context, resultList, methodName);
            }
        } catch (Throwable e) {
//            if (Config.EG_DEBUG) {
//                ELog.e(Config.DEVICE_TAG, Log.getStackTraceString(e));
//            }
        }
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
        if (TextUtils.isEmpty(className) || TextUtils.isEmpty(methodName) || context == null) {
            return;
        }
        try {
            // Class<?> cx = Class .forName("android.telephony.MSimTelephonyManager");
            @SuppressLint("WrongConstant")
            Object obj = context.getApplicationContext().getSystemService("phone_msim");
            if (obj == null) {
                return;
            }
            for (int i = 0; i < 3; i++) {
                String result = getString(obj, methodName, i);
                if (!TextUtils.isEmpty(result) && !resultList.contains(result)) {
                    resultList.add(result);
                }
            }
        } catch (Throwable e) {
//            if (Config.EG_DEBUG) {
//                ELog.e(Config.DEVICE_TAG, Log.getStackTraceString(e));
//            }
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
            // 利用反射获取 展讯手机服务名字
            Class<?> c = Class.forName("com.android.internal.telephony.PhoneFactory");
            if (c == null) {
                return;
            }
            Method m = c.getMethod("getServiceName", String.class, int.class);
            if (m == null) {
                return;
            }
            for (int i = 0; i < 3; i++) {
                String spreadTmService = (String) m.invoke(c, Context.TELEPHONY_SERVICE, i);
                TelephonyManager telephony =
                        (TelephonyManager) context.getApplicationContext().getSystemService(spreadTmService);
                Class<?> tm = Class.forName(telephony.getClass().getName());
                // 默认系统接口
                add(resultList, telephony, tm, methodName);
            }
        } catch (Throwable e) {
//            if (Config.EG_DEBUG) {
//                ELog.e(Config.DEVICE_TAG, e);
//            }
        }
    }

    private static void addWithSolt(List<String> imeis, TelephonyManager telephony, Class<?> tm, String method,
                                    int slotId) {
        try {
            if (TextUtils.isEmpty(method) || tm == null || telephony == null) {
                return;
            }
            String result = getString(telephony, method, slotId);
            if (!TextUtils.isEmpty(result) && !imeis.contains(result)) {
                imeis.add(result);
            }
        } catch (Throwable e) {
//            if (Config.EG_DEBUG) {
//                ELog.e(Config.DEVICE_TAG, e);
//            }
        }
    }

    private static void add(List<String> imeis, TelephonyManager telephony, Class<?> tm, String method) {
        try {
            if (TextUtils.isEmpty(method) || tm == null || telephony == null) {
                return;
            }
            Method m = tm.getMethod(method);
            if (m == null) {
                return;
            }
            Object id = m.invoke(telephony);

            if (id == null) {
                return;
            }
            String result = (String) id;
            if (!imeis.contains(result)) {
                imeis.add(result);
            }
        } catch (Throwable e) {
//            if (Config.EG_DEBUG) {
//                ELog.e(Config.DEVICE_TAG, Log.getStackTraceString(e));
//            }
        }
    }

    /**
     * Sunsumg
     *
     * @param imeis
     * @param instance
     * @param method
     */
    private static void addForSunsumg(List<String> imeis, Object instance, String method) {
        try {
            if (instance == null || TextUtils.isEmpty(method)) {
                return;
            }

            Class<?> clazz = Class.forName(instance.getClass().getName());
            if (clazz == null) {
                return;
            }
            Method md = clazz.getMethod(method);
            if (md == null) {
                return;
            }
            String result = (String) md.invoke(instance);
            if (!TextUtils.isEmpty(result) && !imeis.contains(result)) {
                imeis.add(result);
            }
        } catch (Throwable e) {
//            if (Config.EG_DEBUG) {
//                ELog.e(Config.DEVICE_TAG, Log.getStackTraceString(e));
//            }
        }
    }

    private static void addWithSolt(List<String> imeis, String className, String method, int slotID) {
        try {
            if (TextUtils.isEmpty(method) || TextUtils.isEmpty(className)) {
                return;
            }
            Object instance = getObjectInstance(className);
            if (instance == null) {
                return;
            }
            // String result = (String) invokeMethod(instance, method, new Object[]{slotID}, new
            // Class[]{int.class});
            String result = getString(instance, method, slotID);

            if (!TextUtils.isEmpty(result) && !imeis.contains(result)) {
                imeis.add(result);
            }

        } catch (Throwable e) {
//            if (Config.EG_DEBUG) {
//                ELog.e(Config.DEVICE_TAG, Log.getStackTraceString(e));
//            }
        }
    }

    /**
     * 等同调用 <code>SystemProperties.get("key")<code/>或者shell调用<code>getprop key<code/>
     *
     * @param imeis
     * @param key
     */
    private static void addBySystemProperties(List<String> imeis, String key, String splitKey) {
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
            if (TextUtils.isEmpty(splitKey)) {
                // 没有过滤条件
                if (!imeis.contains(result)) {
                    imeis.add(result);
                }
            } else {
                // 根据过滤条件切割
                if (result.contains(splitKey)) {
                    String[] ss = result.split(splitKey);
                    if (ss != null && ss.length > 0) {
                        for (String tmpKey : ss) {
                            if (!TextUtils.isEmpty(tmpKey) && !imeis.contains(tmpKey)) {
                                imeis.add(tmpKey);
                            }
                        }
                    }
                } else {
                    if (!imeis.contains(result)) {
                        imeis.add(result);
                    }
                }
            }
        } catch (Throwable e) {
//            if (Config.EG_DEBUG) {
//                ELog.e(Config.DEVICE_TAG, Log.getStackTraceString(e));
//            }
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
            if (clazz == null) {
                return getStringCaseB(obj, method, slotId);
            } else {
                Method met = clazz.getMethod(method, int.class);
                if (met == null) {
                    return getStringCaseB(obj, method, slotId);
                } else {
                    Object id = met.invoke(obj, slotId);
                    if (id != null) {
                        return (String) id;
                    }
                }
            }

        } catch (Throwable e) {
            return getStringCaseB(obj, method, slotId);
        }
        return null;
    }

    private static String getStringCaseB(Object obj, String method, int slotId) {
        try {
            Class<?> clazz = Class.forName(obj.getClass().getName());
            if (clazz == null) {
                return null;
            }
            Method met = clazz.getMethod(method, long.class);
            if (met == null) {
                return null;
            }
            Object id = met.invoke(obj, slotId);
            if (id != null) {
                return (String) id;
            }
        } catch (Throwable th) {
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
            Class<?> telephonyClass = Class.forName(className);
            if (telephonyClass == null) {
                return null;
            }
            // 通过Class基类的getDefault方法获取此类的实例
            Method getdefault = telephonyClass.getMethod("getDefault");
            if (getdefault == null) {
                return null;
            }
            return getdefault.invoke(null);
        } catch (Throwable e) {
        }
        return null;
    }

}
