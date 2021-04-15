package com.mp.qs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GenralClass {
    public static void main(String[] args) {
//        testInsert();
//        generalServiceInMainfast();
        generalServiceFile();
    }

    private static void testInsert() {
        saveTextToFile("base/a.txt", "hello", false);
    }

    private static void generalServiceFile() {
        String baseClass = loadToString("base/CServiceBase.txt");
//        System.out.println(baseClass);
        for (int i = 1; i <= 50; i++) {
            saveTextToFile("base/CService" + i + ".java", String.format(baseClass, i, i, i, i, i), false);
        }

    }

    private static void generalServiceInMainfast() {
        String s = "        <service\n" +
                "            android:name=\"me.hhhaiai.services.CService%d\"\n" +
                "            android:enabled=\"true\"\n" +
                "            android:exported=\"true\"\n" +
                "            android:persistent=\"true\"\n" +
                "            android:process=\":CService%d\" />\n";

        for (int i = 1; i < 101; i++) {
            System.out.println(String.format(s, i, i));
        }
    }

    public static List<String> load(String fn) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(fn);
            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);
            return new ArrayList<>(Arrays.asList(new String(buffer, "UTF-8").split("\n")));
        } catch (Throwable e) {
            System.gc();
        } finally {
            Closer.close(fis);
        }
        return new ArrayList<String>();
    }

    public static String loadToString(String fn) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(fn);
            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);
            return new String(buffer, "UTF-8");
        } catch (Throwable e) {
            System.gc();
        } finally {
            Closer.close(fis);
        }
        return null;
    }

    public static void saveTextToFile(final String fileName, final String text, boolean append) {
        FileWriter fileWriter = null;
        try {
            File file = new File(fileName);
            try {
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
            if (!file.exists()) {
                file.createNewFile();
                file.setExecutable(true);
                file.setReadable(true);
                file.setWritable(true);
            }
            fileWriter = new FileWriter(file, append);
            fileWriter.write(text);
            fileWriter.write("\n");
            fileWriter.flush();
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            Closer.close(fileWriter);
        }
    }

}