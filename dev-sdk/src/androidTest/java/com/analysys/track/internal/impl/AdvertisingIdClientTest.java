package com.analysys.track.internal.impl;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.analysys.track.AnalsysTest;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class AdvertisingIdClientTest extends AnalsysTest {
    Context mContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

    @Test
    public void getAdvertisingIdInfo() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    AdvertisingIdClient.AdInfo info1 = AdvertisingIdClient.getAdvertisingIdInfo(mContext);
                    AdvertisingIdClient.AdInfo info2 = AdvertisingIdClient.getAdvertisingIdInfo(mContext);

                    assertNotNull("谷歌广告id，部分机型无法获取是已知的", info1);
                    assertEquals("谷歌广告id两次获取不一致", info1.getId(), info2.getId());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}