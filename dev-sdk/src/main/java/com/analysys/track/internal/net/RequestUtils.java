package com.analysys.track.internal.net;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.text.TextUtils;

import com.analysys.track.BuildConfig;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.utils.BugReportForTest;
import com.analysys.track.utils.reflectinon.EContextHelper;
import com.analysys.track.utils.ELOG;
import com.analysys.track.utils.PermissionUtils;
import com.analysys.track.utils.StreamerUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
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
            return EGContext.RSPONSE_FAIL;
        }
        if (BuildConfig.logcat) {
            ELOG.i(BuildConfig.tag_upload, "will send message to server: " + url);
        }
        /**
         * 1. get connection
         */
        URLConnection connection = initConnection(url);
        if (connection == null) {
            return EGContext.RSPONSE_FAIL;
        }
        /**
         * 2. post msg
         */
        InputStream is = null;
        ByteArrayOutputStream bos = null;
        OutputStream out = null;
        byte[] buffer = new byte[1024];
        try {
            connection.connect();
            return sendMsgToServer(uploadInfo, connection, is, bos, out, buffer);
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        } finally {
            StreamerUtils.safeClose(out, is, bos);
        }


        return EGContext.RSPONSE_FAIL;
    }

    private String sendMsgToServer(String uploadInfo, URLConnection connection, InputStream is, ByteArrayOutputStream bos, OutputStream out, byte[] buffer) throws IOException {
        out = connection.getOutputStream();
        String msg = EGContext.UPLOAD_KEY_WORDS + "=";
        // 发送数据
        if (!TextUtils.isEmpty(uploadInfo)) {
            msg = getOpou() + EGContext.UPLOAD_KEY_WORDS + "=" + URLEncoder.encode(uploadInfo, "UTF-8");
        }
        out.write(msg.getBytes());
        out.flush();

        int status = 200;
        if (connection instanceof HttpURLConnection) {
            status = ((HttpURLConnection) connection).getResponseCode();
        } else if (connection instanceof HttpsURLConnection) {
            status = ((HttpsURLConnection) connection).getResponseCode();
        }
        if (BuildConfig.logcat) {
            ELOG.i(BuildConfig.tag_upload, "server response status:" + status);
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
        } else if (HttpURLConnection.HTTP_ENTITY_TOO_LARGE == status) {
            return EGContext.HTTP_STATUS_413;
        } else {
            return EGContext.RSPONSE_FAIL;
        }
    }

    private String getOpou() {
        try {
            String id = Settings.System.getString(mContext.getContentResolver(),
                    Settings.Secure.ANDROID_ID);
            if (TextUtils.isEmpty(id)) {
                return null;
            }
            id = UploadImpl.getInstance(EContextHelper.getContext()).messageEncrypt(id);
            id = URLEncoder.encode(id, "UTF-8");
            id = EGContext.OPOU_KEY_WORDS + id + "=";
            return id;
        } catch (Throwable e) {
        }
        return null;
    }

    private URLConnection initConnection(String url) {
        URLConnection connection = null;
        if (url.startsWith("http://")) {
            connection = getHttpConnectionForPost(url);
        } else if (url.startsWith("https://")) {
            connection = getHttpsConnectionForPost(url);
        }
        if (connection != null) {
            initConnectionArgs(connection);
        }
        return connection;
    }

    private void initConnectionArgs(URLConnection connection) {
        try {
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setConnectTimeout(EGContext.TIME_MINUTE);
            connection.setReadTimeout(EGContext.TIME_MINUTE);
            connection.setUseCaches(false);
            connection.setDefaultUseCaches(false);
            HeaderHelper.addHeaderProperties(mContext, connection);
//            // Work around pre-Froyo bugs in HTTP connection reuse.
//            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
//                System.setProperty("http.keepAlive", "false");
//
//            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        }
    }

    private URLConnection getHttpConnectionForPost(String url) {

        try {
            URL urlP = new URL(url);
            HttpURLConnection connection = null;
            if (shouldUseProxy()) {
                Proxy proxy = new Proxy(java.net.Proxy.Type.HTTP, new InetSocketAddress(mProxyIp, mProxyPort));
                connection = (HttpURLConnection) urlP.openConnection(proxy);
            } else {
                connection = (HttpURLConnection) urlP.openConnection();
            }
            connection.setRequestMethod("POST");
            return connection;
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        }
        return null;
    }

    private URLConnection getHttpsConnectionForPost(String url) {
        try {
            HttpsURLConnection connection = null;
            if (shouldUseProxy()) {
                Proxy proxy = new Proxy(java.net.Proxy.Type.HTTP, new InetSocketAddress(mProxyIp, mProxyPort));
                connection = (HttpsURLConnection) new URL(url).openConnection(proxy);
            } else {
                connection = (HttpsURLConnection) new URL(url).openConnection();
            }

            setTLSForHttps(connection);
            connection.setRequestMethod("POST");
            return connection;
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        }
        return null;
    }


    private void setTLSForHttps(HttpsURLConnection connection) {
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{new MyTrustManager()}, new SecureRandom());
            HostnameVerifier hv = new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };
            // 设置默认https请求配置
            HttpsURLConnection.setDefaultHostnameVerifier(hv);
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            //设置单次请求配置
            connection.setSSLSocketFactory(sc.getSocketFactory());
            connection.setHostnameVerifier(hv);
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
        }
    }


    private boolean shouldUseProxy() {
        PackageManager pm = mContext.getPackageManager();
        if (pm.checkPermission(Manifest.permission.ACCESS_NETWORK_STATE,
                mContext.getPackageName()) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        try {
            ConnectivityManager connectivity = (ConnectivityManager) mContext.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            if (!PermissionUtils.checkPermission(mContext, Manifest.permission.ACCESS_NETWORK_STATE)) {
                return false;
            }
            NetworkInfo info = connectivity.getActiveNetworkInfo();

            if (info != null && info.getType() != ConnectivityManager.TYPE_WIFI) {
                String extraInfo = info.getExtraInfo();
                if (extraInfo != null && (extraInfo.equals("cmwap") || extraInfo.equals("3gwap") || extraInfo.equals("uniwap"))) {
                    return true;
                }
            }
        } catch (Throwable ex) {
        }

        return false;
    }

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
    private Context mContext = null;
    private String mProxyIp = "10.0.0.172";
    private int mProxyPort = 80;
}


class MyTrustManager implements X509TrustManager {

    X509TrustManager myX509TrustManager;

    public MyTrustManager() {
        try {
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init((KeyStore) null);
            TrustManager tms[] = tmf.getTrustManagers();
            for (int i = 0; i < tms.length; i++) {
                if (tms[i] instanceof X509TrustManager) {
                    myX509TrustManager = (X509TrustManager) tms[i];
                    return;
                }
            }
        } catch (Throwable e) {
        }
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) {
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) {
        // 验证证书可信性
        try {
            myX509TrustManager.checkServerTrusted(chain, authType);
        } catch (CertificateException e) {
        }
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return myX509TrustManager.getAcceptedIssuers();
    }

}