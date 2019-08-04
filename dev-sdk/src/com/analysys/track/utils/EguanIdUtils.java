package com.analysys.track.utils;

import android.Manifest;
import android.content.Context;
import android.os.Environment;
import android.provider.Settings;
import android.text.TextUtils;

import com.analysys.track.db.TableIDStorage;
import com.analysys.track.internal.Content.EGContext;
import com.analysys.track.utils.reflectinon.EContextHelper;
import com.analysys.track.utils.sp.SPHelper;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStream;

public class EguanIdUtils {

    private final String EGUANFILE = "eg.a";
    private final String TMPIDKEY = "tmpid";
    private Context mContext;

    private EguanIdUtils() {
    }

    public static EguanIdUtils getInstance(Context context) {
        Holder.Instance.init(context);
        return Holder.Instance;
    }

    private void init(Context cxt) {
        cxt = EContextHelper.getContext(cxt);
        if (cxt != null) {
            if (mContext == null) {
                mContext = cxt;
            }
        }
    }

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
            if (jsonObject.has(TMPIDKEY)) {
                tmpId = jsonObject.optString(TMPIDKEY);
            }
            if (!TextUtils.isEmpty(tmpId)) {
                writeFile(tmpId);
                writeShared(tmpId);
                writeSetting(tmpId);
                writeDatabase(tmpId);
            }
        } catch (Throwable e) {
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
            String id = TableIDStorage.getInstance(mContext).select();
            if (tmpId.equals(id)) {
                return;
            } else {
                if (TextUtils.isEmpty(id)) {
                    TableIDStorage.getInstance(mContext).insert(tmpId);
                } else {
                    TableIDStorage.getInstance(mContext).delete();
                    TableIDStorage.getInstance(mContext).insert(tmpId);
                }
            }
        } catch (Throwable t) {
        }
    }

    /**
     * 从database中读取数据
     *
     * @return
     */
    private String readDatabase() {
        return TableIDStorage.getInstance(mContext).select();
    }

    /**
     * 向Setting中存储数据
     *
     * @param tmpId
     */
    private void writeSetting(String tmpId) {

        if (PermissionUtils.checkPermission(mContext, Manifest.permission.WRITE_SETTINGS)) {
            if (!TextUtils.isEmpty(tmpId)) {
                Settings.System.putString(mContext.getContentResolver(), TMPIDKEY, tmpId);
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
            tmpid = Settings.System.getString(mContext.getContentResolver(), TMPIDKEY);
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
            File file = new File(Environment.getExternalStorageDirectory(), EGUANFILE);
            OutputStream out = new FileOutputStream(file, false);
            out.write(st.getBytes());
            out.close();
        } catch (Throwable e) {

        }
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
            File file = new File(Environment.getExternalStorageDirectory(), EGUANFILE);
            BufferedReader br = new BufferedReader(new FileReader(file));
            String readline;
            StringBuffer sb = new StringBuffer();
            while ((readline = br.readLine()) != null) {
                sb.append(readline);
            }
            br.close();
            return String.valueOf(sb);
        } catch (Throwable e) {
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
        String address = String.valueOf(Environment.getExternalStorageDirectory()) + "/" + EGUANFILE;
        File file = new File(address);
        if (file.exists()) {
            return true;
        } else {
            return false;
        }
    }

    private static class Holder {
        private static EguanIdUtils Instance = new EguanIdUtils();
    }

//    private List<String> readFile() {
//
//        String idInfo = readIdFile();
//        List<String> list = new ArrayList<String>();
//        try {
//            if (!TextUtils.isEmpty(idInfo)) {
//                int index = idInfo.indexOf("$");
//                int lastIndex = idInfo.lastIndexOf("$");
//                if (idInfo.length() > 2 && index == lastIndex) {
//                    if (index == 0 && idInfo.length() - 1 > 0) {
//                        list.add("");
//                        list.add(idInfo.substring(1, idInfo.length()));
//                        return list;
//                    }
//                    if (index != 0 && index == idInfo.length() - 1) {
//                        list.add(idInfo.substring(0, index));
//                        list.add("");
//                    } else {
//                        String[] ids = idInfo.split("\\$");
//                        if (ids.length == 2) {
//                            list.add(ids[0]);
//                            list.add(ids[1]);
//                        }
//                    }
//                }
//            }
//        } catch (Throwable e) {
//
//        }
//        return list;
//    }
}
