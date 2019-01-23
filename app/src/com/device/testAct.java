package com.device;

import android.app.Activity;
import android.os.Bundle;
import android.util.Base64;

import com.analysys.dev.utils.ELOG;


import java.net.URLDecoder;
import java.net.URLEncoder;


public class testAct extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try{
            test("AAAABBBCCCDDD");
        }catch (Throwable t){
//            ELOG.e(t.getStackTrace());
        }

    }
    public static final String ORIGINKEY_STRING = "sysylana";

    private void test(String str) throws Exception{
        final String TAG = "analysys";
        String key = "";
        String appKey = "7752552892442721d";
        if (null == appKey) {
            appKey = ORIGINKEY_STRING;
        }
        String sdkv = "4.0.1|2345";
        String debug = "1";
        String timestamp = "1548155536157";

        key = makeSercretKey(appKey, sdkv, debug, timestamp);

        ELOG.i("str: " + str);
        ELOG.i("appKey: " + appKey + " sdkv: " + sdkv + " debug: " + debug + " timestamp: " + timestamp);
        ELOG.i("key: " + key);

        byte[] zip = ZipUtils.compressForDeflater(URLEncoder.encode(URLEncoder.encode(str)).getBytes("UTF-8"));
        ELOG.i(TAG + " zip: " + new String(zip));

        byte[] encryptMessage = AESUtils.encrypt(zip, key.getBytes("UTF-8"));
        ELOG.i(TAG + " encryptMessage: " + new String(encryptMessage));

        byte[] returnData = null;
        if (encryptMessage != null) {
            returnData =  android.util.Base64.encode(encryptMessage, Base64.DEFAULT);
        }
        ELOG.i(TAG + " returnData: " + new String(returnData));//加密结束
        ELOG.i(TAG + " -----------加密结束 ！-----------");//加密结束




        byte[] decodeBase64Data = android.util.Base64.decode(returnData, Base64.DEFAULT);
        ELOG.i(TAG + " decodeBase64Data: " + new String(decodeBase64Data));//加密结束

        byte[] aesDecrypt = AESUtils.decrypt(decodeBase64Data, key.getBytes("UTF-8"));
        ELOG.i(TAG + " aesDecrypt：" + aesDecrypt);
        ELOG.i(TAG + " aesDecrypt：" + new String(aesDecrypt));

        byte[] unZip = ZipUtils.decompressForDeflater(aesDecrypt);
        ELOG.i(TAG + " unZip：" + URLDecoder.decode(URLDecoder.decode(new String(unZip))));


        ELOG.i(TAG + " -----------解密结束 ！-----------"); 
    }
    /**
     * 获取加密 key
     *
     * @param appKey
     * @param sdkv
     * @param debug
     * @param timestamp
     * @return
     */
    public static String makeSercretKey(String appKey, String sdkv, String debug, String timestamp) {
        StringBuilder sb = new StringBuilder();
        if (appKey.length() > 3) {
            appKey = appKey.substring(0, 3);
        }
        if (sdkv.contains("|")) {
            sdkv = sdkv.substring(0, sdkv.indexOf("|")).replace(".", "");
        } else {
            sdkv = sdkv.replace(".", "");
        }
        sb.append(sdkv);//版本号-主版本号去掉
        sb.append(debug);//是否debug模式，0/1值
        sb.append(appKey);//前三位
        sb.append(timestamp);

        return Md5Utils.getMD5(sb.toString());
    }
}


