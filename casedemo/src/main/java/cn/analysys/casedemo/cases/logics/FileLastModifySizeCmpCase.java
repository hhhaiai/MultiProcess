package cn.analysys.casedemo.cases.logics;

import java.util.List;

import cn.analysys.casedemo.utils.SDKHelper;
import cn.analysys.casedemo.utils.Woo;
import me.hhhaiai.testcaselib.defcase.ETestCase;
import me.hhhaiai.testcaselib.utils.L;

/**
 * @Copyright © 2021 analsys Inc. All rights reserved.
 * @Description: 安装列表和安装文件对比
 * @Version: 1.0
 * @Create: 2021/03/74 14:27:46
 * @author: sanbo
 */
public class FileLastModifySizeCmpCase extends ETestCase {
    String log = "===========================================\n安装app: %d\n有缓存记录app: %d\n是否为自己: %s";

    public FileLastModifySizeCmpCase() {
        super("[应用]末次修改个数对比");
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
        List<String> lastModifyTimeInfos = SDKHelper.getLastAliveTimeStr();
        int len = lastModifyTimeInfos.size();

        int allAppsize = SDKHelper.getInstallAppSize();

        StringBuffer sb = new StringBuffer();
        sb.append("安装文件数量对比 \n");

        if (len == 1) {
            if (lastModifyTimeInfos.get(0).contains(SDKHelper.getContext().getPackageName())) {
                sb.append(String.format(log, allAppsize, len, "是")).append("\n");
                Woo.logFormCase(sb.toString());
                return;
            }
        } else if (len > 1) {
            for (int i = 0; i < lastModifyTimeInfos.size(); i++) {
                sb.append(lastModifyTimeInfos.get(i))
                        .append("\n");
            }
            sb.append(String.format(log, allAppsize, len, "否"))
                    .append("\n");
            Woo.logFormCase(sb.toString());
            return;
        }
        sb.append(String.format(log, allAppsize, len, "否"))
                .append("\n");
        Woo.logFormCase(sb.toString());
    }

}
