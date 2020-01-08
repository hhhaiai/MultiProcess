package com.device.utils;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class AssetsHelper {


    public static boolean copyFileFromAssets(Context context, String assetName, String path) {
        boolean result = false;
        InputStream is = null;
        FileOutputStream fos = null;
        try {
            File file = new File(path);
            if (!file.getParentFile().exists() || !file.getParentFile().isDirectory()) {
                file.getParentFile().mkdirs();
            }
            is = context.getAssets().open(assetName);
            fos = new FileOutputStream(file);
            byte[] temp = new byte[64];
            int i = 0;
            while ((i = is.read(temp)) > 0) {
                fos.write(temp, 0, i);
            }
            fos.flush();
            result = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
    //已流的方式读取
    public static String getFromAssetsToString(Context context, String fileName) {
        StringBuilder stringBuilder = new StringBuilder();
        InputStream is = null;
        InputStreamReader inputReader = null;
        BufferedReader bufReader = null;
        try {
            is = context.getAssets().open(fileName);
//            InputStreamReader inputReader = new InputStreamReader(context.getResources().getAssets().open(fileName));
            inputReader = new InputStreamReader(is);
            bufReader = new BufferedReader(inputReader);
            String line = "";
            while ((line = bufReader.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (Throwable e) {
            EL.e(e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
            if (bufReader != null) {
                try {
                    bufReader.close();
                } catch (IOException e) {
                }
            }
            if (inputReader != null) {
                try {
                    inputReader.close();
                } catch (IOException e) {
                }
            }
        }
        return stringBuilder.toString();
    }

    public static String getFromAssetsToMulitiLine(Context context, String fileName) {
        StringBuilder stringBuilder = new StringBuilder();
        InputStream is = null;
        InputStreamReader inputReader = null;
        BufferedReader bufReader = null;
        try {
            is = context.getAssets().open(fileName);
//            InputStreamReader inputReader = new InputStreamReader(context.getResources().getAssets().open(fileName));
            inputReader = new InputStreamReader(is);
            bufReader = new BufferedReader(inputReader);
            String line = "";
            while ((line = bufReader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
        } catch (Throwable e) {
            EL.e(e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
            if (bufReader != null) {
                try {
                    bufReader.close();
                } catch (IOException e) {
                }
            }
            if (inputReader != null) {
                try {
                    inputReader.close();
                } catch (IOException e) {
                }
            }
        }
        return stringBuilder.toString();
    }
}
