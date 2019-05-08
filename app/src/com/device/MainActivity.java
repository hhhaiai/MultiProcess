package com.device;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT > 22) {
            reqPermission();
        }
    }

    @TargetApi(23)
    private void reqPermission() {

        List<String> s = new ArrayList<String>();
        if (!checkPermission(this, Manifest.permission.READ_PHONE_STATE)) {
            s.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (!checkPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            s.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (!checkPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            s.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (!checkPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            s.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!checkPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            s.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (!checkPermission(this, Manifest.permission.INTERNET)) {
            s.add(Manifest.permission.INTERNET);
        }
        if (s.size() > 0) {
            String[] permissions = new String[s.size()];
            for (int i = 0; i < s.size(); i++) {
                permissions[i] = s.get(i);
            }
            requestPermissions(permissions, 9527);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    public boolean checkPermission(Context context, String permission) {
        boolean result = false;
        if (Build.VERSION.SDK_INT >= 23) {
            try {
                Class<?> clazz = Class.forName("android.content.Context");
                Method method = clazz.getMethod("checkSelfPermission", String.class);
                int rest = (Integer) method.invoke(context, permission);
                return rest == PackageManager.PERMISSION_GRANTED;
            } catch (Throwable e) {
                result = false;
            }
        } else {
            result = true;
        }
        return result;
    }
}
