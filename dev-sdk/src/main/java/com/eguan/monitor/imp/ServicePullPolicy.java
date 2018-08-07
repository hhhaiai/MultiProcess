package com.eguan.monitor.imp;

import org.json.JSONObject;

import java.util.Map;

/**
 * Created on 2017/8/27.
 * Author : chris
 * Email  : mengqi@analysys.com.cn
 * Detail :
 */
public class ServicePullPolicy {
    private String packageName;
    private String className;
    private String serviceAction;
    private String extra;

    public String getAction() {
        return serviceAction;
    }

    public void setAction(String serviceAction) {
        this.serviceAction = serviceAction;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra){
        this.extra = extra;
    }

    public void setExtra(Map extra) {
        this.extra = new JSONObject(extra).toString();
    }

    public String getPackageName() {
        return packageName;
    }
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
    public String getClassName() {
        return className;
    }
    public void setClassName(String className) {
        this.className = className;
    }
//    public ContentValues transferCV(Context mContext) throws EGDBEncryptException{
//        ContentValues cv = new ContentValues();
//        cv.put(EGServicePullPolicyTable.PACKAGE_NAME, EncryptUtils.encrypt(mContext,packageName));
//        cv.put(EGServicePullPolicyTable.CLASS_NAME,EncryptUtils.encrypt(mContext,className));
//        cv.put(EGServicePullPolicyTable.SERVICE_ACTION,EncryptUtils.encrypt(mContext,serviceAction));
//        cv.put(EGServicePullPolicyTable.EXTRA,EncryptUtils.encrypt(mContext,extra));
//        cv.put(EGServicePullPolicyTable.INSERT_TIME,System.currentTimeMillis() + "");
//        return cv;
//    }
    @Override
    public String toString() {
        return this.hashCode() + "-> " + packageName + ":" + className;
    }
}