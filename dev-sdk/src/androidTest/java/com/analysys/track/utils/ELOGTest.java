package com.analysys.track.utils;

import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry;

import com.analysys.track.AnalsysTest;

import org.junit.Test;

public class ELOGTest extends AnalsysTest {
    Context mContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

    @Test
    public void init() {
        ELOG.init(mContext);
    }

    @Test
    public void v() {
        ELOG.v("hello");
        ELOG.v("hello, %s.", "angle");
        ELOG.v("hello, %s.");
        ELOG.v();
    }

    @Test
    public void d() {
        ELOG.d("hello");
        ELOG.d("hello, %s.", "angle");
        ELOG.d("hello, %s.");
        ELOG.d();
    }

    @Test
    public void i() {
        ELOG.i("hello");
        ELOG.i("hello, %s.", "angle");
        ELOG.i("hello, %s.");
        ELOG.i();
    }

    @Test
    public void w() {
        ELOG.w("hello");
        ELOG.w("hello, %s.", "angle");
        ELOG.w("hello, %s.");
        ELOG.w();
    }

    @Test
    public void e() {
        ELOG.e("hello");
        ELOG.e("hello, %s.", "angle");
        ELOG.e("hello, %s.");
        ELOG.e();
    }

    @Test
    public void wtf() {
        ELOG.wtf("hello");
        ELOG.wtf("hello, %s.", "angle");
        ELOG.wtf("hello, %s.");
        ELOG.wtf();
    }
}