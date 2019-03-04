package com.analysys.track.internal.impl;

import android.util.Base64;

/**
 * @Copyright © 2018 Analysys Inc. All rights reserved.
 * @Description: 上行URL生成类
 * @Version: 1.0
 * @Create: 2018年9月11日 上午11:07:36
 * @Author: sanbo
 */
public class URLGeneral {

    // 基础域名
    @SuppressWarnings("unused")
    private static final String BASE_HOST = Base64.encodeToString(".analysys.cn".getBytes(), Base64.DEFAULT);
    // 前缀域名
    @SuppressWarnings("unused")
    private static final String BASE_PREFIX = Base64.encodeToString("urd".getBytes(), Base64.DEFAULT);
    // 正式端口
    @SuppressWarnings("unused")
    private static final int ONLINE_PORT = 8089;
    // 测试域名
    @SuppressWarnings("unused")
    private static final String DEBUG_URL =
        Base64.encodeToString("http://apptest.analysys.cn:10031".getBytes(), Base64.DEFAULT);

    // 339头部应用 用作测试
    @SuppressWarnings("unused")
    private static final String[] SERVER_ID =
        {"130", "240", "183", "409", "203", "490", "609", "301", "405", "025", "339"};

    /**
     * 
     * @return
     */
    public static String getURL() {

        return null;
    }

}
