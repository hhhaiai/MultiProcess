package com.analysys.track.internal.impl;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import com.analysys.track.AnalsysTest;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.internal.content.UploadKey;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class AppSnapshotImplTest extends AnalsysTest {
    AppSnapshotImpl appSnapshot;

    @Before
    public void setUp() throws Exception {

        appSnapshot = AppSnapshotImpl.getInstance(mContext);
    }

    @After
    public void tearDown() throws Exception {
        appSnapshot = null;
    }

    @Test
    public void getAppDebugStatus() throws Exception {
        List<JSONObject> list = appSnapshot.getAppDebugStatus();
        assertNotNull(list);
        assertTrue(list.size() > 0);

        for (int i = 0; i < list.size(); i++) {
            String app = list.get(i).getString(EGContext.TEXT_DEBUG_APP);
            Boolean debug = list.get(i).getBoolean(EGContext.TEXT_DEBUG_STATUS);

            assertNotNull(app);
            assertNotEquals("", app);

            assertNotNull(debug);
        }
    }

    @Test
    public void getAppType() {
        assertEquals(appSnapshot.getAppType("com.android.contacts"), UploadKey.OCInfo.APPLICATIONTYPE_SYSTEM_APP);
        assertEquals(appSnapshot.getAppType("com.android.calendar"), UploadKey.OCInfo.APPLICATIONTYPE_SYSTEM_APP);
        assertEquals(appSnapshot.getAppType("com.android.settings"), UploadKey.OCInfo.APPLICATIONTYPE_SYSTEM_APP);
        assertEquals(appSnapshot.getAppType("com.aaa.bbb"), UploadKey.OCInfo.APPLICATIONTYPE_THREE_APP);
        assertEquals(appSnapshot.getAppType(mContext.getPackageName()), UploadKey.OCInfo.APPLICATIONTYPE_THREE_APP);
        assertEquals(appSnapshot.getAppType(mContext.getClass().getName()), UploadKey.OCInfo.APPLICATIONTYPE_THREE_APP);
        assertEquals(appSnapshot.getAppType("\n"), UploadKey.OCInfo.APPLICATIONTYPE_THREE_APP);
        assertEquals(appSnapshot.getAppType("1234"), UploadKey.OCInfo.APPLICATIONTYPE_THREE_APP);
        assertEquals(appSnapshot.getAppType("fdsfsdf"), UploadKey.OCInfo.APPLICATIONTYPE_THREE_APP);
        assertEquals(appSnapshot.getAppType("中文"), UploadKey.OCInfo.APPLICATIONTYPE_THREE_APP);
        assertEquals(appSnapshot.getAppType(""), UploadKey.OCInfo.APPLICATIONTYPE_THREE_APP);
        assertEquals(appSnapshot.getAppType("null"), UploadKey.OCInfo.APPLICATIONTYPE_THREE_APP);
        assertEquals(appSnapshot.getAppType(null), UploadKey.OCInfo.APPLICATIONTYPE_THREE_APP);
    }

    @Test
    public void isSystemApps() {
        assertTrue(appSnapshot.isSystemApps("com.android.contacts"));
        assertTrue(appSnapshot.isSystemApps("com.android.calendar"));
        assertTrue(appSnapshot.isSystemApps("com.android.settings"));
        assertFalse(appSnapshot.isSystemApps("com.aaa.bbb"));
        assertFalse(appSnapshot.isSystemApps(mContext.getPackageName()));
        assertFalse(appSnapshot.isSystemApps(mContext.getClass().getName()));
        assertFalse(appSnapshot.isSystemApps("\n"));
        assertFalse(appSnapshot.isSystemApps("1234"));
        assertFalse(appSnapshot.isSystemApps("fdsfsdf"));
        assertFalse(appSnapshot.isSystemApps("中文"));
        assertFalse(appSnapshot.isSystemApps(""));
        assertFalse(appSnapshot.isSystemApps("null"));
        assertFalse(appSnapshot.isSystemApps(null));
    }

    @Test
    public void getInstance() {
        AppSnapshotImpl snapshot = AppSnapshotImpl.getInstance(mContext);
        assertNotNull(snapshot);
        assertEquals(snapshot, appSnapshot);
    }
}