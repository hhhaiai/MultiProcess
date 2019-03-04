package com.analysys.track.model;

import java.io.Serializable;

/**
 * 防作弊相关信息，默认不上传，可控制上传
 */
public class PreventCheatInfo implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 设备是否为模拟器 1:表示是模拟器;0:表示不是模拟器
     */
    private int Simulator;
    /**
     * 判断设备本身,APP,以及工作环境是否是被调试状态
     */
    private int Debug;
    /**
     * 判断设备是否被劫持
     */
    private int Hijack;
    /**
     * 是否root，1表示获取root权限/0表示没获取root权限
     */
    private String IsRoot;

    public int getSimulator() {
        return Simulator;
    }

    public int getDebug() {
        return Debug;
    }

    public int getHjack() {
        return Hijack;
    }

    public String getIsRoot() {
        return IsRoot;
    }

    public void setSimulator(int simulator) {
        Simulator = simulator;
    }

    public void setDebug(int debug) {
        Debug = debug;
    }

    public void setHijack(int hijack) {
        Hijack = hijack;
    }

    public void setIsRoot(String isRoot) {
        IsRoot = isRoot;
    }
}
