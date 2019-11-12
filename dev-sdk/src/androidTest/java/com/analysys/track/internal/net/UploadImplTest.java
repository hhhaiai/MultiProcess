package com.analysys.track.internal.net;

import com.analysys.track.AnalsysTest;
import com.analysys.track.db.DBHelper;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.*;

public class UploadImplTest extends AnalsysTest {

    @Test
    public void upload() {
        UploadImpl.getInstance(mContext).upload();
    }

    @Test
    public void doUploadImpl() {
        UploadImpl.getInstance(mContext).doUploadImpl();
    }

    @Test
    public void messageEncrypt() {
        UploadImpl.getInstance(mContext).messageEncrypt("._-$,;~()/ ");
    }

    @Test
    public void handleUpload() {
    }

    @Test
    public void getInstance() throws InterruptedException {
        final HashSet<Object> helpers = new HashSet();
        Runnable run = new Runnable() {
            @Override
            public void run() {
                UploadImpl ins = UploadImpl.getInstance(mContext);
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
    public void processMsgFromServer() {
        String json = "{\n" +
                "  \"code\": 500,\n" +
                "  \"policy\": {\n" +
                "    \"policyVer\": \"20190907142023\",\n" +
                "    \"serverDelay\": 10,\n" +
                "    \"fail\": {\n" +
                "      \"failCount\": 2,\n" +
                "      \"failTryDelay\": 3600\n" +
                "    },\n" +
                "    \"timerInterval\": 60,\n" +
                "    \"eventCount\": 10,\n" +
                "    \"useRTP\": 0,\n" +
                "    \"useRTL\": 0,\n" +
                "    \"remoteIp\": 0,\n" +
                "    \"uploadSD\": 1,\n" +
                "    \"mergeInterval\": 10,\n" +
                "    \"minDuration\": 10,\n" +
                "    \"maxDuration\": 18000,\n" +
                "    \"domainUpdateTimes\": 1,\n" +
                "    \"servicePull\": [],\n" +
                "    \"appPull\": [],\n" +
                "    \"ocRule\": {\n" +
                "      \"mergeInterval\": 10,\n" +
                "      \"minDuration\": 10\n" +
                "    }\n" +
                "  }\n" +
                "}";
        invokeMethod(UploadImpl.getInstance(mContext), UploadImpl.class.getName(), "processMsgFromServer", json);
    }
}