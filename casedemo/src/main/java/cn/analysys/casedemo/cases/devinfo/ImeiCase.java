package cn.analysys.casedemo.cases.devinfo;

import com.cslib.defcase.ETestCase;

public class ImeiCase extends ETestCase {
    public ImeiCase() {
        super("IMEI");
    }

    @Override
    public void prepare() {
    }

    @Override
    public boolean predicate() {
        return true;
    }

}
