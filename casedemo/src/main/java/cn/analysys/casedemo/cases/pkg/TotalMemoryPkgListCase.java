package cn.analysys.casedemo.cases.pkg;

import java.util.List;

import cn.analysys.casedemo.utils.SDKHelper;
import cn.analysys.casedemo.utils.Woo;
import me.hhhaiai.testcaselib.defcase.ETestCase;
import me.hhhaiai.testcaselib.utils.L;

public class TotalMemoryPkgListCase extends ETestCase {
    String log = "[内存安装列表]取内存安装列表(内存有直接返回,无则),数量: %d ,耗时: %d\n%s";

    public TotalMemoryPkgListCase() {
        super("[总]内存安装列表");
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
            List<String> list = SDKHelper.getPkgList();
            long end = System.currentTimeMillis();
            StringBuilder sb = new StringBuilder();
            if (list != null && list.size() > 0) {
                for (String item : list) {
                    sb.append(item).append("\n");
                }
            }
            Woo.logFormCase(String.format(log, SDKHelper.getInstallAppSize(), (end - begin), sb.toString()));
        } catch (Throwable e) {
            L.e(e);
            return false;
        }
        return true;
    }

}
