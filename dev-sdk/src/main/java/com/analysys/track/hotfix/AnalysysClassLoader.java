package com.analysys.track.hotfix;


import com.analysys.track.internal.content.EGContext;

import java.util.HashMap;
import java.util.Map;

import dalvik.system.DexClassLoader;

/**
 * @Copyright 2019 analysys Inc. All rights reserved.
 * @Description: 热修复要用的类加载器, 反转了双亲委托, 先自己找, 自己找不到拜托parent找,父亲一般是PathClassLoader
 * @Version: 1.0
 * @Create: 2019-10-10 14:29:53
 * @author: miqt
 * @mail: miqingtang@analysys.com.cn
 */
class AnalysysClassLoader extends DexClassLoader {

    /**
     * 用来回调一个类具体是由谁加载了
     */
    private Callback callback;

    public AnalysysClassLoader(String dexPath, String optimizedDirectory, String librarySearchPath, ClassLoader parent, Callback callback) {
        super(dexPath, optimizedDirectory, librarySearchPath, parent);
        this.callback = callback;
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        //-------hotfix cache
        Class<?> c = findLoadedClass(name);
        if (c != null) {
            if (callback != null) {
                callback.onLoadByCache(name);
            }
            return c;
        }
        //-------hotfix self
        try {
            c = findClass(name);
            if (c != null) {
                if (callback != null) {
                    callback.onLoadBySelf(name);
                }
                return c;
            }
        } catch (ClassNotFoundException e) {

        }
        if (callback != null) {
            callback.onSelfNotFound(name);
        }
        //--------host or parent
        try {
            if (getParent() != null) {
                c = getParent().loadClass(name);
            }
            if (c != null) {
                if (callback != null) {
                    callback.onLoadByParent(name);
                }
                return c;
            }
        } catch (ClassNotFoundException e) {
        }
        //not found error
        if (callback != null) {
            callback.onNotFound(name);
        }
        throw new ClassNotFoundException(name);
    }

    public interface Callback {
        void onSelfNotFound(String name);

        void onLoadBySelf(String name);

        void onLoadByCache(String name);

        void onLoadByParent(String name);

        void onNotFound(String name);
    }
}
