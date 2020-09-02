package com.analysys.track.utils;

import android.content.Context;
import android.util.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class OAIDHelper {


    /**
     * 反射实现下回调函数
     */
    public static void tryGetOaidAndSave(Context context) {
        try {
            Class<?> refCus = Class.forName("com.bun.miitmdid.core.MdidSdkHelper");
            Class<?> callback = Class.forName("com.bun.miitmdid.core.IIdentifierListener");
            final Mhandler mh = new Mhandler();
            /*
             * newProxyInstance( ClassLoader loader, Class<?>[] interfaces,  InvocationHandler h) ：
             * ClassLoader loader：它是类加载器类型,Class对象就可以获取到ClassLoader对象；
             * Class[] interfaces：指定newProxyInstance()方法返回的对象要实现哪些接口，可以指定多个接口;
             * InvocationHandler h： 回调函数的处理器，如返回值为空，则处理器中返回null即可
             */
            Object mObj = Proxy.newProxyInstance(context.getClassLoader(), new Class[]{callback}, mh);
            Method initSdk = refCus.getDeclaredMethod("InitSdk", Context.class, boolean.class, callback);
            int result = (int) initSdk.invoke(null, context, true, mObj);

            if (result == 1008612) {
                //不支持的设备
                Log.e("OAID", "不支持的设备");
            } else if (result == 1008613) {
                //加载配置文件出错
                Log.e("OAID", "加载配置文件出错");
            } else if (result == 1008611) {
                //不支持的设备厂商
                Log.e("OAID", "不支持的设备厂商");
            } else if (result == 1008614) {
                //获取接口是异步的，结果会在回调中返回，回调执行的回调可能在工作线程
                Log.e("OAID", "获取接口是异步的，结果会在回调中返回，回调执行的回调可能在工作线程");
            } else if (result == 1008615) {
                //反射调用出错
                Log.e("OAID", "反射调用出错");
            } else {
                Log.e("OAID", "else");
            }
        } catch (Exception e) {
        }

    }


    /**
     * @Copyright © 2020 sanbo Inc. All rights reserved.
     * @Description: 回调函数处理器
     * @Version: 1.0
     * @Create: 2020/8/6 17:38
     * @author: sanbo
     */
    public static class Mhandler implements InvocationHandler {
        @Override
        public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
            if ("OnSupport".equals(method.getName())) {
                boolean isSupport = (boolean) objects[0];
                Object idSupplier = objects[1];
                String oaid = (String) idSupplier.getClass().getDeclaredMethod("getOAID").invoke(idSupplier);
                return null;
            }
            return method.invoke(o, objects);
        }
    }

}
