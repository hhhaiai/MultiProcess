package com.analysys.track.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import com.analysys.track.internal.Content.EGContext;
import com.analysys.track.utils.sp.SPHelper;

import java.lang.reflect.Method;

/**
 * @Copyright © 2018 Analysys Inc. All rights reserved.
 * @Description: 权限检查/权限申请
 * @Version: 1.0
 * @Create: 2018年9月13日 上午11:04:47
 * @Author: sanbo
 */
public class PermissionUtils {
    private static int permissionAskCount = 0;
    /**
     * 检查权限
     * 权限申请被拒绝检测返回false，权限申请通过检测返回true
     * @param context
     * @param permission
     * @return
     */
    public static boolean checkPermission(Context context, String permission) {
        boolean result = false;
        String day = SystemUtils.getDay();
        if(SPHelper.getDefault(context).getString(EGContext.PERMISSION_TIME,"-1").equals(day)&& permissionAskCount > 5){
            return false;
        }
        if (Build.VERSION.SDK_INT >= 23) {
            try {
                Class<?> clazz = Class.forName("android.content.Context");
                Method method = clazz.getMethod("checkSelfPermission", String.class);
                int rest = (Integer)method.invoke(context, permission);
                result = rest == PackageManager.PERMISSION_GRANTED;
            } catch (Exception e) {
                result = false;
            }
        } else {
            PackageManager pm = context.getPackageManager();
            if (pm.checkPermission(permission, context.getPackageName()) == PackageManager.PERMISSION_GRANTED) {
                result = true;
            }
        }
        if(!result){
            //如果是当天，则累加，并将当前count存sp；否则，则置零，重新累加。即，一天只能有5次申请授权
            if(SPHelper.getDefault(context).getString(EGContext.PERMISSION_TIME,"-1").equals(day)){
                permissionAskCount = permissionAskCount+1;
                SPHelper.getDefault(context).edit().putInt(EGContext.PERMISSION_COUNT,permissionAskCount).apply();
            }else{
                permissionAskCount = 0;
                permissionAskCount = permissionAskCount+1;
                SPHelper.getDefault(context).edit().putString(EGContext.PERMISSION_TIME,day).apply();
                SPHelper.getDefault(context).edit().putInt(EGContext.PERMISSION_COUNT,permissionAskCount).apply();
            }
        }
        return result;
    }
}
