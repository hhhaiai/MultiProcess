package com.analysys.track.internal.net;

import android.content.Context;
import android.text.TextUtils;

import com.analysys.track.BuildConfig;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.internal.content.UploadKey;
import com.analysys.track.utils.BugReportForTest;
import com.analysys.track.utils.EContextHelper;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.StreamerUtils;
import com.analysys.track.utils.SystemUtils;
import com.analysys.track.utils.reflectinon.DevStatusChecker;
import com.analysys.track.utils.sp.SPHelper;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * @Copyright © 2019 sanbo Inc. All rights reserved.
 * @Description: 网络请求类
 * @Version: 1.0
 * @Create: 2019-08-05 15:09:48
 * @author: sanbo
 */
public class RequestUtils {


    public String postRequest(String url, String uploadInfo) {

        if (TextUtils.isEmpty(url)) {
            return FAIL;
        }

        return FAIL;
    }




    /**
     * HTTP request
     */
    public String httpRequest(String url, String value, Context context) {

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
            urlP = new URL(url);
            connection = (HttpURLConnection) urlP.openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setConnectTimeout(EGContext.TIME_MINUTE);
            connection.setReadTimeout(EGContext.TIME_MINUTE);
            connection.setRequestMethod("POST");
            HeaderHelper.addHeaderProperties(context, connection);

            out = connection.getOutputStream();
            // 发送数据
            pw = new PrintWriter(out);
            if (TextUtils.isEmpty(value)) {
                pw.print(EGContext.UPLOAD_KEY_WORDS + "=");
            } else {
                pw.print(EGContext.UPLOAD_KEY_WORDS + "=" + URLEncoder.encode(value, "UTF-8"));
            }
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


    /**
     * HTTPS
     */
    public String httpsRequest(String url, String value) {
        HttpsURLConnection connection = null;
        OutputStream outputStream = null;
        try {
            URL urlP = new URL(url);
            connection = (HttpsURLConnection) urlP.openConnection();
            connection.setSSLSocketFactory(createSSL());
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(20 * 1000);
            connection.setReadTimeout(20 * 1000);
            // connection.setRequestProperty("spv", spv);
            connection.connect();
            if (!TextUtils.isEmpty(value)) {
                outputStream = connection.getOutputStream();
                outputStream.write(value.getBytes("UTF-8"));
            }
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                return readStream(connection.getInputStream());
            } else {
                return null;
            }
        } catch (Throwable e) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(e);
            }
        } finally {
            StreamerUtils.safeClose(connection);
            StreamerUtils.safeClose(connection);
        }
        return null;
    }

    public SSLSocketFactory createSSL() throws NoSuchAlgorithmException, KeyManagementException {
        if (tm != null) {
            tm = new TrustManager[]{myX509TrustManager};
        }
        if (sslContext == null) {
            sslContext = SSLContext.getInstance("TLS");
        }
        sslContext.init(null, tm, null);
        if (ssf == null) {
            ssf = sslContext.getSocketFactory();
        }
        return ssf;
    }

    public String readStream(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line = null;
        StringBuffer sb = new StringBuffer();
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        StreamerUtils.safeClose(reader);
        return String.valueOf(sb);
    }


    private TrustManager myX509TrustManager = new X509TrustManager() {

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }
    };

    /********************* get instance begin **************************/
    public static RequestUtils getInstance(Context context) {
        return HLODER.INSTANCE.ininContext(context);
    }

    private RequestUtils ininContext(Context context) {
        if (mContext == null) {
            mContext = EContextHelper.getContext(context);
        }
        return HLODER.INSTANCE;
    }

    private static class HLODER {
        private static final RequestUtils INSTANCE = new RequestUtils();
    }

    private RequestUtils() {
    }

    /********************* get instance end **************************/
    public final String FAIL = "-1";
    private Context mContext = null;
    private TrustManager[] tm = null;
    private SSLContext sslContext = null;
    private SSLSocketFactory ssf = null;
}
