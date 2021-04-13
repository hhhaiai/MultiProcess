package cn.analysys.casedemo.multiprocess;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.cslib.utils.L;

public class Service1 extends Service {
    public Service1() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        L.d("Service1.onBind intent:" + intent);
        return null;
    }

    @Override
    public void onRebind(Intent intent) {
        L.d("Service1.onRebind intent:" + intent);

        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        L.d("Service1.onUnbind intent:" + intent);
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        L.d("Service1.onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        L.d("Service1.onStartCommand flags:" + flags + "; startId: " + startId + " ; intent: " + intent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        L.d("Service1.onStart startId: " + startId + " ; intent: " + intent);

    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        L.d("Service1.onLowMemory ");
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        L.d("Service1.onTrimMemory level:" + level);

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        L.d("Service1.attachBaseContext newBase:" + newBase);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        L.d("Service1.onDestroy ");

    }
}