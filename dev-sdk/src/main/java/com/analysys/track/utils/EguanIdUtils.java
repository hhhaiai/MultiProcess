package com.analysys.track.utils;

import android.Manifest;
import android.content.Context;
import android.os.Environment;
import android.provider.Settings;
import android.text.TextUtils;

import com.analysys.track.BuildConfig;
import com.analysys.track.db.TableProcess;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.utils.sp.SPHelper;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStream;

/**
 * @Copyright © 2019 sanbo Inc. All rights reserved.
 * @Description: eguan id保存类
 * @Version: 1.0
 * @Create: 2019-08-05 16:35:51
 * @author: sanbo
 * @mail: xueyongfu@analysys.com.cn
 */
public class EguanIdUtils {


    /**
     * 获取tempid
     *
     * @return
     */
    public String getId() {
        String tmpId = "";
        try {
            String file = readIdFile();
            String setting = readSetting();
            String shard = readShared();
            String database = readDatabase();
            // 四个文件作对比
            tmpId = getContrastId(file, setting, shard, database);
        } catch (Throwable t) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(t);
            }
            tmpId = "";
        }

        return tmpId;
    }

    /**
     * 更新易观id和临时id
     */
    public void setId(String json) {
        try {
            String tmpId = "";
            JSONObject jsonObject = new JSONObject(json);
            if (jsonObject.has(EGContext.TMPIDKEY)) {
                tmpId = jsonObject.optString(EGContext.TMPIDKEY);
            }
            if (!TextUtils.isEmpty(tmpId)) {
                writeFile(tmpId);
                writeShared(tmpId);
                writeSetting(tmpId);
                writeDatabase(tmpId);
            }
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
        }
    }

    /**
     * 获取临时id,四个文件一致则上传，否则上传空串
     *
     * @param fileId
     * @param settingId
     * @param shardId
     * @param databaseId
     * @return
     */
    private String getContrastId(String fileId, String settingId, String shardId, String databaseId) {
        String id = "";
        // 4个全空返回空
        if (TextUtils.isEmpty(fileId) && TextUtils.isEmpty(settingId) && TextUtils.isEmpty(shardId)
                && TextUtils.isEmpty(databaseId)) {
            return id;
        } else {
            // 不全为空则不空的值相等可传，不等不传
            if (!TextUtils.isEmpty(fileId)) {
                id = fileId;
            }
            if (!TextUtils.isEmpty(settingId)) {
                if (TextUtils.isEmpty(id)) {
                    id = settingId;
                } else if (!settingId.equals(id)) {
                    id = "";
                    return id;
                }
            }
            if (!TextUtils.isEmpty(shardId)) {
                if (TextUtils.isEmpty(id)) {
                    id = shardId;
                } else if (!shardId.equals(id)) {
                    id = "";
                    return id;
                }
            }
            if (!TextUtils.isEmpty(databaseId)) {
                if (TextUtils.isEmpty(id)) {
                    id = databaseId;
                } else if (!databaseId.equals(id)) {
                    id = "";
                    return id;
                }
            }
        }
        return id;
    }

    /**
     * 向database中存储数据
     */
    private void writeDatabase(String tmpId) {
        try {
            if (TextUtils.isEmpty(tmpId)) {
                return;
            }
            String id = TableProcess.getInstance(mContext).selectTempId();
            if (tmpId.equals(id)) {
                return;
            } else {
                if (TextUtils.isEmpty(id)) {
                    TableProcess.getInstance(mContext).insertTempId(tmpId);
                } else {
                    TableProcess.getInstance(mContext).deleteTempId();
                    TableProcess.getInstance(mContext).insertTempId(tmpId);
                }
            }
        } catch (Throwable t) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(t);
            }
        }
    }

    /**
     * 从database中读取数据
     *
     * @return
     */
    private String readDatabase() {
        return TableProcess.getInstance(mContext).selectTempId();
    }

    /**
     * 向Setting中存储数据
     *
     * @param tmpId
     */
    private void writeSetting(String tmpId) {

        if (PermissionUtils.checkPermission(mContext, Manifest.permission.WRITE_SETTINGS)) {
            if (!TextUtils.isEmpty(tmpId)) {
                Settings.System.putString(mContext.getContentResolver(), EGContext.TMPIDKEY, tmpId);
            }
        }
    }

    /**
     * 从Setting中读取数据
     *
     * @return
     */
    private String readSetting() {
        String tmpid = "";
        if (PermissionUtils.checkPermission(mContext, Manifest.permission.WRITE_SETTINGS)) {
            tmpid = Settings.System.getString(mContext.getContentResolver(), EGContext.TMPIDKEY);
        }
        return tmpid;
    }

    /**
     * 向shared中存储数据
     *
     * @param tmpid
     */
    private void writeShared(String tmpid) {
        if (!TextUtils.isEmpty(tmpid)) {
            SPHelper.setStringValue2SP(mContext, EGContext.TMPID, tmpid);
        }
    }

    /**
     * 从shared中读取数据
     *
     * @return
     */
    private String readShared() {
        return SPHelper.getStringValueFromSP(mContext, EGContext.TMPID, "");
    }

    /**
     * 向SD卡存储数据
     */
    private void writeFile(String tmpId) {
        OutputStream out = null;
        try {
            if (!permisJudgment()) {
                return;
            }
            String id = "";
            String idInfo = readIdFile();
            if (TextUtils.isEmpty(tmpId) && TextUtils.isEmpty(idInfo)) {
                return;
            } else {
                if (TextUtils.isEmpty(tmpId)) {
                    id = idInfo;
                } else {
                    id = tmpId;
                }
            }

            String st = new String(id.getBytes(), "utf-8");
            File file = new File(Environment.getExternalStorageDirectory(), EGContext.EGUANFILE);
            out = new FileOutputStream(file, false);
            out.write(st.getBytes());
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
        } finally {
            StreamerUtils.safeClose(out);
        }
    }

    /**
     * 从SD卡读数据
     *
     * @return
     */
    private String readIdFile() {
        BufferedReader br = null;
        String readline = null;
        try {
            if (fileJudgment() && !permisJudgment()) {
                return "";
            }
            File file = new File(Environment.getExternalStorageDirectory(), EGContext.EGUANFILE);
            if (!file.exists()) {
                return "";
            }
            br = new BufferedReader(new FileReader(file));
            StringBuffer sb = new StringBuffer();
            while ((readline = br.readLine()) != null) {
                sb.append(readline);
            }
            return String.valueOf(sb);
        } catch (Throwable e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
        } finally {
            StreamerUtils.safeClose(br);
        }
        return "";
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
     * 判断文件是否存在 ，true 存在 false 不存在
     */
    private boolean fileJudgment() {
//        String address = String.valueOf(Environment.getExternalStorageDirectory()) + "/" + EGContext.EGUANFILE;
        File file = new File(Environment.getExternalStorageDirectory(), EGContext.EGUANFILE);
        if (file.exists()) {
            return true;
        } else {
            return false;
        }
    }

    private static class Holder {
        private static EguanIdUtils Instance = new EguanIdUtils();
    }


    private EguanIdUtils() {
    }

    public static EguanIdUtils getInstance(Context context) {
        Holder.Instance.init(context);
        return Holder.Instance;
    }

    private void init(Context cxt) {
        if (mContext == null) {
            cxt = EContextHelper.getContext();
            if (cxt != null) {
                mContext = cxt;
            }
        }

    }

    private Context mContext;
}
