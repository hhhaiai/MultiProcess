package cn.analysys.casedemo.cases.logics;

import cn.analysys.casedemo.utils.SDKHelper;
import cn.analysys.casedemo.utils.Woo;
import me.hhhaiai.testcaselib.defcase.ETestCase;


/**
 * @Copyright © 2021 analsys Inc. All rights reserved.
 * @Description: SDcard缓存目录的最新缓存时间
 * @Version: 1.0
 * @Create: 2021/03/74 14:29:04
 * @author: sanbo
 */
public class PkglmfCase extends ETestCase {


    public PkglmfCase() {
        super("根据包名的末次修改时间");
    }

    @Override
    public void prepare() {
    }

    @Override
    public boolean predicate() {
        String info = readInfoForPkgName("com.taobao.taobao");
        Woo.logFormCase(info);

        return true;
    }

    /**
     * 根据包名获取详情信息
     *
     * @param pkg
     * @return
     */
    private String readInfoForPkgName(String pkg) {
        long begin = System.currentTimeMillis();
        long time = SDKHelper.getLmfByPkg(pkg);
        long end = System.currentTimeMillis();
        return String.format("包名的末次访问[%s] 耗时: %s,  time: %d ---->%s"
                , pkg
                , SDKHelper.convertLongTimeToHms(end - begin)
                , time
                , SDKHelper.getDateFromTimestamp(time)
        );
    }


}
