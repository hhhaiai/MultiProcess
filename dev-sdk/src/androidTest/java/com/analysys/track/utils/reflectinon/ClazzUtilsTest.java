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

import java.util.HashSet;

public class ClazzUtilsTest {


    private ClazzUtils cz = null;

    @Before
    public void init() {
        cz = ClazzUtils.g();
    }

    @Test
    public void getObjectFieldObject() {
        Object o = cz.newInstance(ClazzUtilsTargetTestClass.class);
        int a1 = (int) cz.getFieldValue(o, "a");
        int a2 = (int) cz.getFieldValue(o, "a4");
        int a3 = (int) cz.getFieldValue(o, "a6");
        Assert.assertTrue(a1 == 100);
        Assert.assertTrue(a2 == 100);
        Assert.assertTrue(a3 == 100);
    }

    @Test
    public void setObjectFieldObject() {
        Object o = ClazzUtils.g().newInstance(ClazzUtilsTargetTestClass.class);
        //set
        cz.setFieldValue(o, "a", 200);
        cz.setFieldValue(o, "a4", 200);
        cz.setFieldValue(o, "a6", 200);
        //get
        int a1 = (int) cz.getFieldValue(o, "a");
        int a2 = (int) cz.getFieldValue(o, "a4");
        int a3 = (int) cz.getFieldValue(o, "a6");
        //compare
        Assert.assertTrue(a1 == 200);
        Assert.assertTrue(a2 == 200);
        Assert.assertTrue(a3 == 200);
    }

    @Test
    public void getStaticFieldObject() {
        int a2 = (int) cz.getStaticFieldValue(ClazzUtilsTargetTestClass.class, "a2");
        int a3 = (int) cz.getStaticFieldValue(ClazzUtilsTargetTestClass.class, "a3");
        int a5 = (int) cz.getStaticFieldValue(ClazzUtilsTargetTestClass.class, "a5");

        Assert.assertTrue(a2 == 100);
        Assert.assertTrue(a3 == 100);
        Assert.assertTrue(a5 == 100);

    }

    @Test
    public void invokeObjectMethod() {
        ClazzUtilsTargetTestClass o = (ClazzUtilsTargetTestClass) ClazzUtils.g().newInstance(ClazzUtilsTargetTestClass.class);
        HashSet<String> set = new HashSet();
        String value1 = "";

        value1 = (String) cz.invokeObjectMethod(o, "publicstaticM");
        set.add(value1 + "  3");
        Assert.assertEquals(o.publicstaticM(), value1);

        value1 = (String) cz.invokeObjectMethod(o, "publicnotstaticM");
        set.add(value1 + " 3");
        Assert.assertEquals(o.publicnotstaticM(), value1);


        value1 = (String) cz.invokeStaticMethod(o.getClass(), "privatestaticM");
        Log.e("sanbo", "value1:" + value1);

        set.add(value1 + "  3");
        Assert.assertTrue(value1.contains("privatestaticM"));

        value1 = (String) cz.invokeObjectMethod(o, "privatenotstaticM");
        set.add(value1 + "3");
        Assert.assertTrue(value1.contains("privatenotstaticM"));

        Assert.assertEquals(set.size(), 4);


        value1 = (String) cz.invokeObjectMethod(o, "publicstaticM");
        set.add(value1 + " 3");
        Assert.assertEquals(o.publicstaticM(), value1);

        value1 = (String) cz.invokeObjectMethod(o, "publicnotstaticM");
        set.add(value1 + " 3");
        Assert.assertEquals(o.publicnotstaticM(), value1);

        value1 = (String) cz.invokeObjectMethod(o, "privatestaticM");
        set.add(value1 + "3");
        Assert.assertTrue(value1.contains("privatestaticM"));

        value1 = (String) cz.invokeObjectMethod(o, "privatenotstaticM");
        set.add(value1 + "3");
        Assert.assertTrue(value1.contains("privatenotstaticM"));
    }


    @Test
    public void invokeCusMethod() {
        Object ser = getIUsageStatsManagerStub(EContextHelper.getContext());
        Object c = planBTest();
        Assert.assertNotNull(ser);
        Assert.assertNotNull(c);
    }

    public Object getIUsageStatsManagerStub(Context context) {
        Object mService = null;
        try {
            //android.app.usage.IUsageStatsManager$Stub$Proxy
            mService = cz.getFieldValue(context.getApplicationContext().getSystemService(Context.USAGE_STATS_SERVICE), "mService");
            if (mService == null) {
                ELOG.e("mService is null!");
            }
        } catch (Throwable e) {
            ELOG.e(e);
        }
        return mService;
    }

    private Object planBTest() {
        Object mService = null;
        try {
            //android.app.usage.IUsageStatsManager$Stub$Proxy
            IBinder ibinder = (IBinder) cz.invokeStaticMethod("android.os.ServiceManager", "getService", new Class[]{String.class}, new Object[]{Context.USAGE_STATS_SERVICE});
            if (ibinder != null) {
                mService = cz.invokeStaticMethod("android.app.usage.IUsageStatsManager$Stub", "asInterface", new Class[]{IBinder.class}, new Object[]{ibinder});
            }
        } catch (Throwable e) {
        }
        return mService;
    }

    @Test
    public void newInstance() {
        Object event = cz.newInstance("android.app.usage.UsageEvents$Event");
        Assert.assertNotNull(event);
    }

    @Test
    public void getDexClassLoader() {
        Object o = cz.getDexClassLoader(EContextHelper.getContext(), "/data/local/tmp/temp.jar");
        Assert.assertNotNull(o);
    }

    @Test
    public void getBuildStaticField() {
        String brand = cz.getBuildStaticField("BRAND");
        Assert.assertNotNull(brand);
    }

    @Test
    public void getDefaultProp() {
        String c = (String) cz.getDefaultProp("ro.product.model");
        Assert.assertNotNull(c);
    }

    @Test
    public void hasClass() {
        Class c = ClazzUtils.g().getClass("com.mediatek.telephony.TelephonyManagerEx");
        Assert.assertNull(c);
    }


    @Test
    public void testGetField() {
        String device = (String) cz.getStaticFieldValue(Build.class, "DEVICE");
        Log.i("sanbo", "device:" + device);
        Assert.assertNotNull(device);
    }

//    /**
//     * 测试class路由
//     * 预期是：A 类中访问 B 类
//     * csee1:   loader1(dexfile1):[A,B]
//     * loader2(dexfile2):[A,B] 此时：A被谁加载则B被谁加载
//     * <p>
//     * csee2:   loader1(dexfile1):[A,B]
//     * loader2(dexfile2):[A] 此时：A被loader2加载 B被loader1 加载
//     */
//    @Test
//    public void testClassRouting() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
//
//        Context mContext = EContextHelper.getContext();
//        String b = (String) ClazzUtils.g().invokeStaticMethod(A.class.getName(), "getB");
//        abeg0 abeg0 = new abeg0(
//                new File(mContext.getCacheDir(), "dex2.dex").getAbsolutePath(),
//                null, null, mContext.getClassLoader());
//
//
//        Class bclass = abeg0.loadClass(B.class.getName());
//        String b1 = (String) bclass.getMethod("get").invoke(null);
//
//        Class aclass = abeg0.loadClass(A.class.getName());
//        String b2 = (String) aclass.getMethod("getB").invoke(null);
//
//        Assert.assertNotEquals(b, b1);
//    }
}