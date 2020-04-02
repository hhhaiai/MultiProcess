package com.analysys.track.impl;

import com.analysys.track.AnalsysTest;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class CusHotTransformTest extends AnalsysTest {

    @Test
    public void getInstance() throws InterruptedException {
        final HashSet<Object> helpers = new HashSet<>();
        Runnable run = new Runnable() {
            @Override
            public void run() {
                helpers.add(CusHotTransform.getInstance(mContext));
            }
        };
        List<Thread> threads = new ArrayList<>(50);
        for (int i = 0; i < 20; i++) {
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
}