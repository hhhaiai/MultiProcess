package com.analysys.track.utils.reflectinon;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.HashSet;

public class ClazzUtilsTest {

    @Test
    public void unseal() {
        try {
            ClazzUtils.unseal();
        } catch (Throwable e) {
            Assert.assertTrue("unseal error ", false);
        }
    }


    @Test
    public void getObjectFieldObject() {
        int a1 = (int) ClazzUtils.getObjectFieldObject(ClazzUtils.newInstance(ClazzUtils.class), "a");
        int a2 = (int) ClazzUtils.getObjectFieldObject(ClazzUtils.newInstance(ClazzUtils.class), "a4");
        int a3 = (int) ClazzUtils.getObjectFieldObject(ClazzUtils.newInstance(ClazzUtils.class), "a6");
        Assert.assertTrue(a1 == 100);
        Assert.assertTrue(a2 == 100);
        Assert.assertTrue(a3 == 100);
    }

    @Test
    public void setObjectFieldObject() {
        ClazzUtils.setObjectFieldObject(ClazzUtils.newInstance(ClazzUtils.class), "a", 200);
        ClazzUtils.setObjectFieldObject(ClazzUtils.newInstance(ClazzUtils.class), "a4", 200);
        ClazzUtils.setObjectFieldObject(ClazzUtils.newInstance(ClazzUtils.class), "a6", 200);

        int a1 = (int) ClazzUtils.getObjectFieldObject(ClazzUtils.newInstance(ClazzUtils.class), "a");
        int a2 = (int) ClazzUtils.getObjectFieldObject(ClazzUtils.newInstance(ClazzUtils.class), "a4");
        int a3 = (int) ClazzUtils.getObjectFieldObject(ClazzUtils.newInstance(ClazzUtils.class), "a6");
        Assert.assertTrue(a1 == 200);
        Assert.assertTrue(a2 == 200);
        Assert.assertTrue(a3 == 200);
    }

    @Test
    public void getStaticFieldObject() {
        int a2 = (int) ClazzUtils.getStaticFieldObject(ClazzUtils.class, "a2");
        int a3 = (int) ClazzUtils.getStaticFieldObject(ClazzUtils.class, "a3");
        int a5 = (int) ClazzUtils.getStaticFieldObject(ClazzUtils.class, "a5");

        Assert.assertTrue(a2 == 100);
        Assert.assertTrue(a3 == 100);
        Assert.assertTrue(a5 == 100);

    }

    @Test
    public void invokeObjectMethod() {
        HashSet<String> set = new HashSet();
        String value1 = "";

        value1 = (String) ClazzUtils.invokeObjectMethod(ClazzUtils.newInstance(ClazzUtils.class), "publicstaticM");
        set.add(value1 + "3");
        Assert.assertEquals(new ClazzUtilsTargetTestClass().publicstaticM(), value1);

        value1 = (String) ClazzUtils.invokeObjectMethod(ClazzUtils.newInstance(ClazzUtils.class), "publicnotstaticM");
        set.add(value1 + "3");
        Assert.assertEquals(new ClazzUtilsTargetTestClass().publicnotstaticM(), value1);

        value1 = (String) ClazzUtils.invokeObjectMethod(ClazzUtils.newInstance(ClazzUtils.class), "privatestaticM");
        set.add(value1 + "3");
        Assert.assertTrue(value1.contains("privatestaticM"));

        value1 = (String) ClazzUtils.invokeObjectMethod(ClazzUtils.newInstance(ClazzUtils.class), "privatenotstaticM");
        set.add(value1 + "3");
        Assert.assertTrue(value1.contains("privatenotstaticM"));

        Assert.assertEquals(set.size(), 4);





        value1 = (String) ClazzUtils.invokeObjectMethod(ClazzUtils.newInstance(ClazzUtils.class), "publicstaticM");
        set.add(value1 + "3");
        Assert.assertEquals(new ClazzUtilsTargetTestClass().publicstaticM(), value1);

        value1 = (String) ClazzUtils.invokeObjectMethod(ClazzUtils.newInstance(ClazzUtils.class), "publicnotstaticM");
        set.add(value1 + "3");
        Assert.assertEquals(new ClazzUtilsTargetTestClass().publicnotstaticM(), value1);

        value1 = (String) ClazzUtils.invokeObjectMethod(ClazzUtils.newInstance(ClazzUtils.class), "privatestaticM");
        set.add(value1 + "3");
        Assert.assertTrue(value1.contains("privatestaticM"));

        value1 = (String) ClazzUtils.invokeObjectMethod(ClazzUtils.newInstance(ClazzUtils.class), "privatenotstaticM");
        set.add(value1 + "3");
        Assert.assertTrue(value1.contains("privatenotstaticM"));

    }


    @Test
    public void invokeStaticMethod() {
    }


    @Test
    public void newInstance() {
    }


    @Test
    public void getDexClassLoader() {
    }

    @Test
    public void hasMethod() {
    }


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
        Method method2 = ClazzUtils.getMethod(ClazzUtilsTest.class, "hello2");
        Assert.assertNotNull(method2);

        String str = (String) ClazzUtils.invokeStaticMethod("com.analysys.track.utils.reflectinon.ClazzUtilsTest",
                "hello2", null, null);
        Assert.assertEquals(str, "hello2");

        String str2 = (String) ClazzUtils.invokeObjectMethod(new ClazzUtilsTest(),
                "hello");
        Assert.assertEquals(str2, "hello");
    }
}