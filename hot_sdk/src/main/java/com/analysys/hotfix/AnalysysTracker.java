package com.analysys.hotfix;

import android.content.Context;

import com.analysys.track.internal.AnalysysInternal;

import dalvik.system.DexClassLoader;

/**
 * @Copyright © 2019 sanbo Inc. All rights reserved.
 * @Description: SDK API层接口类
 * @Version: 1.0
 * @Create: 2019-08-05 16:13:10
 * @author: sanbo
 */
public class AnalysysTracker {

    /**
     * 初始化SDK
     *
     * @param context
     * @param appKey
     * @param channel
     */
    public static void init(Context context, String appKey, String channel) {
        //<editor-fold desc="这部分应该由一个热修复宝管理器来管理拷贝,下发等等的东西,这里初步这么写">
        //todo 修复包下载和验证逻辑
        String path = context.getCacheDir() + "/analysys.jar";
        FileUtils.copyFileFromAssets(context,
                "analysys_track_v4.3.0.5_20190919.jar", path);
        //</editor-fold>
        DexClassLoader loader = new DexClassLoader(path, context
                .getCacheDir().getAbsolutePath(),
                null, Object.class.getClassLoader().getParent());
        try {
            HackClassLoader.hackParentClassLoader(Object.class.getClassLoader(), loader);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //这里只是壳子,由于双亲委托,实际上运行的是jar包里的东西
        //如果空指针证明jar包没Hack成功或者已经被加载过了
        //hack之前千万不能用这个类
        AnalysysInternal.getInstance(context).initEguan(appKey, channel);
    }

    /**
     * 设置Debug模式
     *
     * @param isDebug
     */
    public static void setDebugMode(boolean isDebug) {
        //todo 反射实现? 或者放在AnalysysInternal
    }
}
