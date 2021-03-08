package cn.analysys.casedemo.cases;

import android.content.Context;

import com.cslib.CaseHelper;
import com.cslib.defcase.ETestSuite;

import cn.analysys.casedemo.cases.devinfo.DICase;
import cn.analysys.casedemo.cases.devinfo.ImsiCase;
import cn.analysys.casedemo.cases.devinfo.MacCase;
import cn.analysys.casedemo.cases.devinfo.PhoneNumberCase;
import cn.analysys.casedemo.cases.infos.ELaseModifyTimeCase;
import cn.analysys.casedemo.sdkimport.Helper;

public class CaseCtl {

    private static Context mContext = null;

    public static Context getContext() {
        return mContext;
    }

    public static void addCases(Context context) {
        mContext = Helper.getContext(context);
        ETestSuite infosCases = new ETestSuite("功能性测试");
        infosCases.addCase(new ELaseModifyTimeCase());
        CaseHelper.addSuite(infosCases);

        ETestSuite dev = new ETestSuite("设备信息获取测试");
        dev.addCase(new DICase());
        dev.addCase(new ImsiCase());
        dev.addCase(new MacCase());
        dev.addCase(new PhoneNumberCase());
        CaseHelper.addSuite(dev);
    }
}
