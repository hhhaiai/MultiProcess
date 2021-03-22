package cn.analysys.casedemo.cases.pkg;

import com.cslib.defcase.ETestCase;

import cn.analysys.casedemo.utils.SDKHelper;
import cn.analysys.casedemo.utils.Woo;

public class UidPkgCase extends ETestCase {
    String log = "[测试弹窗]UID方式(PackageManager.getPackagesForUid(uid))取安装列表，数量: %d, 耗时: %d";

    public UidPkgCase() {
        super("[子]UID方式取安装列表");
    }

    @Override
    public void prepare() {
        Woo.logFormCase("清除内存数据");
        SDKHelper.clearMemoryPkgList();
    }

    @Override
    public boolean predicate() {
        try {
            long begin = System.currentTimeMillis();
            SDKHelper.getInstallAppSizeByUid();
            long end = System.currentTimeMillis();
            Woo.logFormCase(String.format(log, SDKHelper.getInstallAppSize(), (end - begin)));
        } catch (Throwable e) {
            return false;
        }
        return true;
    }

}
