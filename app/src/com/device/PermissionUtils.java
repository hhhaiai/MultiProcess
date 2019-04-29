package com.device;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;


import java.lang.reflect.Method;

public class PermissionUtils {
    /**
     * 检查权限
     * 权限申请被拒绝检测返回false，权限申请通过检测返回true
     * @param context
     * @param permission
     * @return
     */
    public static boolean checkPermission(Context context, String permission) {
//        ELOG.i("本次permission::::::"+permission);
        boolean result = false;

        if (Build.VERSION.SDK_INT >= 23) {
            try {
                Class<?> clazz = Class.forName("android.content.Context");
                Method method = clazz.getMethod("checkSelfPermission", String.class);
                int rest = (Integer)method.invoke(context, permission);
                result = rest == PackageManager.PERMISSION_GRANTED;
            } catch (Exception e) {
//                ELOG.i("第2个可能返回的地方");
                result = false;
            }
        } else {
            PackageManager pm = context.getPackageManager();
            if (pm.checkPermission(permission, context.getPackageName()) == PackageManager.PERMISSION_GRANTED) {
                result = true;
            }
        }
//        ELOG.i("第3个可能返回的地方"+result);
        return result;
    }
}
