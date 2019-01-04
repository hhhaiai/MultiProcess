package com.analysys.dev.model;

import java.io.Serializable;

/**
 * 进程信息，几种方式获取
 */
public class ProcessInfo implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * proc方式读取的数据
     */
    private String PROC;
    /**
     * shell执行top指令获取的数据
     */
    private String TOP;
    /**
     * shell执行ps指令获取的数据
     */
    private String PS;

    public String getPROC() {
        return PROC;
    }

    public String getTOP() {
        return TOP;
    }

    public String getPS() {
        return PS;
    }

    public void setPROC(String PROC) {
        this.PROC = PROC;
    }

    public void setTOP(String TOP) {
        this.TOP = TOP;
    }

    public void setPS(String PS) {
        this.PS = PS;
    }
}
