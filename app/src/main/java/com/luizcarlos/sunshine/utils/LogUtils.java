package com.luizcarlos.sunshine.utils;

import android.util.Log;

/**
 * Created by luizcarlos on 25/07/14.
 */
public class LogUtils
{

    public static void info(String tag, String message){
        Log.i( tag, message );
    }

    public static void error(String tag, String error) { Log.e( tag, error ); }


}
