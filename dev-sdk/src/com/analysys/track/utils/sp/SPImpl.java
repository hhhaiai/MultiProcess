package com.analysys.track.utils.sp;

import android.content.SharedPreferences;
import android.os.FileObserver;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.util.Pair;

import com.analysys.track.internal.Content.EGContext;
import com.analysys.track.utils.ELOG;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

/**
 * @Copyright © 2017 sanbo Inc. All rights reserved.
 * @Description:
 *
 * <pre>
 *               重写的两个主要原因：
 *               1.实现多进程数据安全
 *               2.优化IO性能，解决IO卡顿.
 *               注意事项：
 *                  a.本类最大支持1MB的数据，超出部分数据将无法读取和写入，这个限制是处于性能方面考虑作出的
 *                  b.本类支持多进程，多线程数据安全
 *                  c.本类解决了系统SharedPreferences引发的IO卡顿
 *                  d.为了提升进程间同步的性能，直接存储的是字节流数据，会导致getAll方法性能低于系统实现
 *                  e.本类未实现存储和读取getStringSet和putStringSet功能，原因是实现复杂，且极少人使用
 * </pre>
 *
 * @Version: 1.0
 * @Create: 2017年6月12日 下午2:27:52
 * @author: cqs
 * @EMail: sanbo.xyz@gmail.com
 */
class SPImpl implements SharedPreferences {
    // temp bak
    private static final String BACKUP_FILE_SUFFIX = ".tmk";
    private static final int ID_LENGTH = Integer.SIZE / Byte.SIZE;
    /**
     * 文件大小
     */
    private static final int MIN_INCREASE_LENGTH = 2 * 1024;
    private static final String TAG = "SharedPreferencesNew";
    private static final byte FINISH_MARK = 18;
    private static final int FINISH_MARK_LENGTH = 1;
    private static final int MAX_NUM = 999999;
    private static final long DELAY_TIME_TO_SAVE = 1000;
    private static final int MAX_TRY_TIME = 6;
    private static final int TRY_SAVE_TIME_DELAY = 2000;
    private static final long MAX_LOCK_FILE_TIME = 1000 * 10;
    private static final int CONTENT_LENGTH_LOST = 1;
    private static final int MODIFY_ID_LOST = 2;
    private static final int VALUE_LOST = 3;
    private static final int MAPPED_BUFFER_ERROR = 4;
    private static final int CONTENT_OVER_SIZE = 7;
    private static final int DATA_TYPE_ERROR = 8;
    private static final int DATA_TYPE_INVALID = 9;
    private static final int INIT_EXCEPTION = 10;
    private static final int OTHER_EXCEPTION = 11;
    private static final int LOAD_BAK_FILE = 12;
    private static final int TYPE_CAST_EXCEPTION = 13;
    private static final int NUM_ZERO = 0;
    private static final int NUM_TWO = 2;
    private static final int NUM_THREE = 3;
    private static final long TRY_RELOAD_INTERVAL = 60;
    private static final int SAVE_MESSAGE_ID = 21310;
    private static final String SPECIAL_KEY = "@sp_sp_key@";
    private static final String SPECIAL_VALUE = "@_@";
    private final LinkedHashMap<String, Object> mMap = new LinkedHashMap<String, Object>();
    private final ArrayList<OnSharedPreferenceChangeListener> mListeners = new ArrayList<OnSharedPreferenceChangeListener>();
    private final Object mSyncObj = new Object();
    private final Object mSyncSaveObj = new Object();
    private FileMonitor mFileMonitor;
    private boolean mLoaded = true;
    private File mFile;
    private String mBackupFilePath;
    private int mModifyID;
    private FileChannel mFileChannel;
    private MappedByteBuffer mMappedByteBuffer;
    private HandlerThread mThread;
    private Handler mHandler;
    private int mCurTryTime;
    private int mModifyErrorCnt;
    private Vector<Editor> mEditorList = new Vector<Editor>();
    private OnSharedPreferenceErrorListener mErrorListener;
    private boolean mIsSaving = false;
    private final Runnable mTryReloadRunnable = new Runnable() {
        @Override
        public void run() {
            int modifyID = getModifyID();
            if (modifyID > 0 && modifyID != mModifyID) {
                saveInner(false);
            }
        }
    };
    private long mTryReloadStartTime;
    private BaseRunnableEx mSaveRunnable = new BaseRunnableEx() {
        @Override
        public void run() {
            saveInner((Boolean) getArg());
        }
    };

    public SPImpl(File file) {
        this(file, 0, null);
    }

    public SPImpl(File file, OnSharedPreferenceErrorListener lis) {
        this(file, 0, lis);
    }

