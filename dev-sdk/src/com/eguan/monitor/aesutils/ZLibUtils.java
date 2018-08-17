/**
 * 2009-9-9
 */
package com.eguan.monitor.aesutils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;

import com.eguan.monitor.Constants;
import com.eguan.monitor.commonutils.EgLog;

/**
 * Zlib字符串压缩工具类
 */
public class ZLibUtils {

    /**
     * @param data
     * @return byte[]
     */
    public static byte[] compress(byte[] data) {
        byte[] output = new byte[0];
        ByteArrayOutputStream bos = null;
        try {
            Deflater compresser = new Deflater();

            compresser.reset();
            compresser.setInput(data);
            compresser.finish();
            bos = new ByteArrayOutputStream(data.length);
            byte[] buf = new byte[1024];
            while (!compresser.finished()) {
                int i = compresser.deflate(buf);
                bos.write(buf, 0, i);
            }
            output = bos.toByteArray();
            compresser.end();
        } catch (Throwable e) {
            output = data;
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        } finally {
            try {
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return output;
    }

}
