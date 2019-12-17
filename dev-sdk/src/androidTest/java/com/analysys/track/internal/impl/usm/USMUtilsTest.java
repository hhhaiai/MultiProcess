package com.analysys.track.internal.impl.usm;

import android.app.usage.UsageEvents;

import com.analysys.track.AnalsysTest;
import com.analysys.track.internal.content.EGContext;

import org.junit.Assert;
import org.junit.Test;

public class USMUtilsTest extends AnalsysTest {

    @Test
    public void isOption() {
    }

    @Test
    public void openUSMSetting() {
    }

    @Test
    public void getUsageStats() {
    }

    @Test
    public void getUsageStatsByAPI() {
    }

    @Test
    public void getUsageStatsByInvoke() {
    }

    @Test
    public void getUsageEvents() {
        for (int i = 0; i < 7; i++) {
            long stime = EGContext.TIME_HOUR * i;
            long time = 0;
            long timeEnd = 0;
            int count = 0;
            UsageEvents usageEvents = (UsageEvents) USMUtils.getUsageEvents(System.currentTimeMillis() - stime, System.currentTimeMillis(), mContext);
            if (usageEvents == null) {
              continue;
            }
            while (usageEvents.hasNextEvent()) {
                UsageEvents.Event event = new UsageEvents.Event();
                usageEvents.getNextEvent(event);
                if (count == 0) {
                    time = event.getTimeStamp();
                }
                count++;
                timeEnd = event.getTimeStamp();
            }

            Assert.assertTrue(timeEnd - time <= stime);
        }
    }

    @Test
    public void getUsageEventsByInvoke() {
    }

    @Test
    public void getAppPackageList() {
    }

    @Test
    public void getMethod() {
    }

    @Test
    public void getField() {
    }
}