    public SPImpl(File file, int var, OnSharedPreferenceErrorListener lis) {
        mErrorListener = lis;
        mThread = new HandlerThread(file.getName());
        mThread.start();
        mHandler = new Handler(mThread.getLooper());
        mFile = file;
        mBackupFilePath = file.getAbsolutePath() + BACKUP_FILE_SUFFIX;
        if (initBuffer()) {
            startLoadFromDisk();
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    File bakFile = new File(mBackupFilePath);
                    if (!bakFile.exists()) {
                        bakFile.createNewFile();
                    }
                } catch (Exception e) {
                    if (EGContext.FLAG_DEBUG_INNER) {
                        ELOG.e(e);
                    }
                }
                mFileMonitor = new FileMonitor(mBackupFilePath, FileObserver.MODIFY);
                if (mListeners.size() > 0) {
                    mFileMonitor.startWatching();
                }
            }
        });
    }

    @Override
    public Map<String, ?> getAll() {
        awaitLoadedLocked();
        synchronized (mMap) {
            return new HashMap<String, Object>(mMap);
        }
    }

    @Override
    public String getString(String key, String defValue) {
        awaitLoadedLocked();
        synchronized (mMap) {
            try {
                String v = (String) mMap.get(key);
                return v != null ? v : defValue;
            } catch (ClassCastException e) {
                if (mErrorListener != null) {
                    mErrorListener.onError((mFile != null ? mFile.getAbsolutePath() : null) + "#" + key + e,
                            TYPE_CAST_EXCEPTION, mFile != null ? mFile.length() : 0);
                }
                return defValue;
            }
        }
    }

    @Override
    public Set<String> getStringSet(String key, Set<String> defValues) {
        throw new RuntimeException("putStringSet is not supported!");
    }

    @Override
    public int getInt(String key, int defValue) {
        awaitLoadedLocked();
        synchronized (mMap) {
            try {
                Integer v = (Integer) mMap.get(key);
                return v != null ? v : defValue;
            } catch (ClassCastException e) {
                if (mErrorListener != null) {
                    mErrorListener.onError((mFile != null ? mFile.getAbsolutePath() : null) + "#" + key + e,
                            TYPE_CAST_EXCEPTION, mFile != null ? mFile.length() : 0);
                }
                return defValue;
            }
        }
    }

    @Override
    public long getLong(String key, long defValue) {
        awaitLoadedLocked();
        synchronized (mMap) {
            try {
                Long v = (Long) mMap.get(key);
                return v != null ? v : defValue;
            } catch (ClassCastException e) {
                if (mErrorListener != null) {
                    mErrorListener.onError((mFile != null ? mFile.getAbsolutePath() : null) + "#" + key + e,
                            TYPE_CAST_EXCEPTION, mFile != null ? mFile.length() : 0);
                }
                return defValue;
            }
        }
    }

    @Override
    public float getFloat(String key, float defValue) {
        awaitLoadedLocked();
        synchronized (mMap) {
            try {
                Float v = (Float) mMap.get(key);
                return v != null ? v : defValue;
            } catch (ClassCastException e) {
                if (mErrorListener != null) {
                    mErrorListener.onError((mFile != null ? mFile.getAbsolutePath() : null) + "#" + key + e,
                            TYPE_CAST_EXCEPTION, mFile != null ? mFile.length() : 0);
                }
                return defValue;
            }
        }
    }

    @Override
    public boolean getBoolean(String key, boolean defValue) {
        awaitLoadedLocked();
        synchronized (mMap) {
            try {
                Boolean v = (Boolean) mMap.get(key);
                return v != null ? v : defValue;
            } catch (ClassCastException e) {
                if (mErrorListener != null) {
                    mErrorListener.onError((mFile != null ? mFile.getAbsolutePath() : null) + "#" + key + e,
                            TYPE_CAST_EXCEPTION, mFile != null ? mFile.length() : 0);
                }
                return defValue;
            }
        }
    }

    @Override
    public boolean contains(String key) {
        awaitLoadedLocked();
        synchronized (mMap) {
            return mMap.containsKey(key);
        }
    }

    @Override
    public Editor edit() {
        awaitLoadedLocked();
        return new EditorImpl();
    }

    @Override
    public void registerOnSharedPreferenceChangeListener(
            OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {
        if (onSharedPreferenceChangeListener != null) {
            mListeners.add(onSharedPreferenceChangeListener);
            if (mFileMonitor != null) {
                mFileMonitor.startWatching();
            }
        }
    }

    @Override
    public void unregisterOnSharedPreferenceChangeListener(
            OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {
        if (onSharedPreferenceChangeListener != null && mListeners != null) {
            mListeners.remove(onSharedPreferenceChangeListener);
            if (mFileMonitor != null && mListeners.size() <= 0) {
                mFileMonitor.stopWatching();
            }
        }
    }

    /**
     * 将Editor中的改变合并到mMap中
     *
     * @param editor
     */
    private boolean merge(final Editor editor, Map<String, Object> map, boolean fromReloadData) {
        HashMap<String, Object> modify = ((EditorImpl) editor).getAll();
        if (modify.size() == 0) {
            return false;
        }

        if (((EditorImpl) editor).doClear()) {
            map.clear();
        }

        synchronized (editor) {
            Set<Entry<String, Object>> set = modify.entrySet();
            for (Entry<String, Object> entry : set) {
                String key = entry.getKey();
                Object val = entry.getValue();

                if (val == null) {
                    map.remove(key);
                } else {
                    if (map.containsKey(key)) {
                        map.remove(key);
                    }
                    map.put(key, val);
                }

                if (!fromReloadData) {
                    notifyDataChanged(key);
                }
            }
        }

        return true;
    }

    private void notifyDataChanged(String key) {
        if (mListeners.size() > 0) {
            OnSharedPreferenceChangeListener lis = null;
            for (int idx = 0; idx < mListeners.size(); ++idx) {
                lis = mListeners.get(idx);
                if (lis != null) {
                    lis.onSharedPreferenceChanged(this, key);
                }
            }
        }
    }

    /**
     * 当从物理文件中拿到的ModifyID和内存中保存的ModifyID不一致时，需要重新从物理文件中加载数据
     */
    private void tryReload() {
        // 进程间数据安全, 如果距离上次查询过去了TRY_RELOAD_INTERVAL毫秒，则可以发送查询请求
        if ((SystemClock.elapsedRealtime() - mTryReloadStartTime) > TRY_RELOAD_INTERVAL) {
            mTryReloadStartTime = SystemClock.elapsedRealtime();
            mHandler.removeCallbacks(mTryReloadRunnable);
            mHandler.post(mTryReloadRunnable);
        }
    }

    private boolean tryReloadWhenSave() {
        int modifyID = getModifyID();
        if (modifyID > 0 && modifyID != mModifyID) {
            load(true);
            return true;
        } else {
            return false;
        }
    }

    private void saveInner(final boolean force) {
        synchronized (mSyncSaveObj) {
            FileLock fileLock = lockFile(false);
            if (fileLock != null) {
                try {
                    mIsSaving = true;
                    // 这里需要保证拿到最新的文件数据，因此需要等待
                    if (tryReloadWhenSave()) {
                        mergeWhenReload();
                        notifyDataChanged(null);
                    }

                    synchronized (mMap) {
                        if (mEditorList.size() <= 0) {
                            return;
                        }
                    }

                    byte[] totalBytes = obtainTotalBytes();

                    saveToMappedBuffer(totalBytes, force);

                    backup();
                } catch (Throwable e) {
                    if (mErrorListener != null) {
                        mErrorListener.onError(e.getMessage(), OTHER_EXCEPTION, -1);
                    }
                } finally {
                    try {
                        fileLock.release();
                    } catch (IOException e) {
                        if (EGContext.FLAG_DEBUG_INNER) {
                            ELOG.e(e);
                        }
                    }
                    mIsSaving = false;
                }
            } else {
                if (mCurTryTime++ < MAX_TRY_TIME) {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            saveInner(force);
                        }
                    }, TRY_SAVE_TIME_DELAY);
                }
            }
        }
    }

    private void save(final Editor editor, final boolean force, boolean sync, boolean delay) {
        if (editor == null) {
            return;
        }

        // 先提交到内存中
        synchronized (mMap) {
            mCurTryTime = 0;

            if (!merge(editor, mMap, false)) {
                return;
            }

            mEditorList.add(editor);
        }

        if (sync) {
            saveInner(force);
        } else {
            long delayTime = delay ? DELAY_TIME_TO_SAVE : 0;
            mSaveRunnable.setArg(force);
            Message msg = Message.obtain(mHandler, mSaveRunnable);
            msg.what = SAVE_MESSAGE_ID;
            mHandler.sendMessageDelayed(msg, delayTime);
        }
    }

    private Pair<Integer, byte[][]> getDataBytes() {
        byte[][] totalBytes;
        ArrayList<Entry<String, Object>> array;
        synchronized (mMap) {
            totalBytes = new byte[mMap.size() * 5][];
            array = new ArrayList<Entry<String, Object>>(mMap.entrySet());
            // mEditorList必须和本次保存的数据保证一致，否则在获取array和clear之间可能会有mEditorList.add操作，会导致错误地删除数据
            mEditorList.clear();
        }

        int length = 0;
        int cur = 0;
        int size = array.size();
        for (int idx = size - 1; idx >= 0; idx--) {
            Entry<String, Object> entry = array.get(idx);
            String key = entry.getKey();
            Object val = entry.getValue();
            if (key != null && key.trim().length() > 0 && val != null) {
                // Key的字节数组
                byte[] bytes = key.getBytes();
                byte[] lenBytes = ByteIntUtils.intToBytes(bytes.length);
                totalBytes[cur] = lenBytes;
                totalBytes[cur + 1] = bytes;
                length += (lenBytes.length + bytes.length);

                // value的字节数组
                bytes = getBytes(val);
                lenBytes = ByteIntUtils.intToBytes(bytes.length);

                totalBytes[cur + 2] = lenBytes;
                totalBytes[cur + 3] = bytes;
                length += (lenBytes.length + bytes.length);

                // 数据的Type
                byte[] typeBytes = new byte[1];
                typeBytes[0] = (byte) getObjectType(val);
                totalBytes[cur + 4] = typeBytes;
                length += typeBytes.length;

                cur += 5;
            }
        }
        return new Pair<Integer, byte[][]>(length, totalBytes);
    }

    /**
     * 将文件头和数据都写入到Buffer中
     *
     * @param force
     */
    private void saveToMappedBuffer(byte[] bytes, boolean force) {
        synchronized (mSyncObj) {
            mMappedByteBuffer.position(0);
            safeBufferPut(mMappedByteBuffer, bytes);
            if (force) {
                mMappedByteBuffer.force();
            }
        }
    }

    private int increaseModifyID() {
        mModifyID = (mModifyID + 1) % Integer.MAX_VALUE;
        return mModifyID;
    }

    private int getContentLength() {
        if (mMappedByteBuffer == null || mFileChannel == null) {
            return -1;
        }

        synchronized (mSyncObj) {
            mMappedByteBuffer.position(0);
            byte[] lenBytes = new byte[ID_LENGTH];
            safeBufferGet(mMappedByteBuffer, lenBytes);
            int bufferLen = ByteIntUtils.bytesToInt(lenBytes);
            mMappedByteBuffer.position(ID_LENGTH);
            byte mask = mMappedByteBuffer.get();
            boolean isEnable = mask != FINISH_MARK && mask != getMaskByte(lenBytes);
            if (isEnable || bufferLen < 0) {
                if (mErrorListener != null) {
                    mErrorListener.onError(mFile != null ? mFile.getAbsolutePath() : null, CONTENT_LENGTH_LOST,
                            mFile != null ? mFile.length() : 0);
                }

                return -1;
            }

            if (bufferLen > MAX_NUM) {
                bufferLen = MAX_NUM;
            }

            return bufferLen;
        }
    }

    private void reallocBuffer() {
        if (mMappedByteBuffer == null) {
            return;
        }

        synchronized (mSyncObj) {
            try {
                int contentLength = getContentLength();
                int bufferLen = mMappedByteBuffer.capacity();
                if (contentLength > bufferLen) {
                    allocBuffer(contentLength + MIN_INCREASE_LENGTH);
                }
            } catch (Exception e) {
                if (EGContext.FLAG_DEBUG_INNER) {
                    ELOG.e(e);
                }
            }
        }
    }

    private void load(boolean hasLock) {
        @SuppressWarnings("resource")
        FileLock lock = hasLock ? null : lockFile(true);
        if (lock != null || hasLock) {
            byte[] allBytes = null;
            try {
                reallocBuffer();

                if (mMappedByteBuffer == null || mMappedByteBuffer.capacity() == NUM_ZERO) {
                    return;
                }

                long contentLen = getContentLength();
                if (contentLen <= NUM_TWO * (ID_LENGTH + FINISH_MARK_LENGTH)) {
                    return;
                }

                mModifyID = getModifyID();
                if (mModifyID > NUM_ZERO) {
                    synchronized (mSyncObj) {
                        mMappedByteBuffer.position(NUM_TWO * (ID_LENGTH + FINISH_MARK_LENGTH));
                        allBytes = new byte[(int) contentLen - (ID_LENGTH + FINISH_MARK_LENGTH) * 2];
                        safeBufferGet(mMappedByteBuffer, allBytes);
                    }
                }
            } finally {
                boolean parseOK = false;
                try {
                    parseOK = parseBytesIntoMap(allBytes, true);
                } catch (Exception e) {
                    if (EGContext.FLAG_DEBUG_INNER) {
                        ELOG.e(e);
                    }
                }

                if (!parseOK) {
                    loadFromBakFile();
                }

                try {
                    if (lock != null) {
                        lock.release();
                    }
                } catch (Exception e) {
                    if (EGContext.FLAG_DEBUG_INNER) {
                        ELOG.e(e);
                    }
                }
            }
        } else {
            if (!hasLock) {
                loadFromBakFile();
            }
        }
    }

    int getModifyID() {
        if (mMappedByteBuffer == null) {
            return -1;
        }

        synchronized (mSyncObj) {
            mMappedByteBuffer.position(ID_LENGTH + FINISH_MARK_LENGTH);
            byte[] idBytes = new byte[ID_LENGTH];
            safeBufferGet(mMappedByteBuffer, idBytes);
            int modifyID = ByteIntUtils.bytesToInt(idBytes);

            mMappedByteBuffer.position(ID_LENGTH * NUM_TWO + FINISH_MARK_LENGTH);
            byte mask = mMappedByteBuffer.get();
            boolean isEndable = mask != FINISH_MARK && mask != getMaskByte(idBytes);
            if (isEndable || modifyID < NUM_ZERO) {
                ++mModifyErrorCnt;
                if (mModifyErrorCnt < NUM_THREE) {
                    if (mErrorListener != null) {
                        mErrorListener.onError(mFile != null ? mFile.getAbsolutePath() : null, MODIFY_ID_LOST,
                                mFile != null ? mFile.length() : NUM_ZERO);
                    }
                }
                return -1;
            }
            return modifyID;
        }
    }

    private void startLoadFromDisk() {
        synchronized (SPImpl.this) {
            mLoaded = false;
        }

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                synchronized (SPImpl.this) {
                    loadFromDiskLocked();
                }
            }
        });
    }

    private void loadFromDiskLocked() {
        if (mLoaded) {
            return;
        }

        load(false);

        mLoaded = true;
        SPImpl.this.notifyAll();
    }

    private void awaitLoadedLocked() {
        synchronized (SPImpl.this) {
            while (!mLoaded) {
                try {
                    SPImpl.this.wait();
                } catch (Throwable t) {
                    if (EGContext.FLAG_DEBUG_INNER) {
                        ELOG.e(t);
                    }
                }
            }
        }

        tryReload();
    }

    private boolean safeBufferGet(MappedByteBuffer buffer, byte[] bytes) {
        if (buffer == null || bytes == null || bytes.length == 0) {
            return false;
        }
        Arrays.fill(bytes, (byte) 0);
        int pos = buffer.position();
        int bufferLen = buffer.capacity();
        if (pos + bytes.length > bufferLen) {
            return false;
        }
        buffer.get(bytes);
        return true;
    }

    private MappedByteBuffer allocBuffer(int length) {
        int pos = 0;
        if (mMappedByteBuffer != null) {
            pos = mMappedByteBuffer.position();
        }

        try {
            mMappedByteBuffer = mFileChannel.map(FileChannel.MapMode.READ_WRITE, 0, length);
        } catch (Exception e) {
            if (EGContext.FLAG_DEBUG_INNER) {
                ELOG.e(e);
            }
        }

        if (mMappedByteBuffer != null) {
            mMappedByteBuffer.position(pos);
        }

        return mMappedByteBuffer;
    }

    /**
     * 初始化Buffer
     */
    private boolean initBuffer() {
        boolean isFileExist = true;

        if (mMappedByteBuffer == null) {
            try {
                if (!mFile.exists()) {
                    mFile.getParentFile().mkdirs();
                    mFile.createNewFile();

                    File bakFile = new File(mBackupFilePath);
                    if (!bakFile.exists()) {
                        isFileExist = false;
                    }
                } else {
                    long fileLength = mFile.length();
                    if (fileLength == 0) {
                        isFileExist = false;

                        if (mErrorListener != null) {
                            mErrorListener.onError(mFile.getAbsolutePath(), MAPPED_BUFFER_ERROR, mFile.length());
                        }
                    }
                }
                @SuppressWarnings("resource")
                RandomAccessFile randomFile = new RandomAccessFile(mFile, "rw");
                mFileChannel = randomFile.getChannel();
                allocBuffer(2 * (ID_LENGTH + FINISH_MARK_LENGTH));
                if (!isFileExist) {
                    initFileHeader();
                }
            } catch (Exception e) {
                isFileExist = false;

                if (mErrorListener != null) {
                    mErrorListener.onError(mFile.getAbsolutePath() + " " + e.getCause(), INIT_EXCEPTION, -1);
                }
            }
        }

        return isFileExist;
    }

    private FileLock lockFile(boolean block) {
        if (mFileChannel == null) {
            return null;
        }

        FileLock lock = null;
        if (block) {
            long startTime = SystemClock.elapsedRealtime();
            while (lock == null) {
                try {
                    lock = mFileChannel.tryLock();
                } catch (Exception e) {
                }

                if (lock == null) {
                    try {
                        Thread.sleep(100);
                    } catch (Exception e) {
                    }
                }

                if ((SystemClock.elapsedRealtime() - startTime) > MAX_LOCK_FILE_TIME) {
                    break;
                }
            }
        } else {
            try {
                lock = mFileChannel.tryLock();
            } catch (Exception e) {
            }
        }

        return lock;
    }

    public void setSharedPreferenceErrorListener(OnSharedPreferenceErrorListener listener) {
        mErrorListener = listener;
    }

    private void safeBufferPut(MappedByteBuffer buffer, byte[] bytes) {
        if (buffer == null || bytes == null || bytes.length == 0) {
            return;
        }

        int bufLen = buffer.capacity();
        if ((buffer.position() + bytes.length) >= bufLen) {
            buffer = allocBuffer(buffer.position() + bytes.length + MIN_INCREASE_LENGTH);
        }

        buffer.put(bytes);
    }

    private void mergeWhenReload() {
        synchronized (mMap) {
            if (mEditorList.size() > 0) {
                for (Editor editor : mEditorList) {
                    merge(editor, mMap, true);
                }
            }
        }
    }

    /**
     * 将读取到的文件中的Byte数据解析到内存的map中
     *
     * @param totalBytes
     * @return true：解析成功， false：解析失败
     */
    private boolean parseBytesIntoMap(byte[] totalBytes, boolean overrideOldData) {
        boolean parseOK = true;

        if (totalBytes == null || totalBytes.length == 0) {
            parseOK = false;
            return parseOK;
        }

        synchronized (mMap) {
            for (int cur = 0; cur < totalBytes.length;) {
                try {
                    Pair<byte[], Integer> key = getOneString(totalBytes, cur);
                    cur = key.second;

                    Pair<byte[], Integer> value = getOneString(totalBytes, cur);
                    cur = value.second;

                    // 读取类型
                    byte typeByte = totalBytes[cur];
                    cur++;
                    byte finishMark = totalBytes[cur];
                    cur += FINISH_MARK_LENGTH;
                    if (finishMark != FINISH_MARK && finishMark != getMaskByte(new byte[] { typeByte })) {
                        if (mErrorListener != null) {
                            mErrorListener.onError(mFile != null ? mFile.getAbsolutePath() : null, DATA_TYPE_ERROR,
                                    totalBytes.length);
                        }
                        parseOK = false;
                        break;
                    } else {
                        if (!checkTypeValid(typeByte)) {
                            // 出错后就抛弃数据
                            if (mErrorListener != null) {
                                mErrorListener.onError(mFile != null ? mFile.getAbsolutePath() : null,
                                        DATA_TYPE_INVALID, totalBytes.length);
                            }
                            parseOK = false;
                            continue;
                        }
                    }

                    Object valObject = getObjectByType(value.first, typeByte);
                    if (key.first != null && key.first.length > 0 && valObject != null) {
                        String keyStr = new String(key.first);
                        if (!(!overrideOldData && mMap.containsKey(keyStr))) {
                            mMap.put(keyStr, valObject);
                        }
                    }
                } catch (Exception e) {
                    if (mErrorListener != null) {
                        mErrorListener.onError((mFile != null ? mFile.getAbsolutePath() : null) + "#" + e.getCause(),
                                VALUE_LOST, totalBytes.length);
                    }
                    parseOK = false;
                    break;
                }
            }
        }

        return parseOK;
    }

    /**
     * 根据当前字节数组的位置，从字节数组中获取到一个String所对应的字节数组
     *
     * @param totalBytes
     * @param cur
     * @return
     * @throws Exception
     */
    private Pair<byte[], Integer> getOneString(byte[] totalBytes, int cur) throws Exception {
        byte[] lengthBytes = new byte[ID_LENGTH];
        System.arraycopy(totalBytes, cur, lengthBytes, 0, ID_LENGTH);
        cur += ID_LENGTH;

        // 当前结束符只有一位
        if (totalBytes[cur] != FINISH_MARK && totalBytes[cur] != getMaskByte(lengthBytes)) {
            throw new Exception("length string's finish mark missing");
        }

        cur += FINISH_MARK_LENGTH;

        int len = ByteIntUtils.bytesToInt(lengthBytes);
        if (len < 0 || (cur + len >= totalBytes.length) || len > MAX_NUM) {
            throw new Exception("length string is invalid");
        }
        byte[] strBytes = null;
        if (len == 0) {
            // 如果读取到的长度为0，继续读取一位结束符
            cur += FINISH_MARK_LENGTH;
        } else {
            strBytes = new byte[len];
            System.arraycopy(totalBytes, cur, strBytes, 0, len);

            cur += len;
            if (totalBytes[cur] != FINISH_MARK && totalBytes[cur] != getMaskByte(strBytes)) {
                throw new Exception("Stored bytes' finish mark missing");
            }
            cur += FINISH_MARK_LENGTH;
        }
        byte[] results = strBytes;
        Pair<byte[], Integer> pair = new Pair<byte[], Integer>(results, cur);
        return pair;
    }

    private byte[] obtainTotalBytes() {
        Pair<Integer, byte[][]> dataBytes = getDataBytes();

        int strNum = dataBytes.second.length;
        int contentLen = dataBytes.first + (ID_LENGTH + FINISH_MARK_LENGTH) * 2 + strNum * FINISH_MARK_LENGTH;
        if (contentLen > MAX_NUM) {
            contentLen = MAX_NUM;
        }
        byte[] result = new byte[contentLen];

        // 写入文件长度字段
        int cur = 0;
        byte[] contentLenBytes = ByteIntUtils.intToBytes(contentLen);
        System.arraycopy(contentLenBytes, 0, result, cur, contentLenBytes.length);
        cur += contentLenBytes.length;
        result[cur] = getMaskByte(contentLenBytes);
        cur += FINISH_MARK_LENGTH;

        // 写入ModifyID字段
        byte[] modifyIDBytes = ByteIntUtils.intToBytes(increaseModifyID());
        System.arraycopy(modifyIDBytes, 0, result, cur, modifyIDBytes.length);
        cur += modifyIDBytes.length;
        result[cur] = getMaskByte(modifyIDBytes);
        cur += FINISH_MARK_LENGTH;

        // 将上面的结果全部写入result中
        for (byte[] bytes : dataBytes.second) {
            if (bytes != null) {
                if (cur + bytes.length + FINISH_MARK_LENGTH <= MAX_NUM) {
                    System.arraycopy(bytes, 0, result, cur, bytes.length);
                    cur += bytes.length;
                    result[cur] = getMaskByte(bytes);
                    cur += FINISH_MARK_LENGTH;
                } else {
                    Log.e(TAG, "Write too much data in " + (mFile != null ? mFile.getAbsolutePath() : null));
                    if (mErrorListener != null) {
                        mErrorListener.onError(mFile != null ? mFile.getAbsolutePath() : null, CONTENT_OVER_SIZE, -1);
                    }
                    break;
                }
            }
        }
        return result;
    }

    /**
     * 检查数据类型是否正确
     *
     * @return
     */
    boolean checkTypeValid(byte b) {
        return b == SupportedType.TYPE_BOOLEAN || b == SupportedType.TYPE_FLOAT || b == SupportedType.TYPE_INT
                || b == SupportedType.TYPE_LONG || b == SupportedType.TYPE_STRING;
    }

    private void initFileHeader() {
        if (mMappedByteBuffer != null) {
            // 初始化文件头
            byte[] header = new byte[2 * (ID_LENGTH + FINISH_MARK_LENGTH)];
            byte[] contentLength = ByteIntUtils.intToBytes(0);
            System.arraycopy(contentLength, 0, header, 0, ID_LENGTH);
            header[ID_LENGTH] = getMaskByte(contentLength);
            byte[] modifyID = ByteIntUtils.intToBytes(0);
            System.arraycopy(modifyID, 0, header, ID_LENGTH + FINISH_MARK_LENGTH, ID_LENGTH);
            header[ID_LENGTH * 2 + FINISH_MARK_LENGTH] = getMaskByte(modifyID);

            mMappedByteBuffer.position(0);
            mMappedByteBuffer.put(header);
        }
    }

    private int getObjectType(Object obj) {
        if (obj instanceof String) {
            return SupportedType.TYPE_STRING;
        } else if (obj instanceof Boolean) {
            return SupportedType.TYPE_BOOLEAN;
        } else if (obj instanceof Float) {
            return SupportedType.TYPE_FLOAT;
        } else if (obj instanceof Integer) {
            return SupportedType.TYPE_INT;
        } else if (obj instanceof Long) {
            return SupportedType.TYPE_LONG;
        }

        return 0;
    }

    private Object getObjectByType(byte[] value, int type) {
        if (value != null && value.length > 0) {
            try {
                if (type == SupportedType.TYPE_STRING) {
                    return new String(value);
                } else if (type == SupportedType.TYPE_BOOLEAN) {
                    return value[0] == 1;
                } else if (type == SupportedType.TYPE_FLOAT) {
                    return ByteFloatUtils.bytesToFloat(value);
                } else if (type == SupportedType.TYPE_INT) {
                    return ByteIntUtils.bytesToInt(value);
                } else if (type == SupportedType.TYPE_LONG) {
                    return ByteLongUtils.bytesToLong(value);
                }
            } catch (Throwable t) {
            }
        }

        return null;
    }

    private byte[] getBytes(Object obj) {
        if (obj != null) {
            try {
                if (obj instanceof String) {
                    return ((String) obj).getBytes();
                } else if (obj instanceof Boolean) {
                    boolean b = (Boolean) obj;
                    return new byte[] { (byte) (b ? 1 : 0) };
                } else if (obj instanceof Float) {
                    return ByteFloatUtils.floatToBytes((Float) obj);
                } else if (obj instanceof Integer) {
                    return ByteIntUtils.intToBytes((Integer) obj);
                } else if (obj instanceof Long) {
                    return ByteLongUtils.longToBytes((Long) obj);
                }
            } catch (Throwable t) {
            }
        }

        return null;
    }

    private void backup() {
        FileOutputStream os = null;
        FileChannel osChannel = null;
        try {
            File bakFile = new File(mBackupFilePath);
            if (!bakFile.exists()) {
                bakFile.createNewFile();
            }
            os = new FileOutputStream(bakFile);
            osChannel = os.getChannel();
            mFileChannel.transferTo(0, mMappedByteBuffer.capacity(), osChannel);
        } catch (Throwable t) {
        } finally {
            safeClose(os);
            safeClose(osChannel);
        }
    }

    private void safeClose(Closeable obj) {
        if (obj != null) {
            try {
                obj.close();
            } catch (IOException e) {
            }
        }
    }

    private boolean loadFromBakFile() {
        boolean parseOK = true;
        byte[] allBytes = null;
        RandomAccessFile is = null;
        Throwable throwable = null;
        try {
            is = new RandomAccessFile(mBackupFilePath, "r");
            byte[] lengthBytes = new byte[ID_LENGTH];
            is.read(lengthBytes, 0, lengthBytes.length);
            int length = ByteIntUtils.bytesToInt(lengthBytes);
            if (length <= (ID_LENGTH + FINISH_MARK_LENGTH) * NUM_TWO) {
                return false;
            }
            if (length > MAX_NUM) {
                length = MAX_NUM;
            }
            if (length > is.length()) {
                length = (int) is.length();
            }

            allBytes = new byte[length - (ID_LENGTH + FINISH_MARK_LENGTH) * NUM_TWO];
            is.seek((ID_LENGTH + FINISH_MARK_LENGTH) * NUM_TWO);
            is.read(allBytes);
        } catch (Throwable t) {
            throwable = t;
        } finally {
            safeClose(is);

            try {
                parseOK = parseBytesIntoMap(allBytes, false);
            } catch (Exception e) {
            }

            if (allBytes != null || throwable != null) {
                if (mErrorListener != null) {
                    mErrorListener.onError(
                            mBackupFilePath + "#" + (throwable == null ? "" : throwable.getCause()) + "#" + parseOK,
                            LOAD_BAK_FILE, (allBytes == null ? 0 : allBytes.length));
                }
            }
        }

        return parseOK;
    }

    /**
     * @param data
     * @return BCC异或校验码
     */
    private byte getBCCCode(byte[] data) {
        byte count = 0;
        for (byte b : data) {
            count ^= b;
        }
        return count;
    }

    private byte getMaskByte(byte[] data) {
        return getBCCCode(data);
    }

    /**
     * 用于在APP退出前及时落地数据
     */
    void onDestroy() {
        if (mIsSaving || mHandler.hasMessages(SAVE_MESSAGE_ID)) {
            Editor editor = edit();
            editor.putString(SPECIAL_KEY, SPECIAL_VALUE);
            editor.commit();
        }
    }

    public interface OnSharedPreferenceErrorListener {
        /**
         * 异常回调接口
         *
         * @param filepath
         * @param errorCode
         * @param time
         */
        void onError(String filepath, int errorCode, long time);
    }

    private static class ByteLongUtils {
        public static byte[] longToBytes(long x) {
            return ByteBuffer.allocate(Long.SIZE / Byte.SIZE).putLong(x).array();
        }

        public static long bytesToLong(byte[] bytes) {
            return ByteBuffer.wrap(bytes).getLong();
        }
    }

    private static class ByteIntUtils {
        public static byte[] intToBytes(int x) {
            return ByteBuffer.allocate(Integer.SIZE / Byte.SIZE).putInt(x).array();
        }

        public static int bytesToInt(byte[] bytes) {
            return ByteBuffer.wrap(bytes).getInt();
        }
    }

    private static class ByteFloatUtils {
        public static byte[] floatToBytes(float x) {
            return ByteBuffer.allocate(Float.SIZE / Byte.SIZE).putFloat(x).array();
        }

        public static float bytesToFloat(byte[] bytes) {
            return ByteBuffer.wrap(bytes).getFloat();
        }
    }

    public static abstract class BaseRunnableEx implements Runnable {
        private Object mArg;

        public Object getArg() {
            return mArg;
        }

        public void setArg(Object arg) {
            mArg = arg;
        }
    }

    private class SupportedType {
        static final byte TYPE_INT = 1;
        static final byte TYPE_FLOAT = 2;
        static final byte TYPE_LONG = 3;
        static final byte TYPE_BOOLEAN = 4;
        static final byte TYPE_STRING = 5;
    }

    public final class EditorImpl implements Editor {
        private HashMap<String, Object> mModified = new HashMap<String, Object>();
        private boolean mClear;

        @Override
        public Editor putString(String key, String value) {
            synchronized (this) {
                mModified.put(key, value);
                return this;
            }
        }

        @Override
        public Editor putStringSet(String key, Set<String> values) {
            throw new RuntimeException("putStringSet is not supported!");
        }

        @Override
        public Editor putInt(String key, int value) {
            synchronized (this) {
                mModified.put(key, value);
                return this;
            }
        }

        @Override
        public Editor putLong(String key, long value) {
            synchronized (this) {
                mModified.put(key, value);
                return this;
            }
        }

        @Override
        public Editor putFloat(String key, float value) {
            synchronized (this) {
                mModified.put(key, value);
                return this;
            }
        }

        @Override
        public Editor putBoolean(String key, boolean value) {
            synchronized (this) {
                mModified.put(key, value);
                return this;
            }
        }

        @Override
        public Editor remove(String key) {
            synchronized (this) {
                mModified.put(key, null);
                return this;
            }
        }

        @Override
        public Editor clear() {
            synchronized (this) {
                mClear = true;
                return this;
            }
        }

        @Override
        public boolean commit() {
            SPImpl.this.save(this, false, true, false);
            return true;
        }

        @Override
        public void apply() {
            SPImpl.this.save(this, false, false, true);
        }

        boolean doClear() {
            synchronized (this) {
                boolean clear = mClear;
                mClear = false;
                return clear;
            }
        }

        HashMap<String, Object> getAll() {
            synchronized (this) {
                return mModified;
            }
        }
    }

    private final class FileMonitor extends FileObserver {
        public FileMonitor(String path, int mask) {
            super(path, mask);
        }

        @Override
        public void onEvent(int event, String path) {
            if (mListeners.size() > 0) {
                tryReload();
            } else {
                stopWatching();
            }
        }
    }

}
