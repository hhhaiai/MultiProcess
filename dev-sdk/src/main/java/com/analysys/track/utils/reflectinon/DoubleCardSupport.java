package com.analysys.track.utils.reflectinon;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.analysys.track.BuildConfig;
import com.analysys.track.utils.PermissionUtils;
import com.analysys.track.utils.SystemUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Copyright © 2019 Analysys Inc. All rights reserved.
 * @Description 双卡获取方案
 * @Version 1.0
 * @Create 2019/3/21 09:40
 * @Author sanbo
 */
public class DoubleCardSupport {
    
    public List<String> getImeiArray(Context context) {
        List<String> imeis = new ArrayList<String>();
        try {
            if (BuildConfig.ENABLE_IMEI) {
                context = EContextHelper.getContext(context);
                if (context != null) {
                    getContent(context, imeis, "getDeviceId");
                    getContent(context, imeis, "getMeid");
                    getContent(context, imeis, "getImei");
                }
            }
//            /**
//             * SystemProperties获取
//             */
//            addBySystemProperties(imeis, "ril.gsm.imei", ",");
//            addBySystemProperties(imeis, "ril.cdma.meid", ",");
//            for (int i = 0; i < 3; i++) {
//                // 典型机型: 锤子
//                addBySystemProperties(imeis, "ril.modem.imei." + i, "");
//                addBySystemProperties(imeis, "ril.modem.meid." + i, "");
//                // 小米
//                addBySystemProperties(imeis, "ro.ril.miui.imei" + i, "");
//                addBySystemProperties(imeis, "ro.ril.miui.meid" + i, "");
//            }
            
            getImeisByShell(imeis);
        } catch (Throwable e) {
        }
        return imeis;
    }
    public String getIMEIS(Context context) {
        List<String> imeis = getImeiArray(context);
        try {
            if (imeis.size() > 0) {
                StringBuffer sb = new StringBuffer();
                for (String imei : imeis) {
                    if (!DEFAULT_VALUE.contains(imei)) {
                        sb.append(imei).append(SPLIT_S);
                    }
                }
                if (sb.length() > SPLIT_S.length()) {
                    sb.deleteCharAt(sb.length() - SPLIT_S.length());
                }
                return sb.toString();
            }
        } catch (Throwable e) {
        }
        return "";
    }
    
    public List<String> getImsisArrays(Context context) {
        List<String> imsis = new ArrayList<String>();
        try {
            if (BuildConfig.ENABLE_IMEI) {
                context = EContextHelper.getContext(context);
                if (context != null) {
                    getContent(context, imsis, "getSubscriberId");
                }
            }
        } catch (Throwable e) {
        }
        return imsis;
    }
    public String getIMSIS(Context context) {
        List<String> imsis = getImsisArrays(context);
        try {
            if (imsis.size() > 0) {
                StringBuffer sb = new StringBuffer();
                for (String imei : imsis) {
                    if (!DEFAULT_VALUE.contains(imei)) {
                        sb.append(imei).append(SPLIT_S);
                    }
                }
                if (sb.length() > SPLIT_S.length()) {
                    sb.deleteCharAt(sb.length() - SPLIT_S.length());
                }
                return sb.toString();
            }
        } catch (Throwable e) {
        }
        return "";
    }

