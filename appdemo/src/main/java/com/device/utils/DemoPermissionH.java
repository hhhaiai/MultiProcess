package com.device.utils;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


/**
 * @Copyright © 2019 sanbo Inc. All rights reserved.
 * @Description: 权限辅助类
 * @Version: 1.0
 * @Create: 2019-07-27 14:03:08
 * @author: sanbo
 * @mail: xueyongfu@analysys.com.cn
 */
public class DemoPermissionH {

    /**
     * 判断权限是否已经授权
     *
     * @param context
     * @param permission
     * @return
     */
    public static boolean checkPermission(Context context, String permission) {
        boolean result = false;
        if (Build.VERSION.SDK_INT >= 23) {
            try {
                context = DemoEContextHelper.getContext(context);
                if (context instanceof Application) {
                    context = ((Application) context).getBaseContext();
                }
                Class<?> clazz = Class.forName("android.content.Context");
                Method method = clazz.getMethod("checkSelfPermission", String.class);
                int rest = (Integer) method.invoke(context, permission);
                return rest == PackageManager.PERMISSION_GRANTED;
            } catch (Throwable e) {
                result = false;
            }
        } else {
            result = true;
        }
        return result;
    }

    public static List<String> addPermission(Context context, String[] pp) {

        List<String> resultList = new ArrayList<String>();
        for (String s : pp) {
            if (!checkPermission(context, s)) {
                resultList.add(s);
            }
        }
        return resultList;
    }

}
