package com.eguan.monitor.commonutils;

import android.Manifest;
import android.content.Context;
import android.os.Environment;
import android.provider.Settings;
import android.text.TextUtils;

import com.eguan.monitor.Constants;
import com.eguan.monitor.dbutils.device.DeviceTableOperation;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Wang on 2017/2/21.
 */

public class EguanIdUtils {

    private final String EGUANFILE = "eg.a";
    private final String EGIDKEY = "egid";
    private final String TMPIDKEY = "tmpid";

    private static EguanIdUtils instance;
    private Context context;
    private SPUtil spUtil;

    public static EguanIdUtils getInstance(Context context) {
        if (instance == null) {
            synchronized (EguanIdUtils.class) {
                if (instance == null) {
                    instance = new EguanIdUtils(context);
                }
            }
        }
        return instance;
    }

    private EguanIdUtils(Context context) {
        this.context = context.getApplicationContext();
        spUtil = SPUtil.getInstance(context);
    }

    /**
     * 获取临时id和易观id
     *
     * @return
     */
    public List<String> getId() {

        List<String> file = readFile();
        List<String> setting = readSetting();
        List<String> shard = readShared();
        List<String> database = readDatabase();

        List<String> list = new ArrayList<>();
        String egId = getContrastId(file, setting, shard, database, EGIDKEY);
        String tmpId = getContrastId(file, setting, shard, database, TMPIDKEY);

        if (TextUtils.isEmpty(tmpId)) {
            return list;
        }
        list.add(tmpId);
        list.add(egId);


        return list;
    }

    /**
     * 获取临时id或egid
     *
     * @param key
     * @return
     */
    private String getContrastId(List<String> fileId, List<String> settingId,
                                 List<String> shardId, List<String> databaseId, String key) {

        List<String> list = new ArrayList<>();
        String id = "";
        if (fileId.size() == 2) {
            if (EGIDKEY.equals(key)) {
                id = fileId.get(0);
                if (!TextUtils.isEmpty(id)) {
                    list.add(id);
                }
            } else {
                id = fileId.get(1);
                if (!TextUtils.isEmpty(id)) {
                    list.add(id);
                }
            }
        }
        if (settingId.size() == 2) {
            if (EGIDKEY.equals(key)) {
                id = settingId.get(0);
                if (!TextUtils.isEmpty(id)) {
                    list.add(id);
                }
            } else {
                id = settingId.get(1);
                if (!TextUtils.isEmpty(id)) {
                    list.add(id);
                }
            }
        }
        if (shardId.size() == 2) {
            if (EGIDKEY.equals(key)) {
                id = shardId.get(0);
                if (!TextUtils.isEmpty(id)) {
                    list.add(id);
                }
            } else {
                id = shardId.get(1);
                if (!TextUtils.isEmpty(id)) {
                    list.add(id);
                }
            }
        }
        if (databaseId.size() == 2) {
            if (EGIDKEY.equals(key)) {
                id = databaseId.get(0);
                if (!TextUtils.isEmpty(id)) {
                    list.add(id);
                }
            } else {
                id = databaseId.get(1);
                if (!TextUtils.isEmpty(id)) {
                    list.add(id);
                }
            }
        }
        if (list.size() < 2) {
            if (list.size() == 0) {
                return "";
            } else {
                return list.get(0);
            }
        } else {
            for (int i = 1; i < list.size(); i++) {

                if (!list.get(0).equals(list.get(i))) {
                    return "";
                }
            }
            return list.get(0);
        }
    }

