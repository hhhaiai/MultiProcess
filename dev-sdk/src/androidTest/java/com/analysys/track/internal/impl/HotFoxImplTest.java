package com.analysys.track.internal.impl;

import com.analysys.track.AnalsysTest;

import org.junit.Test;

import static org.junit.Assert.*;

public class HotFoxImplTest extends AnalsysTest {

    @Test
    public void reqHotFix() {
        HotFoxImpl.reqHotFix(mContext,null);
    }
}