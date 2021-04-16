package cn.analysys.casedemo.cases.devinfo;

import android.text.TextUtils;

import cn.analysys.casedemo.utils.SDKHelper;
import cn.analysys.casedemo.utils.Woo;
import me.hhhaiai.testcaselib.defcase.ETestCase;

public class ImeisCase extends ETestCase {
    public ImeisCase() {
        super("多IMEI检测");
    }

    @Override
    public void prepare() {
    }

    @Override
    public boolean predicate() {

        String infos = SDKHelper.getMoreImeis();
        if (TextUtils.isEmpty(infos)) {
            return false;
        }
        Woo.logFormCase("IMEIS:" + infos);
        return true;
    }

}
