package com.analysys.dev.utils;

import android.text.TextUtils;

import java.io.File;

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
}
