package com.analysys.track.utils.reflectinon;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class EContextHelperTest {
    Context context;

    @Before
    public void start() {
        context = InstrumentationRegistry.getTargetContext();
    }

    @Test
    public void getContext() {
        this.context = context.getApplicationContext();
        assertNotNull(this.context);
        Context context = EContextHelper.getContext(this.context);

        assertEquals(context, this.context);

        context = EContextHelper.getContext(null);

        assertEquals(context, this.context);
    }
}