package com.analysys.track.db;

import com.analysys.track.AnalsysTest;

import org.junit.Test;
import org.junit.internal.runners.statements.RunAfters;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

import static org.junit.Assert.*;

public class DBHelperTest extends AnalsysTest {
    @Test
    public void getInstance() throws InterruptedException {
        final HashSet<DBHelper> helpers = new HashSet<>();
        Runnable run = new Runnable() {
            @Override
            public void run() {
                DBHelper ins = DBHelper.getInstance(mContext);
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

        assertEquals(1, helpers.size());

    }

    @Test
    public void onCreate() {
    }

    @Test
    public void onUpgrade() {
    }

    @Test
    public void recreateTables() {
    }

    @Test
    public void rebuildDB() {
    }
}