package com.mirea.kt.ribo;

import android.util.Log;

public class Logger {
    public static void Info(String msg) {
        Log.d(Config.LOGGER_TAG, msg);
    }

    public static void Error(String msg) {
        Log.e(Config.LOGGER_TAG, msg);
    }
}
