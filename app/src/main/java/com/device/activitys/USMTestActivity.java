package com.device.activitys;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.usage.UsageStats;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.device.R;
import com.device.utils.USMUtils;

import java.text.SimpleDateFormat;
import java.util.List;

public class USMTestActivity extends Activity {

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usmtest);
        final TextView textView = findViewById(R.id.text);
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
                setData(textView);
            }
        });
        setData(textView);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setData(TextView textView) {

        PackageManager packageManager = getPackageManager();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        StringBuilder builder = new StringBuilder();
        List<String> stringList = USMUtils.getAppPackageList(this);
        builder.append("包名数:").append(stringList.size()).append("\n\n\n\n").append("---------\n");

        List<UsageStats> usageStats = USMUtils.getUsageStatsByInvoke(0, System.currentTimeMillis(), this);
        if (usageStats != null) {
            for (int i = 0; i < usageStats.size(); i++) {
                UsageStats usageStats1 = usageStats.get(i);
                try {
                    builder.append(usageStats1.getPackageName()).append("|")
                            .append(packageManager.getPackageInfo(usageStats1.getPackageName(), 0).applicationInfo.loadLabel(packageManager))
                            .append("[").append(dateFormat.format(usageStats1.getLastTimeUsed())).append("]\n");
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        textView.setText(builder.toString());
    }
}
