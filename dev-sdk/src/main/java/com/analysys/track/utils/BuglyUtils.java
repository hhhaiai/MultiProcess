package com.analysys.track.utils;

import android.content.Context;

import java.lang.reflect.Method;

/**
 * @Copyright 2019 analysys Inc. All rights reserved.
 * @Description: 注意:这个类是内部测试用的,这个类帮助把SDK出现的一些错误,报到Bugly上去,正常发包情况下是不可以启用的,会误报错误到对接方的bugly上
 * @Version: 1.0
 * @Create: 2019-11-06 11:34:15
 * @author: miqt
 * @mail: miqingtang@analysys.com.cn
 */
public class BuglyUtils {

    public static void commitError(Throwable throwable) {
        ELOG.i(throwable);
        try {
            Class clazz = Class.forName("com.tencent.bugly.crashreport.CrashReport");
            setTag(clazz, 1002);
            postException(throwable, clazz);
            setTag(clazz, 1001);
        } catch (Throwable e) {

        }
    }

    private static void postException(Throwable throwable, Class<?> clazz) {
        try {
            Method postCatchedException = clazz.getMethod("postCatchedException", Throwable.class);
            postCatchedException.invoke(null, throwable);
        } catch (Throwable e) {

        }
    }

    private static void setTag(Class<?> clazz, int tag) {
        try {
            Method setUserSceneTag = clazz.getMethod("setUserSceneTag", Context.class, int.class);
            setUserSceneTag.invoke(null, EContextHelper.getContext(), tag);
        } catch (Throwable e) {

        }
    }
}
