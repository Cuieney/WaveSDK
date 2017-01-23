package com.feetsdk.android.common.utils;

import java.io.File;

public class LocalPathResolver {

    private static final String TAG = LocalPathResolver.class.getSimpleName();

    private static final String BASE_DIR = "/feet/Music/";
    private static final String BASE_IMG_DIR = "/feet/img/";
    private static String base;

    public static void init(String base) {
        LocalPathResolver.base = base;
    }

    public static String getDir() {
        String path = base + BASE_DIR;
        createPath(path);

        return path;
    }

    public static String getImgDir(){
        String path = base + BASE_IMG_DIR;
        createPath(path);

        return path;
    }


    private static void createPath(String path) {
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

}
