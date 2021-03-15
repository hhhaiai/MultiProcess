package com.analysys.track.internal.impl.ftime;

import android.content.Context;

import com.analysys.track.utils.MDate;
import com.analysys.track.utils.pkg.PkgList;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * @Copyright © 2020 analsys Inc. All rights reserved.
 * @Description: read file
 * @Version: 1.0
 * @Create: 2020-11-17 11:58:02
 * @author: Administrator
 */
public class LastModifyByFile {

    public static class AppTime {
        private String sPackageName;
        private long lastAliveTime;

        public String getPackageName() {
            return sPackageName;
        }

        public long getLastAliveTime() {
            return lastAliveTime;
        }

        public AppTime(String __pkg, long __time) {
            this.sPackageName = __pkg;
            this.lastAliveTime = __time;
        }

        @Override
        public String toString() {
            return String.format("[%s]---->%s ", sPackageName, MDate.getDateFromTimestamp(lastAliveTime));
        }
    }

    public static List<AppTime> getLastAliveTime(Context context) {

        List<String> pkgs = PkgList.getInstance(context).getAppPackageList();
        List<AppTime> list = new ArrayList<AppTime>();
        for (String pkg : pkgs) {
            long filesTime = new File("/sdcard/Android/data/" + pkg + "/files").lastModified();
            long cacheTime = new File("/sdcard/Android/data/" + pkg + "/cache").lastModified();
            long time = Math.max(filesTime, cacheTime);
            filesTime = new File("/data/data/" + pkg + "/files").lastModified();
            time = Math.max(filesTime, time);
            cacheTime = new File("/data/data/" + pkg + "/cache").lastModified();

            time = Math.max(cacheTime, time);
            if (time == 0) {
                continue;
            }
            list.add(new AppTime(pkg, time));
        }

        Collections.sort(list, new Comparator<AppTime>() {
            @Override
            public int compare(AppTime at1, AppTime at2) {
                return (int) (at2.lastAliveTime / 1000 - at1.lastAliveTime / 1000);
            }
        });

        return list;
    }
}
