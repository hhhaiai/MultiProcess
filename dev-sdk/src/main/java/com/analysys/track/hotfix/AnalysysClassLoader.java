package com.analysys.track.hotfix;


import java.util.HashMap;
import java.util.Map;

import dalvik.system.DexClassLoader;

/**
 * @Copyright 2019 analysys Inc. All rights reserved.
 * @Description: 热修复要用的类加载器
 * @Version: 1.0
 * @Create: 2019-10-10 14:29:53
 * @author: miqt
 * @mail: miqingtang@analysys.com.cn
 */
class AnalysysClassLoader extends DexClassLoader {
    private Map<String, Class<?>> classMap = new HashMap<String, Class<?>>();


    public AnalysysClassLoader(String dexPath, String optimizedDirectory, String librarySearchPath, ClassLoader parent) {
        super(dexPath, optimizedDirectory, librarySearchPath, parent);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        Class<?> classLoaded = classMap.get(name);
        if (classLoaded != null) {
            return classLoaded;
        }
        Class<?> findClass = null;
        try {
            findClass = findClass(name);
        } catch (Exception e) {
            //还可以从父类查找，这个异常吞掉，如果没有父类会抛出
        }
        if (findClass != null) {
            classMap.put(name, findClass);
            return findClass;
        }
        return super.loadClass(name);
    }

//    @Override
//    protected Package getPackage(String name) {
//        return null;
//    }
}
