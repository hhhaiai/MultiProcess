package com.analysys;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 每个info节点的时候调用插件来尝试增加内容
 */
public class PluginHandler {
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

    public boolean start() {
        return true;
    }

    public boolean stop() {
        return true;
    }

    /**
     * 是否兼容指定的jarVersion版本，不兼容将会被删除。
     * 适用于：对某个版本单独开发的插件，当新升级的时候
     */
    public boolean compatible(String jarVersion) {
        return true;
    }

    public List<Map<String, Object>> getData() {
        List<Map<String, Object>> datas = new ArrayList<>();
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
            devMap.put(DATA, data);
            //解密方式|key|当前数据集标识（可空）
            devMap.put(TOKEN, "1|GJK|RTYHJ");
        }
        return datas;
    }

    public boolean clearData(String token) {
        return true;
    }

}
