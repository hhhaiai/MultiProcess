package com.analysys.track.internal.impl.proc;

/**
 * @Copyright © 2019 analysys Inc. All rights reserved.
 * @Description: 获取进程详情
 * @Version: 1.0
 * @Create: Mar 6, 2019 8:28:01 PM
 * @Author: sanbo
 */
public class ProcessInfo {
    public int mPid = -1;
    public String mPkgName = "";

    public ProcessInfo(int pid, String pkg) {
        mPid = pid;
        mPkgName = pkg;
    }

    public int getPid() {
        return mPid;
    }

    public String getPkgName() {
        return mPkgName;
    }

    @Override
    public String toString() {
        return String.format("(%s:%s)", mPid, mPkgName);
    }
}