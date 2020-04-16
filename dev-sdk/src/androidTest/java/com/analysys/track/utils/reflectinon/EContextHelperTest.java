package com.analysys.track.utils.reflectinon;

import android.app.Instrumentation;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.util.Log;

import com.analysys.track.AnalysysTracker;
import com.analysys.track.impl.CusHotTransform;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.utils.EContextHelper;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.sp.SPHelper;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
        Context context = EContextHelper.getContext();

        assertEquals(context, this.context);

        context = EContextHelper.getContext();

        assertEquals(context, this.context);
    }

    /**
     * 在热更包中,调用情况
     */
    @Test
    public void getContextByHotFix() {
        this.context = context.getApplicationContext();
        assertNotNull(this.context);
//        AnalysysTracker.setContext(context);

        assertEquals(context, this.context);
//        AnalysysTracker.setContext(context);

        assertEquals(context, this.context);
        String path = "/data/user/0/com.device/fileshf_track_v4.3.0.5_20191023.dex";
        if (path == null || path.equals("") || !new File(path).isFile()) {
            SPHelper.setBooleanValue2SP(context, EGContext.HOT_FIX_ENABLE_STATE, false);
            return;
        }
        try {
            Context o = (Context) CusHotTransform.getInstance(context).transform(false, EContextHelper.class.getName(),
                    "getContext", new Object[]{null});
            assertEquals(o, this.context);
        } catch (Throwable e) {
        }
    }

    @Test
    public void getContextA() {
        Context c = EContextHelper.getContext(context);
        ELOG.i("getContextA: " + c);
        Assert.assertTrue("context获取检查", c != null);
        Assert.assertNotNull("context非空", c);
    }

    @Test
    public void g() {
        try {
            // 先获取到当前的ActivityThread对象
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
            currentActivityThreadMethod.setAccessible(true);
            Object currentActivityThread = currentActivityThreadMethod.invoke(null);

            // 拿到原始的 mInstrumentation字段
            Field mInstrumentationField = activityThreadClass.getDeclaredField("mInstrumentation");
            mInstrumentationField.setAccessible(true);
            Instrumentation mInstrumentation = (Instrumentation) mInstrumentationField.get(currentActivityThread);
            Context c = mInstrumentation.getTargetContext();
            Log.i("analysys", "[g]==========Context=======: " + c);
            // 此时日志打印，会出现问题
//            ELOG.i("[g]==========Context=======: " + c);
            Assert.assertTrue("[g]context获取检查", c != null);
            Assert.assertNotNull("[g]context非空", c);
        } catch (Throwable e) {
            ELOG.e(e);
        }
    }

}