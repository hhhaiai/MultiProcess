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
        try {
            JSONArray jsonArray = USMImpl.getUSMInfo(USMTestActivity.this, 0, System.currentTimeMillis());
            if (jsonArray == null) {
                textView.setText("null");
                return;
            }
            textView.setText(jsonArray.toString(2));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public void onClick(View view) {

    }
}
