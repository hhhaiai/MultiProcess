package com.analysys.track.utils;

import android.app.ActivityManager;
import android.content.Context;

/**
 * @Copyright 2019 analysys Inc. All rights reserved.
 * @Description: 低内存不工作的判断
 * @Version: 1.0
 * @Create: 2019-11-30 17:48:36
 * @author: miqt
 * @mail: miqingtang@analysys.com.cn
 */
public class NinjaUtils {

    /**
     * 判断是否是低内存
     */
    public static boolean isLowMemory(Context context) {
        try {
            if (context == null) {
                return false;
            }
            ActivityManager inst = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            float u = 0;
            if (inst != null) {
                ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
                try {
                    inst.getMemoryInfo(memoryInfo);
                    return memoryInfo.lowMemory;
                } catch (Throwable e) {
                    //异常后用使用比例判断
                }
                //后台进程大于这个内存值就被回收
                long maxBack = memoryInfo.threshold;
                if (maxBack <= 0) {
                    //除零异常
                    return false;
                }
                //当前使用了
                long currUsed = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                u = currUsed * 1.0F / maxBack * 100;
            } else {
                //最大可用内存
                long maxCanUsed = Runtime.getRuntime().maxMemory();
                if (maxCanUsed <= 0) {
                    //除零异常
                    return false;
                }
                //当前使用了
                long currUsed = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                u = currUsed * 1.0F / maxCanUsed * 100;
            }
            //使用率大于60算低内存
            return u >= 80;
        } catch (Throwable e) {
        }
        return false;
    }

}
