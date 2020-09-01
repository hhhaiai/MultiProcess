package com.analysys;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import org.json.JSONObject;

/**
 * @Copyright © 2020 analysys Inc. All rights reserved.
 * @Description: dex类
 * @Version: 1.0
 * @Create: 2020-01-08 15:25
 * @author: sanbo
 */
public class Ab {

    private static final String appkey = "7752552892442721d";
    private static final String channel = "wandoujia";
    private static final String version = "1.0";
    private static final int count = 3;

    public static void main(String[] args) {
        init(getContext());
    }


    public static final synchronized void init(Context context) {
        synchronized (Ab.class) {
            init(context, appkey, channel);
        }
    }

    public static final synchronized void init(Context context, String key, String channel) {
        synchronized (Ab.class) {

            Log.e("Analysys_Plugin", key + "," + channel);

            PluginHandler.getInstance().subscribe(new PluginHandler.PluginCallback() {
                @Override
                public boolean onAction(Object o, String actionId) {
                    if (actionId == null) {
                        return false;
                    }
                    try {
                        if (actionId.equals("getImei")) {
                            JSONObject object = (JSONObject) o;
                            object.put("pluginimie", "xxx");
                        }else if(actionId.equals("DEVINFO")){

                        }else if(){


                        }
                    } catch (Throwable e) {
                    }
                    return true;
                }
            });

            Log.e("Analysys_Plugin", key + "," + channel);
        }

    }

    private static Context getContext() {
        Application application = null;
        try {
            application = (Application) Class.forName("android.app.ActivityThread").getMethod("currentApplication").invoke(null, (Object[]) null);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        if (application == null) {
            try {
                application = (Application) Class.forName("android.app.AppGlobals").getMethod("getInitialApplication").invoke(null, (Object[]) null);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return application;
    }
}
