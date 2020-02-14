package com.analysys.track.impl;

/**
 * @Copyright 2019 analysys Inc. All rights reserved.
 * @Description: 用来短路热修复转向,比如热修复文件损坏,不激活,本次不启用等
 * @Version: 1.0
 * @Create: 2019-11-06 11:33:12
 * @author: miqt
 * @mail: miqingtang@analysys.com.cn
 */
public class HotFixTransformCancel extends Exception {
    public HotFixTransformCancel(String message) {
        super(message);
    }
}
