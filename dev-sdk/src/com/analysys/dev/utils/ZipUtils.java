package com.analysys.dev.utils;

import android.text.TextUtils;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @Copyright © 2018 EGuan Inc. All rights reserved.
 * @Description: Gzip 压缩 base64编码
 * @Version: 1.0
 * @Create: 2018/3/13
 * @Author: Wang-X-C
 */
public class ZipUtils {
  /**
   * Gzip 压缩数据
   */
  public static byte[] compressForGzip(String unGzipStr) {
    if (TextUtils.isEmpty(unGzipStr)) {
      return null;
    }
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      GZIPOutputStream gzip = new GZIPOutputStream(baos);
      gzip.write(unGzipStr.getBytes());
      gzip.close();
      byte[] encode = baos.toByteArray();
      baos.flush();
      baos.close();
      return encode;
    } catch (Throwable e) {

    }
    return null;
  }

  /**
   * Gzip解压数据
   */
  public static String decompressForGzip(byte[] gzipStr) {
    try {
      if (gzipStr == null) {
        return null;
      }
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      ByteArrayInputStream in = new ByteArrayInputStream(gzipStr);
      GZIPInputStream gzip = new GZIPInputStream(in);
      byte[] buffer = new byte[256];
      int n = 0;
      while ((n = gzip.read(buffer, 0, buffer.length)) > 0) {
        out.write(buffer, 0, n);
      }
      gzip.close();
      in.close();
      out.close();
      return out.toString();
    } catch (Throwable e) {
    }
    return null;
  }
}
