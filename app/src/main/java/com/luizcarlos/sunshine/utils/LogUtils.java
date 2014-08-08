package com.luizcarlos.sunshine.utils;

import android.util.Log;

/**
 * Created by luizcarlos on 25/07/14.
 */
public class LogUtils
{

    public static void logInfo( String tag, String message ){
        Log.i( tag, message );
    }

    public static void logError( String tag, String error ) { Log.e( tag, error ); }


}
