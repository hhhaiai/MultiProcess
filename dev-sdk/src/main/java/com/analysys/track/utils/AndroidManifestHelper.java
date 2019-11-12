package com.analysys.track.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.os.Bundle;
import android.text.TextUtils;

import com.analysys.track.BuildConfig;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.utils.reflectinon.EContextHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * @Copyright © 2019 sanbo Inc. All rights reserved.
 * @Description: mainfest解析类
 * @Version: 1.0
 * @Create: 2019-08-05 16:36:27
 * @author: sanbo
 * @mail: xueyongfu@analysys.com.cn
 */
public class AndroidManifestHelper {

    /**
     * 判断AndroidManifest中是否声明该服务
     *
     * @param context
     * @param clazz   判断的服务
     * @return
     */
    public static boolean isServiceDefineInManifest(Context context, Class<?> clazz) {
        try {
            context = EContextHelper.getContext(context);
            if (context != null) {
                ServiceInfo info = getServiceInfo(context, clazz);
                if (info != null) {
                    return true;
                }
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
        }

        return false;
    }

    public static boolean isJobServiceDefineInManifest(Context context, Class<?> clazz) {
        try {
            context = EContextHelper.getContext(context);
            if (context != null) {
                ServiceInfo info = getServiceInfo(context, clazz);
                if (info != null && "android.permission.BIND_JOB_SERVICE".equals(info.permission)) {
                    return true;
                }
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
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
            context = EContextHelper.getContext(context);
            if (context != null && !TextUtils.isEmpty(permission)) {
                PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(),
                        PackageManager.GET_PERMISSIONS);
                if (packageInfo != null) {
                    String[] permissions = packageInfo.requestedPermissions;
                    if (permissions.length <= 0) {
                        return false;
                    }
                    if (Arrays.asList(permissions).contains(permission)) {
                        return true;
                    }
                }
            }

        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(e);
            }
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
            context = EContextHelper.getContext(context);
            if (context != null) {
                ServiceInfo info = getServiceInfo(context, clazz);
                if (info != null) {
                    label = context.getResources().getString(info.labelRes);
                    if (TextUtils.isEmpty(label)) {
                        label = context.getResources().getString(info.applicationInfo.labelRes);
                    }
                }
                return label;
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
        }
        return label;
    }

    /**
     * 获取XML中的metadata对象
     *
     * @param context
     * @return
     */
    public static Bundle getMetaData(Context context) {
        try {
            context = EContextHelper.getContext(context);
            if (context != null) {
                PackageManager manager = context.getPackageManager();
                ApplicationInfo info = manager.getApplicationInfo(context.getPackageName(),
                        PackageManager.GET_META_DATA);
                if (info != null) {
                    return info.metaData;
                }
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(e);
            }
        }
        return Bundle.EMPTY;
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

    private static ServiceInfo getServiceInfo(Context context, Class<?> clazz) {
        try {
            context = EContextHelper.getContext(context);
            if (context == null || clazz == null) {
                return null;
            }
            ComponentName cn = new ComponentName(context, clazz);
            if (cn != null) {
                return context.getPackageManager().getServiceInfo(cn, PackageManager.GET_META_DATA);
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
        }
        return null;
    }
}
