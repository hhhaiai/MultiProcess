package com.analysys.dev.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;

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
            output = data;
            e.printStackTrace();
        } finally {
            try {
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        compresser.end();
        return output;
    }
    public static String makeSercretKey(String value) {
        StringBuffer sb = new StringBuffer();
        sb.append(value.charAt(0)).append(value.charAt(1)).append(value.charAt(7))
                .append(value.charAt(8)).append(value.charAt(9)).append(value.charAt(10))
                .append(value.charAt(15)).append(value.charAt(16));
        String key = sb.toString();
        String cardinalNum = "", evenNum = "";
        for (int i = 0; i < key.length(); i++) {
            if (i % 2 == 1) {
                cardinalNum += key.charAt(i);//索引为奇就算奇数
            } else {
                evenNum += key.charAt(i);//索引为偶就算偶数
            }
        }
        return evenNum + new StringBuffer(cardinalNum).reverse().toString();
    }

}
