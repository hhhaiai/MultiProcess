package com.analysys.track.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.analysys.track.internal.impl.DevStatusChecker;
import com.analysys.track.internal.impl.DeviceImpl;
import com.analysys.track.internal.net.PolicyImpl;
import com.analysys.track.internal.Content.DeviceKeyContacts;
import com.analysys.track.internal.Content.EGContext;
import com.analysys.track.internal.model.PolicyInfo;
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

public class RequestUtils {

    private static TrustManager[] tm = null;
    private static SSLContext sslContext = null;
    private static SSLSocketFactory ssf = null;
    private static TrustManager myX509TrustManager = new X509TrustManager() {
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

    /**
     * HTTP
     */
    public static String httpRequest(String url, String value, Context context) {
        if (EGContext.FLAG_DEBUG_INNER) {
            ELOG.i(url);
        }
        String response = "";
        URL urlP;
        HttpURLConnection connection;
        InputStream is = null;
        ByteArrayOutputStream bos = null;
        PrintWriter pw;
        byte[] buffer = new byte[1024];
        try {
            String ver = PolicyInfo.getInstance().getPolicyVer();
            PolicyImpl policy = PolicyImpl.getInstance(context);
            String version = policy.getSP().getString(DeviceKeyContacts.Response.RES_POLICY_VERSION, "0");
            String pl = TextUtils.isEmpty(ver) ? version : ver;
            urlP = new URL(url);
            connection = (HttpURLConnection) urlP.openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setConnectTimeout(EGContext.TIME_OUT_TIME);
            connection.setReadTimeout(EGContext.TIME_OUT_TIME);
            connection.setRequestMethod("POST");
            // 添加头信息

            connection.setRequestProperty(EGContext.SDKV, SPHelper.getStringValueFromSP(context, EGContext.SDKV, ""));
//            connection.setRequestProperty(EGContext.DEBUG, DeviceImpl.getInstance(context).getDebug());
            connection.setRequestProperty(EGContext.DEBUG, DevStatusChecker.getInstance().isSelfDebugApp(context) ? "1" : "0");
            connection.setRequestProperty(EGContext.APPKEY, SystemUtils.getAppKey(context));
            connection.setRequestProperty(EGContext.TIME, SPHelper.getStringValueFromSP(context, EGContext.TIME, ""));
            connection.setRequestProperty(EGContext.POLICYVER, pl);// 策略覆盖
            //  // 区分3.x. 可以忽略不写
            // connection.setRequestProperty(EGContext.PRO, EGContext.PRO_KEY_WORDS);// 写死
            // // 兼容墨迹版本区别需求增加。普通版本不增加该值
            // connection.setRequestProperty(EGContext.UPLOAD_HEAD_APPV, SystemUtils.getAppV(context));
            // 打印请求头信息内容
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.i(connection.getRequestProperties().toString());
            }
            // 发送数据
            pw = new PrintWriter(connection.getOutputStream());
            pw.print(EGContext.UPLOAD_KEY_WORDS + "=" + URLEncoder.encode(value, "UTF-8"));
            pw.flush();
            pw.close();

            int status = connection.getResponseCode();
            L.info(context, "status:" + status);
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
            L.info(context, Log.getStackTraceString(e));
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(e);
            }
            response = "-1";
        } finally {
            StreamerUtils.safeClose(is);
            StreamerUtils.safeClose(bos);
        }
        return response;
    }

    /**
     * HTTPS
     */
    public static String httpsRequest(String url, String value) {
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
            if (connection != null) {
                connection.disconnect();
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (Throwable e) {
                }
            }
        }
        return null;
    }

    public static SSLSocketFactory createSSL() throws NoSuchAlgorithmException, KeyManagementException {
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

    public static String readStream(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line = null;
        StringBuffer sb = new StringBuffer();
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        return String.valueOf(sb);
    }
}
