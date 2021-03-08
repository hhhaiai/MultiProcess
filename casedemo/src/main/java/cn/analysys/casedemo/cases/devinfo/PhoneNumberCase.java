package cn.analysys.casedemo.cases.devinfo;

import com.cslib.defcase.ETestCase;

public class PhoneNumberCase extends ETestCase {
    public PhoneNumberCase() {
        super("PhoneNumber");
    }

    @Override
    public void prepare() {
    }

    @Override
    public boolean predicate() {
        return true;
    }

}
