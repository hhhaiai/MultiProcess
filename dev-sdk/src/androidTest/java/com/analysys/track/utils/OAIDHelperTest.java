package com.analysys.track.utils;

import com.analysys.track.AnalsysTest;

import org.junit.Test;

import static org.junit.Assert.*;

public class OAIDHelperTest extends AnalsysTest {

    @Test
    public void tryGetOaidAndSave() {
        OAIDHelper.tryGetOaidAndSave(mContext);
    }
}