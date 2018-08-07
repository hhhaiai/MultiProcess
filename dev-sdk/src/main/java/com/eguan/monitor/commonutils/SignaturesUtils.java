package com.eguan.monitor.commonutils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;

import com.eguan.monitor.Constants;

import java.io.ByteArrayInputStream;
import java.security.MessageDigest;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

/**
 * Created on 17/5/22.
 * Author : chris
 * Email  : mengqi@analysys.com.cn
 * Detail :
 */

public class SignaturesUtils {
    public static String getSingInfo(Context mContext) {
        try {
            PackageInfo packageInfo = mContext.getPackageManager().getPackageInfo("com.aneesoft.ygqf", PackageManager.GET_SIGNATURES);
            Signature[] signs = packageInfo.signatures;
            Signature sign = signs[0];
            return parseSignature(sign.toByteArray());
        } catch (Exception e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        }
        return null;
    }
    private static String parseSignature(byte[] signature) {
        try {
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(signature));
            String pubKey = cert.getPublicKey().toString();
//            String signNumber = cert.getSerialNumber().toString();
            return pubKey;
        } catch (Exception e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        }
        return null;
    }

    /**
     * 获取对应mContext应用的认证指文
     * @param mContext
     */
    public static String getCertificateWithMd5(Context mContext) {
        try {
            PackageManager pm = mContext.getPackageManager();
            Signature sig = pm.getPackageInfo(mContext.getPackageName(), PackageManager.GET_SIGNATURES).signatures[0];
            String md5Fingerprint = doFingerprint(sig.toByteArray(), "MD5");
            return md5Fingerprint;
        } catch (Exception e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        }
        return null;
    }
    /**
     * @param certificateBytes 获取到应用的signature值
     * @param algorithm        在上文指定MD5算法
     * @return md5签名
     */
    private static String doFingerprint(byte[] certificateBytes, String algorithm) throws Exception {
        MessageDigest md = MessageDigest.getInstance(algorithm);
        md.update(certificateBytes);
        byte[] digest = md.digest();

        String toRet = "";
        for (int i = 0; i < digest.length; i++) {
            if (i != 0) {
                toRet += ":";
            }
            int b = digest[i] & 0xff;
            String hex = Integer.toHexString(b);
            if (hex.length() == 1) {
                toRet += "0";
            }
            toRet += hex;
        }
        return toRet;
    }
}
