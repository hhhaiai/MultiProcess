package com.analysys.track.internal.impl.net;

import com.analysys.track.AnalsysTest;
import com.analysys.track.db.TableProcess;
import com.analysys.track.utils.ShellUtils;

import org.json.JSONArray;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;

public class NetImplTest extends AnalsysTest {

    @Test
    public void getPackagesForUid() {
    }

    @Test
    public void getUidFromNet() {
        for (int i = 0; i < 100; i++) {
            HashMap<String, NetInfo> infos = NetImpl.getInstance(mContext).getNetInfo();
            Assert.assertTrue(infos.size() > 0);
        }
        JSONArray array = null;
        array = TableProcess.getInstance(mContext).selectNet(1 * 1024 * 1024);
        Assert.assertTrue(array != null);
        TableProcess.getInstance(mContext).deleteNet();
        array = TableProcess.getInstance(mContext).selectNet(1 * 1024 * 1024);
        Assert.assertEquals(array.length(), 0);
    }

    @Test
    public void timeTestShell() {
        for (int i = 0; i < 1; i++) {
            String[] cmds = {
                    "cat /proc/net/tcp",
                    "cat /proc/net/tcp6",
                    "cat /proc/net/udp",
                    "cat /proc/net/udp6",
                    "cat /proc/net/raw",
                    "cat /proc/net/raw6",
            };
            for (String cmd : cmds) {
                String result = ShellUtils.shell(cmd.concat(" \n"));
                Assert.assertNotNull(result);
            }
        }

    }

//    @Test
//    public void timeTestFile() {
//        for (int i = 0; i < 1; i++) {
//            String[] cmds = {
//                    "/proc/net/tcp",
//                    "/proc/net/tcp6",
//                    "/proc/net/udp",
//                    "/proc/net/udp6",
//                    "/proc/net/raw",
//                    "/proc/net/raw6",
//            };
//            for (String cmd : cmds) {
//                String result = NetImpl.getInstance(mContext).runShell(cmd);
//                Assert.assertNotNull(result);
//            }
//        }
//    }

}