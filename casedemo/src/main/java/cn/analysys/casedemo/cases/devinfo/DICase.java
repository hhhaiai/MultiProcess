package cn.analysys.casedemo.cases.devinfo;

import com.analysys.track.internal.impl.DeviceImpl;
import com.cslib.defcase.ETestCase;

import cn.analysys.casedemo.utils.SDKHelper;
import cn.analysys.casedemo.utils.Woo;

public class DICase extends ETestCase {
    public DICase() {
        super("DI");
    }

    @Override
    public void prepare() {
    }

    @Override
    public boolean predicate() {
        String di = DeviceImpl.getInstance(SDKHelper.getContext()).getDeviceId();
        Woo.logFormCase("di: " + di);
        return true;
    }

}
