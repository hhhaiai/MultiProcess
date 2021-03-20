package com.analysys.track.db;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import com.analysys.track.AnalsysTest;

import org.json.JSONArray;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class DBManagerTest extends AnalsysTest {
    @Before
    public void bif() {
//        File f = mContext.getDatabasePath("ev2.data");
//        if (f.exists()) {
//            f.delete();
//        }
    }
    Context mContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

    @Test
    public void getInstance() throws InterruptedException {
        final HashSet<DBManager> helpers = new HashSet();
        Runnable run = new Runnable() {
            @Override
            public void run() {
                helpers.add(DBManager.getInstance(mContext));
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

        assertEquals(1, helpers.size());
    }

    @Test
    public void openDB() throws InterruptedException {
       // TableProcess.getInstance(mContext).deleteNet();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep((long) (Math.random() * 100 + 1));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                JSONArray jsonArray = new JSONArray();
                jsonArray.put(hashCode() + "-" + Math.random() + "");
                TableProcess.getInstance(mContext).insertNet(jsonArray.toString());
            }
        };
        Runnable delRun = new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep((long) (Math.random() * 20 + 1));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                File f = mContext.getDatabasePath("ev2.data");
                if (f.exists()) {
                    f.delete();
                }
            }
        };
        List<Thread> threads = new ArrayList<>(50);
        for (int i = 0; i < 50; i++) {
            Thread thread = new Thread(runnable);
            threads.add(thread);
            thread.start();
        }
//        Thread del = new Thread(delRun);
//        threads.add(del);
//        del.start();
        for (Thread thread :
                threads) {
            thread.join();
        }
        JSONArray jsonArray = TableProcess.getInstance(mContext).selectNet(Integer.MAX_VALUE);
        Assert.assertNotNull(jsonArray);
    }

    @Test
    public void closeDB() {
    }
}