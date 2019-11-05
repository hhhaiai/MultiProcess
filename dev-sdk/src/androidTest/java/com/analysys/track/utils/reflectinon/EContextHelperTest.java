package com.analysys.track.utils.reflectinon;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import com.analysys.track.hotfix.HotFixTransformCancel;
import com.analysys.track.hotfix.HotFixImpl;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.utils.sp.SPHelper;

import org.junit.Before;
import org.junit.Test;

import java.io.File;

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

    /**
     * 在热更包中,调用情况
     */
    @Test
    public void getContextByHotFix() {
        this.context = context.getApplicationContext();
        assertNotNull(this.context);
        Context context = EContextHelper.getContext(this.context);

        assertEquals(context, this.context);

        context = EContextHelper.getContext(null);

        assertEquals(context, this.context);
        String path = "/data/user/0/com.device/fileshf_track_v4.3.0.5_20191023.dex";
        if (path == null || path.equals("") || !new File(path).isFile()) {
            SPHelper.setBooleanValue2SP(context, EGContext.HOT_FIX_ENABLE_STATE, false);
            return;
        }
        try {
            HotFixImpl.init(context);
            Context o = HotFixImpl.transform(null, EContextHelper.class.getName(),
                    "getContext", new Object[]{null});
            assertEquals(o, this.context);
        } catch (HotFixTransformCancel e) {
        }
    }

}