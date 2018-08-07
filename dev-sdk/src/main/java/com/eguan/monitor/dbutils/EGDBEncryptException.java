package com.eguan.monitor.dbutils;

/**
 * @Copyright © 2018 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 18/2/3 16:04
 * @Author: sanbo
 */
public class EGDBEncryptException extends Exception {
    public EGDBEncryptException() {
        super();
    }

    public EGDBEncryptException(String msg) {
        super(msg);
    }

    public EGDBEncryptException(Exception e) {
        super(e);
    }
}
