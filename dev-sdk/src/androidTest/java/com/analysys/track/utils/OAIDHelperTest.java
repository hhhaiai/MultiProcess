package com.analysys.track.utils;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import com.analysys.track.AnalsysTest;
import com.analysys.track.utils.id.OAIDHelper;

import org.junit.Test;

public class OAIDHelperTest extends AnalsysTest {
    Context mContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

    @Test
    public void tryGetOaidAndSave() {
        OAIDHelper.tryGetOaidAndSave(mContext);
    }
}