package com.analysys.track;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.analysys.track.utils.ShellUtils;

import org.junit.Assert;
import org.junit.Test;

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
                pkgs.addAll(getUidFromNet(cmd));
            }
            for (String s : pkgs) {
                PackageManager pm = mContext.getPackageManager();
                ApplicationInfo appInfo = pm.getApplicationInfo(s, PackageManager.GET_META_DATA);
                CharSequence lable = appInfo.loadLabel(pm);
                Intent intent = pm.getLaunchIntentForPackage(s);
                if (intent != null) {
                    Assert.assertNotNull(lable);
                }
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
    private HashSet<String> getUidFromNet(String cmd) {
        String result = ShellUtils.shell(cmd);
        String[] urdStrs = new String[0];
        if (result != null) {
            urdStrs = result.split("\n");
        }
        Pattern pattern = Pattern.compile("(^\\s+[^\\s]+\\s+[^\\s]+\\s+[^\\s]+\\s+[^\\s]+\\s+[^\\s]+\\s+[^\\s]+\\s+[^\\s]+\\s+)([^\\s]+)");
        HashSet<String> pkgs = new HashSet<>();
        for (int i = 1; i < urdStrs.length; i++) {
            Matcher matcher = pattern.matcher(urdStrs[1]);
            if (!matcher.find() || matcher.groupCount() < 2) {
                continue;
            }
            String urd = matcher.group(2).trim();
            try {
                String[] pn = mContext.getPackageManager()
                        .getPackagesForUid(Integer.valueOf(urd));
                if (pn != null) {
                    pkgs.addAll(Arrays.asList(pn));
                }
            } catch (Throwable ignored) {
            }
        }
        return pkgs;
    }
}
