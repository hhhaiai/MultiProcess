package com.hhhaiai.mpdemo;

import me.hhhaiai.ImpTask;
import me.hhhaiai.mptils.EContext;
import me.hhhaiai.mptils.MPSupport1;
import me.hhhaiai.mptils.MpLog;

public class ChinaPrint implements ImpTask {
    @Override
    public String getName() {
        return "[" + MPSupport1.getCurrentProcessName(EContext.getContext()) + "] I'm Chinese!";
    }

    @Override
    public void work() {
        MpLog.d(
                "ChinaPrint. work ------------["
                        + MPSupport1.getCurrentProcessName(EContext.getContext())
                        + "]------print msg");
    }
}
