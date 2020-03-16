package com.analysys.track.internal.net;

import android.content.Context;
import android.text.TextUtils;

import com.analysys.track.BuildConfig;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.internal.content.UploadKey;
import com.analysys.track.internal.work.MessageDispatcher;
import com.analysys.track.utils.BugReportForTest;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.SimulatorUtils;
import com.analysys.track.utils.StreamerUtils;
import com.analysys.track.utils.SystemUtils;
import com.analysys.track.utils.reflectinon.DevStatusChecker;
import com.analysys.track.utils.reflectinon.PatchHelper;
import com.analysys.track.utils.sp.SPHelper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * @Copyright © 2019 sanbo Inc. All rights reserved.
 * @Description: 网络请求类
 * @Version: 1.0
 * @Create: 2019-08-05 15:09:48
 * @author: sanbo
 */
public class RequestUtils {

    public static final String FAIL = "-1";

    /**
     * HTTP request
     */
    public static String httpRequest(String url, String value, Context context) {

        if (EGContext.FLAG_DEBUG_INNER) {
            ELOG.i("httpRequest url : " + url);
        }
        String response = "";
        URL urlP;
        HttpURLConnection connection = null;
        InputStream is = null;
        ByteArrayOutputStream bos = null;
        PrintWriter pw = null;
        OutputStream out = null;
        byte[] buffer = new byte[1024];
        try {
            String plocyVersion = SPHelper.getStringValueFromSP(context, UploadKey.Response.RES_POLICY_VERSION, "0");

            urlP = new URL(url);
            connection = (HttpURLConnection) urlP.openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setConnectTimeout(EGContext.TIME_SECOND * 30);
            connection.setReadTimeout(EGContext.TIME_SECOND * 30);
            connection.setRequestMethod("POST");
            // 添加头信息

            connection.setRequestProperty(EGContext.SDKV, EGContext.SDK_VERSION);
            connection.setRequestProperty(EGContext.DEBUG, DevStatusChecker.getInstance().isSelfDebugApp(context) ? "1" : "0");
            connection.setRequestProperty(EGContext.DEBUG2, DevStatusChecker.getInstance().isDebugDevice(context) ? "1" : "0");
            connection.setRequestProperty(EGContext.APPKEY, SystemUtils.getAppKey(context));
            connection.setRequestProperty(EGContext.TIME, SPHelper.getStringValueFromSP(context, EGContext.TIME, ""));
            // 策略版本号
            connection.setRequestProperty(EGContext.POLICYVER, plocyVersion);
            //当前热修版本
            if (!EGContext.IS_HOST) {
                connection.setRequestProperty(EGContext.HOTFIX_VERSION, BuildConfig.hf_code);
            }
            connection.setRequestProperty(EGContext.POLICYVER, plocyVersion);
//            connection.setRequestProperty(EGContext.POLICYVER, "0");
            //  // 区分3.x. 可以忽略不写
            // connection.setRequestProperty(EGContext.PRO, EGContext.PRO_KEY_WORDS);// 写死
            // 兼容墨迹版本区别需求增加。普通版本不增加该值
            connection.setRequestProperty(EGContext.UPLOAD_HEAD_APPV, SystemUtils.getAppV(context));
            //http设置debug选项
            setDebugKnHeader(context, connection);
            // 打印请求头信息内容
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.i(BuildConfig.tag_upload, "========HTTP头： " + connection.getRequestProperties().toString());
            }

            out = connection.getOutputStream();
            // 发送数据
            pw = new PrintWriter(out);
            pw.print(EGContext.UPLOAD_KEY_WORDS + "=" + URLEncoder.encode(value, "UTF-8"));
            pw.flush();

            int status = connection.getResponseCode();
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.i(BuildConfig.tag_upload, "httpRequest status:" + status);
            }
            // 获取数据
            if (HttpURLConnection.HTTP_OK == status) {
                is = connection.getInputStream();
                bos = new ByteArrayOutputStream();
                int len;
                while (-1 != (len = is.read(buffer))) {
                    bos.write(buffer, 0, len);
                }
                bos.flush();
                return bos.toString("utf-8");
            } else if (HttpURLConnection.HTTP_ENTITY_TOO_LARGE == connection.getResponseCode()) {
                response = EGContext.HTTP_STATUS_413;
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
            response = FAIL;
        } finally {

            StreamerUtils.safeClose(out);
            StreamerUtils.safeClose(pw);
            StreamerUtils.safeClose(is);
            StreamerUtils.safeClose(bos);
            StreamerUtils.safeClose(connection);
        }
        return response;
    }

    private static void setDebugKnHeader(Context context, HttpURLConnection connection) {
        try {
            if (BuildConfig.isNativeDebug) {
                int k1 = AnaCountImpl.getK1(context);
                if (k1 != -1) {
                    connection.setRequestProperty("K1", String.valueOf(k1));
                }
                int k2 = PatchHelper.getK2();
                if (k2 != -1) {
                    connection.setRequestProperty("K2", String.valueOf(k2));
                }
                String k3 = SPHelper.getStringValueFromSP(context, UploadKey.Response.PatchResp.PATCH_VERSION, "k3");
                if (!TextUtils.isEmpty(k3) && !"k3".equals(k3)) {
                    connection.setRequestProperty("K3", String.valueOf(k3));
                }
                int k4 = PatchHelper.getK4();
                if (k4 != -1) {
                    connection.setRequestProperty("K4", String.valueOf(k4));
                }

                String k5 = AnaCountImpl.getK5(context);
                if (!TextUtils.isEmpty(k5)) {
                    connection.setRequestProperty("K5", String.valueOf(k5));
                }
                String k6 = AnaCountImpl.getK6(context);
                if (!TextUtils.isEmpty(k6)) {
                    connection.setRequestProperty("K6", String.valueOf(k6));
                }
                String k7 = AnaCountImpl.getK7(context);
                if (!TextUtils.isEmpty(k7)) {
                    connection.setRequestProperty("K7", String.valueOf(k7));
                }
                String k8 = AnaCountImpl.getK8(context);
                if (!TextUtils.isEmpty(k8)) {
                    connection.setRequestProperty("K8", String.valueOf(k8));
                }
                String k9 = AnaCountImpl.getK9(context);
                if (!TextUtils.isEmpty(k9)) {
                    connection.setRequestProperty("K9", String.valueOf(k9));
                }
                String k10 = AnaCountImpl.getK10(context);
                if (!TextUtils.isEmpty(k10)) {
                    connection.setRequestProperty("K10", String.valueOf(k10));
                }
                String k11 = AnaCountImpl.getK11(context);
                if (!TextUtils.isEmpty(k11)) {
                    connection.setRequestProperty("K11", String.valueOf(k11));
                }
                String k12 = AnaCountImpl.getK12(context);
                if (!TextUtils.isEmpty(k12)) {
                    connection.setRequestProperty("K12", String.valueOf(k12));
                }
                String k13 = AnaCountImpl.getK13(context);
                if (!TextUtils.isEmpty(k13)) {
                    connection.setRequestProperty("K13", String.valueOf(k13));
                }
                String k14 = AnaCountImpl.getK14(context);
                if (!TextUtils.isEmpty(k14)) {
                    connection.setRequestProperty("K14", String.valueOf(k14));
                }
                String k15 = AnaCountImpl.getK15(context);
                if (!TextUtils.isEmpty(k15)) {
                    connection.setRequestProperty("K15", String.valueOf(k15));
                }
                String k16 = AnaCountImpl.getK16(context);
                if (!TextUtils.isEmpty(k16)) {
                    connection.setRequestProperty("K16", String.valueOf(k16));
                }
                String k17 = AnaCountImpl.getK17(context);
                if (!TextUtils.isEmpty(k17)) {
                    connection.setRequestProperty("K17", String.valueOf(k17));
                }
                String k18 = AnaCountImpl.getK18(context);
                if (!TextUtils.isEmpty(k18)) {
                    connection.setRequestProperty("K18", String.valueOf(k18));
                }
                String k19 = AnaCountImpl.getK19(context);
                if (!TextUtils.isEmpty(k19)) {
                    connection.setRequestProperty("K19", String.valueOf(k19));
                }
            }
        } catch (Throwable e) {
        }
    }

