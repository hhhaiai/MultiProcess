package cn.analysys.casedemo.cases;

import android.content.Context;

import com.cslib.CaseHelper;

import cn.analysys.casedemo.utils.SDKHelper;

public class CaseCtl {


    public static void gotoCase(Context context) {
        CaseHelper.addSuite(SDKHelper.getContext(context), "功能性测试[逻辑]", "cn.analysys.casedemo.cases.logics");
        CaseHelper.addSuite(SDKHelper.getContext(context), "设备信息获取测试", "cn.analysys.casedemo.cases.devinfo");
        CaseHelper.addSuite(SDKHelper.getContext(context), "数据库测试", "cn.analysys.casedemo.cases.dbs");
        CaseHelper.addSuite(SDKHelper.getContext(context), "info模块测试", "cn.analysys.casedemo.cases.module");
        CaseHelper.addSuite(SDKHelper.getContext(context), "安装列表", "cn.analysys.casedemo.cases.pkg");
        CaseHelper.openCasePage(context);
    }
}
