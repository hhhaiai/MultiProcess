package com.device;

import java.io.ByteArrayOutputStream;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class ZipUtils {
    /**
     * Deflater 压缩数据
     */
    public static byte[] compressForDeflater (byte[] data) throws Exception{
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
            output = data;
            e.printStackTrace();
        } finally {
            try {
                bos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        compresser.end();
        return output;

    }

    /**
     * Deflater 解压数据
     */
    public static byte[] decompressForDeflater(byte[] data) throws Exception{
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
            e.printStackTrace();
        } finally {
            try {
                o.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        decompresser.end();
        return output;
    }
}
