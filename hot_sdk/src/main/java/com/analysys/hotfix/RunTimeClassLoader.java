package com.analysys.hotfix;

import dalvik.system.DexClassLoader;

public class RunTimeClassLoader extends DexClassLoader {
    private String path;

    public RunTimeClassLoader(String dexPath, String optimizedDirectory, String librarySearchPath, ClassLoader parent) {
        super(dexPath, optimizedDirectory, librarySearchPath, parent);
        this.path = dexPath;
    }

    public String getPath() {
        return path;
    }
}
