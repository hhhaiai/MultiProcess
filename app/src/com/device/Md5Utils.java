package com.device;

import java.security.MessageDigest;

public class Md5Utils {
    public static String getMD5(String val) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(val.getBytes("UTF-8"));
            byte[] m = md5.digest();//加密
            return getString(m);
        } catch (Throwable t) {

        }
        return "";
    }

    private static String getString(byte[] b) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            int a = b[i];
            if (a < 0){
                a += 256;
            }
            if (a < 16){
                buf.append("0");
            }
            buf.append(Integer.toHexString(a));
        }
        return String.valueOf(buf).substring(8, 24);  //16位; //32位
    }
}
