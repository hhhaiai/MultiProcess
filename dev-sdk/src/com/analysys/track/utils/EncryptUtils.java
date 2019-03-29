package com.analysys.track.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Base64;

import com.analysys.track.internal.Content.EGContext;

import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * @Copyright © 2018 EGuan Inc. All rights reserved.
 * @Description: 加解密工具类。用法: 数据库初始化时,调用checkEncryptKey(Context context)确认之前加解密部分是否正常工作,正常工作可以测试下DB之前数据是否正常解密,根据结果进行相关操作
 * 加密调用接口:encrypt(Context context, String str) 解密调用接口:decrypt(Context context, String str)
 * @Version: 1.0
 * @Create: 2018年2月2日 上午11:50:40
 * @Author: sanbo
 */
public class EncryptUtils {
    private static final byte[] iv = {0xA, 0xB, 0xC, 0xD, 0xE, 0xF, 3, 11, 42, 9, 1, 6, 8, 33, 21, 91};
    private static String mEncryptKey = null;
    private static final String SP_EK_ID = "track_id";
    private static final String SP_CONTENT = "track";

//    public static String getCheckID(Context context) {
//        if (context == null) {
//            return "";
//        }
//        if (TextUtils.isEmpty(mEncryptKey)) {
//            init(context);
//        }
//        try {
//            return encrypt(context, SP_CONTENT);
//        }catch (Throwable e) {
//        }
//        return "";
//    }

    /**
     * 测试key是否可用
     *
     * @param context
     * @return
     */
    public static boolean checkEncryptKey(Context context) {
        try {
            String testEnKey = encrypt(context, SP_CONTENT);
            if (!TextUtils.isEmpty(testEnKey)) {
                String testDeKey = decrypt(context, testEnKey);
                if (!TextUtils.isEmpty(testDeKey)) {
                    if (SP_CONTENT.equals(testDeKey)) {
                        return true;
                    }
                }

            }
        } catch (Throwable e) {
        }
        return false;
    }

    public static void reInitKey(Context context) {
        if (context == null) {
            return;
        }
        clearEncryptKey(context);
        init(context);
    }

