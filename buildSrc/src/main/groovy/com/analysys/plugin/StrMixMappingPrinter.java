package com.analysys.plugin;

import com.android.ddmlib.Log;

import org.apache.commons.io.IOUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public final class StrMixMappingPrinter {

    private File mMappingFile;
    private BufferedWriter mWriter;

    private String mCurrentClassName;


    public StrMixMappingPrinter(File mappingFile) {
        this.mMappingFile = mappingFile;
    }


    public void startMappingOutput() {
        try {
            if (mMappingFile.exists() && !mMappingFile.delete()) {
                throw new IOException();
            }
            File dir = mMappingFile.getParentFile();
            if (dir.exists() || dir.mkdirs()) {
                mWriter = new BufferedWriter(new FileWriter(mMappingFile));
            } else {
                throw new IOException();
            }
        } catch (IOException e) {
            Log.e("StrMixMappingPrinter", "Create ReplaceStrMix mapping file failed.");
        }
    }


    public void ouputInfo(String key, String implementation) {
        try {
            mWriter.write("ReplaceStrMix key : " + key);
            mWriter.newLine();
            mWriter.write("ReplaceStrMix impl: " + implementation);
            mWriter.newLine();
        } catch (IOException e) {

        }
    }


    public void output(String className, String originValue, String encryptValue) {
        if (TextUtils.isEmpty(className)) {
            return;
        }
        try {
            if (!className.equals(mCurrentClassName)) {
                mWriter.newLine();
                mWriter.write("[" + className + "]");
                mWriter.newLine();
                mCurrentClassName = className;
            }
            mWriter.write(originValue + " -> " + encryptValue);
            mWriter.newLine();
        } catch (IOException e) {

        }
    }


    public void endMappingOutput() {
        if (mWriter != null) {
            IOUtils.closeQuietly(mWriter);
        }
    }

}
