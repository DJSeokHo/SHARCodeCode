package com.swein.sharcodecode.framework.util.debug;

import android.util.Log;

import com.swein.sharcodecode.BuildConfig;

public class ILog {

    private static String HEAD = "[- ILog Print -] ";
    private static String TAG = " ||===>> ";

    public static void iLogDebug(String tag, Object content) {
        if (BuildConfig.DEBUG) {
            Log.d(HEAD + TAG + tag, String.valueOf(content));
        }
    }

    public static void iLogInfo(String tag, Object content) {
        if (BuildConfig.DEBUG) {
            Log.i(HEAD + TAG + tag, String.valueOf(content));
        }
    }

    public static void iLogError(String tag, Object content) {
        if (BuildConfig.DEBUG) {
            Log.e(HEAD + TAG + tag, String.valueOf(content));
        }
    }

    public static void iLogWarn(String tag, Object content) {
        if (BuildConfig.DEBUG) {
            Log.w(HEAD + TAG + tag, String.valueOf(content));
        }
    }
}
