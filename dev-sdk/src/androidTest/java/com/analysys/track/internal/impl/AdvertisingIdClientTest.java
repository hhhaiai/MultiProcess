package com.analysys.track.internal.impl;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import org.junit.Test;

import static org.junit.Assert.*;

public class AdvertisingIdClientTest {
    Context mContext = InstrumentationRegistry.getContext();

    @Test
    public void getAdvertisingIdInfo() {
        try {
            AdvertisingIdClient.getAdvertisingIdInfo(mContext);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}