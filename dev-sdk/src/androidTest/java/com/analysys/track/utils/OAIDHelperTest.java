package com.analysys.track.utils;

import com.analysys.track.AnalsysTest;
import com.analysys.track.utils.id.OAIDHelper;

import org.junit.Test;

public class OAIDHelperTest extends AnalsysTest {

    @Test
    public void tryGetOaidAndSave() {
        OAIDHelper.tryGetOaidAndSave(mContext);
    }
}