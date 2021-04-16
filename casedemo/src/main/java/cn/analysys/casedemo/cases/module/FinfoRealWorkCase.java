package cn.analysys.casedemo.cases.module;

import cn.analysys.casedemo.utils.SDKHelper;
import me.hhhaiai.testcaselib.defcase.ETestCase;


/**
 * @Copyright © 2021 analsys Inc. All rights reserved.
 * @Description: finfo模块真正工作
 * @Version: 1.0
 * @Create: 2021/04/103 11:33:38
 * @author: sanbo
 */
public class FinfoRealWorkCase extends ETestCase {

    static String mName = String.format("Finfo Loop工作");

    public FinfoRealWorkCase() {
        super(mName);
    }

    @Override
    public void prepare() {
    }

    @Override
    public boolean predicate() {

        SDKHelper.runOnWorkThread(() -> {
            gotoWork();
        });
        return true;
    }

    private void gotoWork() {
        SDKHelper.postLastModifyTimeToDispatcher();
    }


}
