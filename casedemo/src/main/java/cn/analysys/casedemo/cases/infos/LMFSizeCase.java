package cn.analysys.casedemo.cases.infos;

import com.cslib.defcase.ETestCase;

import java.util.List;

import cn.analysys.casedemo.cases.utils.Woo;
import cn.analysys.casedemo.sdkimport.Helper;
import cn.analysys.casedemo.utils.EL;

public class LMFSizeCase extends ETestCase {
    String log = "安装app: %d\n有缓存记录app: %d\n是否为自己: %s";

    public LMFSizeCase() {
        super("[文件]末次修改个数对比");
    }

    @Override
    public void prepare() {
    }

    @Override
    public boolean predicate() {

        List<String> lastModifyTimeInfos = Helper.getLastAliveTimeStr();
        int len = lastModifyTimeInfos.size();

        int allAppsize = Helper.getInstallAppSize();

        if (len == 1) {
            if (lastModifyTimeInfos.get(0).contains(Helper.getContext().getPackageName())) {
                Woo.logFormCase(String.format(log, allAppsize, len, "是"));
                return false;
            }
        } else if (len > 1) {
            for (int i = 0; i < lastModifyTimeInfos.size(); i++) {
                EL.d(lastModifyTimeInfos.get(i));
            }
            Woo.logFormCase(String.format(log, allAppsize, len, "否"));
            return true;
        }
        Woo.logFormCase(String.format(log, allAppsize, len, "否"));
        return false;
    }

}
