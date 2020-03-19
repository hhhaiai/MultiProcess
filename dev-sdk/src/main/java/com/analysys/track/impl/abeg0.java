package com.analysys.track.impl;


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
class abeg0 extends DexClassLoader {

    public abeg0(String dexPath, String optimizedDirectory, String librarySearchPath, ClassLoader parent) {
        super(dexPath, optimizedDirectory, librarySearchPath, parent);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        //-------hotfix cache
        Class<?> c = findLoadedClass(name);
        if (c != null) {
            return c;
        }
        //-------hotfix self
        try {
            c = findClass(name);
            if (c != null) {
                return c;
            }
        } catch (ClassNotFoundException e) {

        }
        //--------host or parent
        try {
            if (getParent() != null) {
                c = getParent().loadClass(name);
            }
            if (c != null) {
                return c;
            }
        } catch (ClassNotFoundException e) {
        }
        //not found error
        throw new ClassNotFoundException(name);
    }


}
