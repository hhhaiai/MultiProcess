//package com.analysys.track.utils.memorydex;
//
//import android.annotation.SuppressLint;
//import android.content.Context;
//import android.os.Build;
//
//import java.nio.ByteBuffer;
//import java.util.HashSet;
//import java.util.Set;
//
//import dalvik.system.DexClassLoader;
//import dalvik.system.InMemoryDexClassLoader;
//
///**
// * @Copyright 2020 analysys Inc. All rights reserved.
// * @Description: 支持内存加载类文件，并修改了双亲委派逻辑的classloader
// * <p>
// * 目前执行的双亲委派逻辑依次为：<p>
// * 自身内存（仅android api >=26 支持）<p>
// * 自身缓存<p>
// * 自身文件<p>
// * 自身父亲<p>
// * 自身孩子<p>
// * <p>
// * throw ClassNotFoundException
// * @Version: 1.0
// * @Create: 2020-03-27 16:13:47
// * @author: miqt
// * @mail: miqingtang@analysys.com.cn
// */
//class FlipDelegateClassLoader extends DexClassLoader {
//    private final Context context;
//    /**
//     * 用来回调一个类具体是由谁加载了
//     */
//    private Callback callback;
//    /**
//     * 记录自己找不到的类
//     */
//    private Set<String> notFoundClass;
//    /**
//     * 内存dex
//     */
//    private InMemoryDexClassLoader memoryDexClassLoader;
//
//    /**
//     * api > 26 才可用
//     */
//    @SuppressLint("NewApi")
//    public FlipDelegateClassLoader(Context context, ByteBuffer dexBuffers, ClassLoader parent, Callback callback) {
//        this(context, "", null, null, parent, callback);
//        memoryDexClassLoader = new InMemoryDexClassLoader(dexBuffers, this);
//    }
//
//    /**
//     * api > 26 才可用
//     */
//    @SuppressLint("NewApi")
//    public FlipDelegateClassLoader(Context context, ByteBuffer[] dexBuffers, String librarySearchPath, ClassLoader parent, Callback callback) {
//        this(context, "", null, null, parent, callback);
//        memoryDexClassLoader = new InMemoryDexClassLoader(dexBuffers, librarySearchPath, this);
//    }
//
//    /**
//     * api > 27 才可用
//     */
//    @SuppressLint("NewApi")
//    public FlipDelegateClassLoader(Context context, ByteBuffer[] dexBuffers, ClassLoader parent, Callback callback) {
//        this(context, "", null, null, parent, callback);
//        memoryDexClassLoader = new InMemoryDexClassLoader(dexBuffers, this);
//    }
//
//
//    public FlipDelegateClassLoader(Context context, String dexPath, String optimizedDirectory, String librarySearchPath, ClassLoader parent, Callback callback) {
//        super(dexPath, optimizedDirectory, librarySearchPath, parent);
//        notFoundClass = new HashSet<>();
//        this.callback = callback;
//        this.context = context;
//    }
//
//    public FlipDelegateClassLoader(Context context, String dexPath, String optimizedDirectory, String librarySearchPath, ClassLoader parent) {
//        this(context, dexPath, optimizedDirectory, librarySearchPath, parent, null);
//    }
//
//    @Override
//    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
//        if (notFoundClass.contains(name)) {
//            notFoundClass.remove(name);
//            //向下双亲委派,告诉孩子："为父也找不到啊"
//            throw new ClassNotFoundException(name);
//        }
//        Class<?> c = null;
//        //-------内存文件中找，内存loader有可能会继续因为双亲委托拜托给我，因此我记下来这个类是给内存loader找的，拜托我的时候，直接告诉他"我也找不到啊"
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && memoryDexClassLoader != null) {
//            try {
//                notFoundClass.add(name);
//                c = memoryDexClassLoader.loadClass(name);
//                if (c != null) {
//                    if (callback != null) {
//                        callback.onLoadClass(memoryDexClassLoader, name);
//                    }
//                    return c;
//                }
//            } catch (ClassNotFoundException e) {
//            }
//        }
//        //-------自己缓存里面找
//        c = findLoadedClass(name);
//        if (c != null) {
//            if (callback != null) {
//                callback.onLoadClass(this, name);
//            }
//            return c;
//        }
//        //-------自己dex中找
//        try {
//            c = findClass(name);
//            if (c != null) {
//                if (callback != null) {
//                    callback.onLoadClass(this, name);
//                }
//                return c;
//            }
//        } catch (ClassNotFoundException e) {
//
//        }
//        //--------从父亲中找
//        try {
//            if (getParent() != null) {
//                c = getParent().loadClass(name);
//            }
//            if (c != null) {
//                if (callback != null) {
//                    callback.onLoadClass(getParent(), name);
//                }
//                return c;
//            }
//        } catch (ClassNotFoundException e) {
//        }
//        //--------给孩子找，孩子有可能会继续因为双亲委托拜托给我，因此我记下来这个类是给孩子找的，拜托我的时候，直接告诉他"为父也找不到啊"
//        notFoundClass.add(name);
//        try {
//            if (context.getClassLoader() != null) {
//                c = context.getClassLoader().loadClass(name);
//            }
//            if (c != null) {
//                if (callback != null) {
//                    callback.onLoadClass(context.getClassLoader(), name);
//                }
//                return c;
//            }
//        } catch (ClassNotFoundException e) {
//        }
//        //not found exception
//        if (callback != null) {
//            callback.onNotFound(name);
//        }
//        throw new ClassNotFoundException(name);
//    }
//
//    public interface Callback {
//        void onLoadClass(ClassLoader loader, String name);
//
//        void onNotFound(String name);
//    }
//}
