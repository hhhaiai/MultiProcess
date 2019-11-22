package com.analysys.track.internal.impl;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class AdvertisingIdClientTest {
    Context mContext = InstrumentationRegistry.getContext();

    @Test
    public void getAdvertisingIdInfo() {
        try {
            AdvertisingIdClient.AdInfo info1 = AdvertisingIdClient.getAdvertisingIdInfo(mContext);
            AdvertisingIdClient.AdInfo info2 = AdvertisingIdClient.getAdvertisingIdInfo(mContext);

            assertNotNull(info1);
            assertEquals(info1.getId(), info2.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}