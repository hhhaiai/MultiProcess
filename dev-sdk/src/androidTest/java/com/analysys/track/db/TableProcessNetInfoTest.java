package com.analysys.track.db;

import android.text.TextUtils;

import com.analysys.track.AnalsysTest;
import com.analysys.track.internal.impl.net.NetInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TableProcessNetInfoTest extends AnalsysTest {

    @Before
    public void setUp() throws Exception {
        DBHelper.getInstance(mContext).rebuildDB(null);
    }

    @Test
    public void testNet() {
        JSONArray jsonArray1 = new JSONArray();
        for (int i = 0; i < 100; i++) {
            jsonArray1.put("com.123." + i + "_" + "哈哈" + i);
        }
        TableProcess.getInstance(mContext).insertNet(jsonArray1.toString());
        JSONArray jsonArray2 = TableProcess.getInstance(mContext).selectNet(1024 * 1024);
        try {
            Assert.assertTrue(jsonArray1.equals(jsonArray2.get(0)));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        TableProcess.getInstance(mContext).deleteNet();
    }


    @Test
    public void testScanningInfo() {

        insertData();

        List<NetInfo.ScanningInfo> scanningInfos =
                TableProcess.getInstance(mContext).selectScanningInfoByPkg("com.hello.9", false);
        List<NetInfo.ScanningInfo> scanningInfos1 =
                TableProcess.getInstance(mContext).selectAllScanningInfos(1024);
        List<NetInfo.ScanningInfo> scanningloasInfos =
                TableProcess.getInstance(mContext).selectScanningInfoByPkg("com.hello.9", true);
        Assert.assertNotNull(scanningInfos);
        Assert.assertNotNull(scanningInfos1);
        Assert.assertNotNull(scanningloasInfos);
        Assert.assertTrue(scanningInfos.size() == 10);

        Assert.assertTrue(scanningloasInfos.size() == 1);
        for (int i = 0; i < scanningInfos.size(); i++) {
            Assert.assertTrue(scanningloasInfos.get(0).time >= scanningInfos.get(i).time);
        }
        List<NetInfo.ScanningInfo> scanningInfos2 =
                TableProcess.getInstance(mContext).selectAllScanningInfos(1024);
        TableProcess.getInstance(mContext).deleteScanningInfosById();
        List<NetInfo.ScanningInfo> scanningInfos3 =
                TableProcess.getInstance(mContext).selectAllScanningInfos(Integer.MAX_VALUE);
        TableProcess.getInstance(mContext).deleteScanningInfosById();
        List<NetInfo.ScanningInfo> scanningInfos4 =
                TableProcess.getInstance(mContext).selectAllScanningInfos(Integer.MAX_VALUE);

        Assert.assertTrue(scanningInfos4.isEmpty());
    }

    private void insertData() {
        for (int i = 1; i < 100; i++) {
            NetInfo.ScanningInfo info = new NetInfo.ScanningInfo();
            info.pkgname = "com.hello." + i % 10;
            info.appname = "天猫" + i % 10;
            info.api_4 = "com.api4." + i;
            info.proc_56 = new JSONObject();
            info.usm = "com.usm." + i;
            info.time = System.currentTimeMillis();
            if (i % 3 == 0 || i % 4 == 0) {
                info.tcpInfos = new ArrayList<>();
                for (int j = 0; j < i % 5; j++) {
                    NetInfo.TcpInfo tcpInfo = new NetInfo.TcpInfo();
                    tcpInfo.local_addr = "123.123.123.123:local_addr";
                    tcpInfo.remote_addr = "123.123.123.123:remote_addr";
                    tcpInfo.socket_type = "123.123.123.123:socket_type";
                    tcpInfo.protocol = "123.123.123.123:protocol";
                    info.tcpInfos.add(tcpInfo);
                }
            }
            TableProcess.getInstance(mContext).insertScanningInfo(info);
        }
    }

    @Test
    public void testData() {
        insertData();
        HashMap<String, NetInfo> map = new HashMap<>();
        List<NetInfo.ScanningInfo> scanningInfos =
                TableProcess.getInstance(mContext).selectAllScanningInfos(1024 * 1024);
        for (int i = 0; scanningInfos != null && i < scanningInfos.size(); i++) {
            NetInfo.ScanningInfo scanningInfo = (NetInfo.ScanningInfo) scanningInfos.get(i);
            String pkg = scanningInfo.pkgname;
            if (TextUtils.isEmpty(pkg)) {
                continue;
            }
            NetInfo netInfo = map.get(pkg);
            if (netInfo == null) {
                netInfo = new NetInfo();
                netInfo.pkgname = scanningInfo.pkgname;
                netInfo.appname = scanningInfo.appname;
                netInfo.scanningInfos = new ArrayList<>();
                map.put(pkg, netInfo);
            }
            netInfo.scanningInfos.add(scanningInfo);
        }
        JSONArray arr = new JSONArray();
        for (String pkg : map.keySet()) {
            arr.put(map.get(pkg).toJson());
        }
        Assert.assertNotNull(arr);
    }


}