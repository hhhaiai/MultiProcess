//package com.analysys.track.utils.memorydex;
//
//import android.content.Context;
//
//import com.analysys.track.utils.StreamerUtils;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.nio.ByteBuffer;
//import java.nio.channels.FileChannel;
//
//public class DexFileEncryptUtils {
//    //这是用来加密dex文件的一张图片
//    public static final byte[] SMILE_IMAGE_BYTE = new byte[]{-119, 80, 78, 71, 13, 10, 26, 10, 0, 0, 0, 13,
//            73, 72, 68, 82, 0, 0, 0, 6, 0, 0, 0, 6, 8, 6, 0, 0, 0, -32, -52, -17, 72, 0, 0, 0, 1, 115, 82,
//            71, 66, 0, -82, -50, 28, -23, 0, 0, 0, 4, 103, 65, 77, 65, 0, 0, -79, -113, 11, -4, 97, 5, 0,
//            0, 0, 9, 112, 72, 89, 115, 0, 0, 14, -60, 0, 0, 14, -60, 1, -107, 43, 14, 27, 0, 0, 0, 111, 73,
//            68, 65, 84, 24, 87, 85, -51, 49, 10, 3, 33, 20, -124, -31, 63, 33, -83, -80, -107, -126, 71, -15,
//            2, 86, 30, 74, -49, -76, -24, 1, -68, -123, 7, -80, -78, 17, 68, -53, 77, 54, 100, 19, -14, 53,
//            3, 111, 6, -34, -19, 120, -119, 49, 34, -91, -92, -9, -50, -100, 19, -25, 28, -92, -108, -50,
//            -18, -49, -66, -17, -57, 93, 41, -123, -9, -98, 75, 8, 1, -83, 53, -28, -100, 63, -69, -97, -13,
//            -10, 24, 99, -48, 90, -93, -108, -14, 78, 99, 12, 107, 45, -66, -49, -123, 16, 108, -37, 70,
//            -83, 21, 107, 45, 79, 96, -88, 85, -47, -110, -63, 9, -109, 0, 0, 0, 0, 73, 69, 78, 68, -82,
//            66, 96, -126};
//
//    public static ByteBuffer encode(File target) throws IOException {
//        ByteBuffer buffer = file2ByteBuffer(target);
//        ByteBuffer resultBuffer = ByteBuffer.allocate(SMILE_IMAGE_BYTE.length + buffer.limit());
//        resultBuffer.put(SMILE_IMAGE_BYTE);
//        //todo 额外的加密转换
//        resultBuffer.put(buffer);
//        return resultBuffer;
//    }
//
//    public static ByteBuffer decode(ByteBuffer byteBuffer) {
//        byteBuffer.rewind();
//        byteBuffer.position(SMILE_IMAGE_BYTE.length);
//        byte[] dst = new byte[byteBuffer.limit() - SMILE_IMAGE_BYTE.length];
//        byteBuffer.get(dst);
//        //todo 额外转换对应的解密转换
//        return ByteBuffer.wrap(dst);
//    }
//
//    public static void saveByteBuffer(ByteBuffer byteBuffer, File target) throws IOException {
//        byteBuffer.flip();
//        FileOutputStream outputStream = null;
//        FileChannel fileChannel = null;
//        outputStream = new FileOutputStream(target);
//        fileChannel = outputStream.getChannel();
//        fileChannel.write(byteBuffer);
//        byteBuffer.rewind();
//        StreamerUtils.safeClose(outputStream);
//        StreamerUtils.safeClose(fileChannel);
//    }
//
//    public static ByteBuffer file2ByteBuffer(File file) throws IOException {
//        FileInputStream inputStream = null;
//        FileChannel fileChannel = null;
//        ByteBuffer buffer = null;
//        inputStream = new FileInputStream(file);
//        fileChannel = inputStream.getChannel();
//        buffer = ByteBuffer.allocate((int) fileChannel.size());
//        fileChannel.read(buffer);
//        buffer.rewind();
//        StreamerUtils.safeClose(inputStream);
//        StreamerUtils.safeClose(fileChannel);
//        return buffer;
//    }
//
//    public static ByteBuffer assets2ByteBuffer(Context context, String assetsName) throws IOException {
//        InputStream inputStream = null;
//        int length = 0;
//        ByteBuffer buffer = null;
//        inputStream = context.getResources().getAssets().open(assetsName);
//        length = inputStream.available();
//        byte[] bytes = new byte[length];
//        int result = inputStream.read(bytes);
//        buffer = ByteBuffer.wrap(bytes);
//        StreamerUtils.safeClose(inputStream);
//        return buffer;
//    }
//}
