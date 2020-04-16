package com.analysys.track.internal.net;

import com.analysys.track.AnalsysTest;
import com.analysys.track.internal.content.UploadKey;
import com.analysys.track.internal.impl.usm.USMImpl;
import com.analysys.track.utils.sp.SPHelper;

import org.json.JSONArray;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;

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

    @Test(timeout = 30 * 1000)
    public void getUsmData() {
        //USM 可用,允许上传
        if (SPHelper.getBooleanValueFromSP(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_USM, true)) {
            JSONArray usmJson = USMImpl.getUSMInfo(mContext);
            Assert.assertNotNull("usmJson非空判断", usmJson);
            Assert.assertTrue("usmJson个数大于0判断", usmJson.length() > 0);
        } else {
            Assert.fail("不允许上传错误");
        }

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

    @Test
    public void getUSMData() {
        //USM 可用,允许上传
        boolean isUploadUSM = SPHelper.getBooleanValueFromSP(mContext, UploadKey.Response.RES_POLICY_MODULE_CL_USM, true);
        Assert.assertEquals("是否允许上传", isUploadUSM, true);
        JSONArray usmJson = USMImpl.getUSMInfo(mContext);
        Assert.assertNotNull(usmJson);
        Assert.assertTrue("USM信息获取长度大于0", usmJson.length() > 0);
    }
}