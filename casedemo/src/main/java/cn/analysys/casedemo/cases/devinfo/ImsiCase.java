package cn.analysys.casedemo.cases.devinfo;

import com.cslib.defcase.ETestCase;

public class ImsiCase extends ETestCase {
    public ImsiCase() {
        super("IMSI");
    }

    @Override
    public void prepare() {
    }

    @Override
    public boolean predicate() {
        return true;
    }

}
