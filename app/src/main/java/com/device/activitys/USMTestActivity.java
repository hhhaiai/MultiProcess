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

    static long lasttime = System.currentTimeMillis() - 3600 * 1000 * 1;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setView() {
        try {
            JSONArray jsonArray = USMImpl.getUSMInfo(USMTestActivity.this, lasttime, System.currentTimeMillis());
            lasttime = System.currentTimeMillis();
            if (jsonArray == null) {
                textView.setText("null");
                return;
            }
            textView.setText(jsonArray.toString(2));
        } catch (JSONException e) {
            e.printStackTrace();
        }
//        try {
//            SimpleDateFormat data = new SimpleDateFormat("yyyy-MM-dd hh-mm-ss");
//            long time = System.currentTimeMillis();
//            PackageManager packageManager = getPackageManager();
//            UsageEvents usageStats = USMUtils.getUsageEvents(lasttime, time, this);
//            JSONArray jsonArray = new JSONArray();
//            if (usageStats != null) {
//                while (usageStats.hasNextEvent()) {
//                    UsageEvents.Event event = new UsageEvents.Event();
//                    usageStats.getNextEvent(event);
//                    JSONObject jsonObject = new JSONObject();
//                    jsonObject.putOpt("EventType", event.getEventType());
//                    jsonObject.putOpt("ClassName", event.getClassName());
//                    jsonObject.putOpt("PackageName", event.getPackageName());
//                    jsonObject.putOpt("TimeStamp",event.getTimeStamp());
//                    jsonArray.put(jsonObject);
//                }
//            }
//            lasttime = time;
//
//            textView.setText(jsonArray.toString(4));
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
    }


    public void onClick(View view) {

    }
}
