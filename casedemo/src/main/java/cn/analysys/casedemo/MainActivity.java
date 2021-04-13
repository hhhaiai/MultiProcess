package cn.analysys.casedemo;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import com.analysys.track.internal.work.ServiceHelper;
import com.analysys.track.utils.EThreadPool;
import com.cslib.utils.L;

import java.util.ArrayList;
import java.util.List;

import cn.analysys.casedemo.cases.CaseCtl;
import cn.analysys.casedemo.multiprocess.Service1;
import cn.analysys.casedemo.utils.SDKHelper;

public class MainActivity extends Activity {

    private Activity mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prepare();
    }


    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnGotoTestCasePage:
                CaseCtl.gotoCase(mContext);
                break;
            case R.id.btnTest:
                EThreadPool.runOnWorkThread(() -> {
                    Intent intent = new Intent(MainActivity.this, Service1.class);
                    startService(intent);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                    L.w("startService调用完毕，服务状态:" + ServiceHelper.getInstance(MainActivity.this).isServiceWorking(Service1.class.getName()));
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                    stopService(intent);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                    L.w("stopService调用完毕，服务状态:" + ServiceHelper.getInstance(MainActivity.this).isServiceWorking(Service1.class.getName()));

                });
                break;
            default:
                break;
        }
    }


    private void prepare() {
        try {
            mContext = this;
            openLog();
            reqPer();
        } catch (Throwable e) {
            L.e(e);
        }
    }

    private void openLog() {
        new Thread(() -> {
            try {
                SDKHelper.shell("setprop log.tag.sanbo VERBOSE");
                SDKHelper.shell("setprop log.tag.analysys VERBOSE");
            } catch (Throwable e) {
                L.e(e);
            }
        }).start();
    }

    /**************************************************************************************************/
    /***************************************    权限申请     *******************************************/
    /**************************************************************************************************/
    private void reqPer() {
        if (Build.VERSION.SDK_INT > 22) {
            try {
                SDKHelper.reqPermission(
                        mContext,
                        new String[]{
                                Manifest.permission.READ_PHONE_STATE
                                , "android.permission.READ_PRIVILEGED_PHONE_STATE"
                                , "android.permission.ACCESS_BACKGROUND_LOCATION"
                                , Manifest.permission.ACCESS_FINE_LOCATION
                                , Manifest.permission.ACCESS_COARSE_LOCATION
                                , Manifest.permission.WRITE_EXTERNAL_STORAGE
                                , Manifest.permission.READ_EXTERNAL_STORAGE
                                , Manifest.permission.INTERNET
                                , Manifest.permission.WRITE_SETTINGS

                        },
                        9527
                );
            } catch (Throwable e) {
                L.e(e);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

}