package cn.analysys.casedemo.cases.pkg;

import com.cslib.defcase.ETestCase;

import cn.analysys.casedemo.utils.SDKHelper;
import cn.analysys.casedemo.utils.Woo;

public class APIpkgCase extends ETestCase {
    String log = "[测试是否弹窗]API方式(packageManager.getInstalledPackages)取安装列表, 数量: %d, 耗时: %d";

    public APIpkgCase() {
        super("[子]API方式取安装列表");
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
            SDKHelper.getInstallAppSizeByApi();
            long end = System.currentTimeMillis();
            Woo.logFormCase(String.format(log, SDKHelper.getInstallAppSize(),(end - begin)));
        } catch (Throwable e) {
            return false;
        }
        return true;
    }

}
