package com.device.activitys;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.device.R;
import com.device.utils.EL;
import com.device.utils.PermissionH;

import java.util.List;


/**
 * @Copyright © 2019 sanbo Inc. All rights reserved.
 * @Description: 主页面
 * @Version: 1.0
 * @Create: 2019-07-27 14:02:49
 * @author: sanbo
 * @mail: xueyongfu@analysys.com.cn
 */
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        reqPer();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnGoTestMainActivity:
                EL.i("click btnGoTestMainActivity");
                startActivity(new Intent(this, TestCase1Activity.class));
                break;
            default:
                break;
        }
    }


    /**************************************************************************************************/
    /***************************************    权限申请     *******************************************/
    /**************************************************************************************************/
    private void reqPer() {
        if (Build.VERSION.SDK_INT > 22) {
            reqPermission();
        }
    }

    @TargetApi(23)
    private void reqPermission() {

        List<String> pps = PermissionH.addPermission(this, new String[]{
                Manifest.permission.READ_PHONE_STATE
                , Manifest.permission.ACCESS_FINE_LOCATION
                , Manifest.permission.ACCESS_COARSE_LOCATION
                , Manifest.permission.WRITE_EXTERNAL_STORAGE
                , Manifest.permission.READ_EXTERNAL_STORAGE
                , Manifest.permission.INTERNET
                , Manifest.permission.WRITE_SETTINGS

        });

        if (pps.size() > 0) {
            String[] permissions = new String[pps.size()];
            for (int i = 0; i < pps.size(); i++) {
                permissions[i] = pps.get(i);
            }
            requestPermissions(permissions, 9527);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }


}
