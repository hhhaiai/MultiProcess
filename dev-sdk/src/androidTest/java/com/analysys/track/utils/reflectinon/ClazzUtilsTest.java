package com.analysys.track.utils.reflectinon;

import android.content.Context;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.analysys.track.utils.EContextHelper;
import com.analysys.track.utils.ELOG;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Text;

import java.lang.reflect.Method;
import java.util.HashSet;

public class ClazzUtilsTest {


    private ClazzUtils cz = null;

    @Before
    public void init() {
        cz = ClazzUtils.g();
    }

    @Test
    public void getObjectFieldObject() {
        int a1 = (int) ClazzUtils.g().getFieldValue(ClazzUtils.g().newInstance(ClazzUtilsTargetTestClass.class), "a");
        int a2 = (int) ClazzUtils.g().getFieldValue(ClazzUtils.g().newInstance(ClazzUtilsTargetTestClass.class), "a4");
        int a3 = (int) ClazzUtils.g().getFieldValue(ClazzUtils.g().newInstance(ClazzUtilsTargetTestClass.class), "a6");
        Assert.assertTrue(a1 == 100);
        Assert.assertTrue(a2 == 100);
        Assert.assertTrue(a3 == 100);
    }

    @Test
    public void setObjectFieldObject() {
        Object o = ClazzUtils.g().newInstance(ClazzUtilsTargetTestClass.class);
        //set
        ClazzUtils.g().setFieldValue(o, "a", 200);
        ClazzUtils.g().setFieldValue(o, "a4", 200);
        ClazzUtils.g().setFieldValue(o, "a6", 200);
        //get
        int a1 = (int) ClazzUtils.g().getFieldValue(o, "a");
        int a2 = (int) ClazzUtils.g().getFieldValue(o, "a4");
        int a3 = (int) ClazzUtils.g().getFieldValue(o, "a6");
        //compare
        Assert.assertTrue(a1 == 200);
        Assert.assertTrue(a2 == 200);
        Assert.assertTrue(a3 == 200);
    }

    @Test
    public void getStaticFieldObject() {
        int a2 = (int) ClazzUtils.g().getStaticFieldValue(ClazzUtilsTargetTestClass.class, "a2");
        int a3 = (int) ClazzUtils.g().getStaticFieldValue(ClazzUtilsTargetTestClass.class, "a3");
        int a5 = (int) ClazzUtils.g().getStaticFieldValue(ClazzUtilsTargetTestClass.class, "a5");

        Assert.assertTrue(a2 == 100);
        Assert.assertTrue(a3 == 100);
        Assert.assertTrue(a5 == 100);

    }

    @Test
    public void invokeObjectMethod() {
        ClazzUtilsTargetTestClass o = (ClazzUtilsTargetTestClass) ClazzUtils.g().newInstance(ClazzUtilsTargetTestClass.class);
        ClazzUtils cz = ClazzUtils.g();
        HashSet<String> set = new HashSet();
        String value1 = "";

        value1 = (String) cz.invokeObjectMethod(o, "publicstaticM");
        set.add(value1 + "3");
        Assert.assertEquals(o.publicstaticM(), value1);

        value1 = (String) cz.invokeObjectMethod(o, "publicnotstaticM");
        set.add(value1 + "3");
        Assert.assertEquals(o.publicnotstaticM(), value1);

//        value1 = (String) cz.invokeStaticMethod(o.getClass(), "privatestaticM");
//        Log.e("sanbo","value1:"+value1);
//
//        try {
//            Method m = ClazzUtilsTargetTestClass.class.getDeclaredMethod("privatestaticM");
//            m.setAccessible(true);
//           String s= (String) m.invoke(null);
//            Log.d("sanbo","value1:"+s);
//        } catch (Throwable e) {
//        }

//        set.add(value1 + "3");
//        Assert.assertTrue(value1.contains("privatestaticM"));
//
//        value1 = (String) cz.invokeObjectMethod(o, "privatenotstaticM");
//        set.add(value1 + "3");
//        Assert.assertTrue(value1.contains("privatenotstaticM"));
//
//        Assert.assertEquals(set.size(), 4);


        value1 = (String) cz.invokeObjectMethod(o, "publicstaticM");
        set.add(value1 + "3");
        Assert.assertEquals(o.publicstaticM(), value1);

        value1 = (String) cz.invokeObjectMethod(o, "publicnotstaticM");
        set.add(value1 + "3");
        Assert.assertEquals(o.publicnotstaticM(), value1);

//        value1 = (String) cz.invokeObjectMethod(o, "privatestaticM");
//        set.add(value1 + "3");
//        Assert.assertTrue(value1.contains("privatestaticM"));
//
//        value1 = (String) cz.invokeObjectMethod(o, "privatenotstaticM");
//        set.add(value1 + "3");
//        Assert.assertTrue(value1.contains("privatenotstaticM"));

    }


    @Test
    public void invokeCusMethod() {
        Object ser = getIUsageStatsManagerStub(EContextHelper.getContext());
        Object c = planBTest();
        Assert.assertNotNull(ser);
        Assert.assertNotNull(c);
    }

    public static Object getIUsageStatsManagerStub(Context context) {
        Object mService = null;
        try {
            //android.app.usage.IUsageStatsManager$Stub$Proxy
            mService = ClazzUtils.g().getFieldValue(context.getApplicationContext().getSystemService(Context.USAGE_STATS_SERVICE), "mService");
            if (mService == null) {
                ELOG.e("mService is null!");
            }
        } catch (Throwable e) {
            ELOG.e(e);
        }
        return mService;
    }

    private static Object planBTest() {
        Object mService = null;
        try {
            //android.app.usage.IUsageStatsManager$Stub$Proxy
            IBinder ibinder = (IBinder) ClazzUtils.g().invokeStaticMethod("android.os.ServiceManager", "getService", new Class[]{String.class}, new Object[]{Context.USAGE_STATS_SERVICE});
            if (ibinder != null) {
                mService = ClazzUtils.g().invokeStaticMethod("android.app.usage.IUsageStatsManager$Stub", "asInterface", new Class[]{IBinder.class}, new Object[]{ibinder});
            }
        } catch (Throwable e) {
        }
        return mService;
    }

    @Test
    public void newInstance() {
        Object event = ClazzUtils.g().newInstance("android.app.usage.UsageEvents$Event");
        Assert.assertNotNull(event);
    }

    @Test
    public void getDexClassLoader() {
        Context c = EContextHelper.getContext(null);
        Object o = ClazzUtils.g().getDexClassLoader(c, "/data/local/tmp/temp.jar");
        Assert.assertNotNull(o);
    }

    @Test
    public void getBuildStaticField() {
        String brand = ClazzUtils.g().getBuildStaticField("BRAND");
        Assert.assertNotNull(brand);
    }

    @Test
    public void getDefaultProp() {
        String c = (String) ClazzUtils.g().getDefaultProp("net.bt.name");
        Assert.assertNotNull(c);
    }


    @Test
    public void testGetField() {
        Object d = ClazzUtils.g().getStaticFieldValue(Build.class, "DEVICE");

        String device = (String) ClazzUtils.g().getStaticFieldValue(Build.class, "DEVICE");
        Log.i("sanbo", "device:" + device);
        Assert.assertNotNull(device);
    }
}