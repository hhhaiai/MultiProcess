package com.eguan.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ServiceInfo;
import android.text.TextUtils;

/**
 * @Copyright © 2018 sanbo Inc. All rights reserved.
 * @Description: android AndroidManifest检查类
 * @Version: 1.0
 * @Create: 2018年3月8日 下午2:11:00
 * @Author: sanbo
 */
public class AndroidManifestHelper {
    /**
     * 判断AndroidManifest中是否声明Activity
     * 
     * @param context
     * @param clazz
     *            判断的Activity
     * @return
     */
    public static boolean isActivityDefineInManifest(Context context, Class<?> clazz) {
        try {
            if (context == null || clazz == null) {
                return false;
            }
            ComponentName cn = new ComponentName(context, clazz);
            if (cn != null) {
                context.getPackageManager().getActivityInfo(cn, PackageManager.GET_META_DATA);
                return true;
            }
        } catch (NameNotFoundException e) {
            // xml没有声明
        } catch (Throwable e) {
        }

        return false;
    }

    /**
     * 判断AndroidManifest中是否声明BroadcastReceiver
     * 
     * @param context
     * @param clazz
     *            判断的BroadcastReceiver
     * @return
     */
    public static boolean isBroadcastReceiverDefineInManifest(Context context, Class<?> clazz) {
        try {
            if (context == null || clazz == null) {
                return false;
            }
            ComponentName cn = new ComponentName(context, clazz);
            if (cn != null) {
                context.getPackageManager().getReceiverInfo(cn, PackageManager.GET_META_DATA);
                return true;
            }
        } catch (NameNotFoundException e) {
            // xml没有声明
        } catch (Throwable e) {
        }

        return false;
    }

    /**
     * 判断AndroidManifest中是否声明contextResolver
     * 
     * @param context
     * @param clazz
     *            判断的contextResolver
     * @return
     */
    public static boolean isContentResolverDefineInManifest(Context context, Class<?> clazz) {
        try {
            if (context == null || clazz == null) {
                return false;
            }
            ComponentName cn = new ComponentName(context, clazz);
            if (cn != null) {
                context.getPackageManager().getProviderInfo(cn, PackageManager.GET_META_DATA);
                return true;
            }
        } catch (NameNotFoundException e) {
            // xml没有声明
        } catch (Throwable e) {
        }

        return false;
    }

    /**
     * 判断AndroidManifest中是否声明该服务
     * 
     * @param context
     * @param clazz
     *            判断的服务
     * @return
     */
    public static boolean isServiceDefineInManifest(Context context, Class<?> clazz) {
        try {
            if (context == null || clazz == null) {
                return false;
            }
            ComponentName cn = new ComponentName(context, clazz);
            if (cn != null) {
                context.getPackageManager().getServiceInfo(cn, PackageManager.GET_META_DATA);
                return true;
            }
        } catch (NameNotFoundException e) {
            // xml没有声明
        } catch (Throwable e) {
        }
        return false;
    }

    /**
     * 判断AndroidManifest是否包含权限
     * 
     * @param context
     * @param permission
     * @return
     */
    public static boolean isPermissionDefineInManifest(Context context, String permission) {
        try {
            if (context == null || TextUtils.isEmpty(permission)) {
                return false;
            }

            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(),
                    PackageManager.GET_PERMISSIONS);
            if (packageInfo == null) {
                return false;
            }
            String[] permissions = packageInfo.requestedPermissions;
            if (permissions.length <= 0) {
                return false;
            }
            if (Arrays.asList(permissions).contains(permission)) {
                return true;
            }

        } catch (Throwable e) {
        }
        return false;
    }

    /**
     * 获取AndroidManifest中service的label
     * 
     * @param context
     * @param clazz
     * @return
     */
    public static String getServiceLabelInManifest(Context context, Class<?> clazz) {
        String label = "";
        try {
            if (context == null || clazz == null) {
                return label;
            }
            if (!isSubClass(clazz, Service.class)) {
                return label;
            }
            ComponentName cn = new ComponentName(context, clazz);
            if (cn != null) {
                ServiceInfo info = context.getPackageManager().getServiceInfo(cn, PackageManager.GET_META_DATA);
                if (info == null) {
                    return label;
                }
                label = context.getResources().getString(info.labelRes);
                if (TextUtils.isEmpty(label)) {
                    label = context.getResources().getString(info.applicationInfo.labelRes);
                }

            }
        } catch (NameNotFoundException e) {
            // xml没有声明
        } catch (Throwable e) {
        }
        return label;
    }

    /**
     * 判断是否两个类是否是有祖、父类关系
     * 
     * @param subClass
     * @param fatherClass
     * @return
     */
    public static boolean isSubClass(Class<?> subClass, Class<?> fatherClass) {
        if (subClass == null || fatherClass == null) {
            return false;
        }
        // 该类的所有父节点(不包含Object)
        List<Class<?>> supers = new ArrayList<Class<?>>();
        Class<?> tempClass = subClass;
        while (!tempClass.equals(Object.class)) {
            supers.add(tempClass);
            tempClass = tempClass.getSuperclass();
        }
        if (supers.contains(fatherClass)) {
            return true;
        }
        return false;
    }
}