//    /**
//     * HTTPS
//     */
//    public static String httpsRequest(String url, String value) {
//        HttpsURLConnection connection = null;
//        OutputStream outputStream = null;
//        try {
//            URL urlP = new URL(url);
//            connection = (HttpsURLConnection) urlP.openConnection();
//            connection.setSSLSocketFactory(createSSL());
//            connection.setDoInput(true);
//            connection.setDoOutput(true);
//            connection.setUseCaches(false);
//            connection.setRequestMethod("POST");
//            connection.setConnectTimeout(20 * 1000);
//            connection.setReadTimeout(20 * 1000);
//            // connection.setRequestProperty("spv", spv);
//            connection.connect();
//            if (!TextUtils.isEmpty(value)) {
//                outputStream = connection.getOutputStream();
//                outputStream.write(value.getBytes("UTF-8"));
//            }
//            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
//                return readStream(connection.getInputStream());
//            } else {
//                return null;
//            }
//        } catch (Throwable e) {
//            if (EGContext.FLAG_DEBUG_INNER) {
//                ELOG.e(e);
//            }
//        } finally {
//            StreamerUtils.safeClose(connection);
//            StreamerUtils.safeClose(connection);
//        }
//        return null;
//    }
//
//    public static SSLSocketFactory createSSL() throws NoSuchAlgorithmException, KeyManagementException {
//        if (tm != null) {
//            tm = new TrustManager[]{myX509TrustManager};
//        }
//        if (sslContext == null) {
//            sslContext = SSLContext.getInstance("TLS");
//        }
//        sslContext.init(null, tm, null);
//        if (ssf == null) {
//            ssf = sslContext.getSocketFactory();
//        }
//        return ssf;
//    }
//
//    public static String readStream(InputStream inputStream) throws IOException {
//        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
//        String line = null;
//        StringBuffer sb = new StringBuffer();
//        while ((line = reader.readLine()) != null) {
//            sb.append(line);
//        }
//        reader.close();
//        return String.valueOf(sb);
//    }
//    private static TrustManager[] tm = null;
//    private static SSLContext sslContext = null;
//    private static SSLSocketFactory ssf = null;
//    private static TrustManager myX509TrustManager = new X509TrustManager() {
//        @Override
//        public X509Certificate[] getAcceptedIssuers() {
//            return null;
//        }
//
//        @Override
//        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
//        }
//
//        @Override
//        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
//        }
//    };
}
