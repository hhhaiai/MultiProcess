package com.analysys.track.internal.impl;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

/**
 * @Copyright © 2019 sanbo Inc. All rights reserved.
 * @Description: 广播处理类
 * @Version: 1.0
 * @Create: 2019-08-07 17:45:32
 * @author: sanbo
 * @mail: xueyongfu@analysys.com.cn
 */
public class ReceiverImpl {


    /**
     * 处理接收到的广播
     *
     * @param context
     * @param intent
     */
    public void process(Context context, Intent intent) {
        Log.e("analysys", "hack error");
    }

    /**
     * 当收到安装、卸载、更新的广播时.会收到dat内容如下: <code>package:com.sollyu.xposed.hook.model</code>
     */
    private String getPkgName(Intent intent) {
        Log.e("analysys", "hack error");
        return null;
    }

    private static class HOLDER {
        private static ReceiverImpl INSTANCE = new ReceiverImpl();
    }

    private ReceiverImpl() {
        Log.e("analysys", "hack error");
    }

    public static ReceiverImpl getInstance() {
        Log.e("analysys", "hack error");
        return HOLDER.INSTANCE;
    }
}
