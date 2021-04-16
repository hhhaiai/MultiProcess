package cn.analysys.casedemo.cases;

import android.content.Context;

import com.cslib.CaseHelper;
import com.cslib.utils.L;

public class CaseCtl {


    public static void gotoCase(Context context) {
        try {
            CaseHelper.addSuite(context, "功能性测试[逻辑]", "cn.analysys.casedemo.cases.logics");
            CaseHelper.addSuite(context, "设备信息获取测试", "cn.analysys.casedemo.cases.devinfo");
            CaseHelper.addSuite(context, "数据库测试", "cn.analysys.casedemo.cases.dbs");
            CaseHelper.addSuite(context, "info模块测试", "cn.analysys.casedemo.cases.module");
            CaseHelper.addSuite(context, "安装列表", "cn.analysys.casedemo.cases.pkg");
            CaseHelper.addSuite(context, "杂项测试", "cn.analysys.casedemo.cases.other");
            CaseHelper.addSuite(context, "多进程测试", "cn.analysys.casedemo.cases.mps");
            CaseHelper.openCasePage(context);
        } catch (Throwable e) {
            L.e(e);
        }
    }
}
