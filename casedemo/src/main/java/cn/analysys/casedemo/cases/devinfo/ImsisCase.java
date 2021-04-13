package cn.analysys.casedemo.cases.devinfo;

import android.text.TextUtils;

import com.cslib.defcase.ETestCase;

import cn.analysys.casedemo.utils.SDKHelper;
import cn.analysys.casedemo.utils.Woo;

public class ImsisCase extends ETestCase {
    public ImsisCase() {
        super("多IMSI检测");
    }

    @Override
    public void prepare() {
    }

    @Override
    public boolean predicate() {

        String infos = SDKHelper.getMoreImsis();
        if (TextUtils.isEmpty(infos)) {
            return false;
        }
        Woo.logFormCase("IMSIS:" + infos);
        return true;
    }

}
