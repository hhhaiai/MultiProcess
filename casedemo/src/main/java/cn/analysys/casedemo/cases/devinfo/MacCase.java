package cn.analysys.casedemo.cases.devinfo;

import com.cslib.defcase.ETestCase;

public class MacCase extends ETestCase {
    public MacCase() {
        super("MAC");
    }

    @Override
    public void prepare() {
    }

    @Override
    public boolean predicate() {
        return true;
    }

}
