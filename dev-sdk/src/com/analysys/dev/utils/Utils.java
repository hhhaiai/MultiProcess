package com.analysys.dev.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;

import java.util.Calendar;
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
     * 获取时段标记
     */
    public static int getTimeTag(long timestamp) {
        int tag = 0;
        final Calendar mCalendar = Calendar.getInstance();
        mCalendar.setTimeInMillis(timestamp);
        int h = mCalendar.get(Calendar.HOUR_OF_DAY);
        if (0 < h && h < 6) {
            tag = 1;
        } else if (6 <= h && h < 12) {
            tag = 2;
        } else if (12 <= h && h < 18) {
            tag = 3;
        } else if (18 <= h && h < 24) {
            tag = 4;
        }
        return tag;
    }

    /**
     * 数据加密
     */
    public static String encrypt(String info, long time) {
        if (TextUtils.isEmpty(info)) {
            return null;
        }
        int key = getTimeTag(time);
        byte[] bytes = info.getBytes();
        int len = bytes.length;
        for (int i = 0; i < len; i++) {
            bytes[i] = (byte)(bytes[i] ^ key);
            key = bytes[i];
        }
        byte[] bt = Base64.encode(bytes, Base64.NO_WRAP);
        return new String(bt);
    }

    /**
     * 数据解密
     */
    public static String decrypt(String info, long time) {
        if (TextUtils.isEmpty(info)) {
            return null;
        }
        int key = getTimeTag(time);
        byte[] bytes = Base64.decode(info.getBytes(), Base64.NO_WRAP);
        int len = bytes.length;
        for (int i = len - 1; i > 0; i--) {
            bytes[i] = (byte)(bytes[i] ^ bytes[i - 1]);
        }
        bytes[0] = (byte)(bytes[0] ^ key);
        return new String(bytes);
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
    public void setId(String json,Context ctx) {

        try {
            String tmpId = "", egid = "";
            JSONObject jsonObject = new JSONObject(json);
            if (jsonObject.has(DeviceKeyContacts.DevInfo.TempID)) {
                tmpId =  jsonObject.optString(DeviceKeyContacts.DevInfo.TempID);

            }
            if (jsonObject.has(DeviceKeyContacts.DevInfo.EguanID)) {
                egid =  jsonObject.optString(DeviceKeyContacts.DevInfo.EguanID);
            }

            if (!TextUtils.isEmpty(tmpId) || !TextUtils.isEmpty(egid)) {
                writeFile(egid, tmpId);
                writeShared(egid, tmpId);
                writeSetting(egid, tmpId);
//                writeDatabase(egid, tmpId);
                //TODO
            }
        } catch (Throwable e) {
        }
    }
    /**
     * 向SD卡存储数据
     */
    private void writeFile(String egId, String tmpId) {

        try {
            if (!FileUtils.permisJudgment()) {
                return;
            }
            String id = "", egid = "", tmpid = "";
            List<String> idInfo = FileUtils.readFile();
            if (idInfo.size() == 2) {
                egid = idInfo.get(0);
                tmpid = idInfo.get(1);
            }
            if (!TextUtils.isEmpty(egId) && !TextUtils.isEmpty(tmpId)) {
                id = egId + "$" + tmpId;
            } else if (!TextUtils.isEmpty(egId)) {
                id = egId + "$" + tmpid;
            } else if (!TextUtils.isEmpty(tmpId)) {
                id = egid + "$" + tmpId;
            } else {
                return;
            }

            String st = new String(id.getBytes(), "utf-8");
            File file = new File(Environment.getExternalStorageDirectory(), EGContext.EGUANFILE);
            OutputStream out = new FileOutputStream(file, false);
            out.write(st.getBytes());
            out.close();
        } catch (Throwable e) {

        }
    }


    /**
     * 向Setting中存储数据
     *
     * @param egId
     */
    private void writeSetting(String egId, String tmpId) {
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
    public void writeShared(String eguanId, String tmpid) {

        if (!TextUtils.isEmpty(eguanId)) {
            SoftwareInfo.getInstance().setEguanID(eguanId);
            SPHelper.getDefault(EContextHelper.getContext(null)).edit().putString(DeviceKeyContacts.DevInfo.EguanID,eguanId);
        }
        if (!TextUtils.isEmpty(tmpid)) {
            SoftwareInfo.getInstance().setTempID(tmpid);
            SPHelper.getDefault(EContextHelper.getContext(null)).edit().putString(DeviceKeyContacts.DevInfo.TempID,tmpid);
        }
    }

}
