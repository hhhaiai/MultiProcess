package com.analysys.dev.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import com.analysys.dev.internal.Content.DeviceKeyContacts;
import com.analysys.dev.internal.Content.EGContext;
import com.analysys.dev.model.DevInfo;
import com.analysys.dev.model.SoftwareInfo;
import com.analysys.dev.service.AnalysysAccessibilityService;
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

/**
 * @Copyright © 2018 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2018/10/12 11:29
 * @Author: Wang-X-C
 */
public class Utils {

    /**
     * 判断服务是否启动
     */
    public static boolean isServiceWork(Context mContext, String serviceName) {
        boolean isWork = false;
        try {
            ActivityManager myAM = (ActivityManager)mContext.getSystemService(Context.ACTIVITY_SERVICE);
            @SuppressWarnings("deprecation")
            List<ActivityManager.RunningServiceInfo> myList = myAM.getRunningServices(Integer.MAX_VALUE);
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
     * 此方法用来判断当前应用的辅助功能服务是否开启
     */
    public static boolean isAccessibilitySettingsOn(Context context) {
        String accessibilityServiceName =
            new String(context.getPackageName() + "/" + AnalysysAccessibilityService.class.getCanonicalName());
        int accessibilityEnabled = 0;
        try {
            accessibilityEnabled = Settings.Secure.getInt(context.getContentResolver(),
                android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException e) {
        }
        if (accessibilityEnabled == 1) {
            String services =
                Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);

            if (services != null) {
                return services.toLowerCase(Locale.getDefault())
                    .contains(accessibilityServiceName.toLowerCase(Locale.getDefault()));
            }
        }
        return false;
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

    /**
     * Gzip 压缩数据
     */
    public static byte[] compressGzip(byte[] unGzipData) {
        if (unGzipData == null) {
            return null;
        }
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GZIPOutputStream gzip = new GZIPOutputStream(baos);
            gzip.write(unGzipData);
            gzip.close();
            byte[] encode = baos.toByteArray();
            baos.flush();
            baos.close();
            return encode;
        } catch (Throwable e) {

        }
        return null;
    }

    /**
     * Gzip解压数据
     */
    public static String decompressGzip(byte[] gzipData) {
        try {
            if (gzipData == null) {
                return null;
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ByteArrayInputStream in = new ByteArrayInputStream(gzipData);
            GZIPInputStream gzip = new GZIPInputStream(in);
            byte[] buffer = new byte[256];
            int n = 0;
            while ((n = gzip.read(buffer, 0, buffer.length)) > 0) {
                out.write(buffer, 0, n);
            }
            gzip.close();
            in.close();
            out.close();
            return out.toString();
        } catch (Throwable e) {
        }
        return null;
    }

    /**
     * 内部使用 加密 通过 rawpassword 加密 content
     */
    public static byte[] aesEncrypt(byte[] content, byte[] pwd) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(pwd, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            return cipher.doFinal(content);
        } catch (Throwable e) {
        }

        return null;
    }

    /**
     * 内部使用 解密
     */
    public static byte[] aesDecrypt(byte[] content, byte[] pwd) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(pwd, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);

            byte[] result = cipher.doFinal(content);
            return result;
        } catch (Throwable e) {
        }

        return null;
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
    public void setId(String json) {

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
            if (!permisJudgment()) {
                return;
            }
            String id = "", egid = "", tmpid = "";
            List<String> idInfo = readFile();
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
    private List<String> readFile() {

        String idInfo = readIdFile();

        List<String> list = new ArrayList<>();
        try {
            if (!TextUtils.isEmpty(idInfo)) {
                int index = idInfo.indexOf("$");
                int lastIndex = idInfo.lastIndexOf("$");
                if (idInfo.length() > 2 && index == lastIndex) {
                    if (index == 0 && idInfo.length() - 1 > 0) {
                        list.add("");
                        list.add(idInfo.substring(1, idInfo.length()));
                        return list;
                    }
                    if (index != 0 && index == idInfo.length() - 1) {
                        list.add(idInfo.substring(0, index));
                        list.add("");
                    } else {
                        String[] ids = idInfo.split("\\$");
                        if (ids.length == 2) {
                            list.add(ids[0]);
                            list.add(ids[1]);
                        }
                    }
                }
            }
        } catch (Throwable e) {

        }
        return list;
    }
    /**
     * 判断SDCard是否为可读写状态
     *
     * @return
     */
    private boolean permisJudgment() {
        String en = Environment.getExternalStorageState();
        if (en.equals(Environment.MEDIA_MOUNTED)) {
            return true;
        }
        return false;
    }
    /**
     * 从SD卡读数据
     *
     * @return
     */
    private String readIdFile() {
        try {
            if (fileJudgment() && !permisJudgment()) {
                return "";
            }
            File file = new File(Environment.getExternalStorageDirectory(), EGContext.EGUANFILE);
            BufferedReader br = new BufferedReader(new FileReader(file));
            String readline;
            StringBuffer sb = new StringBuffer();
            while ((readline = br.readLine()) != null) {
                sb.append(readline);
            }
            br.close();
            return sb.toString();
        } catch (Throwable e) {
        }
        return "";
    }
    /**
     * 判断文件是否存在 ，true 存在 false 不存在
     */
    private boolean fileJudgment() {
        String address = Environment.getExternalStorageDirectory().toString() + "/" + EGContext.EGUANFILE;
        File file = new File(address);
        if (file.exists()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 向shared中存储数据
     *
     * @param eguanId
     */
    private void writeShared(String eguanId, String tmpid) {

        if (!TextUtils.isEmpty(eguanId)) {
            SoftwareInfo.getInstance().setEguanID(eguanId);
            SPHelper.getDefault(EContextHelper.getContext(null)).edit().putString(DeviceKeyContacts.DevInfo.EguanID,eguanId);
        }
        if (!TextUtils.isEmpty(tmpid)) {
            SoftwareInfo.getInstance().setTempID(tmpid);
            SPHelper.getDefault(EContextHelper.getContext(null)).edit().putString(DeviceKeyContacts.DevInfo.TempID,tmpid);
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

}
