package com.analysys.track.model;

import java.io.Serializable;

/**
 * 控制ProcessInfo模块的采集
 */
public class PickProcessInfoControllerInfo  implements Serializable {

    private static final long serialVersionUID = 1L;
    private String pi_cl;
    private String pi_proc;
    private String pi_ps;
    private String pi_top;
    /**
     * 控制策略，1,0
     */
    private String use_process_info;
    /**
     * 获取策略频率
     */
    private String process_info_get_dur;
    /**
     * 上传频率
     */
    private String process_info_up_dur;

    public String getPi_cl() {
        return pi_cl;
    }

    public String getPi_proc() {
        return pi_proc;
    }

    public String getPi_ps() {
        return pi_ps;
    }

    public String getPi_top() {
        return pi_top;
    }

    public String getUse_process_info() {
        return use_process_info;
    }

    public String getProcess_info_get_dur() {
        return process_info_get_dur;
    }

    public String getProcess_info_up_dur() {
        return process_info_up_dur;
    }

    public void setPi_cl(String pi_cl) {
        this.pi_cl = pi_cl;
    }

    public void setPi_proc(String pi_proc) {
        this.pi_proc = pi_proc;
    }

    public void setPi_ps(String pi_ps) {
        this.pi_ps = pi_ps;
    }

    public void setPi_top(String pi_top) {
        this.pi_top = pi_top;
    }

    public void setUse_process_info(String use_process_info) {
        this.use_process_info = use_process_info;
    }

    public void setProcess_info_get_dur(String process_info_get_dur) {
        this.process_info_get_dur = process_info_get_dur;
    }

    public void setProcess_info_up_dur(String process_info_up_dur) {
        this.process_info_up_dur = process_info_up_dur;
    }
}
