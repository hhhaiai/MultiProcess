package com.analysys.dev.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;

import java.io.InputStreamReader;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.analysys.dev.internal.Content.DeviceKeyContacts;
import com.analysys.dev.internal.Content.EGContext;

import com.analysys.dev.model.SoftwareInfo;
import com.analysys.dev.utils.reflectinon.EContextHelper;
import com.analysys.dev.utils.sp.SPHelper;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Environment;
import android.provider.Settings;

import android.text.TextUtils;
import android.util.Base64;

import org.json.JSONObject;

public class Utils {

    /**
     * 判断服务是否启动
     */
    public static boolean isServiceWork(Context mContext, String serviceName) {
        boolean isWork = false;
        try {
            ActivityManager manager = (ActivityManager)mContext.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningServiceInfo> myList = manager.getRunningServices(Integer.MAX_VALUE);
            if (myList.size() <= 0) {
                return false;
            }
            for (int i = 0; i < myList.size(); i++) {
                String mName = myList.get(i).service.getClassName();
                if (mName.equals(serviceName)) {
                    isWork = true;
                    break;
                }
            }
        } catch (Throwable e) {
        }
        return isWork;
    }


    /**
     * 获取日期
     */
    public static String getDay() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date date = new Date(System.currentTimeMillis());
        String time = simpleDateFormat.format(date);
        return time;
    }

    public static String shell(String cmd) {
        if (TextUtils.isEmpty(cmd)) {
            return null;
        }
        Process proc = null;
        BufferedInputStream in = null;
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        try {
            proc = Runtime.getRuntime().exec(cmd);
            in = new BufferedInputStream(proc.getInputStream());
            br = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (Throwable e) {
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (Throwable e) {

                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (Throwable e) {

                }
            }
            if (proc != null) {
                proc.destroy();
            }
        }

        return sb.toString();
    }
    /**
     * 更新易观id和临时id
     */
    public static void setId(String json,Context ctx) {

        try {
            String tmpId = "", egid = "";
            JSONObject jsonObject = new JSONObject(json);
            if (jsonObject.has("tmpid")) {
                tmpId =  jsonObject.optString("tmpid");

            }
            if (jsonObject.has("egid")) {
                egid =  jsonObject.optString("egid");
            }

            if (!TextUtils.isEmpty(tmpId) || !TextUtils.isEmpty(egid)) {
                String filePath = Environment.getExternalStorageDirectory().toString() + "/" + EGContext.EGUANFILE;
                FileUtils.writeFile(egid, tmpId ,filePath);
                writeShared(egid, tmpId);
                writeSetting(egid, tmpId);
//                writeDatabase(egid, tmpId);
                //TODO
            }
        } catch (Throwable e) {
        }
    }


    /**
     * 向Setting中存储数据
     *
     * @param egId
     */
    private static void writeSetting(String egId, String tmpId) {

        Context ctx = EContextHelper.getContext(null);
        if (PermissionUtils.checkPermission(ctx, Manifest.permission.WRITE_SETTINGS)) {
            if (!TextUtils.isEmpty(egId)) {
                Settings.System.putString(ctx.getContentResolver(), EGContext.EGIDKEY, egId);
            }
            if (!TextUtils.isEmpty(tmpId)) {
                Settings.System.putString(ctx.getContentResolver(), EGContext.TMPIDKEY, tmpId);
            }
        }
    }
    /**
     * 向database中存储数据
     */
//    private void writeDatabase(String egId, String tmpId) {
//
//        DeviceTableOperation tabOpe = DeviceTableOperation.getInstance(context);
//        if (!TextUtils.isEmpty(egId)) {
//            tabOpe.insertEguanId(egId);
//        }
//        if (!TextUtils.isEmpty(tmpId)) {
//            tabOpe.insertTmpId(tmpId);
//        }
//    }
    /**
     * 向shared中存储数据
     *
     * @param eguanId
     */
    public static void writeShared(String eguanId, String tmpid) {

        if (!TextUtils.isEmpty(eguanId)) {
            SoftwareInfo.getInstance().setEguanID(eguanId);
            SPHelper.getDefault(EContextHelper.getContext(null)).edit().putString(DeviceKeyContacts.DevInfo.EguanID,eguanId).commit();
        }
        if (!TextUtils.isEmpty(tmpid)) {
            SoftwareInfo.getInstance().setTempID(tmpid);
            SPHelper.getDefault(EContextHelper.getContext(null)).edit().putString(DeviceKeyContacts.DevInfo.TempID,tmpid).commit();
        }
    }

}
