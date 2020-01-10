package com.analysys.track.utils;

import android.text.TextUtils;
import android.util.Base64;

import com.analysys.track.BuildConfig;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.utils.data.AESUtils;
import com.analysys.track.utils.data.Md5Utils;

import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * @Copyright © 2019 sanbo Inc. All rights reserved.
 * @Description: @策略加密
 * @Version: 1.0
 * @Create: 2019-12-04 19:39
 * @author: sanbo
 */
public class PolicyEncrypt {

    private static volatile PolicyEncrypt instance = null;

    private PolicyEncrypt() {
    }

    public static PolicyEncrypt getInstance() {
        if (instance == null) {
            synchronized (PolicyEncrypt.class) {
                if (instance == null) {
                    instance = new PolicyEncrypt();
                }
            }
        }
        return instance;
    }

    /**
     * 加密
     *
     * @param msg       加密字符串
     * @param appkey    appkey
     * @param sdkv      sdk版本(4.3.0.5|20191212)
     * @param isDebug   request body使用的是0/1，respons policy使用的是-1
     * @param timestamp 时间戳，SDK request body使用是外部传入的当前时间，respons policy 使用的是固定字符串[不含空格]: 20191206_Analysys
     * @return
     */
    public String encode(String msg, String appkey, String sdkv, String isDebug, String timestamp) {
        try {
            if (TextUtils.isEmpty(msg)) {
                return msg;
            }
            if (TextUtils.isEmpty(appkey)) {
                appkey = EGContext.ORIGINKEY_STRING;
            }
            if (TextUtils.isEmpty(isDebug)) {
                timestamp = "-1";
            }
            if (TextUtils.isEmpty(timestamp)) {
                timestamp = "20191206_Analysys";
            }
            String sercretKey = makeSercretKey(appkey, sdkv, isDebug, timestamp);

            byte[] def = DeflterCompressUtils.compress(URLEncoder.encode(URLEncoder.encode(msg, "UTF-8"), "UTF-8").getBytes("UTF-8"));
            byte[] encryptMessage = AESUtils.encrypt(def, sercretKey.getBytes("UTF-8"));
            if (encryptMessage != null) {
                byte[] returnData = Base64.encode(encryptMessage, Base64.DEFAULT);
                return new String(returnData).replace("\n", "");
            }
        } catch (Throwable t) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(t);
            }
        }

        return null;
    }


    /**
     * 解密
     *
     * @param msg       解密字符串
     * @param appkey    appkey
     * @param sdkv      sdk版本(4.3.0.5|20191212)
     * @param isDebug   request body使用的是0/1，respons policy使用的是-1
     * @param timestamp 时间戳，SDK request body使用是外部传入的当前时间，respons policy 使用的是固定字符串[不含空格]: 20191206_Analysys
     * @return
     */
    public String decode(String msg, String appkey, String sdkv, String isDebug, String timestamp) {

        try {
            if (TextUtils.isEmpty(msg)) {
                return msg;
            }
            if (TextUtils.isEmpty(appkey)) {
                appkey = EGContext.ORIGINKEY_STRING;
            }
            if (TextUtils.isEmpty(isDebug)) {
                timestamp = "-1";
            }
            if (TextUtils.isEmpty(timestamp)) {
                timestamp = "20191206_Analysys";
            }
            String sercretKey = makeSercretKey(appkey, sdkv, isDebug, timestamp);
            byte[] decodeBase64Data = Base64.decode(msg, Base64.DEFAULT);
            byte[] aesDecrypt = AESUtils.decrypt(decodeBase64Data, sercretKey.getBytes("UTF-8"));
            byte[] deflterData = DeflterCompressUtils.decompress(aesDecrypt);
            String unzipData = new String(deflterData, "UTF-8");
            return URLDecoder.decode(URLDecoder.decode(unzipData, "UTF-8"), "UTF-8");
        } catch (Throwable igone) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(igone);
            }
        }


        return null;
    }

    private static String makeSercretKey(String appkey, String sdkv, String isDebug, String timestamp) {
        StringBuilder sb = new StringBuilder();
        if (appkey.length() > 3) {
            appkey = appkey.substring(0, 3);
        }
        if (sdkv.contains("|")) {
            sdkv = sdkv.substring(0, sdkv.indexOf("|")).replace(".", "");
        } else {
            sdkv = sdkv.replace(".", "");
        }
        sb.append(sdkv);
        sb.append(isDebug);
        sb.append(appkey);// 前三位
        sb.append(timestamp);
        return Md5Utils.getMD5(String.valueOf(sb));
    }

}
