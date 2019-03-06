package com.analysys.track.internal.impl.proc;

import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.analysys.track.database.TableOCCount;
import com.analysys.track.database.TableOCTemp;
import com.analysys.track.internal.Content.DeviceKeyContacts;
import com.analysys.track.internal.Content.EGContext;
import com.analysys.track.utils.Applist;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.NetworkUtils;
import com.analysys.track.utils.SystemUtils;
import com.analysys.track.utils.sp.SPHelper;

import android.content.Context;
import android.util.Log;

public class ProcessManager {
    // 是否开启工作
    private volatile static boolean isCollected = true;
    private static JSONObject XXXInfo = new JSONObject();;

    private static JSONArray ocList = new JSONArray();
    private static boolean isSaveForScreenOff = false;

    public static void setIsCollected(boolean isCollected) {
        ProcessManager.isCollected = isCollected;
    }

    public static JSONObject getRunningForegroundApps(Context ctx) {
        isSaveForScreenOff = false;
        JSONArray uploadArray = new JSONArray();
        while (true) {
            if (isCollected) {
                ELOG.i("start===>" + isCollected);
                try {
                    JSONObject obj = ProcParser.getRunningInfo(ctx);
                    uploadArray.put(obj);
                    XXXInfo.put("XXXInfo", uploadArray);
                    Log.i("ALL", XXXInfo.toString());
                    return XXXInfo;
                } catch (Exception e) {
                    ELOG.i(e.getMessage() + "   getRunningForegroundApps()");
                }
            }
            try {
                Thread.sleep(EGContext.OC_CYCLE_OVER_5);
            } catch (Throwable e) {
            }
        }
    }

    public static void dealScreenOff(Context ctx) {
        if (!isSaveForScreenOff) {
            isSaveForScreenOff = true;
        } else {
            // 确保在每个l轮循之间，只有一次的锁屏saveDB。
            return;
        }
        // 保存ProcTemp表中数据至OCInfo表中
        Map<String, String> map = TableOCTemp.getInstance(ctx).queryProcTemp();
        ocList = getOCInfoProcTemp(ctx, map, 0);
        ELOG.i("dealScreenOff:::::" + ocList);
        TableOCCount.getInstance(ctx).insertArray(ocList);
        // 需要对ocList清空
        ocList = null;
        // 清除ProcTemp表
        TableOCTemp.getInstance(ctx).delete();
    }

    /**
     * @param map
     * @param type 0 表示锁屏，1表示服务重启
     * @return
     */
    private static JSONArray getOCInfoProcTemp(Context ctx, Map<String, String> map, int type) {
        String endTime = "";
        if (type == 0) {
            endTime = System.currentTimeMillis() + "";
        } else if (type == 1) {
            endTime = SPHelper.getEndTime(ctx) + "";
        }
        JSONArray ocArray = new JSONArray();
        JSONObject ocInfo;
        try {
            List list = SystemUtils.getDiffNO(map.size());
            int i = 0, r = -1;
            for (Map.Entry<String, String> entry : map.entrySet()) {
                i++;
                r = (Integer)list.get(i);
                ELOG.i("r  value ...::::" + r);
                String startTime = entry.getValue();
                ocInfo = new JSONObject();
                ocInfo.put(DeviceKeyContacts.OCInfo.ApplicationPackageName, entry.getKey());
                ocInfo.put(DeviceKeyContacts.OCInfo.ApplicationOpenTime, startTime);
                ocInfo.put(DeviceKeyContacts.OCInfo.ApplicationCloseTime, Integer.parseInt(endTime) - r);
                ocInfo.put(DeviceKeyContacts.OCInfo.NetworkType, NetworkUtils.getNetworkType(ctx));
                ocInfo.put(DeviceKeyContacts.OCInfo.CollectionType, "2");
                ocInfo.put(DeviceKeyContacts.OCInfo.SwitchType, type == 0 ? "2" : "3");
                ocInfo.put(DeviceKeyContacts.OCInfo.ApplicationType,
                    Applist.getInstance(ctx).getAppType(entry.getKey()));
                ocInfo.put(DeviceKeyContacts.OCInfo.ApplicationVersionCode,
                    SystemUtils.getApplicationVersion(ctx, entry.getKey()));
                ocInfo.put(DeviceKeyContacts.OCInfo.ApplicationName,
                    SystemUtils.getApplicationName(ctx, entry.getKey()));
                // bugfix:上传OC中,关闭时间与开始时间相同,与关闭时间小于开始时间的问题
                if (startTime != null && endTime != null && Long.valueOf(endTime) > Long.valueOf(startTime)) {
                    ocArray.put(ocInfo);
                }
            }
        } catch (Throwable t) {

        }

        return ocArray;
    }

}
