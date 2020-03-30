package com.analysys.track.internal.net;

import android.content.Context;
import android.text.TextUtils;

import com.analysys.track.BuildConfig;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.internal.content.UploadKey;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.SystemUtils;
import com.analysys.track.utils.reflectinon.DevStatusChecker;
import com.analysys.track.utils.sp.SPHelper;

import java.net.HttpURLConnection;

/**
 * @Copyright © 2020 sanbo Inc. All rights reserved.
 * @Description: http Resuest header
 * @Version: 1.0
 * @Create: 2020/3/30 11:12
 * @author: sanbo
 */
public class HeaderHelper {

    /**
     * add Http's header
     *
     * @param context
     * @param connection
     */
    public static void addHeaderProperties(Context context, HttpURLConnection connection) {
        try {
            if (connection == null) {
                return;
            }
            String plocyVersion = SPHelper.getStringValueFromSP(context, UploadKey.Response.RES_POLICY_VERSION, "0");

            // 添加头信息
            connection.setRequestProperty(EGContext.SDKV, EGContext.SDK_VERSION);
            connection.setRequestProperty(EGContext.DEBUG, DevStatusChecker.getInstance().isSelfDebugApp(context) ? "1" : "0");
            connection.setRequestProperty(EGContext.DEBUG2, DevStatusChecker.getInstance().isDebugDevice(context) ? "1" : "0");
            connection.setRequestProperty(EGContext.APPKEY, SystemUtils.getAppKey(context));
            connection.setRequestProperty(EGContext.TIME, SPHelper.getStringValueFromSP(context, EGContext.TIME, ""));
            // 策略版本号
            connection.setRequestProperty(EGContext.POLICYVER, plocyVersion);

            connection.setRequestProperty(EGContext.POLICYVER, plocyVersion);
            // connection.setRequestProperty(EGContext.POLICYVER, "0");
            //  // 区分3.x. 可以忽略不写
            // connection.setRequestProperty(EGContext.PRO, EGContext.PRO_KEY_WORDS);// 写死
            // 兼容墨迹版本区别需求增加。普通版本不增加该值
            connection.setRequestProperty(EGContext.UPLOAD_HEAD_APPV, SystemUtils.getAppV(context));
            //当前热修版本
            if (!EGContext.IS_HOST) {
                connection.setRequestProperty(EGContext.HOTFIX_VERSION, BuildConfig.hf_code);
            }
            //http设置debug选项
            setDebugKnHeader(context, connection);
            // 打印请求头信息内容
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.i(BuildConfig.tag_upload, "========HTTP头： " + connection.getRequestProperties().toString());
            }
        } catch (Throwable e) {
        }
    }

    private static void setDebugKnHeader(Context context, HttpURLConnection connection) {
        try {
            if (BuildConfig.isNativeDebug) {

                String k1 = AnaCountImpl.getKx1(context);
                if (!TextUtils.isEmpty(k1)) {
                    connection.setRequestProperty("K1", String.valueOf(k1));
                }
                String k2 = AnaCountImpl.getKx2(context);
                if (!TextUtils.isEmpty(k2)) {
                    connection.setRequestProperty("K2", String.valueOf(k2));
                }
            }
        } catch (Throwable e) {
        }
    }

}
