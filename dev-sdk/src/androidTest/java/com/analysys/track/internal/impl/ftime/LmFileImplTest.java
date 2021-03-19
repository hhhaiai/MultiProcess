package com.analysys.track.internal.impl.ftime;

import android.content.Context;
import android.os.Looper;
import android.support.test.InstrumentationRegistry;
import android.util.Log;
import android.widget.Toast;

import com.analysys.track.utils.ELOG;

import junit.framework.TestCase;

import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class LmFileImplTest extends TestCase {
    Context mContext = InstrumentationRegistry.getContext();

    @Test
    public void testTryGetFileTime() {
        LmFileImpl.getInstance(mContext).realGetFlt(null);
        Map<String, Long> mem = LmFileImpl.getInstance(mContext).getMemoryData();
        ELOG.i(mem.toString());
        Assert.assertTrue(mem.size() > 0);
    }

    public void testGetInstance() {
    }
}