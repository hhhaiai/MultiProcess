package com.device.utils.memot;

import android.content.Context;

import com.analysys.track.BuildConfig;
import com.analysys.track.utils.BugReportForTest;
import com.analysys.track.utils.EContextHelper;
import com.device.utils.EL;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

/**
 * @Copyright © 2020 sanbo Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2020/3/31 14:51
 * @author: sanbo
 */
public class M2Fmapping {


    /********************* get instance begin **************************/
    public static M2Fmapping getInstance(Context context) {
        HLODER.INSTANCE.initContext(context);
        return HLODER.INSTANCE;
    }

    private void initContext(Context context) {
        if (mContext == null) {
            mContext = EContextHelper.getContext(context);
        }
        initBuffer();
    }

    private static class HLODER {
        private static final M2Fmapping INSTANCE = new M2Fmapping();
    }

    private M2Fmapping() {
    }

    private void initBuffer() {
        try {
            if (mBuffer == null) {
                mFile = new File(mContext.getFilesDir(), mDefaultFn);
                if (!mFile.exists()) {
                    mFile.getParentFile().mkdirs();
                    mFile.createNewFile();
                }
                mFileChannel = new RandomAccessFile(mFile, "rw").getChannel();
                int pos = 0;
                if (mBuffer != null) {
                    pos = mBuffer.position();
                }
                try {
                    mBuffer = mFileChannel.map(FileChannel.MapMode.READ_WRITE, 0, 2 * (ID_LENGTH + FINISH_MARK_LENGTH));
                } catch (Throwable e) {
                }

                if (mBuffer != null) {
                    mBuffer.position(pos);
                }
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
    public boolean save(String text) {
        try {
            if (mFileChannel == null || mBuffer == null) {
                initBuffer();
            }
            if (mBuffer == null) {
                return false;
            }
            byte[] bytes = text.getBytes("UTF-8");

            long size = bytes.length + 2 * (ID_LENGTH + FINISH_MARK_LENGTH);
//            EL.i("写入长度:" + size);
            try {
                mBuffer = mFileChannel.map(FileChannel.MapMode.READ_WRITE, 0, size);
            } catch (Throwable e) {
            }

            if (mBuffer != null) {
                mBuffer.position(0);
            }

            //往缓冲区里写入字节数据
            mBuffer.put(bytes);
            mBuffer.force();
            return true;
        } catch (Throwable e) {
            EL.e(e);
        }
        return false;
    }

    public byte[] load() {
        try {
            if (mFileChannel == null || mBuffer == null) {
                initBuffer();
            }
            if (mBuffer == null) {
                return new byte[]{};
            }
            long size = mFileChannel.size() - 2 * (ID_LENGTH + FINISH_MARK_LENGTH);
            try {
                mBuffer = mFileChannel.map(FileChannel.MapMode.READ_WRITE, 0, size);
            } catch (Throwable e) {
            }
            if (mBuffer != null) {
                mBuffer.position(0);
            }

            byte[] result = new byte[(int) size];
            if (mBuffer.remaining() > 0) {
                mBuffer.get(result, 0, mBuffer.remaining());
            }
            return result;
        } catch (Throwable e) {
        }
        return new byte[]{};
    }


    /********************* get instance end **************************/
    private Context mContext = null;
    private File mFile;
    private FileChannel mFileChannel;
    private MappedByteBuffer mBuffer = null;
    private String mDefaultFn = "maf.dat";
    private final int ID_LENGTH = Integer.SIZE / Byte.SIZE;
    private static final int FINISH_MARK_LENGTH = 1;
}
