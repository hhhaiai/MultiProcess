package com.device.activitys;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.analysys.track.internal.impl.usm.USMImpl;
import com.analysys.track.internal.impl.usm.USMUtils;
import com.device.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.concurrent.TimeUnit;

public class USMTestActivity extends Activity {

    private TextView textView;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usmtest);
        textView = findViewById(R.id.text);
        Button button = findViewById(R.id.button);
        Button button2 = findViewById(R.id.button2);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                USMUtils.openUSMSetting(USMTestActivity.this);
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setView();
            }
        });
        setView();

    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setView() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final JSONArray jsonArray = USMImpl.getUSMInfo(USMTestActivity.this, System.currentTimeMillis() - 18 * 60 * 60 * 1000, System.currentTimeMillis());
                if (jsonArray == null) {
                    //textView.setText("null");
                    return;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            textView.setText(jsonArray.toString(2));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }).start();

    }


    public void onClick(View view) {

    }
}
