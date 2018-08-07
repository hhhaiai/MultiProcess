package com.eguan.monitor.imp;

/**
 * Created on 2018/1/4.
 * Author : chris
 * Email  : mengqi@analysys.com.cn
 * Detail :
 */

public class OCTimeBean {
    public String packageName;
    public String timeInterval;
    public int count;

    public OCTimeBean(){

    }

    public OCTimeBean(String packageName, String timeInterval, int count) {
        this.packageName = packageName;
        this.timeInterval = timeInterval;
        this.count = count;
    }
}
