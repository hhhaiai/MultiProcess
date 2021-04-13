package com.analysys.track.internal.impl.ftime;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import com.analysys.track.AnalsysTest;
import com.analysys.track.utils.ELOG;

import junit.framework.TestCase;

import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class LmFileImplTest extends AnalsysTest {
    Context mContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

    @Test
    public void testRealGetFlt() {
        LmFileImpl.getInstance(mContext).realGetFlt();
        Map<String, Long> mem = LmFileImpl.getInstance(mContext).getMemDataForTest();
        ELOG.i(mem.toString());
        Assert.assertTrue(mem.size() > 0);
    }

    @Test
    public void testGetInstance() {
        Assert.assertEquals(LmFileImpl.getInstance(mContext), LmFileImpl.getInstance(mContext));
    }

}