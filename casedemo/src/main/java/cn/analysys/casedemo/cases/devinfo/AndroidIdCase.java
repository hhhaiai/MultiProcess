package cn.analysys.casedemo.cases.devinfo;

import android.text.TextUtils;

import com.cslib.defcase.ETestCase;

import cn.analysys.casedemo.cases.utils.Woo;
import cn.analysys.casedemo.sdkimport.Helper;

public class AndroidIdCase extends ETestCase {
    public AndroidIdCase() {
        super("AndroidId");
    }

    @Override
    public void prepare() {
    }

    @Override
    public boolean predicate() {
        String androidId = Helper.getAndroid();
        Woo.logFormCase("android id:" + androidId);
        if (TextUtils.isEmpty(androidId)) {
            return false;
        }
        return true;
    }

}
