package com.analysys.track.internal.net;

import android.content.Context;

import com.analysys.track.BuildConfig;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.internal.content.UploadKey;
import com.analysys.track.utils.BuglyUtils;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.StreamerUtils;
import com.analysys.track.utils.SystemUtils;
import com.analysys.track.utils.reflectinon.DevStatusChecker;
import com.analysys.track.utils.sp.SPHelper;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
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
     * HTTP
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
//            connection.setRequestProperty(EGContext.DEBUG, DeviceImpl.getInstance(context).getDebug());
            connection.setRequestProperty(EGContext.DEBUG, DevStatusChecker.getInstance().isSelfDebugApp(context) ? "1" : "0");
            connection.setRequestProperty(EGContext.APPKEY, SystemUtils.getAppKey(context));
            connection.setRequestProperty(EGContext.TIME, SPHelper.getStringValueFromSP(context, EGContext.TIME, ""));
            // 策略版本号
            connection.setRequestProperty(EGContext.POLICYVER, plocyVersion);
            //当前热修版本
            if (!EGContext.IS_HOST) {
                connection.setRequestProperty(EGContext.HOTFIX_VERSION, BuildConfig.hf_version);
            }
            connection.setRequestProperty(EGContext.POLICYVER, plocyVersion);
            //  // 区分3.x. 可以忽略不写
            // connection.setRequestProperty(EGContext.PRO, EGContext.PRO_KEY_WORDS);// 写死
            // // 兼容墨迹版本区别需求增加。普通版本不增加该值
            connection.setRequestProperty(EGContext.UPLOAD_HEAD_APPV, SystemUtils.getAppV(context));
            // 打印请求头信息内容
            if (EGContext.DEBUG_UPLOAD) {
                ELOG.i(EGContext.TAG_UPLOAD, "========HTTP头： " + connection.getRequestProperties().toString());
            }

            // 发送数据
            pw = new PrintWriter(connection.getOutputStream());
            pw.print(EGContext.UPLOAD_KEY_WORDS + "=" + URLEncoder.encode(value, "UTF-8"));
            pw.flush();

            int status = connection.getResponseCode();
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.i(EGContext.TAG_UPLOAD, "httpRequest status:" + status);
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
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(e);
            }
            response = FAIL;
        } finally {

            StreamerUtils.safeClose(pw);
            StreamerUtils.safeClose(is);
            StreamerUtils.safeClose(bos);
            StreamerUtils.safeClose(connection);
        }
        return response;
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
