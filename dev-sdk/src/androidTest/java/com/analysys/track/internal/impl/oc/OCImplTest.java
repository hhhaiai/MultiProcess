package com.analysys.track.internal.impl.oc;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import com.analysys.track.internal.work.ECallBack;

import org.json.JSONArray;
import org.junit.Test;

public class OCImplTest {
    Context mContext = InstrumentationRegistry.getContext();

    @Test
    public void processOCMsg() {
        OCImpl.getInstance(mContext).processOCMsg(null);
        OCImpl.getInstance(mContext).processOCMsg(new ECallBack() {
            @Override
            public void onProcessed() {
            }
        });
    }

    @Test
    public void processOC() {
        OCImpl.getInstance(mContext).processOC();
    }

    @Test
    public void getInfoByVersion() {
        OCImpl.getInstance(mContext).getInfoByVersion(false, false);
        OCImpl.getInstance(mContext).getInfoByVersion(true, false);
        OCImpl.getInstance(mContext).getInfoByVersion(false, true);
        OCImpl.getInstance(mContext).getInfoByVersion(true, true);
    }

    @Test
    public void getAliveAppByProc() {
        JSONArray arr = new JSONArray();
        arr.put("com.device");
        arr.put("com.alipay.hulu");
        OCImpl.getInstance(mContext).getAliveAppByProc(arr);
    }

    @Test
    public void processSignalPkgName() {

    }

    @Test
    public void cacheDataToMemory() {
    }

    @Test
    public void processOCByUsageStatsManager() {
    }

    @Test
    public void getOCDurTime() {
    }

    @Test
    public void processOCWhenScreenChange() {
    }

    @Test
    public void processScreenOff() {
    }

    @Test
    public void getInstance() {
    }
}