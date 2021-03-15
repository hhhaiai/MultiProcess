package cn.analysys.casedemo.cases.infos;

import com.cslib.defcase.ETestCase;

import cn.analysys.casedemo.cases.utils.Woo;
import cn.analysys.casedemo.sdkimport.Helper;

public class PkgListByAPICase extends ETestCase {
    String log = "API方式取安装列表耗时: %d";

    public PkgListByAPICase() {
        super("[弹窗]API方式取安装列表");
    }

    @Override
    public void prepare() {
    }

    @Override
    public boolean predicate() {
        try {
            long begin = System.currentTimeMillis();
            Helper.getInstallAppSizeByApi();
            long end = System.currentTimeMillis();
            Woo.logFormCase(String.format(log, (end - begin)));
        } catch (Throwable e) {
            return false;
        }
        return true;
    }

}