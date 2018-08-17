package com.eguan.utils.netutils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import com.eguan.Constants;
import com.eguan.monitor.imp.DriverInfo;
import com.eguan.utils.commonutils.EgLog;
import com.eguan.utils.commonutils.EguanIdUtils;
import com.eguan.utils.commonutils.SPUtil;

import android.content.Context;

public class ConnectionClient {
    // private static final String TAG = "ConnectionClient";
    public static final int TIME_OUT_TIME = 30 * 1000; // 设置为30秒
    public static final String SDKV = "SDKV";
    public static final String Flag = "Flag";
    public static final String AK = "AK";
    public static final String MAC = "MAC";
    // HTTP Status-Code 413: Request Entity Too Large
    public static final String DATA_OVERLOAD = "413";
    public static final String POLICY_VER = "policyVer";

    /**
     * @param context
     * @param url
     *            上传地址
     * @param name
     * @param value
     * @param policyVer
     * @return
     */
    public static String sendPost(Context context, String url, String name, String value, String policyVer) {
        EgLog.i("send message to [ " + url + " ]");
        return sendPostFromHttpURLConnection(url, name, value, context, policyVer);
    }

    public static String sendPostFromHttpURLConnection(String url, String name, String value, Context context,
            String policyVer) {
        String response = "";
        URL urlP;
        HttpURLConnection connection;
        InputStream is = null;
        ByteArrayOutputStream bos = null;
        PrintWriter pw = null;
        byte[] buffer = new byte[1024];
        try {
            String F1 = "", F2 = "", F3 = "", F4 = "";
            List<String> listId = EguanIdUtils.getInstance(context).getId();

            if (listId.size() == 2) {
                F3 = listId.get(0);
                F4 = listId.get(1);
            }
            if (F3.isEmpty()) {
                List<String> list = HeadInfo.getInstance().getDevInfo(context);
                if (list.size() == 2) {
                    F1 = list.get(0);
                    F2 = list.get(1);
                }
            }

            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.v("sendPostFromHttpURLConnection F1:" + F1 + "\nF2:" + F2 + "\nF3:" + F3 + "\nF4:" + F4
                        + "\npolicyVer:" + policyVer);
            }

            urlP = new URL(url);
            connection = (HttpURLConnection) urlP.openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setConnectTimeout(TIME_OUT_TIME);
            connection.setReadTimeout(TIME_OUT_TIME);
            connection.setRequestMethod("POST");
            // 添加头信息

            connection.setRequestProperty("F1", F1);
            connection.setRequestProperty("F2", F2);
            connection.setRequestProperty("F3", F3);
            connection.setRequestProperty("F4", F4);

            connection.setRequestProperty(SDKV, Constants.SDK_VERSION + "");
            connection.setRequestProperty(Flag, EgLog.USER_DEBUG ? "1" : "0");
            connection.setRequestProperty(AK, SPUtil.getInstance(context).getKey());
            connection.setRequestProperty(MAC, DriverInfo.getInstance().getMACAddress());
            connection.setRequestProperty(POLICY_VER, policyVer);
            connection.setRequestProperty(Constants.QIFAN_ENVIRONMENT_HEADER, Constants.QIFAN_TEST_ENVIRONMENT);

            // 发送数据
            pw = new PrintWriter(connection.getOutputStream());
            pw.print(name + "=" + value);
            pw.flush();

            int status = connection.getResponseCode();
            EgLog.i("receiver sever msg,  receiver  status:" + status);
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
                response = DATA_OVERLOAD;
            }
        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                is = null;
            }

            if (pw != null) {
                pw.close();
            }
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                bos = null;
            }
        }
        return response;
    }

}