    private static void clearEncryptKey(Context context) {
        if (context == null) {
            return;
        }
        SharedPreferences pref = context.getSharedPreferences(EGContext.SPUTIL, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        // editor.putString(SP_EK_ID, "");
        editor.remove(SP_EK_ID);
        editor.apply();
        mEncryptKey = null;
    }

    /**
     * 加密接口
     *
     * @param context
     * @param src     加密的字符串
     * @return 加密后的字符串，可能为""
     */
    public static String encrypt(Context context, String src) {
        try {
            if (TextUtils.isEmpty(mEncryptKey)) {
                init(context);
            }
            if (TextUtils.isEmpty(src)) {
                return "";
            }
            byte[] b = encrypt(src.getBytes(), mEncryptKey.getBytes());
            return Base64.encodeToString(b, Base64.DEFAULT);

        } catch (Throwable e) {
            return "";
        }
    }

    /**
     * 解密接口
     *
     * @param context
     * @param str     待解密的字符串
     * @return 解密后的字符串
     */
    public static String decrypt(Context context, String str) {
        try {

            if (TextUtils.isEmpty(mEncryptKey)) {
                init(context);
            }
            if (TextUtils.isEmpty(str)) {
                return "";
            }
            byte[] b = Base64.decode(str.getBytes(), Base64.DEFAULT);
            return new String(decrypt(b, mEncryptKey.getBytes()));
        } catch (Throwable e) {
            return "";
        }
    }

    /**
     * 初始化秘钥key,检验确保可用.
     *
     * @param context
     */
    public static void init(Context context) {
        try {
            // 1.获取本地参考key
            SharedPreferences pref = context.getSharedPreferences(EGContext.SPUTIL, Context.MODE_PRIVATE);
            String id = pref.getString(SP_EK_ID, "");

            // 2.参考key异常则重新生成
            if (TextUtils.isEmpty(id) || !id.contains("|")) {
                String preID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                String serialNO = null;
                if (Build.VERSION.SDK_INT > 24) {
                    if (checkPermission(context, Manifest.permission.READ_PHONE_STATE)) {
                        try {
                            Class<?> clazz = Class.forName("android.os.Build");
                            Method method = clazz.getMethod("getSerial");
                            serialNO = (String) method.invoke(null);
                        } catch (Exception e) {
                        }
                    }
                } else {
                    serialNO = Build.SERIAL;
                }

                // 确保preID非空
                if (TextUtils.isEmpty(preID)) {

                    String uuid = UUID.randomUUID().toString();
                    if (uuid.contains("-")) {
                        String[] s = uuid.split("-");
                        if (s.length > 0) {
                            preID = s[s.length - 1];
                        }
                    } else {
                        preID = uuid;
                    }
                }
                // 确保serialNO非空
                if (TextUtils.isEmpty(serialNO)) {
                    serialNO = String.valueOf(System.currentTimeMillis() / 10000);
                }
                id = preID + "|" + serialNO;
                pref.edit().putString(SP_EK_ID, id).apply();
            }

            // 3.确保参考key的情况下，生成秘钥
            if (!TextUtils.isEmpty(id)) {
                String[] ss = id.split("\\|");
                String preID = ss[0];
                String fID = ss[1];
                int firstKey = 2;

                try {
                    firstKey = ensure(preID.charAt(2));
                } catch (NumberFormatException e) {
                    try {
                        firstKey = ensure(preID.charAt(2));
                    } catch (NumberFormatException ee) {
                        firstKey = 3;
                    } catch (Throwable eeee) {
                    }
                }

                int endKey = 5;
                try {
                    endKey = ensure(fID.charAt(2));
                } catch (NumberFormatException e) {
                    try {
                        endKey = ensure(fID.charAt(3));
                    } catch (NumberFormatException ee) {
                        endKey = 6;
                    }
                }

                int start = 0;
                if (firstKey < 6) {
                    start = firstKey;
                } else {
                    start = ensureLowFive(firstKey);
                }

                int dur = 0;
                if (endKey < 6) {
                    dur = endKey;
                } else {
                    dur = ensureLowFive(endKey);
                }
                mEncryptKey = md5(SP_CONTENT + id.substring(start, (start + dur)));
            }
        } catch (Throwable e) {
        }
    }

    /**
     * 确保小于10
     *
     * @param key 大于5的数字
     * @return 小于10的值
     */
    private static int ensureLowFive(int key) {
        ;
        int temp = Math.abs(key - 10);
        if (temp > 5) {
            return ensureLowFive(temp);
        } else {
            return temp;
        }
    }

    /**
     * 确定char是数字
     *
     * @param key
     * @return 返回小于10的数字
     */
    private static int ensure(char key) throws NumberFormatException {
        return Integer.parseInt(Character.toString(key));
    }

    private static String md5(String str) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(str.getBytes());
            String result = "";
            for (byte b : bytes) {
                String temp = Integer.toHexString(b & 0xff);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                result += temp;
            }
            return result;
        } catch (Exception e) {
        }
        return "";
    }

    @SuppressLint("TrulyRandom")
    private static byte[] encrypt(byte[] input, byte[] password) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
        SecretKeySpec keySpec = new SecretKeySpec(password, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
        return cipher.doFinal(input);
    }

    private static byte[] decrypt(byte[] input, byte[] password) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
        SecretKeySpec keySpec = new SecretKeySpec(password, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
        return cipher.doFinal(input);
    }

    private static boolean checkPermission(Context context, String permission) {
        boolean result = false;
        if (Build.VERSION.SDK_INT >= 23) {
            try {
                Class<?> clazz = Class.forName("android.content.Context");
                Method method = clazz.getMethod("checkSelfPermission", String.class);
                int rest = (Integer) method.invoke(context, permission);
                result = rest == PackageManager.PERMISSION_GRANTED;
            } catch (Exception e) {
                result = false;
            }
        } else {
            PackageManager pm = context.getPackageManager();
            if (pm.checkPermission(permission, context.getPackageName()) == PackageManager.PERMISSION_GRANTED) {
                result = true;
            }
        }
        return result;
    }

}
