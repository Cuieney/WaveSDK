package com.feetsdk.android.common.utils;

import android.util.Log;

/**
 * Created by cuieney on 16/11/11.
 */
public class Logger {
    private static Logger logger;
    private static boolean debug = true;
    private static String tag = "logger";
    public Logger() {
    }

    public static Logger getInstance() {
        synchronized (Logger.class) {
            if (logger == null) {
                logger = new Logger();
            }
        }
        return logger;
    }


    public static void d(String tag,String msg){
        if (debug) {
            Log.d(tag,msg);
        }
    }

    public static void i(String tag,String msg){
        if (debug) {
            Log.i(tag,msg);
        }
    }

    public static void e(String tag,String msg){
        if (debug) {
            Log.e(tag,msg);
        }
    }

    public static void d(String msg){
        if (debug) {
            Log.d(tag,msg);
        }
    }

    public static void i(String msg){
        if (debug) {
            Log.i(tag,msg);
        }
    }

    public static void e(String msg){
        if (debug) {
            Log.e(tag,msg);
        }
    }
}

