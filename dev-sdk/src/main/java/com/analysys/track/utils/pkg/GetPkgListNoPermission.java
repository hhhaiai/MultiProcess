package com.analysys.track.utils.pkg;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.analysys.track.BuildConfig;
import com.analysys.track.utils.BugReportForTest;
import com.analysys.track.utils.reflectinon.EContextHelper;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @Copyright © 2021 sanbo Inc. All rights reserved.
 * @Description: 不要权限获取包名
 * @Version: 1.0
 * @Create: 2021/03/67 14:55:08
 * @author: sanbo
 */
public class GetPkgListNoPermission {

    public static class PkgInfo {
        private String packageName;
        private String appName;
        private int uid;

        public PkgInfo(String pkg, String name, int _uid) {
            this.packageName = pkg;
            this.appName = name;
            this.uid = _uid;
        }

        public String getPackageName() {
            return packageName;
        }

        public void setPackageName(String packageName) {
            this.packageName = packageName;
        }

        public String getAppName() {
            return appName;
        }

        public void setAppName(String appName) {
            this.appName = appName;
        }

        public int getUid() {
            return uid;
        }

        public void setUid(int uid) {
            this.uid = uid;
        }

        @Override
        public String toString() {
            return "[" + this.appName + "]---->" + this.packageName + "=====uid: " + this.uid;
        }
    }


    public List<PkgInfo> getPackageList(Context ctx) {
        CopyOnWriteArrayList<PkgInfo> pkgs = new CopyOnWriteArrayList<PkgInfo>();
        try {
//            EL.i("========================无需权限获取应用列表=========================");
            PackageManager pkgManager = EContextHelper.getContext(ctx).getPackageManager();
            String[] v2 = null;
//            int uid = 1000;
            // 1000以上有效
            int uid = -17;
            while (uid <= 19999) {
                v2 = pkgManager.getPackagesForUid(uid);
                if (v2 != null && v2.length > 0) {
                    PkgInfo pkg = null;
                    for (String pkgName : v2) {
                        try {
                            final PackageInfo pkgInfo = pkgManager.getPackageInfo(pkgName, 0);
                            if (pkgInfo == null) {
                                break;
                            }
                            CharSequence appName = pkgManager.getApplicationLabel(pkgManager.getApplicationInfo(pkgInfo.packageName, PackageManager.GET_META_DATA));
//                            EL.d("UID->[" + uid + "] 应用名称--->" + appName.toString() + "<---  pkg->(" + pkgInfo.packageName + ")");
                            pkg = new PkgInfo(pkgName, appName.toString(), uid);
                        } catch (Throwable e) {
                            if (e instanceof PackageManager.NameNotFoundException) {
//                                EL.i("发生异常（NameNotFoundException） 包名: " + pkgName + " ; UID: " + uid);
                            }
                            pkg = new PkgInfo(pkgName, "", uid);
                        } finally {
                            if (pkg != null) {
                                pkgs.add(pkg);
                            }
                            pkg = null;
                        }
                    }
                }
                uid++;
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(BuildConfig.tag_filetime, e);
            }
        }
        return pkgs;
    }

    /********************* get instance begin **************************/
    public static GetPkgListNoPermission getInstance() {
        return HLODER.INSTANCE;
    }

    private static class HLODER {
        private static final GetPkgListNoPermission INSTANCE = new GetPkgListNoPermission();
    }

    private GetPkgListNoPermission() {
    }
    /********************* get instance end **************************/
}
