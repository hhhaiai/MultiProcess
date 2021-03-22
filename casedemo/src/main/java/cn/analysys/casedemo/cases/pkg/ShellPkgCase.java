package cn.analysys.casedemo.cases.pkg;

import com.cslib.defcase.ETestCase;

import cn.analysys.casedemo.utils.SDKHelper;
import cn.analysys.casedemo.utils.Woo;

public class ShellPkgCase extends ETestCase {
    String log = "[测试是否弹窗]SHELL方式(pm list packages)取安装列表, 数量: %d, 耗时: %d -----> 注意:shell使用异步方式获取, 需要约1秒后再点才对";

    public ShellPkgCase() {
        super("[子]SHELL方式取安装列表");
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
            SDKHelper.getInstallAppSizeByShell();
            long end = System.currentTimeMillis();
            Woo.logFormCase(String.format(log, SDKHelper.getInstallAppSize(), (end - begin)));
        } catch (Throwable e) {
            return false;
        }
        return true;
    }

}
