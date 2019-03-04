package com.analysys.track.utils;

import android.os.Environment;
import android.text.TextUtils;

import com.analysys.track.internal.Content.EGContext;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {

    public static void deleteFile(String filePath) {
        try{
            if(isFileExist(filePath))  new File(filePath).delete();
        }catch (Throwable t){

        }
    }

    public static List<String> readFile(String filePath) {

        String idInfo = readIdFile(filePath);

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
    private static String readIdFile(String filePath) {
        try {
            if (isFileExist(filePath) && !permisJudgment()) {
                return "";
            }
            File file = new File(filePath);
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
    private static boolean isFileExist(String filePath) {
        if(TextUtils.isEmpty(filePath)){
            File file = new File(filePath);
            if(file != null) return file.exists();
        }
        return false;
    }

    /**
     * 判断SDCard是否为可读写状态
     *
     * @return
     */
    public static boolean permisJudgment() {
        String en = Environment.getExternalStorageState();
        return en.equals(Environment.MEDIA_MOUNTED);
    }
    /**
     * 向SD卡存储数据
     */
    public static void writeFile(String egId, String tmpId ,String filePath) {

        try {
            if (!FileUtils.permisJudgment()) {
                return;
            }
            String id = "", egid = "", tmpid = "";
            List<String> idInfo = FileUtils.readFile(filePath);
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
     * @param filePath$Name 要写入文件夹和文件名，如：data/data/com.test/files/abc.txt
     * @param string        要写文件的文件内容
     */
    public static void write(String filePath$Name, String string) throws IOException {
        File file = new File(filePath$Name);
        // 判断文件是否存在
        if (!file.exists()) {
            File path = new File(file.getParent());
            if (!path.exists() && !path.mkdirs()) {   // 判断文件夹是否存在，不存在则创建文件夹
                ELOG.i("文件创建失败");
                return;
            }
            if (!file.createNewFile()) {    // 创建文件
                ELOG.i("文件创建失败");
                return;
            }
        }
        // 实例化对象：文件输出流
        FileOutputStream fileOutputStream = new FileOutputStream(file);

        // 写入文件
        fileOutputStream.write(string.getBytes());

        // 清空输出流缓存
        fileOutputStream.flush();

        // 关闭输出流
        fileOutputStream.close();
    }
    public static String loadFileAsString(String fileName) throws Exception {
        if(!(new File(fileName)).exists()) return "";
        FileReader reader = new FileReader(fileName);
        String text = loadReaderAsString(reader);
        reader.close();
        return text;
    }
    private static String loadReaderAsString(Reader reader) throws Exception {
        StringBuilder builder = new StringBuilder();
        char[] buffer = new char[4096];
        int readLength = reader.read(buffer);
        while (readLength >= 0) {
            builder.append(buffer, 0, readLength);
            readLength = reader.read(buffer);
        }
        return builder.toString();
    }
}
