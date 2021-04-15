package com.hhhaiai.mpdemo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import me.hhhaiai.multiprocess.MultiprocessManager;
import me.hhhaiai.services.CService1;
import me.hhhaiai.utils.MpLog;
import me.hhhaiai.utils.ServiceHelper;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnTest:
//                ServiceHelper.startService(MainActivity.this, CService1.class, new ChinaPrint());
//                try {
//                    Thread.sleep(50);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                MpLog.i("---------------startService----->" + ServiceHelper.isServiceWorking(MainActivity.this, CService1.class));
//                ServiceHelper.stopService(MainActivity.this, CService1.class);
//                try {
//                    Thread.sleep(50);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                MpLog.i("------------------stopService----->" + ServiceHelper.isServiceWorking(MainActivity.this, CService1.class));

                MultiprocessManager.getInstance(MainActivity.this).postMultiMessages(4,new ChinaPrint());
                break;
            default:
                break;
        }
    }
}