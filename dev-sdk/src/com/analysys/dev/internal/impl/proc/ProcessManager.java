package com.analysys.dev.internal.impl.proc;

import android.content.Context;
import android.util.Log;

import com.analysys.dev.internal.Content.EGContext;
import com.analysys.dev.utils.ELOG;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProcessManager {
    // 是否开启工作
    private volatile static boolean isCollected = true;
    private static JSONObject XXXInfo = new JSONObject();;

    public static void setIsCollected(boolean isCollected) {
        ProcessManager.isCollected = isCollected;
    }

    public static JSONObject getRunningForegroundApps(Context ctx) {
        JSONArray uploadArray = new JSONArray();
        while(true) {
            if (isCollected) {
                ELOG.i("start===>" + isCollected);
                try {
                    JSONObject obj = ProcParser.getRunningInfo(ctx);
                    uploadArray.put(obj);
                    XXXInfo.put("XXXInfo", uploadArray);
                    Log.i("ALL", XXXInfo.toString());
                   return XXXInfo;
                }catch (Exception e){
                    ELOG.i(e.getMessage()+"   getRunningForegroundApps()");
                }
            }
            try {
                Thread.sleep(EGContext.OC_CYCLE_OVER_5);
            } catch (Throwable e) {
            }
        }

    }
//    public static JSONObject getRunningForegroundInfos(final Context ctx) {
//        JSONArray uploadArray = new JSONArray();
//        while (true) {
//            if (isCollected) {
//                ELOG.i("start===>" + isCollected);
//                JSONObject obj = ProcParser.getRunningInfo(ctx);
//                uploadArray.put(obj);
//                try {
//                    XXXInfo.put("XXXInfo", uploadArray);
//                    Log.i("ALL", XXXInfo.toString());
//                    return XXXInfo;
//                } catch (Exception e) {
//                }
//            }
//            try {
//                Thread.sleep(EGContext.OC_CYCLE_OVER_5);
//            } catch (Throwable e) {
//            }
//        }
//
//
//    }
}
