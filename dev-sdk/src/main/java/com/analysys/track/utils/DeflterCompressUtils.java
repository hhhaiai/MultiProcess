package com.analysys.track.utils;

import android.content.Context;

import com.analysys.track.BuildConfig;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.utils.data.Md5Utils;
import com.analysys.track.utils.reflectinon.DevStatusChecker;
import com.analysys.track.utils.sp.SPHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * @Copyright © 2019 sanbo Inc. All rights reserved.
 * @Description: 数据压缩处理类
 * @Version: 1.0
 * @Create: 2019-08-05 16:36:12
 * @author: sanbo
 * @mail: xueyongfu@analysys.com.cn
 */
public class DeflterCompressUtils {

    /**
     * @param data
     * @return byte[]
     */
    public static byte[] compress(byte[] data) {
        byte[] output = new byte[0];

        Deflater compresser = new Deflater();

        compresser.reset();
        compresser.setInput(data);
        compresser.finish();
        ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length);
        try {
            byte[] buf = new byte[1024];
            while (!compresser.finished()) {
                int i = compresser.deflate(buf);
                bos.write(buf, 0, i);
            }
            output = bos.toByteArray();
        } catch (Exception e) {
            if (BuildConfig.ENABLE_BUG_REPORT) {
                BugReportForTest.commitError(e);
            }
            output = data;
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(e);
            }
        } finally {
            try {
                bos.close();
            } catch (IOException e) {
                if (BuildConfig.ENABLE_BUG_REPORT) {
                    BugReportForTest.commitError(e);
                }
            }
        }
        compresser.end();
        return output;
    }

    public static String makeSercretKey(String key, Context ctx) {
        if (EGContext.FLAG_DEBUG_INNER) {
            ELOG.i(BuildConfig.tag_upload, " 入参 参考key：" + key);
        }
        StringBuilder sb = new StringBuilder();
        if (key.length() > 3) {
            SPHelper.setStringValue2SP(ctx, EGContext.APPKEY, key);
            key = key.substring(0, 3);
        }
        String sdkv = EGContext.SDK_VERSION;
        if (sdkv.contains("|")) {
            sdkv = sdkv.substring(0, sdkv.indexOf("|")).replace(".", "");
        } else {
            sdkv = sdkv.replace(".", "");
        }
        sb.append(sdkv);// 版本号-主版本号去掉点---规则变动，不需要处理了
//        sb.append(DeviceImpl.getInstance(ctx).getDebug());// 是否debug模式，0/1值
        // app自身的debug模式
//        sb.append(DevStatusChecker.getInstance().isSelfDebugApp(ctx) ? "1" : "0");// 是否debug模式，0/1值
        // 是否debug模式，0/1值
        sb.append(DevStatusChecker.getInstance().isSelfDebugApp(ctx) ? "1" : "0");
        sb.append(key);// 前三位
        long time = System.currentTimeMillis();
        SPHelper.setStringValue2SP(ctx, EGContext.TIME, String.valueOf(time));
        sb.append(time);
        return Md5Utils.getMD5(String.valueOf(sb));
    }

    /**
     * Deflater 解压数据
     */
    public static byte[] decompress(byte[] data) {
        if (data == null) {
            return new byte[0];
        }


        byte[] output = new byte[0];

        Inflater decompresser = new Inflater();
        decompresser.reset();
        decompresser.setInput(data);

        ByteArrayOutputStream o = new ByteArrayOutputStream(data.length);
        try {
            byte[] buf = new byte[1024];
            while (!decompresser.finished()) {
                int i = decompresser.inflate(buf);
                o.write(buf, 0, i);
            }
            output = o.toByteArray();
        } catch (Exception e) {
            output = data;
        } finally {
            try {
                o.close();
            } catch (IOException e) {
            }
        }

        decompresser.end();
        return output;
    }
}
