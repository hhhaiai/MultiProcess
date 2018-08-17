package com.demo;

import java.io.IOException;
import java.lang.reflect.Method;

import com.eguan.EguanImpl;
import com.eguan.utils.commonutils.EgLog;
import com.eguan.utils.commonutils.EguanIdUtils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    @SuppressWarnings("deprecation")
    private String[] permissionArray = new String[] { Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.WRITE_SETTINGS,
            Manifest.permission.GET_TASKS, Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.READ_PHONE_STATE };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        showDebugDBAddressLogToast(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
        case PermissionUtils.PERMISSION_REQUEST_CODE:
            if (PermissionUtils.verifyPermissions(grantResults)) {
                // Permission Granted do you action
            } else {
                // Permission Denied
                Toast.makeText(this, "WRITE_CONTACTS Denied", Toast.LENGTH_SHORT).show();
            }
            break;
        default:
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PermissionUtils.PERMISSION_SETTING_REQ_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(this)) {
                    // do action
                } else {
                    Toast.makeText(this, "not has setting permission", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        }
    }

    public static void showDebugDBAddressLogToast(Context context) {
        if (BuildConfig.DEBUG) {
            try {
                Class<?> debugDB = Class.forName("com.amitshekhar.DebugDB");
                Method getAddressLog = debugDB.getMethod("getAddressLog");
                Object value = getAddressLog.invoke(null);
                Toast.makeText(context, (String) value, Toast.LENGTH_LONG).show();
                Log.i("sanbo", (String) value);
            } catch (Exception ignore) {

            }
        }
    }

    @SuppressWarnings("unused")
    private void testAlertPermission() {
        WindowManager mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        mWindowManager.addView(new TextView(this), params);
    }

    // 多个权限申请
    public void requestManifest(View v) {
        PermissionUtils.checkPermissionArray(MainActivity.this, permissionArray, 2);
    }

    public void testInsertID(View view) {
        String json = "{\"code\":200,\"tmpid\":\"1d9df036e52db67e0fef98dbf4b4acdb1143b2\",\"egid\":\"\"}";
        EguanIdUtils.getInstance(this).setId(json);
    }

    public void testReadID(View view) {
        EgLog.i("sanbo", EguanIdUtils.getInstance(this).getId());
    }

    public void initSDK(View view) {
        EguanImpl.getInstance().setDebugMode(this, true);
        EguanImpl.getInstance().initEguan(this, "7752552892442721d", "app channel");
    }

    public void clearSelf(View view) {
        try {
            Runtime.getRuntime().exec("pm clear " + getPackageName());
        } catch (IOException e) {
        }
    }
}