    /**
     * 更新易观id和临时id
     */
    public void setId(String json) {

        try {
            String tmpId = "", egid = "";
            JSONObject jsonObject = new JSONObject(json);
            if (jsonObject.has(TMPIDKEY)) {

                tmpId = jsonObject.getString(TMPIDKEY);
            }
            if (jsonObject.has(EGIDKEY)) {

                egid = jsonObject.getString(EGIDKEY);
            }
            if (!TextUtils.isEmpty(tmpId) || !TextUtils.isEmpty(egid)) {
                writeFile(egid, tmpId);
                writeShared(egid, tmpId);
                writeSetting(egid, tmpId);
                writeDatabase(egid, tmpId);
            }
        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        }
    }

//    public void deleteEguanId() {
//        EGThreadPool.pushDB(new Runnable() {
//            @Override
//            public void run() {
//                writeFile("");
//                writeSetting("");
//                writeShared("");
//                TableOperation.getInstance(context).deleteEguanId();
//            }
//        });
//    }

    /**
     * 向database中存储数据
     */
    private void writeDatabase(String egId, String tmpId) {

        DeviceTableOperation tabOpe = DeviceTableOperation.getInstance(context);
        if (!TextUtils.isEmpty(egId)) {
            tabOpe.insertEguanId(egId);
        }
        if (!TextUtils.isEmpty(tmpId)) {
            tabOpe.insertTmpId(tmpId);
        }
    }

    /**
     * 从database中读取数据
     *
     * @return
     */
    private List<String> readDatabase() {
        DeviceTableOperation tabOpe = DeviceTableOperation.getInstance(context);
        List<String> list = new ArrayList<>();
        list.add(tabOpe.selectEguanId());
        list.add(tabOpe.selectTmpId());
        return list;
    }

    /**
     * 向Setting中存储数据
     *
     * @param egId
     */
    private void writeSetting(String egId, String tmpId) {

        if (SystemUtils.checkPermission(context, Manifest.permission.WRITE_SETTINGS)) {

            if (!TextUtils.isEmpty(egId)) {

                Settings.System.putString(context.getContentResolver(), EGIDKEY, egId);
            }
            if (!TextUtils.isEmpty(tmpId)) {

                Settings.System.putString(context.getContentResolver(), TMPIDKEY, tmpId);
            }
        }
    }

    /**
     * 从Setting中读取数据
     *
     * @return
     */
    private List<String> readSetting() {
        List<String> list = new ArrayList<>();
        if (SystemUtils.checkPermission(context, Manifest.permission.WRITE_SETTINGS)) {
            String egid = Settings.System.getString(context.getContentResolver(), EGIDKEY);
            String tmpid = Settings.System.getString(context.getContentResolver(), TMPIDKEY);

            if (!TextUtils.isEmpty(egid)) {
                list.add(egid);
            } else {
                list.add("");
            }
            if (!TextUtils.isEmpty(tmpid)) {
                list.add(tmpid);
            } else {
                list.add("");
            }
        }
        return list;
    }

    /**
     * 向shared中存储数据
     *
     * @param eguanId
     */
    private void writeShared(String eguanId, String tmpid) {

        if (!TextUtils.isEmpty(eguanId)) {
            spUtil.setEguanId(eguanId);
        }
        if (!TextUtils.isEmpty(tmpid)) {
            spUtil.setTmpId(tmpid);
        }
    }

    /**
     * 从shared中读取数据
     *
     * @return
     */
    private List<String> readShared() {
        List<String> list = new ArrayList<>();
        list.add(spUtil.getEguanId());
        list.add(spUtil.getTmpId());
        return list;
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
            File file = new File(Environment.getExternalStorageDirectory(), EGUANFILE);
            OutputStream out = new FileOutputStream(file, false);
            out.write(st.getBytes());
            out.close();
        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
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
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
        }
        return list;
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
            return sb.toString();
        } catch (Throwable e) {
            if (Constants.FLAG_DEBUG_INNER) {
                EgLog.e(e);
            }
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
        return en.equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * 判断文件是否存在 ，true 存在 false 不存在
     */
    private boolean fileJudgment() {
        String address = Environment.getExternalStorageDirectory().toString() + "/" + EGUANFILE;
        File file = new File(address);
        return file.exists();
    }
}
