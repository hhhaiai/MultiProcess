package com.analysys.hotfix.utils;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
/**
 * @Copyright 2019 analysys Inc. All rights reserved.
 * @Description: 进程安全的SharedPreferences，通过文件锁保证读写同步
 * 注意:
 * 1. 要使进程同步务必写入数据的时候使用commit
 * @Version: 1.0
 * @Create: 2019-09-07 10:43:19
 * @author: miqt
 * @mail: miqingtang@analysys.com.cn
 */
public final class SysnKV implements SharedPreferences {
    private static final String TAG = "SysnKV";
    private static final String DEF_NAME = "sysn_kv";
    private static final String SUFFIX = ".skv";
    /**
     * 默认200kb
     * <p>
     * 分块存储文件最大值,超过这个值就加一块
     */
    private int mMaxBlockSize = 1024 * 10;
    private final Context context;
    private String name = "def_sysnkv";
    private ArrayList<Block> mBlockList;
    private Queue<Editor> mEditorQueue;
    private Handler mHandler;
    public SysnKV(Context context) {
        this(context, DEF_NAME);
    }
    public SysnKV(Context context, String name) {
        this.name = name;
        this.context = context;
        mBlockList = new ArrayList<>();
        try {
            for (int i = 0; ; i++) {
                String path = getBlockFile(context, name, i);
                File blockFile = new File(path);
                if (blockFile.exists() && blockFile.isFile()) {
                    Block block = null;
                    block = new Block(blockFile);
                    mBlockList.add(block);
                } else {
                    break;
                }
            }
            if (mBlockList.size() == 0) {
                String path = getBlockFile(context, name, mBlockList.size());
                Block block = new Block(new File(path));
                mBlockList.add(block);
            }
            mEditorQueue = new LinkedList<>();
            HandlerThread thread = new HandlerThread("SysnKV");
            thread.start();
            mHandler = new Handler(thread.getLooper(), new Work());
        } catch (Throwable e) {
            //1.文件禁止访问
            //2.无法创建文件
            e.printStackTrace();
        }
    }
    private String getBlockFile(Context context, String name, int num) {
//        String dir = context.getFilesDir().getAbsolutePath()
//                .concat(File.separator);
        String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath()
                .concat(File.separator).concat("testSysnP/");
        return dir.concat(name).concat(String.valueOf(num)).concat(name.indexOf('.') != -1 ? "" : SUFFIX);
    }
    @Override
    public Map<String, ?> getAll() {
        Map<String, Object> mValue = new HashMap<>();
        for (Block block : mBlockList) {
            mValue.putAll(block.getValue());
        }
        return mValue;
    }
    @Override
    public String getString(String key, String defValue) {
        try {
            for (Block block : mBlockList) {
                String o = (String) block.getValue().get(key);
                if (o != null) {
                    return o;
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return defValue;
    }
    @Override
    public Set<String> getStringSet(String key, Set<String> defValues) {
        try {
            for (Block block : mBlockList) {
                Object array = block.getValue().get(key);
                //hashmap 存完了json解析出来是jsonarray
                if (array instanceof Set) {
                    return (Set<String>) array;
                } else if (array instanceof JSONArray) {
                    if (array == null) {
                        return defValues;
                    }
                    JSONArray jsonArray = (JSONArray) array;
                    Set<String> strings;
                    strings = new HashSet<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        strings.add((String) jsonArray.opt(i));
                    }
                    return strings;
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return defValues;
    }
    @Override
    public int getInt(String key, int defValue) {
        try {
            for (Block block : mBlockList) {
                Object val = block.getValue().get(key);
                if (val != null) {
                    return (int) val;
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return defValue;
    }
    @Override
    public long getLong(String key, long defValue) {
        try {
            for (Block block : mBlockList) {
                Object val = block.getValue().get(key);
                if (val != null) {
                    //java.lang.ClassCastException: java.lang.Integer cannot be cast to java.lang.
                    if (val instanceof Integer) {
                        return (int) val;
                    } else {
                        return (long) val;
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return defValue;
    }
    @Override
    public float getFloat(String key, float defValue) {
        try {
            for (Block block : mBlockList) {
                Object val = block.getValue().get(key);
                if (val != null) {
                    if (val instanceof Double) {
                        double d = (double) val;
                        return (float) d;
                    } else {
                        return (float) val;
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return defValue;
    }
    @Override
    public boolean getBoolean(String key, boolean defValue) {
        try {
            for (Block block : mBlockList) {
                Object val = block.getValue().get(key);
                if (val != null) {
                    return (boolean) val;
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return defValue;
    }
    @Override
    public boolean contains(String key) {
        for (Block block : mBlockList) {
            Object o = block.getValue().get(key);
            if (o != null) {
                return true;
            }
        }
        return false;
    }
    @Override
    public Editor edit() {
        return new EditorImpl();
    }
    @Override
    @Deprecated
    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
    }
    @Override
    @Deprecated
    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
    }
    final class EditorImpl implements Editor {
        Map<String, Object> addMap = new HashMap<>();
        Set<String> deleteKey = new HashSet<>();
        boolean isClear;
        @Override
        public Editor putString(String key, String value) {
            addMap.put(key, value);
            return this;
        }
        @Override
        public Editor putStringSet(String key, Set<String> values) {
            addMap.put(key, values);
            return this;
        }
        @Override
        public Editor putInt(String key, int value) {
            addMap.put(key, value);
            return this;
        }
        @Override
        public Editor putLong(String key, long value) {
            addMap.put(key, value);
            return this;
        }
        @Override
        public Editor putFloat(String key, float value) {
            addMap.put(key, value);
            return this;
        }
        @Override
        public Editor putBoolean(String key, boolean value) {
            addMap.put(key, value);
            return this;
        }
        @Override
        public Editor remove(String key) {
            deleteKey.add(key);
            addMap.remove(key);
            return this;
        }
        @Override
        public Editor clear() {
            isClear = true;
            deleteKey.clear();
            addMap.clear();
            return this;
        }
        @Override
        public boolean commit() {
            if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
                //在主线程操作可能会因为等待文件锁anr
                Log.w(TAG, "在主线程操作,最好使用apply防止ANR");
            }
            boolean result = false;
            try {
                for (int i = 0; i < mBlockList.size(); i++) {
                    boolean isMdf = false;
                    Block block = mBlockList.get(i);
                    if (isClear) {
                        block.getValue().clear();
                        isMdf = true;
                    } else {
                        for (String key : deleteKey) {
                            block.sync();
                            Object value = block.getValue().remove(key);
                            if (value != null) {
                                deleteKey.remove(key);
                                isMdf = true;
                            }
                        }
                        if (block.getSize() > mMaxBlockSize) {
                            continue;
                        }
                    }
                    if (!addMap.isEmpty() && block.getSize() < mMaxBlockSize) {
                        block.getValue().putAll(addMap);
                        addMap.clear();
                        isMdf = true;
                    }
                    if (isMdf) {
                        result = block.write();
                    }
                }
                if (!addMap.isEmpty()) {
                    String path = getBlockFile(context, name, mBlockList.size());
                    Block block = new Block(new File(path));
                    mBlockList.add(block);
                    block.getValue().putAll(addMap);
                    result = block.write();
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return result;
        }
        @Override
        public void apply() {
            SysnKV.this.mEditorQueue.add(this);
            Message.obtain(SysnKV.this.mHandler, Work.WHAT_APPLY, SysnKV.this.mEditorQueue);
        }
    }
    final static class Block {
        private Map<String, Object> value;
        private File mFile;
        //版本id
        private Integer mId;
        private RandomAccessFile mAccessFile;
        private FileChannel mChannel;
        public Block(File file) throws IOException {
            this.mFile = file;
            if (!mFile.exists() || !mFile.isFile()) {
                File dir = mFile.getParentFile();
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                mFile.createNewFile();
            }
            value = new HashMap<>();
        }
        public Map<String, Object> getValue() {
            sync();
            return value;
        }
        public long getSize() {
            return mFile.length();
        }
        public boolean write() {
            return doMap2File();
        }
        private void sync() {
            ByteBuffer buffer = null;
            FileLock lock = null;
            try {
                //读mid
                lock = lock(0, 4, true);
                buffer = ByteBuffer.allocate(4);
                int size = mChannel.read(buffer, 0);
                unLock(lock);
                if (size == 4) {
                    buffer.flip();
                    //比较mid
                    int mid = buffer.getInt();
                    //当前mid为空，没同步过，同步，mid不一致，同步
                    if (Block.this.mId == null || Block.this.mId != mid) {
                        doFile2Map();
                        //同步完成，更新mid
                        Block.this.mId = mid;
                    }
                }
            } catch (Throwable e) {
                //读取mid出io异常
                unLock(lock);
                e.printStackTrace();
            }
            if (buffer != null) {
                buffer.clear();
            }
        }
        private FileLock lock(long position, long size, boolean shared) {
            try {
                if (mAccessFile == null || mChannel == null || !mChannel.isOpen()) {
                    mAccessFile = new RandomAccessFile(mFile, "rw");
                    mChannel = mAccessFile.getChannel();
                }
                if (mChannel != null && mChannel.isOpen()) {
                    size = Math.min(size, mAccessFile.length());
                    return mChannel.lock(position, size, shared);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        private void unLock(FileLock lock) {
            if (lock != null) {
                try {
                    lock.release();
                    release();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                lock = null;
            }
        }
        private void release() {
            if (mChannel != null) {
                try {
                    mChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mChannel = null;
            }
            if (mAccessFile != null) {
                try {
                    mAccessFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mAccessFile = null;
            }
        }
        private void doFile2Map() {
            FileLock lock = lock(5, Long.MAX_VALUE, true);
            try {
                //前4位是mid,跳过
                mChannel.position(4);
                ByteBuffer buffer = ByteBuffer.allocate((int) (mChannel.size() - 4));
                int len = mChannel.read(buffer);
                if (len == -1) {
                    return;
                }
                buffer.flip();
                value.clear();
                JSONObject object = new JSONObject(Charset.forName("utf-8").decode(buffer).toString());
                for (Iterator<String> it = object.keys(); it.hasNext(); ) {
                    String k = it.next();
                    value.put(k, object.get(k));
                }
            } catch (IOException e) {
                // io 读文件失败,不用处理
                e.printStackTrace();
            } catch (JSONException e) {
                // json 解析错误,文件出错,删除这个文件
                unLock(lock);
                try {
                    mFile.delete();
                } catch (Exception e1) {
                    //删除文件失败,不处理
                    e1.printStackTrace();
                }
                e.printStackTrace();
                return;
            }
            unLock(lock);
        }
        private boolean doMap2File() {
            boolean result = false;
            FileLock lock = lock(0, Long.MAX_VALUE, false);
            try {
                JSONObject object = new JSONObject(value);
                byte[] bt = object.toString(0).getBytes(Charset.forName("utf-8"));
                ByteBuffer buf = ByteBuffer.allocate(bt.length + 4);
                if (mId == null) {
                    mId = Integer.MIN_VALUE;
                } else {
                    mId = (mId + 1) % (Integer.MAX_VALUE - 10);
                }
                buf.putInt(mId);
                buf.put(bt);
                buf.flip();
                //前4位是mid
                mChannel.position(0);
                while (buf.hasRemaining()) {
                    mChannel.write(buf);
                }
                //删除后面的文件
                mChannel.truncate(4 + bt.length);
                mChannel.force(true);
                result = true;
            } catch (IOException e) {
                //todo 写入文件失败,用备份文件方式处理
                e.printStackTrace();
            } catch (JSONException e) {
                //map转json串会出异常?先不处理,最多就是数据存不进去
                //可能map存储了含有特殊字符串的value会有这个异常.
                e.printStackTrace();
            }
            unLock(lock);
            return result;
        }
    }
    final static class Work implements Handler.Callback {
        public final static int WHAT_APPLY = 1;
        public final static int WHAT_INIT_SYSN = 2;
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT_APPLY:
                    Queue<Editor> queue = null;
                    if (msg.obj instanceof Queue) {
                        queue = (Queue<Editor>) msg.obj;
                    }
                    if (queue == null) {
                        break;
                    }
                    while (!queue.isEmpty()) {
                        Editor editor = queue.poll();
                        editor.commit();
                    }
                    break;
                case WHAT_INIT_SYSN:
                    break;
                default:
                    break;
            }
            return true;
        }
    }
}