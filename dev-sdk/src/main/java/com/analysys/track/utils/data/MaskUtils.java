package com.analysys.track.utils.data;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;

import com.analysys.track.BuildConfig;
import com.analysys.track.internal.content.EGContext;
import com.analysys.track.utils.BugReportForTest;
import com.analysys.track.utils.EContextHelper;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

/**
 * @Copyright 2020 analysys Inc. All rights reserved.
 * @Description: dex 文件伪装存储
 * @Version: 1.0
 * @Create: 2020-08-24 14:42:35
 * @author: miqt
 * @mail: miqingtang@analysys.com.cn
 */
public class MaskUtils {
    /**
     * 获取当前运行app的图标png数据，非png会转为png
     */
    private static byte[] getIconPngBytes() {
        try {
            PackageManager packageManager = EContextHelper.getContext().getApplicationContext().getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(EContextHelper.getContext().getPackageName(), 0);
            Drawable icon = applicationInfo.loadIcon(packageManager);
            Bitmap bitmap;
            //api 26+ 自适配图标adaptive-icon
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                    && icon instanceof AdaptiveIconDrawable) {
                bitmap = Bitmap.createBitmap(60,
                        60, Bitmap.Config.RGB_565);
                Canvas canvas = new Canvas(bitmap);
                icon.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                icon.draw(canvas);
            } else {
                Bitmap db = ((BitmapDrawable) icon).getBitmap();
                bitmap = Bitmap.createScaledBitmap(db, 60, 60, true);
            }
            if (bitmap == null) {
                return null;
            }
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            bitmap.recycle();
            byte[] bytes = outputStream.toByteArray();
            outputStream.close();
            return bytes;
        } catch (Throwable e) {
        }
        return null;
    }

    private static void combinedSave(File outFile, byte[] png, byte[] dex) {
        if (outFile == null) {
            return;
        }
        if (png == null) {
            return;
        }
        if (dex == null) {
            return;
        }
        try {
            // save dex.png = bm + 固定code + dexFile
            int count = dex.length + png.length;
            ByteBuffer buffer = ByteBuffer.allocate(count);
            //png
            buffer.put(png);
            //dex
            buffer.put(dex);
            buffer.rewind();
            File dir = outFile.getParentFile();
            if (dir == null) {
                return;
            }
            if (!dir.exists() || !dir.isDirectory() ) {
              boolean result =   dir.mkdirs();
            }
            //完全覆盖模式
            FileChannel fileChannel = new FileOutputStream(outFile,false).getChannel();
            fileChannel.write(buffer);
            fileChannel.close();
        } catch (Throwable e) {
        }
    }

    /**
     * 给一个文件带上app icon 的面具
     * @param outfile
     * @param dexData
     */
    public static void wearMask(File outfile, byte[] dexData) {
        try {
            combinedSave(outfile, getIconPngBytes(), dexData);
        } catch (Throwable e) {
        }
    }

    /**
     * 帮一个文件摘下面具
     * @param inFile
     * @return
     */
    public static byte[] takeOffMask(File inFile) {
        try {
            byte[] pngHead = new byte[]{-119, 80, 78, 71, 13, 10, 26, 10};
            byte[] pngEnd = new byte[]{73, 69, 78, 68, -82, 66, 96, -126};
            if (!(inFile.exists() && inFile.isFile()
                    && inFile.length() > pngHead.length + pngEnd.length)) {
                return null;
            }
            FileInputStream inputStream = new FileInputStream(inFile);
            BufferedInputStream stream = new BufferedInputStream(inputStream);
            byte[] data = new byte[pngHead.length];
            int result = stream.read(data);
            //验证头部
            if (!(result == pngHead.length && Arrays.equals(data, pngHead))) {
                return null;
            }
            //验证尾部
            long position = result;
            byte[] curr = new byte[1];
            //kmp索引
            int endIndex = 0;
            while (true) {
                result = stream.read(curr);
                position += result;
                //读到文件末尾都没找到图片尾部节点
                if (result == -1) {
                    return null;
                }
                if (curr[0] == pngEnd[endIndex]) {
                    endIndex++;
                    if (endIndex == pngEnd.length) {
                        //找到了png格式文件尾部
                        break;
                    }
                } else {
                    endIndex = 0;
                    if (curr[0] == pngEnd[endIndex]) {
                        endIndex++;
                    }
                }
            }
            //取得dex加密元数据
            byte[] bytes = new byte[(int) (inFile.length() - position)];
            result = stream.read(bytes);
            if (result != -1) {
                return bytes;
            }
        } catch (Throwable e) {
            if(BuildConfig.ENABLE_BUG_REPORT){
                BugReportForTest.commitError(e);
            }
        }
        return null;
    }
}
