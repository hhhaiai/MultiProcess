package com.eguan.utils.thread;

import com.eguan.Constants;
import com.eguan.utils.commonutils.EgLog;

/**
 * @Copyright © 2017 Sanbo Inc. All rights reserved.
 * @Description:
 * @Create: 2017年6月6日 下午9:10:00
 * @Author: sanbo
 * @Version: 1.0
 */
public abstract class SafeRunnable implements Runnable {

    @Override
    public void run() {
        try {
            safeRun();
        } catch (Throwable th) {
            if (th != null)
                if (Constants.FLAG_DEBUG_INNER) {
                    EgLog.e(th);
                }
        }
    }

    public abstract void safeRun();
}