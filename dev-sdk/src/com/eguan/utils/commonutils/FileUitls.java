//package com.eguan.utils.commonutils;
//
//import java.io.BufferedReader;
//import java.io.FileReader;
//import java.io.IOException;
//
///**
// * Created on 17/5/26. Author : chris Email : mengqi@analysys.com.cn Detail :
// */
//
//public class FileUitls {
//
//    public static String getContentFromFile(String filePath) {
//        StringBuffer sb = new StringBuffer();
//        FileReader mFileReader = null;
//        BufferedReader mBufferedReader = null;
//        try {
//            mFileReader = new FileReader(filePath);
//            if (mFileReader != null) {
//                mBufferedReader = new BufferedReader(mFileReader, 1024);
//                String cache = null;
//                while ((cache = mBufferedReader.readLine()) != null) {
//                    sb.append(cache);
//                }
//            }
//        } catch (Throwable e) {
//        } finally {
//            try {
//                if (mFileReader != null) {
//                    mFileReader.close();
//                }
//                if (mBufferedReader != null) {
//                    mBufferedReader.close();
//                }
//            } catch (IOException e) {
//            }
//        }
//        return sb.toString();
//    }
//}
