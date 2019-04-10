package com.analysys.track.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import com.analysys.track.internal.Content.EGContext;
import com.analysys.track.utils.sp.SPHelper;

import java.lang.reflect.Method;

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
//        ELOG.i("本次permission::::::"+permission);
        boolean result = false;
        String day = SystemUtils.getDay();
        if(SPHelper.getStringValueFromSP(context,EGContext.PERMISSION_TIME,"-1").equals(day) && permissionAskCount > 5){
//            ELOG.i("第一个可能返回的地方");
            return false;
        }
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
        if(!result && permission.equals(Manifest.permission.ACCESS_COARSE_LOCATION)){
            //如果是当天，则累加，并将当前count存sp；否则，则置零，重新累加。即，一天只能有5次申请授权
            if(SPHelper.getStringValueFromSP(context,EGContext.PERMISSION_TIME,"-1").equals(day)){
                permissionAskCount += 1;
                SPHelper.setIntValue2SP(context,EGContext.PERMISSION_COUNT,permissionAskCount);
            }else{
                permissionAskCount += 1;
                permissionAskCount = permissionAskCount+1;
                SPHelper.setStringValue2SP(context,EGContext.PERMISSION_TIME,day);
                SPHelper.setIntValue2SP(context,EGContext.PERMISSION_COUNT,permissionAskCount);
            }
        }
//        ELOG.i("第3个可能返回的地方"+result);
        return result;
    }
}
