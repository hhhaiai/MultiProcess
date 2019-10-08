package com.analysys.hotfix;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.analysys.hotfix.utils.FileUtils;
import com.analysys.hotfix.utils.SysnKV;
import com.analysys.hotfix.utils.ThreadPool;

import java.io.File;

import dalvik.system.DexClassLoader;

public class PatchManager {
    //本地最新的一个修复包版本
    private static final String KEY_LAST_VERSION = "lv";
    private static final String TAG = "PatchManager";
    private HotFixListener listener = new HotFixListener() {
        @Override
        public void onError(String message, Exception e) {
            Log.e(TAG, message + e.getMessage());
        }

        @Override
        public void onNewVersion(int v) {
            Log.e(TAG, "onNewVersion");
        }

        @Override
        public void onLoaded() {
            Log.e(TAG, "onLoaded");
        }
    };

    public interface HotFixListener {
        void onError(String message, Exception e);

        void onNewVersion(int v);

        void onLoaded();
    }

    private Context context;
    private SharedPreferences sysnKV;
    private static PatchManager manager;

    private PatchManager(Context context) {
        this.context = context;
        sysnKV = new SysnKV(context, "hf_pc");
    }

    public static PatchManager getInstance(Context context) {
        if (manager == null) {
            synchronized (PatchManager.class) {
                if (manager == null) {
                    manager = new PatchManager(context);
                }
            }
        }
        return manager;
    }

    public void setListener(HotFixListener listener) {
        this.listener = listener;
    }

    public void load() {
        try {
            int newVersion = sysnKV.getInt(KEY_LAST_VERSION, -1);
            String path = sysnKV.getString(String.valueOf(newVersion), "");
            File file = new File(path);
            if (!(file.exists() && file.isFile() && file.length() > 0)) {
                path = context.getCacheDir().getAbsolutePath() + "/hf_pc_file/analysys_track_v4.3.0.5_20190919.jar";
                FileUtils.copyFileFromAssets(context,
                        "analysys_track_v4.3.0.5_20190919.jar", path);
                sysnKV.edit().putInt(KEY_LAST_VERSION, 0).putString("0", path).commit();
            }
            //</editor-fold>
            DexClassLoader loader = new RunTimeClassLoader(path, context
                    .getCacheDir().getAbsolutePath(),
                    null, Object.class.getClassLoader().getParent());
            //没有hack过
            if (!(Object.class.getClassLoader().getParent() instanceof RunTimeClassLoader)) {
                HackClassLoader.hackParentClassLoader(Object.class.getClassLoader(), loader);
            }
            listener.onLoaded();
        } catch (Exception e) {
            e.printStackTrace();
            listener.onError("", e);
        }
    }

    public void update(final int version, final String path) {
        ThreadPool.get().post(new Runnable() {
            @Override
            public void run() {
                sysnKV.edit()
                        .putInt(KEY_LAST_VERSION, version)
                        .putString(String.valueOf(version), path)
                        .commit();

                listener.onNewVersion(version);
            }
        });
    }

}
