package com.demo;

import java.lang.reflect.Method;
import java.util.ArrayList;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;

/**
 * @Copyright © 2018 sanbo Inc. All rights reserved.
 * @Description: 用法:
 *               <p>
 *               <code>
 * //        单个权限申请
 * PermissionUtils.checkPermission(MainActivity.this, permission, 1);
 * 多个权限申请
 * PermissionUtils.checkPermissionArray(MainActivity.this, permissionArray, 2);
 * //        特殊权限-系统弹窗
 * PermissionUtils.checkSettingAlertPermission(MainActivity.this, PermissionUtils.PERMISSION_SETTING_REQ_CODE);
 * //        特殊权限-系统弹窗
 * PermissionUtils.checkSettingSystemPermission(MainActivity.this, PermissionUtils.PERMISSION_SETTING_REQ_CODE);
 * </code>
 * @Version: 1.0
 * @Create: 2018/8/16 15:02
 * @Author: sanbo
 */
public class PermissionUtils {
    private static final String TAG = "PermissionUtils";
    public static final int PERMISSION_REQUEST_CODE = 0x10;
    public static final int PERMISSION_SETTING_REQ_CODE = 0x1000;

    @TargetApi(23)
    public static boolean checkPermission(Object cxt, String permission, int requestCode) {
        if (!checkSelfPermissionWrapper(cxt, permission)) {
            if (!shouldShowRequestPermissionRationaleWrapper(cxt, permission)) {
                requestPermissionsWrapper(cxt, new String[] { permission }, requestCode);
            } else {
                Log.d(TAG, "should show rational");
            }
            return false;
        }

        return true;
    }

    private static void requestPermissionsWrapper(Object cxt, String[] permission, int requestCode) {
        if (cxt instanceof Activity) {
            Activity activity = (Activity) cxt;
            ActivityCompat.requestPermissions(activity, permission, requestCode);
        } else if (cxt instanceof Fragment) {
            Fragment fragment = (Fragment) cxt;
            fragment.requestPermissions(permission, requestCode);
        } else {
            throw new RuntimeException("cxt is net a activity or fragment");
        }
    }

    private static boolean shouldShowRequestPermissionRationaleWrapper(Object cxt, String permission) {
        if (cxt instanceof Activity) {
            Activity activity = (Activity) cxt;
            return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
        } else if (cxt instanceof Fragment) {
            Fragment fragment = (Fragment) cxt;
            return fragment.shouldShowRequestPermissionRationale(permission);
        } else {
            throw new RuntimeException("cxt is net a activity or fragment");
        }
    }

    private static String[] checkSelfPermissionArray(Object cxt, String[] permission) {
        ArrayList<String> permiList = new ArrayList<String>();
        for (String p : permission) {
            if (!checkSelfPermissionWrapper(cxt, p)) {
                permiList.add(p);
            }
        }

        return permiList.toArray(new String[permiList.size()]);
    }

    @TargetApi(23)
    public static boolean checkPermissionArray(Object cxt, String[] permission, int requestCode) {
        String[] permissionNo = checkSelfPermissionArray(cxt, permission);
        if (permissionNo.length > 0) {
            requestPermissionsWrapper(cxt, permissionNo, requestCode);
            return false;
        } else
            return true;
    }

    public static boolean verifyPermissions(int[] grantResults) {
        // At least one result must be checked.
        if (grantResults.length < 1) {
            return false;
        }

        // Verify that each required permission has been granted, otherwise return
        // false.
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * WRITE_SETTINGS 权限
     *
     * @param cxt
     * @param req
     * @return
     */
    @TargetApi(23)
    public static boolean checkSettingSystemPermission(Object cxt, int req) {
        if (cxt instanceof Activity) {
            Activity activity = (Activity) cxt;
            if (!Settings.System.canWrite(activity)) {
                Log.i(TAG, "Setting not permission");

                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + activity.getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivityForResult(intent, req);
                return false;
            }
        } else if (cxt instanceof Fragment) {
            Fragment fragment = (Fragment) cxt;
            if (!Settings.System.canWrite(fragment.getContext())) {
                Log.i(TAG, "Setting not permission");

                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + fragment.getContext().getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                fragment.startActivityForResult(intent, req);
                return false;
            }
        } else {
            throw new RuntimeException("cxt is net a activity or fragment");
        }

        return true;
    }

    /**
     * 检测系统弹出权限
     *
     * @param cxt
     * @param req
     * @return
     */
    @TargetApi(23)
    public static boolean checkSettingAlertPermission(Object cxt, int req) {
        if (cxt instanceof Activity) {
            Activity activity = (Activity) cxt;
            if (!Settings.canDrawOverlays(activity.getBaseContext())) {
                Log.i(TAG, "Setting not permission");

                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + activity.getPackageName()));
                activity.startActivityForResult(intent, req);
                return false;
            }
        } else if (cxt instanceof Fragment) {
            Fragment fragment = (Fragment) cxt;
            if (!Settings.canDrawOverlays(fragment.getActivity())) {
                Log.i(TAG, "Setting not permission");

                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + fragment.getActivity().getPackageName()));
                fragment.startActivityForResult(intent, req);
                return false;
            }
        } else {
            throw new RuntimeException("cxt is net a activity or fragment");
        }

        return true;
    }

    public static boolean checkSelfPermissionWrapper(Object obj, String permission) {
        boolean result = false;
        Context cxt = null;
        if (!Context.class.isAssignableFrom(obj.getClass())) {
            return result;
        } else {
            cxt = (Context) obj;
        }
        if (cxt != null) {
            if (Build.VERSION.SDK_INT >= 23) {
                try {
                    Class<?> clazz = Class.forName("android.content.Context");
                    Method method = clazz.getMethod("checkSelfPermission", String.class);
                    int rest = (Integer) method.invoke(cxt, permission);
                    result = rest == PackageManager.PERMISSION_GRANTED;
                } catch (Exception e) {
                    result = false;
                }
            } else {
                PackageManager pm = cxt.getPackageManager();
                if (pm.checkPermission(permission, cxt.getPackageName()) == PackageManager.PERMISSION_GRANTED) {
                    result = true;
                }
            }
        }
        return result;
    }
}
