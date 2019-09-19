package com.analysys.track.utils.reflectinon;

import com.analysys.track.AnalsysTest;

import org.junit.Test;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import static org.junit.Assert.*;

public class PatchHelperTest extends AnalsysTest {

    @Test
    public void load() {
    }

    @Test
    public void loads() {
    }

    @Test
    public void loadStatic() {
        try {
            //测试加载外部路径jar
            File file = new File("/mnt/sdcard/DCIM/test.jar");
            String classname = "AD";
            String methodName = "getString";
            Class[] pt = new Class[]{String.class};
            Object[] pv = new Object[]{"123123"};
            //测试加载内部路径jar
            File innerF = new File(mContext.getFilesDir().getAbsolutePath() + "/jarp/test.jar");
            file.renameTo(innerF);
            classname = "AD";
            methodName = "getString";
            pt = new Class[]{String.class};
            pv = new Object[]{"123123"};
            PatchHelper.loadStatic(mContext, innerF, classname, methodName, pt, pv);
            //测试加载静态内部类的static方法
            classname = "AD$Inner";
            methodName = "getString";
            pt = new Class[]{String.class};
            pv = new Object[]{"123123"};
            PatchHelper.loadStatic(mContext, innerF, classname, methodName, pt, pv);
            //测试反射ActivityThread
            classname = "AD$Inner";
            methodName = "closeAndroidPDialog";
            pt = new Class[]{};
            pv = new Object[]{};
            PatchHelper.loadStatic(mContext, innerF, classname, methodName, pt, pv);

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } finally {
        }
    }

    @Test
    public void load1() {
    }
}