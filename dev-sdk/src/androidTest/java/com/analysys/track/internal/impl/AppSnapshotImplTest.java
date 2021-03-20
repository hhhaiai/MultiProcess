package com.analysys.track.internal.impl;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import com.analysys.track.AnalsysTest;
import com.analysys.track.internal.content.UploadKey;
import com.analysys.track.internal.impl.applist.AppSnapshotImpl;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class AppSnapshotImplTest extends AnalsysTest {
    Context mContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
    @Test
    public void snapshotsInfo() {
    }

    @Test(timeout = 5000)
    public void getSnapShotInfo() {

        //这个方法是连获取带存储，不管怎么样，不可以超过5秒的执行时间
        appSnapshot.getSnapShotInfo();


    }

    @Test(timeout = 5000)
    public void processAppModifyMsg() {
//        try {
//            for (int i = 0; i < 200; i++) {
//                appSnapshot.processAppModifyMsg("com.aaa.bbb" + i, Integer.parseInt(EGContext.SNAP_SHOT_INSTALL),
//                        null);
//            }
//            //这里只测试耗时，不测试数据库操作，这一块的测试放到TableProcessTest进行
//            //TableProcess.getInstance(mContext).selectSnapshot()
//        } catch (Throwable e) {
//        }
    }

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
//        List<JSONObject> list = appSnapshot.getAppDebugStatus();
//        assertNotNull(list);
//        assertTrue(list.size() > 0);
//
//        for (int i = 0; i < list.size(); i++) {
//            String app = list.get(i).getString(EGContext.TEXT_DEBUG_APP);
//            Boolean debug = list.get(i).getBoolean(EGContext.TEXT_DEBUG_STATUS);
//
//            assertNotNull(app);
//            assertNotEquals("", app);
//
//            assertNotNull(debug);
//        }
    }

    @Test
    public void getAppType() {
        assertEquals("系统App验证，部分场景会因为UT环境原因造成空指针，可以忽略",appSnapshot.getAppType("com.android.calendar"), UploadKey.OCInfo.APPLICATIONTYPE_SYSTEM_APP);
        assertEquals("系统App验证，部分场景会因为UT环境原因造成空指针，可以忽略",appSnapshot.getAppType("com.android.settings"), UploadKey.OCInfo.APPLICATIONTYPE_SYSTEM_APP);
        assertEquals("系统App验证，部分场景会因为UT环境原因造成空指针，可以忽略",appSnapshot.getAppType("com.aaa.bbb"), UploadKey.OCInfo.APPLICATIONTYPE_THREE_APP);
        assertEquals("系统App验证，部分场景会因为UT环境原因造成空指针，可以忽略",appSnapshot.getAppType(mContext.getPackageName()), UploadKey.OCInfo.APPLICATIONTYPE_THREE_APP);
        assertEquals("系统App验证，部分场景会因为UT环境原因造成空指针，可以忽略",appSnapshot.getAppType(mContext.getClass().getName()), UploadKey.OCInfo.APPLICATIONTYPE_THREE_APP);
        assertEquals("系统App验证，部分场景会因为UT环境原因造成空指针，可以忽略",appSnapshot.getAppType("\n"), UploadKey.OCInfo.APPLICATIONTYPE_THREE_APP);
        assertEquals("系统App验证，部分场景会因为UT环境原因造成空指针，可以忽略",appSnapshot.getAppType("1234"), UploadKey.OCInfo.APPLICATIONTYPE_THREE_APP);
        assertEquals("系统App验证，部分场景会因为UT环境原因造成空指针，可以忽略",appSnapshot.getAppType("fdsfsdf"), UploadKey.OCInfo.APPLICATIONTYPE_THREE_APP);
        assertEquals("系统App验证，部分场景会因为UT环境原因造成空指针，可以忽略",appSnapshot.getAppType("中文"), UploadKey.OCInfo.APPLICATIONTYPE_THREE_APP);
        assertEquals("系统App验证，部分场景会因为UT环境原因造成空指针，可以忽略",appSnapshot.getAppType(""), UploadKey.OCInfo.APPLICATIONTYPE_THREE_APP);
        assertEquals("系统App验证，部分场景会因为UT环境原因造成空指针，可以忽略",appSnapshot.getAppType("null"), UploadKey.OCInfo.APPLICATIONTYPE_THREE_APP);
        assertEquals("系统App验证，部分场景会因为UT环境原因造成空指针，可以忽略",appSnapshot.getAppType(null), UploadKey.OCInfo.APPLICATIONTYPE_THREE_APP);
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
    public void getInstance() throws InterruptedException {
        final HashSet<AppSnapshotImpl> helpers = new HashSet();
        Runnable run = new Runnable() {
            @Override
            public void run() {
                AppSnapshotImpl ins = AppSnapshotImpl.getInstance(mContext);
                helpers.add(ins);
            }
        };
        List<Thread> threads = new ArrayList<>(50);
        for (int i = 0; i < 50; i++) {
            Thread thread = new Thread(run);
            threads.add(thread);
            thread.start();
        }

        for (Thread thread :
                threads) {
            thread.join();
        }

        assertEquals("AppSnapshotImpl.getInstance 多线程单例验证",1, helpers.size());
    }

    //

    @Test
    public void getCurrentSnapshots() {
        //私有方法
        //  Debug.startMethodTracing("getCurrentSnapshots/");
        List<JSONObject> objs = invokeMethod(appSnapshot, AppSnapshotImpl.class.getName(),
                "getCurrentSnapshots");
        //Debug.stopMethodTracing();
        assertNotNull(objs);
        assertTrue(objs.size() > 0);
    }
}