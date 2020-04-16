package com.analysys.track.internal.impl.usm;

import com.analysys.track.internal.content.EGContext;

public class SyncTime {
    private volatile long start;
    private volatile long end;

    public SyncTime(long s, long e) {
        this.start = s;
        this.end = e;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public SyncTime invoke() {
        if (end < 0) {
            end = System.currentTimeMillis();
        }
        if (start < 0) {
            start = end - EGContext.TIME_HOUR * 20;
        }
        if (start > end) {
            end = start;
            start = end - 20 * EGContext.TIME_HOUR;
        }
        if (end - start >= EGContext.TIME_HOUR * 20) {
            start = end - EGContext.TIME_HOUR * 20;
        }
        return this;
    }
}
