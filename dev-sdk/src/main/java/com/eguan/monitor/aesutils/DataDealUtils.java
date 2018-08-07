package com.eguan.monitor.aesutils;

import android.content.Context;

import com.eguan.monitor.Constants;
import com.eguan.monitor.commonutils.EgLog;
import com.eguan.monitor.commonutils.SPUtil;

import java.io.UnsupportedEncodingException;

public class DataDealUtils {

    public static String key = "";

    public static final String ORIGINKEY_STRING = "sysylana";

    public static String dealUploadData(Context mContext, String original) {
        String key_inner = SPUtil.getInstance(mContext).getKey();
        if (null != key_inner && key_inner.length() == 17) {
            key = makeSercretKey(key_inner);
        } else {
            key = ORIGINKEY_STRING;
        }
        byte[] zlibData = null;
        try {
            zlibData = ZLibUtils.compress(original.getBytes("utf-8"));
        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        }
        byte[] aesData = AESUtils.encrypt(zlibData, AESUtils.checkKey(key).getBytes());
        String result = AESUtils.toHex(aesData);

        return result;

    }

    private static String makeSercretKey(String value) {
        StringBuffer sb = new StringBuffer();
        sb.append(value.charAt(0)).append(value.charAt(1)).append(value.charAt(7))
                .append(value.charAt(8)).append(value.charAt(9)).append(value.charAt(10))
                .append(value.charAt(15)).append(value.charAt(16));
        String key = sb.toString();
        char[] keyCharArray = key.toCharArray();
        char[] cardinalNum = new char[4];
        char[] evenNum = new char[4];
        int x = 0;
        int y = 0;
        for (int i = 0; i < keyCharArray.length; i++) {
            if (i % 2 == 1) {
                cardinalNum[x++] = keyCharArray[i];
            } else {
                evenNum[y++] = keyCharArray[i];
            }
        }

        char[] cardinal2Num = new char[cardinalNum.length];
        for (int i = 0; i < cardinalNum.length; i++) {
            cardinal2Num[cardinalNum.length - i - 1] = cardinalNum[i];
        }
        return new String(evenNum) + new String(cardinal2Num);
    }
}
