package com.analysys.track.utils.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import android.util.Base64;

/**
 * @Copyright © 2019 Analysys Inc. All rights reserved.
 * @Description: 字符串混淆类
 * @Create: 2019-12-13 23:03
 * @author: hcq
 */
public class StringConfuse {

    // private static void test() {
    // String aa = encode("阿卡事件uiwe\n\r23><?{}");
    // Log.i("ssss", aa);
    // String bb = decode(aa);
    // Log.i("ssss", bb);
    // }

    /**
     * 加密字符串，极端近况下可能加密失败，返回Base64加密字符串
     */
    public static String encode(String string) {
        byte[] data = string.getBytes();
        int[] flag = new int[data.length];
        int nCount = 0;
        for (int i = 0; i < data.length; i++) {
            byte newValue = reverse(data[i]);
            if (newValue < 0) {
                flag[nCount++] = i;
                data[i] = (byte)-newValue;
            } else {
                data[i] = newValue;
            }
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        try {
            // magic number
            dos.writeByte(12);
            dos.writeByte(34);
            dos.writeByte(43);
            dos.writeByte(21);
            dos.writeInt(nCount);
            for (int i = 0; i < nCount; i++) {
                dos.writeInt(flag[i]);
            }
            dos.write(data, 0, data.length);
            dos.flush();
            return new String(bos.toByteArray());
        } catch (Exception e) {
            // 极端情况下用Base64代替
            return new String(Base64.encode(string.getBytes(), Base64.NO_WRAP));
        } finally {
            try {
                dos.close();
            } catch (Throwable e) {
            }
        }
    }

    /**
     * 解密，极端近况下可能解析失败，返回null，使用前需要判空
     */
    public static String decode(String string) {
        byte[] data = string.getBytes();
        boolean[] flag = new boolean[data.length];
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        DataInputStream dis = new DataInputStream(bis);
        try {
            byte m1 = dis.readByte();
            byte m2 = dis.readByte();
            byte m3 = dis.readByte();
            byte m4 = dis.readByte();
            if (m1 != 12 || m2 != 34 || m3 != 43 || m4 != 21) {
                return new String(Base64.decode(data, Base64.NO_WRAP));
            }
            int nCount = dis.readInt();
            for (int i = 0; i < nCount; i++) {
                flag[dis.readInt()] = true;
            }
            byte[] strData = new byte[data.length - 4 - (nCount + 1) * 4];
            for (int i = 0; i < strData.length; i++) {
                byte dt;
                if (flag[i]) {
                    dt = (byte)-dis.readByte();
                } else {
                    dt = dis.readByte();
                }
                byte newValue = reverse(dt);
                strData[i] = newValue;
            }
            return new String(strData);
        } catch (Throwable e) {
            // 极端情况解析失败返回空值
            return null;
        } finally {
            try {
                dis.close();
            } catch (Throwable e) {
            }
        }
    }

    private static byte reverse(byte dt) {
        int bt = dt & 255;
        int mask = 240;
        int high = bt & mask;
        mask = 15;
        int low = bt & mask;
        return (byte)(high >> 4 | low << 4);
    }
}
