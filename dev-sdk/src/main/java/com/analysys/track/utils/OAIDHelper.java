package com.analysys.track.utils;

import android.content.Context;

import com.analysys.track.BuildConfig;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.utils.sp.SPHelper;
import com.bun.miitmdid.core.ErrorCode;
import com.bun.miitmdid.core.IIdentifierListener;
import com.bun.miitmdid.core.MdidSdkHelper;
import com.bun.miitmdid.supplier.IdSupplier;

public class OAIDHelper {

    public static String OAID = "oaid";

    public static boolean tryGetOaidAndSave(final Context context) {

        try {
            if (BuildConfig.logcat) {
                ELOG.d("tryGetOaidAndSave");
            }
            Class clazzMdidSdkHelper = Class.forName("com.bun.miitmdid.core.MdidSdkHelper");
            if (clazzMdidSdkHelper != null) {
                class InnerClass implements IIdentifierListener {
                    @Override
                    public void OnSupport(boolean isSupport, IdSupplier _supplier) {
                        if (_supplier == null) {
                            return;
                        }
                        String oaid = _supplier.getOAID();
                        //存oaid
                        if (oaid == null) {
                            return;
                        }
                        if (BuildConfig.logcat) {
                            ELOG.d("OAID = " + oaid);
                        }
                        SPHelper.setStringValue2SP(context, OAID, oaid);
//                        String vaid = _supplier.getVAID();
//                        String aaid = _supplier.getAAID();
                        _supplier.shutDown();
                    }
                }
                int result = MdidSdkHelper.InitSdk(context, true, new InnerClass());
                if (result == ErrorCode.INIT_ERROR_DEVICE_NOSUPPORT) {
                    //不支持的设备
                    if (BuildConfig.logcat) {
                        ELOG.d("不支持的设备");
                    }
                    return false;
                } else if (result == ErrorCode.INIT_ERROR_LOAD_CONFIGFILE) {
                    //加载配置文件出错
                    if (BuildConfig.logcat) {
                        ELOG.d("加载配置文件出错");
                    }
                    return false;
                } else if (result == ErrorCode.INIT_ERROR_MANUFACTURER_NOSUPPORT) {
                    //不支持的设备厂商
                    if (BuildConfig.logcat) {
                        ELOG.d("不支持的设备厂商");
                    }
                    return false;
                } else if (result == ErrorCode.INIT_ERROR_RESULT_DELAY) {
                    //获取接口是异步的，结果会在回调中返回，回调执行的回调可能在工作线程
                    if (BuildConfig.logcat) {
                        ELOG.d("获取接口是异步的，结果会在回调中返回，回调执行的回调可能在工作线程");
                    }
                    return false;
                } else if (result == ErrorCode.INIT_HELPER_CALL_ERROR) {
                    //反射调用出错
                    if (BuildConfig.logcat) {
                        ELOG.d("反射调用出错");
                    }
                    return false;
                } else {
                    return true;
                }
            }
        } catch (Throwable e) {
            //没有这个类代表宿主没集成 OAID 相关 SDK,不处理
            if (BuildConfig.logcat) {
                ELOG.d(e);
            }
        }

        return false;
    }
}
