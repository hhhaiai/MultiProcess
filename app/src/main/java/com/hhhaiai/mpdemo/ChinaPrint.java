package com.hhhaiai.mpdemo;

import me.hhhaiai.ImpTask;
import me.hhhaiai.utils.EContext;
import me.hhhaiai.utils.MPSupport1;
import me.hhhaiai.utils.MpLog;

public class ChinaPrint implements ImpTask {
    @Override
    public String getName() {
        return "[" + MPSupport1.getCurrentProcessName(EContext.getContext()) + "] I'm Chinese!";
    }

    @Override
    public void work() {
        MpLog.d("ChinaPrint. work ------------[" + MPSupport1.getCurrentProcessName(EContext.getContext()) + "]------print msg");
    }
}
