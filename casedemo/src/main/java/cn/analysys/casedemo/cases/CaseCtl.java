package cn.analysys.casedemo.cases;

import android.content.Context;

import com.cslib.CaseHelper;
import com.cslib.defcase.ETestSuite;

import cn.analysys.casedemo.cases.devinfo.DICase;
import cn.analysys.casedemo.cases.devinfo.AndroidIdCase;
import cn.analysys.casedemo.cases.devinfo.MacCase;
import cn.analysys.casedemo.cases.devinfo.PhoneNumberCase;
import cn.analysys.casedemo.cases.infos.PkgListByUidCase;
import cn.analysys.casedemo.cases.infos.PkgListGetByShellCase;
import cn.analysys.casedemo.cases.infos.LMFSizeCase;
import cn.analysys.casedemo.cases.infos.PkgListByAPICase;
import cn.analysys.casedemo.sdkimport.Helper;

public class CaseCtl {

    private static Context mContext = null;

    public static Context getContext() {
        return mContext;
    }

    public static void addCases(Context context) {
        mContext = Helper.getContext(context);
        ETestSuite infosCases = new ETestSuite("功能性测试");
        infosCases.addCase(new LMFSizeCase());
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
