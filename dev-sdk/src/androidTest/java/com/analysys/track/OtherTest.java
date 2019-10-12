package com.analysys.track;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.FileObserver;
import android.support.annotation.Nullable;
import android.util.Log;

import com.analysys.track.utils.ShellUtils;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OtherTest extends AnalsysTest {
    @Test
    public void getPackagesForUid() {
        String result[] = {
                "cat /proc/net/tcp  \n",
                "cat /proc/net/tcp6 \n",
                "cat /proc/net/udp  \n",
                "cat /proc/net/udp6 \n",
                "cat /proc/net/raw  \n",
                "cat /proc/net/raw6 \n",
        };
        try {
            HashSet<String> pkgs = new HashSet<String>();
            for (String cmd : result
            ) {
                pkgs.addAll(getPkgForNet(cmd));
            }
            for (String s : pkgs) {
                PackageManager pm = mContext.getPackageManager();
                ApplicationInfo appInfo = pm.getApplicationInfo(s, PackageManager.GET_META_DATA);
                CharSequence lable = appInfo.loadLabel(pm);
                Assert.assertNotNull(lable);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 只支持输入
     * cat /proc/net/tcp
     * cat /proc/net/tcp6
     * cat /proc/net/udp
     * cat /proc/net/udp6
     * cat /proc/net/raw
     * cat /proc/net/raw6
     *
     * @return
     */
    private HashSet<String> getPkgForNet(String cmd) {
        String result = ShellUtils.shell(cmd);
        String[] urdStrs = result.split("\n");
        Pattern pattern = Pattern.compile("(^\\s+[^\\s]+\\s+[^\\s]+\\s+[^\\s]+\\s+[^\\s]+\\s+[^\\s]+\\s+[^\\s]+\\s+[^\\s]+\\s+)([^\\s]+)");


        HashSet<String> pkgs = new HashSet<>();
        for (int i = 1; i < urdStrs.length; i++) {
            Matcher matcher = pattern.matcher(urdStrs[1]);
            matcher.find();
            String urd = matcher.group(2).trim();
            String[] pn = mContext.getPackageManager()
                    .getPackagesForUid(Integer.valueOf(urd));
            if (pn != null) {
                pkgs.addAll(Arrays.asList(pn));
            }
        }
        return pkgs;
    }
}
