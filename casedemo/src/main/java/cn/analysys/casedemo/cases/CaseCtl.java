package cn.analysys.casedemo.cases;

import android.content.Context;

import com.cslib.CaseHelper;
import com.cslib.defcase.ETestSuite;import cn.analysys.casedemo.cases.devinfo.*;
import cn.analysys.casedemo.cases.infos.*;
import cn.analysys.casedemo.sdkimport.Helper;

public class CaseCtl {

    private static Context mContext = null;

    public static Context getContext() {
        return mContext;
    }



    public static void addCases(Context context) {
        mContext = Helper.getContext(context);
        ETestSuite infosCases = new ETestSuite("功能性测试");
        infosCases.addCase(new FileLastModifySizeCmpCase());
        infosCases.addCase(new FileLastModifyBaseDirTimeCase());
        infosCases.addCase(new PkgListGetByShellCase());
        infosCases.addCase(new PkgListByAPICase());
        infosCases.addCase(new PkgListByUidCase());
        CaseHelper.addSuite(infosCases);

        ETestSuite dev = new ETestSuite("设备信息获取测试");
        dev.addCase(new DICase());
        dev.addCase(new AndroidIdCase());
        dev.addCase(new MacCase());
        dev.addCase(new PhoneNumberCase());
        CaseHelper.addSuite(dev);
    }
}
