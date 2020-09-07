package com.analysys.track.utils;

import android.content.Context;

import com.analysys.track.utils.sp.SPHelper;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class OAIDHelper {
    public static final String OAID = "OAIDKEY";

    public static boolean tryGetOaidAndSave(Context context) {
        try {
            Class<?> refCus = Class.forName("com.bun.miitmdid.core.MdidSdkHelper");
            Class<?> callback;
            try {
                // 1.0.10 版本
                callback = Class.forName("com.bun.miitmdid.core.IIdentifierListener");
            } catch (ClassNotFoundException e) {
                // 1.0.23 版本
                callback = Class.forName("com.bun.miitmdid.interfaces.IIdentifierListener");
            }
            final InnerIdSupplier mh = new InnerIdSupplier();
            Object mObj = Proxy.newProxyInstance(context.getClassLoader(), new Class[]{callback}, mh);
            Method initSdk = refCus.getDeclaredMethod("InitSdk", Context.class, boolean.class, callback);
            int result = (int) initSdk.invoke(null, context, true, mObj);
            return result == 0;
        } catch (Throwable e) {
            return false;
        }
    }

    private static class InnerIdSupplier implements InvocationHandler {
        @Override
        public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
            try {
                if ("OnSupport".equals(method.getName())) {
                    boolean isSupport = (boolean) objects[0];
                    if (isSupport) {
                        Object idSupplier = objects[1];
                        String oaid = (String) idSupplier.getClass().getDeclaredMethod("getOAID").invoke(idSupplier);
                        SPHelper.setStringValue2SP(EContextHelper.getContext(),
                                OAIDHelper.OAID, oaid);
                    }
                    return null;
                }
                return method.invoke(o, objects);
            } catch (Throwable e) {
                return null;
            }
        }
    }

}
