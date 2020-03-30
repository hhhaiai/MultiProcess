package com.analysys.track.utils.memorydex;

import android.content.Context;
import android.os.Build;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public class ClassLoaderFactory {
    public static ClassLoader getClassLoader(Context context, File dexFile) {
        ClassLoader classLoader = null;
        try {
            FlipDelegateClassLoader.Callback callback = new FlipDelegateClassLoader.Callback() {
                @Override
                public void onLoadClass(ClassLoader loader, String name) {

                }

                @Override
                public void onNotFound(String name) {

                }
            };
            if (Build.VERSION.SDK_INT >= 26) {
                //内存解密加载
                ByteBuffer buffer = DexFileEncryptUtils.file2ByteBuffer(dexFile);
                DexFileEncryptUtils.decode(buffer);
                classLoader = new FlipDelegateClassLoader(context, buffer, context.getClassLoader().getParent(), callback);
            } else {
                //解密后存文件加载
                ByteBuffer buffer = DexFileEncryptUtils.file2ByteBuffer(dexFile);
                DexFileEncryptUtils.decode(buffer);
                File newDexFile = new File(dexFile.getParent(), dexFile.getName().replace(".png", ".dex"));
                DexFileEncryptUtils.saveByteBuffer(buffer, newDexFile);
                classLoader = new FlipDelegateClassLoader(context, newDexFile.getAbsolutePath(), null, null,
                        context.getClassLoader().getParent(), callback);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return classLoader;
    }
}