    /**
     * 公共双卡获取方法.包含IMEI和IMSI
     *
     * @param context
     * @param resultList
     * @param methodName
     */
    private void getContent(Context context, List<String> resultList, String methodName) {
        if (BuildConfig.ENABLE_IMEI) {
            try {
                if (TextUtils.isEmpty(methodName)) {
                    return;
                }
                if (PermissionUtils.checkPermission(context, Manifest.permission.READ_PHONE_STATE)) {
                    TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                
                    // 默认系统接口
                    add(resultList, telephony, methodName);
                    // 联发科: 代表手机：魅族
                    add(resultList, telephony, methodName + "Gemini");
                    for (int i = 0; i < 3; i++) {
                        // 这部分貌似是MTK的方案
                        addWithSolt(resultList, telephony, methodName + "Gemini", i);
                        // 高通系列: 代表手机：小米，vivo，oppo
                        // 华为系: 华为荣耀系列，P系列，mate系列
                        addWithSolt(resultList, telephony, methodName, i);
                    
                        if (ClazzUtils.g().getClass("com.mediatek.telephony.TelephonyManagerEx") != null) {
                            // MTK
                            addWithSolt(resultList, "com.mediatek.telephony.TelephonyManagerEx", methodName, i);
                        }
                        if (ClazzUtils.g().getClass("android.telephony.MSimTelephonyManager") != null) {
                            // 高通
                            addWithSolt(resultList, "android.telephony.MSimTelephonyManager", methodName, i);
                        }
                    
                    }
                
                    if (ClazzUtils.g().getClass("android.telephony.MSimTelephonyManager") != null) {
                        // 高通另一种方式获取
                        addForQualcomm(context, resultList, "android.telephony.MSimTelephonyManager", methodName);
                    }
                
                    // 360高通的某一个获取不到
                    // 三星的双卡 代表手机：note2，3，s4
                    if (ClazzUtils.g().getClass("android.telephony.MultiSimTelephonyManager") != null) {
                        addForSunsumg(resultList, "android.telephony.MultiSimTelephonyManager", methodName);
                    }
                    if (ClazzUtils.g().getClass("com.samsung.android.telephony.MultiSimManager") != null) {
                        addForSunsumg(resultList, "com.samsung.android.telephony.MultiSimManager", methodName);
                    }
//                if (Build.VERSION.SDK_INT < 21) {
//                    addForSunsumg(resultList, "android.telephony.MultiSimTelephonyManager", methodName);
//                } else {
//                    addForSunsumg(resultList, "com.samsung.android.telephony.MultiSimManager", methodName);
//                }
                    // 展讯手机
                    addForZhanXun(context, resultList, methodName);
                }
            } catch (Throwable e) {
            }
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

    private void addForQualcomm(Context context, List<String> resultList, String className, String methodName) {
        if (BuildConfig.ENABLE_IMEI) {
            if (TextUtils.isEmpty(className) || TextUtils.isEmpty(methodName) || context == null) {
                return;
            }
            try {
                // Class<?> cx = Class .forName("android.telephony.MSimTelephonyManager");
                @SuppressLint("WrongConstant")
                Object obj = context.getApplicationContext().getSystemService("phone_msim");
                if (obj == null) {
                    obj = getObjectInstance(className);
                    if (obj == null) {
                        return;
                    }
                    return;
                }
                for (int i = 0; i < 3; i++) {
                    String result = getString(obj, methodName, i);
                    if (!TextUtils.isEmpty(result) && !resultList.contains(result)) {
                        resultList.add(result);
                    }
                }
            } catch (Throwable e) {
            }
        }
        
    }

    /**
     * 展讯获取逻辑: 优先取名字，然后通过获取服务方式获取
     *
     * @param context
     * @param resultList
     * @param methodName
     */
    private void addForZhanXun(Context context, List<String> resultList, String methodName) {
        if (BuildConfig.ENABLE_IMEI) {
            try {
                if (TextUtils.isEmpty(methodName)) {
                    return;
                }
            
                for (int i = 0; i < 3; i++) {
                    String spreadTmService = null;
                
                    if (ClazzUtils.g().getClass("com.android.internal.telephony.PhoneFactory") != null) {
                        try {
                            // 调整为调用静态方法
                            spreadTmService = (String) ClazzUtils.g().invokeStaticMethod(
                                    "com.android.internal.telephony.PhoneFactory", "getServiceName",
                                    new Class[]{String.class, int.class}, new Object[]{Context.TELEPHONY_SERVICE, i});
                            ;
                        } catch (Throwable e) {
                            // 尝试调用非静态方法
                            spreadTmService = (String) ClazzUtils.g().invokeObjectMethod(getObjectInstance(
                                    "com.android.internal.telephony.PhoneFactory"), "getServiceName",
                                    new Class[]{String.class, int.class}, new Object[]{Context.TELEPHONY_SERVICE, i});
                        }
                    }
                
                
                    if (!TextUtils.isEmpty(spreadTmService)) {
                        TelephonyManager telephony =
                                (TelephonyManager) context.getApplicationContext().getSystemService(spreadTmService);
                        // 默认系统接口
                        add(resultList, telephony, methodName);
                    }
                
                }
            } catch (Throwable e) {
            }
        }
    }

    private void addWithSolt(List<String> imeis, TelephonyManager telephony, String method, int slotId) {
        if (BuildConfig.ENABLE_IMEI) {
            try {
                String result = getString(telephony, method, slotId);
                if (!TextUtils.isEmpty(result) && !imeis.contains(result)) {
                    imeis.add(result);
                }
            } catch (Throwable e) {
            }
        }
    }

    private void add(List<String> imeis, TelephonyManager telephony, String method) {
        if (BuildConfig.ENABLE_IMEI) {
            try {
                Object id = ClazzUtils.g().invokeObjectMethod(telephony, method);
                if (id == null) {
                    return;
                }
                String result = (String) id;
                if (!imeis.contains(result)) {
                    imeis.add(result);
                }
            } catch (Throwable e) {
            }
        }
        
    }

//    /**
//     * Sunsumg
//     *
//     * @param imeis
//     * @param instance
//     * @param method
//     */
//    private void addForSunsumg(List<String> imeis, Object instance, String method) {
//        if (BuildConfig.ENABLE_IMEI) {
//            try {
//                String result = (String) ClazzUtils.g().invokeObjectMethod(instance, method);
//                if (!TextUtils.isEmpty(result) && !imeis.contains(result)) {
//                    imeis.add(result);
//                }
//            } catch (Throwable e) {
//            }
//        }
//    }

    private void addForSunsumg(List<String> imeis, String clazzName, String method) {
        if (BuildConfig.ENABLE_IMEI) {
            try {
                Object instance = getObjectInstance(clazzName);
                if (instance == null) {
                    instance = ClazzUtils.g().newInstance(clazzName);
                }
                String result = (String) ClazzUtils.g().invokeObjectMethod(instance, method);
                if (!TextUtils.isEmpty(result) && !imeis.contains(result)) {
                    imeis.add(result);
                }
            } catch (Throwable e) {
            }
        }
    }

    private void addWithSolt(List<String> imeis, String className, String method, int slotID) {
        if (BuildConfig.ENABLE_IMEI) {
            try {
                Object instance = getObjectInstance(className);
                if (instance == null) {
                    return;
                }
                String result = getString(instance, method, slotID);
                if (!TextUtils.isEmpty(result) && !imeis.contains(result)) {
                    imeis.add(result);
                }
            
            } catch (Throwable e) {
            }
        }
    }

    public void getImeisByShell(List<String> imeis) {
        List<String> ifs = Arrays.asList(
                // 具体机型忘记， 需要,分割
                "ril.gsm.imei"
                , "ril.gsm.meid"
                , "ril.cdma.imei"
                , "ril.cdma.meid"
                //小米   ro.ril.miui.imei1/ro.ril.miui.imei2
                //  猜测 ro.ril.miui.meid1/ro.ril.miui.meid2
                , "ro.ril.miui.imei"
                , "ro.ril.miui.meid"
                , "persist.radio.imei"
                , "persist.radio.meid"
                , "ro.ril.oem.imei"
                , "ro.ril.oem.meid"
                // 锤子 ril.modem.imei.1/ril.modem.imei.2
                //  猜测 ril.modem.meid.1/ril.modem.meid.2
                , "ril.modem.imei"
                , "ril.modem.meid"
                // 联想 gsm.device.imei1/gsm.device.imei2/gsm.device.meid1
                //  猜测 cdma.device.imei1/cdma.device.imei2/cdma.device.meid1
                , "gsm.device.imei"
                , "gsm.device.meid"
                , "cdma.device.imei"
                , "cdma.device.meid"
                // 联想 gsm.meid/gsm.imei1/gsm.imei2
                //  猜测 cdma.meid/cdma.imei1/cdma.imei2
                , "gsm.meid"
                , "gsm.imei"
                , "cdma.meid"
                , "cdma.imei"
                // VIVO
                // persist.sys.meid  返回值特殊 +MEID: "A00000859BAB69"
                // persist.sys.updater.imei/persist.sys.vtouch.imei
                // 猜测 persist.sys.imei/persist.sys.updater.meid/ersist.sys.vtouch.meid
                , "persist.sys.imei"
                , "persist.sys.meid"
                , "persist.sys.updater.imei"
                , "persist.sys.updater.meid"
                , "persist.sys.vtouch.imei"
                , "persist.sys.vtouch.meid"
                // 一加 vendor.oem.device.imeicache0/vendor.oem.device.imeicache1
                // 猜测 vendor.oem.device.meidcache0/vendor.oem.device.meidcache1
                , "vendor.oem.device.imeicache"
                , "vendor.oem.device.meidcache"
                , "vendor.radio.device.imeicache"
                , "vendor.radio.device.meidcache"
        );

        for (String f : ifs) {
            parserOnePath(imeis, f);
            for (int i = 0; i < 3; i++) {
                parserOnePath(imeis, f + i);
                parserOnePath(imeis, f + "." + i);
            }
        }
    }

    private void parserOnePath(List<String> imeis, String f) {
        try {
            String env = SystemUtils.getSystemEnv(f);
            if (!TextUtils.isEmpty(env)) {
                env = env.trim();
                //处理包含逗号的case
                if (env.contains(",")) {
                    getImeiIfContainsComma(imeis, env);
                } else if (env.contains(":") && env.contains("\"")) {
                    // support vivo
                    getImeiIfContainsColon(imeis, env);
                } else {
                    if (!imeis.contains(env)) {
                        imeis.add(env);
                    }
                }
            }
        } catch (Throwable e) {
        }
    }

    private void getImeiIfContainsColon(List<String> imeis, String env) {
        String s = env.replaceAll("\"", "");
        String[] ss = env.split(":");
        if (ss != null && ss.length > 0) {
            String tmpKey = ss[ss.length - 1];
            if (!TextUtils.isEmpty(tmpKey) && !imeis.contains(tmpKey)) {
                imeis.add(tmpKey.trim());
            }
        }
    }

    private void getImeiIfContainsComma(List<String> imeis, String env) {
        String[] ss = env.split(",");
        if (ss != null && ss.length > 0) {
            for (String tmpKey : ss) {
                if (!TextUtils.isEmpty(tmpKey) && !imeis.contains(tmpKey)) {
                    imeis.add(tmpKey);
                }
            }
        }
    }

//    /**
//     * 等同调用 <code>SystemProperties.get("key")<code/>或者shell调用<code>getprop key<code/>
//     *
//     * @param imeis
//     * @param key
//     */
//    private void addBySystemProperties(List<String> imeis, String key, String splitKey) {
//        try {
//            String result = (String) ClazzUtils.g().getDefaultProp(key);
//            if (TextUtils.isEmpty(result)) {
//                return;
//            }
//            if (TextUtils.isEmpty(splitKey)) {
//                // 没有过滤条件
//                if (!imeis.contains(result)) {
//                    imeis.add(result);
//                }
//            } else {
//                // 根据过滤条件切割
//                if (result.contains(splitKey)) {
//                    String[] ss = result.split(splitKey);
//                    if (ss != null && ss.length > 0) {
//                        for (String tmpKey : ss) {
//                            if (!TextUtils.isEmpty(tmpKey) && !imeis.contains(tmpKey)) {
//                                imeis.add(tmpKey);
//                            }
//                        }
//                    }
//                } else {
//                    if (!imeis.contains(result)) {
//                        imeis.add(result);
//                    }
//                }
//            }
//        } catch (Throwable e) {
//        }
//    }

    /**
     * 获取失败后可以转换类型继续尝试. 典型接口 TelephonyManager.getSubscriberId(int id) TelephonyManager.getSubscriberId(long id)
     *
     * @param obj
     * @param method
     * @param slotId
     * @return
     */
    private String getString(Object obj, String method, int slotId) {
        if (BuildConfig.ENABLE_IMEI) {
            try {
                Object id = ClazzUtils.g().invokeObjectMethod(obj, method, new Class[]{int.class}, new Object[]{slotId});
                if (id == null) {
                    id = ClazzUtils.g().invokeObjectMethod(obj, method, new Class[]{long.class}, new Object[]{slotId});
                }
                if (id != null) {
                    return (String) id;
                }
            } catch (Throwable e) {
            }
        }
        return null;
    }

    /**
     * 获取tm示例
     *
     * @param className
     * @return
     */
    private Object getObjectInstance(String className) {
        return ClazzUtils.g().invokeStaticMethod(className, "getDefault");
    }

    /*********************************************单例*************************************/
    private static class HOLDER {
        private static DoubleCardSupport INSTANCE = new DoubleCardSupport();
    }

    private DoubleCardSupport() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 18; i++) {
            sb.append("0");
            DEFAULT_VALUE.add(sb.toString());
        }
    }

    public static DoubleCardSupport getInstance() {
        return HOLDER.INSTANCE;
    }

    // 连接符
    private final String SPLIT_S = "|";
    // 默认值
    private final List<String> DEFAULT_VALUE = new ArrayList<String>();

}
