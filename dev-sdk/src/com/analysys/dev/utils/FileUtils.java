package com.analysys.dev.utils;

import android.os.Environment;
import android.text.TextUtils;

import com.analysys.dev.internal.Content.EGContext;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {

    public static void deleteFile(String filePath) {
        try{
            if (!TextUtils.isEmpty(filePath)) {
                File result = new File(filePath);
                if (result != null) {
                    if (result.exists()) {
                        result.delete();
                    }
                }
            }
        }catch (Throwable t){

        }
    }
    public static List<String> readFile() {

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
     * 从SD卡读数据
     *
     * @return
     */
    private static String readIdFile() {
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
    private static boolean fileJudgment() {
        String address = Environment.getExternalStorageDirectory().toString() + "/" + EGContext.EGUANFILE;
        File file = new File(address);
        if (file.exists()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 判断SDCard是否为可读写状态
     *
     * @return
     */
    public static boolean permisJudgment() {
        String en = Environment.getExternalStorageState();
        if (en.equals(Environment.MEDIA_MOUNTED)) {
            return true;
        }
        return false;
    }

}
