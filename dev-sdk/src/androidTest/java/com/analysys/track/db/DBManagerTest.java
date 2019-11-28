package com.analysys.track.db;

import com.analysys.track.AnalsysTest;

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
        File f = mContext.getDatabasePath("ev2.data");
        if (f.exists()) {
            f.delete();
        }
    }

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
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                TableProcess.getInstance(mContext).insertNet("hetlo");
                TableProcess.getInstance(mContext).insertNet("her3lo");
                TableProcess.getInstance(mContext).insertNet("hegflo");
                TableProcess.getInstance(mContext).insertNet("hefdlo");
                TableProcess.getInstance(mContext).insertNet("hefdao");
                TableProcess.getInstance(mContext).insertTempId(hashCode() + "3213123");
                TableProcess.getInstance(mContext).insertTempId(hashCode() + "fd23");
                TableProcess.getInstance(mContext).insertTempId(hashCode() + "32sdfa3123");
            }
        };
        List<Thread> threads = new ArrayList<>(50);
        for (int i = 0; i < 50; i++) {
            Thread thread = new Thread(runnable);
            threads.add(thread);
            thread.start();
        }
        for (Thread thread :
                threads) {
            thread.join();
        }
    }

    @Test
    public void closeDB() {
    }
}