package com.device.activitys;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.device.R;
import com.device.utils.EL;
import com.device.utils.USMUtils;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

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

        UsageEvents usageStats = USMUtils.getUsageEvents(System.currentTimeMillis() - 3600 * 1000, System.currentTimeMillis(), this);
        if (usageStats != null) {
            JSONArray jsonArray = new JSONArray();
            OCInfo openEvent = null;
            while (usageStats.hasNextEvent()) {
                UsageEvents.Event event = new UsageEvents.Event();
                usageStats.getNextEvent(event);
                if (openEvent == null) {
                    openEvent = new OCInfo(event.getTimeStamp(), event.getPackageName());
                } else {
                    if (!openEvent.getPkgName().equals(event.getPackageName())) {
                        openEvent.setCloseTime(event.getTimeStamp());
                        jsonArray.put(openEvent.toJson());
                    }
                }
            }
            try {
                textView.setText(jsonArray.toString(2));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void onClick(View view) {
        try {
            UsageStatsManager usageStatsManager = (UsageStatsManager) getApplicationContext()
                    .getSystemService(Context.USAGE_STATS_SERVICE);
            List<UsageStats> uss = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, 0, System.currentTimeMillis());
            EL.i("uss====>" + uss.toString());
            UsageEvents eus = usageStatsManager.queryEvents(0, System.currentTimeMillis());
            EL.i("eus====>" + eus.toString());
            Map<String, UsageStats> map = usageStatsManager.queryAndAggregateUsageStats(0, System.currentTimeMillis());
            EL.i("map====>" + map.toString());

            for (UsageStats us : uss) {
                EL.i(us.getPackageName() + "--------" + us.describeContents() + "-----" + us.getLastTimeStamp());
            }

// Field field = UsageStatsManager.class.getField("mService");
            Field field = UsageStatsManager.class.getDeclaredField("mService");
            field.setAccessible(true);
            Object mService = field.get(getApplicationContext().getSystemService(Context.USAGE_STATS_SERVICE));
            if (mService == null) {
                EL.i("service 方式二获取");
                Method method = Class.forName("android.os.ServiceManager").getMethod("getService", String.class);
                IBinder iBinder = (IBinder) method.invoke(null, "usagestats");
                mService = Class.forName("android.app.usage.IUsageStatsManager$Stub").getMethod("asInterface", IBinder.class).invoke(null, iBinder);
            }
            if (mService == null) {
                EL.e("service获取异常.....");
                return;
            }
// mService.getClass().getMethods();

            //public android.content.pm.ParceledListSlice android.app.usage.IUsageStatsManager$Stub$Proxy.queryUsageStats(int,long,long,java.lang.String) throws android.os.RemoteException
            Method method = mService.getClass().getMethod("queryUsageStats", int.class, long.class, long.class, String.class);
            if (method == null) {
                EL.i("method 方式二获取");
                method = mService.getClass().getDeclaredMethod("queryUsageStats", int.class, long.class, long.class, String.class);
            }
            if (method == null) {
                EL.e("method获取异常.....");
                return;
            }
            Object parceledListSlice = method.invoke(mService, UsageStatsManager.INTERVAL_BEST, 0, System.currentTimeMillis(), "info.kfsoft.datamonitor");

            EL.e("parceledListSlice: " + parceledListSlice);


        } catch (Throwable e) {
            EL.e(e);
        }
    }
}
