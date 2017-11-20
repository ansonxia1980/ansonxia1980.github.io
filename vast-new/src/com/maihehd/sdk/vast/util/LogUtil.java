package com.maihehd.sdk.vast.util;

import android.util.Log;

/**
 * Created by Roger on 16/1/6.
 */
public class LogUtil {

    public static Boolean logEnabled = true;
    public static String logTag = null;

    public static void d(String tag, String message){
        if (!logEnabled) {
            return;
        }

        if (logTag != null){
            message = tag + " => " + message;
            tag = logTag;
        }

        Log.d(tag, message);
    }
}
