package com.analysys;

import android.content.Context;
import android.util.Log;
import android.util.Pair;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 每个info节点的时候调用插件来尝试增加内容
 */
public class PluginHandler {
    private static final String TAG = "PluginHandler";

    private static final String RUN_MODE = "RM";
    private static final String COMPATIBILITY_MODE = "CM";
    private static final String ALLOW_DEBUG = "AD";
    private static final String CACHE_DIRECTORY = "CD";

    private static final String DATA_LOCATION = "DL";
    private static final String DATA = "DT";
    private static final String TOKEN = "TK";
    private static final String DATA_TYPE = "DTT";

    private static volatile PluginHandler instance = null;

    private PluginHandler() {
    }

    public static PluginHandler getInstance() {
        if (instance == null) {
            synchronized (PluginHandler.class) {
                if (instance == null) {
                    instance = new PluginHandler();
                }
            }
        }
        return instance;
    }

    public static void init(Context context, String appId) {
        Log.e(TAG, "init:" + appId);
    }

    public boolean start() {
        Log.e(TAG, "start");
        return true;
    }

    public boolean stop() {
        Log.e(TAG, "stop");
        return true;
    }

    /**
     * 是否兼容指定的jarVersion版本，不兼容将会被删除。
     * 适用于：对某个版本单独开发的插件，当新升级的时候
     */
    public boolean compatible(String jarVersion) {
        Log.e(TAG, "compatible:" + jarVersion);
        return true;
    }

    public List<Map<String, Object>> getData() {
        Log.e(TAG, "getData");
        List<Map<String, Object>> datas = new ArrayList<>();
        try {
            {
                //IMEI
                Map<String, Object> devMap = new HashMap<>();
                //数据类型，增删改 ADD，DEL，UPD
                devMap.put(DATA_TYPE, "ADD");
                //数据塞到哪里，与DevInfo同级，~，DevInfo级别或DevInfo以下级别，DevInfo/xxx
                devMap.put(DATA_LOCATION, "DevInfo");
                //数据
                Map<String, String> data = new HashMap<>();
                data.put("IMEI_PLUGIN", "123123123");
                Pair s = EncUtils.enc(new JSONObject(data).toString(0), 4);
                devMap.put(DATA, s.second);
                //解密方式|key|当前数据集标识（可空）
                devMap.put(TOKEN, s.first);
                datas.add(devMap);
            }
            {
                //IMEI
                Map<String, Object> devMap = new HashMap<>();
                //数据类型，增删改 ADD，DEL，UPD
                devMap.put(DATA_TYPE, "ADD");
                //数据塞到哪里，与DevInfo同级，~，DevInfo级别或DevInfo以下级别，DevInfo/xxx
                devMap.put(DATA_LOCATION, "~");
                //数据
                Map<String, Object> data = new HashMap<>();
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("name", "xiaoming");
                jsonObject.put("age", "12");
                jsonObject.put("addr", "bj");
                data.put("NewInfo", jsonObject);
                String jsonStr = new JSONObject(data).toString(0);
                Pair pair = EncUtils.enc(jsonStr, 4);
                devMap.put(DATA, pair.second);
                //解密key
                devMap.put(TOKEN, pair.first);
                datas.add(devMap);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return datas;
    }

    public boolean clearData() {
        Log.e(TAG, "clearData");
        return true;
    }

}
