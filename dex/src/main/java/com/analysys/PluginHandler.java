package com.analysys;

import java.util.ArrayList;
import java.util.List;

/**
 * 每个info节点的时候调用插件来尝试增加内容
 */
public class PluginHandler {

    private static volatile PluginHandler instance = null;
    private List<PluginCallback> callbacks;

    private PluginHandler() {
        callbacks = new ArrayList<>();
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
    public void publish(Object data, String action) {
        for (int i = 0; i < callbacks.size(); i++) {
            boolean alreadyConsumed = callbacks.get(i).onAction(data, action);
            if (alreadyConsumed) {
                return;
            }
        }
    }

    public interface PluginCallback {
        /**
         * 当发布事件时调用
         *
         * @param o        参数
         * @param actionId 事件id
         * @return 是否已经消费 true 不继续传递，false 继续传递
         */
        public boolean onAction(Object o, String actionId);
    }


}
