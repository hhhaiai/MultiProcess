package cn.analysys.casedemo.cases.logics;

import android.annotation.SuppressLint;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.cslib.defcase.ETestCase;
import com.cslib.utils.L;

import cn.analysys.casedemo.utils.SDKHelper;
import cn.analysys.casedemo.utils.Woo;


/**
 * @Copyright © 2021 analsys Inc. All rights reserved.
 * @Description: SDcard缓存目录的最新缓存时间
 * @Version: 1.0
 * @Create: 2021/03/74 14:29:04
 * @author: sanbo
 */
public class PkgInfoCase extends ETestCase {


    public PkgInfoCase() {
        super("根据包名获取应用详情");
    }

    @Override
    public void prepare() {
    }

    @Override
    public boolean predicate() {
        new Thread(() -> {
            try {
                gotoWork();
            } catch (Throwable e) {
                L.e();
            }
        }).start();


        return true;
    }

    private void gotoWork() {
        PackageManager pm = SDKHelper.getContext().getPackageManager();
        // 不晓得包名为什么会获取失败
        pkg(pm, "com.chaozh.ireader");
        pkg(pm, "com.eg.android.alipaygphone");
        pkg(pm, "com.android.videoplayer");
        pkg(pm, "com.vivo.tips");
        // 正规包名
        pkg(pm, "com.eg.android.AlipayGphone");
        pkg(pm, "com.android.VideoPlayer");
    }

    @SuppressLint("WrongConstant")
    private void pkg(PackageManager pm, String pkg) {
        try {
            PackageInfo pi = pm.getPackageInfo(pkg, 4096);
            Woo.logFormCase("--------" + pi.packageName);
        } catch (Throwable e) {
            L.e(e.toString());
        }
    }


}
