package com.device;

import android.app.Activity;
import android.os.Bundle;

import com.analysys.dev.EguanMonitorAgent;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EguanMonitorAgent.getInstance().initEguan(this, "qwertyuiop123", "WanDouJia", true);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }
}
