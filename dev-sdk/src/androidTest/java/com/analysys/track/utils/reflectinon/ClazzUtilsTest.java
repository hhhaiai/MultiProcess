package com.analysys.track.utils.reflectinon;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;

public class ClazzUtilsTest {

    public String hello() {
        return "hello";
    }

    public static String hello2() {
        return "hello2";
    }

    @Test
    public void test1() throws Exception {

        Method getMethod = Class.class.getDeclaredMethod("getMethod", String.class, Class[].class);
        Method method = (Method) getMethod.invoke(ClazzUtilsTest.class, "hello2", null);

        Assert.assertNotNull(method);


    }

    @Test
    public void test2() throws Exception {

        Method getMethod = Class.class.getDeclaredMethod("getMethod", String.class, Class[].class);
        Method method = (Method) getMethod.invoke(this, "hello2", null);

        Assert.assertNotNull(method);

    }

    @Test
    public void test3() throws Exception {

        Method getMethod = Class.class.getDeclaredMethod("getMethod", String.class, Class[].class);
        Method method = (Method) getMethod.invoke(null, "hello2", null);

        Assert.assertNotNull(method);

    }

    @Test
    public void test4() {
        Method method2 = ClazzUtils.getMethod(ClazzUtilsTest.class, "hello2", null);
        Assert.assertNotNull(method2);

        String str = (String) ClazzUtils.invokeStaticMethod("com.analysys.track.utils.reflectinon.ClazzUtilsTest",
                "hello2", null, null);
        Assert.assertEquals(str, "hello2");

        String str2 = (String) ClazzUtils.invokeObjectMethod(new ClazzUtilsTest(),
                "hello");
        Assert.assertEquals(str2, "hello");
    }
}