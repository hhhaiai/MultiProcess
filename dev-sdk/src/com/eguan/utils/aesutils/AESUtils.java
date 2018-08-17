package com.eguan.utils.aesutils;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.eguan.Constants;
import com.eguan.utils.commonutils.EgLog;

import android.annotation.SuppressLint;
import android.text.TextUtils;

public class AESUtils {
    static final String CIPHER_ALGORITHM_ECB = "AES/ECB/PKCS5Padding";

    public static String checkKey(String rawpassword) {

        int strLen = rawpassword.length();
        if (strLen > 16) {
            rawpassword = rawpassword.substring(0, 16);
        } else {
            while (strLen < 16) {
                StringBuffer buffer = new StringBuffer();
                buffer.append(rawpassword).append("0");
                rawpassword = buffer.toString();
                strLen = rawpassword.length();
            }
        }
        return rawpassword;
    }

    /**
     * 内部使用 加密 通过 rawpassword 加密 content
     *
     * @param content
     * @param rawpassword
     * @return
     */
    @SuppressWarnings("unused")
    @SuppressLint("TrulyRandom")
    public static byte[] encrypt(byte[] content, byte[] rawpassword) {
        try {
            byte[] rawkey = getRawKey(rawpassword);
            // 应该使用rawkey;
            SecretKeySpec secretKeySpec = new SecretKeySpec(rawpassword, "AES");
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM_ECB);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);

            byte[] result = cipher.doFinal(content);
            return result;
        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        }

        return null;
    }

    /**
     * 内部使用 解密
     *
     * @param content
     *            待解密内容
     * @param rawpassword
     *            解密密钥
     * @return
     */

    @SuppressWarnings("unused")
    public static byte[] decrypt(byte[] content, byte[] rawpassword) {

        try {
            byte[] rawkey = getRawKey(rawpassword);
            // 应该使用RAWKEY;
            SecretKeySpec secretKeySpec = new SecretKeySpec(rawpassword, "AES");

            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM_ECB);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);

            byte[] result = cipher.doFinal(content);
            return result;
        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        }

        return null;
    }

    /**
     * 与 toHex 成对使用 String 转 byte[] 将传进来的字符串转换成byte数组
     *
     * @param cipherString
     * @return 将传进来的字符串转换成byte数组
     */
    public static byte[] toBytes(String cipherString) {
        if (TextUtils.isEmpty(cipherString))
            return null;
        int len = cipherString.length() / 2;
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++) {
            result[i] = Integer.valueOf(cipherString.substring(2 * i, 2 * i + 2), 16).byteValue();
        }
        return result;
    }

    /**
     * 与 toBytes 成对使用 byte[] 转 String
     *
     * @param contentBytes
     *            需要转换的byte数组
     * @return 返回contentBytes转换成的16进制字符串
     */
    public static String toHex(byte[] contentBytes) {

        String HEX = "0123456789ABCDEF";
        if (contentBytes == null)
            return "";
        StringBuffer result = new StringBuffer(contentBytes.length * 2);
        for (int i = 0; i < contentBytes.length; i++) {
            // byte数组的每个元素为8位，前四位right shift 4 后与 00001111与运算 ，后四位 直接与00001111与运算
            result.append(HEX.charAt((contentBytes[i] >> 4) & 0x0f)).append(HEX.charAt(contentBytes[i] & 0x0f));
        }
        return result.toString();
    }

    /**
     * 通过最初的rawKey获得子key
     *
     * @param rawpassword
     * @return
     */
    private static byte[] getRawKey(byte[] rawpassword) {
        try {
            KeyGenerator keygen = KeyGenerator.getInstance("AES");
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            sr.setSeed(rawpassword);
            keygen.init(128, sr);
            SecretKey secretKey = keygen.generateKey();
            byte[] result = secretKey.getEncoded();

            return result;

        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        }
        return null;
    }

}
