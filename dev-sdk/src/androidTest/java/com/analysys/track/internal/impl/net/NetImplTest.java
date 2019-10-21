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
        NetImpl.getInstance(mContext).getNetInfo();
        JSONArray array = null;

        array = TableProcess.getInstance(mContext).selectNet(1 * 1024 * 1024);
        Assert.assertTrue(array.length() > 0);
        TableProcess.getInstance(mContext).deleteNet();
        array = TableProcess.getInstance(mContext).selectNet(1 * 1024 * 1024);

        Assert.assertEquals(array.length(), 0);
    }

}