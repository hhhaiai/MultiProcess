package com.analysys.track.utils.data;

import com.analysys.track.internal.Content.EGContext;
import com.analysys.track.utils.ELOG;

import java.security.MessageDigest;

/**
 * @Copyright © 2019 sanbo Inc. All rights reserved.
 * @Description: MD5工具类
 * @Version: 1.0
 * @Create: 2019-08-05 16:33:18
 * @author: sanbo
 * @mail: xueyongfu@analysys.com.cn
 */
public class Md5Utils {
    public static String getMD5(String val) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(val.getBytes("UTF-8"));
            byte[] m = md5.digest();// 加密
            return getString(m);
        } catch (Throwable t) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(t);
            }
        }
        return "";
    }

    private static String getString(byte[] b) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            int a = b[i];
            if (a < 0) {
                a += 256;
            }
            if (a < 16) {
                buf.append("0");
            }
            buf.append(Integer.toHexString(a));
        }
        return String.valueOf(buf).substring(8, 24); // 16位; //32位
    }
}
