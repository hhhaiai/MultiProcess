package com.hhhaiai.mpdemo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import me.hhhaiai.multiprocess.MultiprocessManager;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnTest:
                //                ServiceHelper.startService(MainActivity.this, CService1.class, new
                // ChinaPrint());
                //                try {
                //                    Thread.sleep(50);
                //                } catch (InterruptedException e) {
                //                    e.printStackTrace();
                //                }
                //                MpLog.i("---------------startService----->" +
                // ServiceHelper.isServiceWorking(MainActivity.this, CService1.class));
                //                ServiceHelper.stopService(MainActivity.this, CService1.class);
                //                try {
                //                    Thread.sleep(50);
                //                } catch (InterruptedException e) {
                //                    e.printStackTrace();
                //                }
                //                MpLog.i("------------------stopService----->" +
                // ServiceHelper.isServiceWorking(MainActivity.this, CService1.class));

                MultiprocessManager.getInstance(MainActivity.this)
                        .postMultiMessages(4, new ChinaPrint());
                //                new Thread(() -> {
                //                    try {
                //                        Thread.sleep(10 * 1000);
                //
                //                        MpLog.i("1: " +
                // ServiceHelper.isServiceWorking(MainActivity.this,
                // "me.hhhaiai.services.CService1"));
                //                        MpLog.i("2: " +
                // ServiceHelper.isServiceWorking(MainActivity.this,
                // "me.hhhaiai.services.CService2"));
                //                        MpLog.i("3: " +
                // ServiceHelper.isServiceWorking(MainActivity.this,
                // "me.hhhaiai.services.CService3"));
                //                        MpLog.i("4: " +
                // ServiceHelper.isServiceWorking(MainActivity.this,
                // "me.hhhaiai.services.CService4"));
                //                    } catch (Throwable e) {
                //                        MpLog.e(e);
                //                    }
                //                }).start();
                break;
            default:
                break;
        }
    }
}
