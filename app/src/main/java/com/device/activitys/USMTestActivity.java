package com.device.activitys;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
        List<UsageEvents.Event> events = new ArrayList<>();
        if (usageStats != null) {
            while (usageStats.hasNextEvent()) {
                UsageEvents.Event event = new UsageEvents.Event();
                usageStats.getNextEvent(event);
                events.add(event);
            }
        }

        class RecentUseComparator implements Comparator<UsageEvents.Event> {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public int compare(UsageEvents.Event lhs, UsageEvents.Event rhs) {
                return (lhs.getTimeStamp() > rhs.getTimeStamp()) ? -1
                        : (lhs.getTimeStamp() == rhs.getTimeStamp()) ? 0 : 1;
            }
        }
        Collections.sort(events, new RecentUseComparator());

        for (int i = 0; i < events.size(); i++) {
            try {
                UsageEvents.Event event = events.get(i);
                builder.append(event.getPackageName()).append("|").append(packageManager.getPackageInfo(event.getPackageName(), 0).applicationInfo.loadLabel(packageManager)).append("\n");
                builder.append("\t[").append("TimeStamp:").append(dateFormat.format(event.getTimeStamp())).append("]\n");
                builder.append("\t[").append("ClassName:").append(event.getClassName()).append("]\n");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                    builder.append("\t[").append("ShortcutId:").append(event.getShortcutId()).append("]\n");
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    builder.append("\t[").append("AppStandbyBucket:").append(event.getAppStandbyBucket()).append("]\n");
                }
                Configuration configuration = event.getConfiguration();
                if (configuration != null) {
                    builder.append("\t[").append("Configuration:").append(configuration.toString()).append("]\n");
                }

                String typeString = null;
                switch (event.getEventType()) {
                    case UsageEvents.Event.NONE:
                        typeString = "NONE: ";
                        break;
                    case UsageEvents.Event.ACTIVITY_RESUMED:
                        typeString = "ACTIVITY_RESUMED: ";
                        break;
                    case UsageEvents.Event.ACTIVITY_STOPPED:
                        typeString = "ACTIVITY_STOPPED: ";
                        break;
                    case UsageEvents.Event.ACTIVITY_PAUSED:
                        typeString = "ACTIVITY_PAUSED: ";
                        break;
                    case UsageEvents.Event.CONFIGURATION_CHANGE:
                        typeString = "CONFIGURATION_CHANGE: ";
                        break;
                    case UsageEvents.Event.DEVICE_SHUTDOWN:
                        typeString = "DEVICE_SHUTDOWN: ";
                        break;
                    case UsageEvents.Event.DEVICE_STARTUP:
                        typeString = "DEVICE_STARTUP: ";
                        break;
                    case UsageEvents.Event.FOREGROUND_SERVICE_START:
                        typeString = "FOREGROUND_SERVICE_START:";
                        break;
                    case UsageEvents.Event.FOREGROUND_SERVICE_STOP:
                        typeString = "FOREGROUND_SERVICE_STOP: ";
                        break;
                    case UsageEvents.Event.KEYGUARD_HIDDEN:
                        typeString = "KEYGUARD_HIDDEN: ";
                        break;
                    case UsageEvents.Event.KEYGUARD_SHOWN:
                        typeString = "KEYGUARD_SHOWN: ";
                        break;
                    case UsageEvents.Event.SCREEN_INTERACTIVE:
                        typeString = "SCREEN_INTERACTIVE: ";
                        break;
                    case UsageEvents.Event.USER_INTERACTION:
                        typeString = "USER_INTERACTION: ";
                        break;
                }
                builder.append("\t[").append("EventType:").append(typeString).append("]\n");
                builder.append("\n");
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        textView.setText(builder.toString());
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
