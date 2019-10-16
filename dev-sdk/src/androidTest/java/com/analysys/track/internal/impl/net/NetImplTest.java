package com.analysys.track.internal.impl.net;

import com.analysys.track.AnalsysTest;
import com.analysys.track.db.TableProcess;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;

public class NetImplTest extends AnalsysTest {

    @Test
    public void getPackagesForUid() {
    }

    @Test
    public void getUidFromNet() {
        HashSet<NetInfo> infos = NetImpl.getInstance(mContext).getNetInfo();
        JSONArray array = new JSONArray();
        for (NetInfo info :
                infos) {
            array.put(info.toJson());
        }
        Assert.assertNotNull(array.toString());

        TableProcess.getInstance(mContext).insertNet(array);
        array = TableProcess.getInstance(mContext).selectNet(1 * 1024 * 1024);
        Assert.assertNotNull(array.toString());
        TableProcess.getInstance(mContext).deleteNet();
        array = TableProcess.getInstance(mContext).selectNet(1 * 1024 * 1024);

        Assert.assertNotNull(array.toString());
    }

    @Test
    public void testIpv6() {
        String hello = NetImpl.getInstance(mContext).ipv6("0000000000000000FFFF0000A407A8C0:B929");

        Assert.assertNotNull(hello);
    }
}