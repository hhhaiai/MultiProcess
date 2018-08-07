package com.eguan.monitor.imp;


import android.content.Context;

import com.eguan.monitor.Constants;
import com.eguan.monitor.commonutils.EgLog;
import com.eguan.monitor.commonutils.SPUtil;

public class ProcessTimeManager {

    private ProcessTimeManager() {
    }

    public static ProcessTimeManager getInstance() {
        return Holder.INSTANCE;
    }

    private static class Holder {
        private static final ProcessTimeManager INSTANCE = new ProcessTimeManager();
    }

    /**
     * 记录PL信息
     *
     * @param context
     */
    public void setProcessTime(Context context) {

        SPUtil spUtil = SPUtil.getInstance(context);
        long startTime = spUtil.getStartTime();
        long endTime = spUtil.getEndTime();
        long spanTime = System.currentTimeMillis() - endTime;

        if (spanTime < Constants.SPAN_TIME) {

            return;
        }
        if (startTime != 0 && endTime != 0) {
            long data = spUtil.getProcessLifecycle();

            long time = (endTime - startTime) / 1000;
            data += time;

            EgLog.e("PL信息-------" + data);
            spUtil.setProcessLifecycle(data);
        }
        spUtil.setStartTime(System.currentTimeMillis());

    }
}
