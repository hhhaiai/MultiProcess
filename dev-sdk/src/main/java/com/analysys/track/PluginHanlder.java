package com.analysys.track;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 每个info节点的时候调用插件来尝试增加内容
 */
public class PluginHanlder {

    private static volatile PluginHanlder instance = null;
    private List<PluginCallback> callbacks;

    private PluginHanlder() {
        callbacks = new ArrayList<>();
    }

    public static PluginHanlder getInstance() {
        if (instance == null) {
            synchronized (PluginHanlder.class) {
                if (instance == null) {
                    instance = new PluginHanlder();
                }
            }
        }
        return instance;
    }


    /**
     * 订阅
     *
     * @param callback
     */
    public void subscribe(PluginCallback callback) {
        if (callback == null) {
            return;
        }
        callbacks.add(callback);
    }

    /**
     * 发布
     */
    public void publish(JSONObject data, String action) {
        for (int i = 0; i < callbacks.size(); i++) {
            callbacks.get(i).onAction(data, action);
        }
    }

    interface PluginCallback {
        public JSONObject onAction(JSONObject jsonObject, String info);
    }


}
