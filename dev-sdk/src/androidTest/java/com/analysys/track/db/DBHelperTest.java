package com.analysys.track.db;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import junit.framework.Assert;

public class DBHelperTest {
    Context mContext = InstrumentationRegistry.getContext();

    @org.junit.Test
    public void getInstance() {
        DBHelper a = DBHelper.getInstance(mContext);
        DBHelper b = DBHelper.getInstance(mContext);
        Assert.assertEquals(a, b);
//        dbHelper = DBHelper(RuntimeEnvironment.application)
    }

    @org.junit.Test
    public void onCreate() {
//        DBHelper.getInstance(mContext).onCreate();
    }

    @org.junit.Test
    public void onUpgrade() {
    }

    @org.junit.Test
    public void recreateTables() {
    }

    @org.junit.Test
    public void rebuildDB() {
    }
}