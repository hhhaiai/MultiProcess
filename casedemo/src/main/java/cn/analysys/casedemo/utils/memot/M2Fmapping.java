package cn.analysys.casedemo.utils.memot;

import android.content.Context;

import com.analysys.track.utils.EContextHelper;

import java.io.File;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import cn.analysys.casedemo.utils.EL;

/**
 * @Copyright © 2020 sanbo Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2020/3/31 14:51
 * @author: sanbo
 */
public class M2Fmapping {


    private void initBuffer() {
        try {
            if (mBuffer == null) {
                mFile = new File(mContext.getFilesDir(), mDefaultFn);
                if (!mFile.exists()) {
                    mFile.getParentFile().mkdirs();
                    mFile.createNewFile();
                }
                mFileChannel = new RandomAccessFile(mFile, "rw").getChannel();
                resize(2 * (ID_LENGTH + FINISH_MARK_LENGTH));
            }

        } catch (Throwable e) {
            EL.e(e);
        }
    }

    /**
     * 写入文件
     *
     * @param text
     * @return true:写入成功. false：写入失败
     */
    public synchronized boolean save(String text) {
        try {
            if (mFileChannel == null || mBuffer == null) {
                initBuffer();
            }
            if (mBuffer == null) {
                return false;
            }
            byte[] bytes = text.getBytes("UTF-8");

            long size = bytes.length + 2 * (ID_LENGTH + FINISH_MARK_LENGTH);

            resize(size);
            //往缓冲区里写入字节数据
            mBuffer.put(bytes);
            mBuffer.force();

            return true;
        } catch (Throwable e) {
            EL.e(e);
        } finally {
            safeClose(mBuffer);
        }
        return false;
    }

    public synchronized String load() {
        try {
            if (mFileChannel == null || mBuffer == null) {
                initBuffer();
            }
            if (mBuffer == null) {
                return "";
            }
            long size = mFileChannel.size() - 2 * (ID_LENGTH + FINISH_MARK_LENGTH);
            resize(size);
            byte[] result = new byte[(int) size];
            if (mBuffer.remaining() > 0) {
                mBuffer.get(result, 0, mBuffer.remaining());
            }
            if (result.length > 0) {
                return new String(result, "UTF-8");
            }
        } catch (Throwable e) {
        } finally {
            safeClose(mBuffer);
        }
        return "";
    }

    private MappedByteBuffer resize(long len) {
        try {
            mBuffer = mFileChannel.map(FileChannel.MapMode.READ_WRITE, 0, len);
        } catch (Throwable e) {
        }

        if (mBuffer != null) {
            mBuffer.position(0);
        }
        return mBuffer;
    }

    private void safeClose(MappedByteBuffer buffer) {
        if (buffer == null) {
            return;
        }
        if (buffer != null) {
//            等同代码
//            sun.misc.Cleaner c = ((sun.nio.ch.DirectBuffer) byteBuffer).cleaner();
//            if (c != null) {
//                c.clean();
//            }
            try {
                Class<?> z = Class.forName("sun.nio.ch.DirectBuffer");
                Method cleaner = z.getMethod("cleaner");
                if (cleaner == null) {
                    cleaner = z.getDeclaredMethod("cleaner");
                }
                if (cleaner != null) {
                    cleaner.setAccessible(true);
                    //sun.misc.Cleaner
                    Object cObj = cleaner.invoke(buffer);
                    if (cObj != null) {
//                        ClazzUtils.invokeObjectMethod(cObj, "clean");
                        Method clean = cObj.getClass().getDeclaredMethod("clean");
                        if (clean != null) {
                            clean.invoke(cObj);
                        }
                    }
                }
            } catch (Throwable e) {
            }
        }
    }


    /********************* get instance begin **************************/
    public static M2Fmapping getInstance(Context context) {
        HLODER.INSTANCE.initContext(context);
        return HLODER.INSTANCE;
    }

    private void initContext(Context context) {
        if (mContext == null) {
            mContext = EContextHelper.getContext(context);
        }
//        initBuffer();
    }

    private static class HLODER {
        private static final M2Fmapping INSTANCE = new M2Fmapping();
    }

    private M2Fmapping() {
    }
    /********************* get instance end **************************/
    private Context mContext = null;
    private File mFile;
    private FileChannel mFileChannel;
    private MappedByteBuffer mBuffer = null;
    private String mDefaultFn = "maf.dat";
    private final int ID_LENGTH = Integer.SIZE / Byte.SIZE;
    private final int FINISH_MARK_LENGTH = 1;
}
