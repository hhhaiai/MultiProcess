package com.analysys.track.internal.impl.ftime;

import android.content.Context;
import android.text.TextUtils;

import com.analysys.track.utils.MDate;
import com.analysys.track.utils.pkg.GetPkgListNoPermission;

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
        private String packageName;
        private long time;
        private String appName;
        private int uid;

        public AppTime(String pkg, long time) {
            this.packageName = pkg;
            this.time = time;
        }

        public AppTime(String __pkg, long __time, String __appName, int __uid) {
            this.packageName = __pkg;
            this.time = __time;
            this.appName = __appName;
            this.uid = __uid;
        }

        @Override
        public String toString() {

            if (TextUtils.isEmpty(appName)) {
                return String.format("[%s]---->%s ", packageName, MDate.formatLongTimeToDate(time));
            } else {
                return String.format("%s [%s]---[%d] ---->%s ", appName, packageName, uid, MDate.formatLongTimeToDate(time));
            }
        }
    }

    public static List<AppTime> getLatestApp(Context context) {

        List<GetPkgListNoPermission.PkgInfo> pkgs = GetPkgListNoPermission.getInstance().getPackageList(context);

//        EL.i("==============================文件访问末次修改时间======================");
        List<AppTime> list = new ArrayList<>();
        for (GetPkgListNoPermission.PkgInfo pkgInfo : pkgs) {
            String pkg = pkgInfo.getPackageName();
            String app = pkgInfo.getAppName();
            int uid = pkgInfo.getUid();

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
//            EL.i(pkg.toString() + "--------->" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(time)));
            list.add(new AppTime(pkg, time, app, uid));
        }

        Collections.sort(list, new Comparator<AppTime>() {
            @Override
            public int compare(AppTime at1, AppTime at2) {
                return (int) (at2.time / 1000 - at1.time / 1000);
            }
        });

        return list;
    }
}
