package com.luizcarlos.sunshine.service;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.luizcarlos.sunshine.App;
import com.luizcarlos.sunshine.R;
import com.luizcarlos.sunshine.controller.ControllerSaveData;
import com.luizcarlos.sunshine.utils.LogUtils;
import com.luizcarlos.sunshine.utils.Utils;

public class SunshineService extends IntentService {

    public static final String LOCATION_QUERY_EXTRA = "lqe";
    private static final String LOG_TAG = SunshineService.class.getSimpleName();
    public Controller controller = new Controller();
    public boolean enable;

    public SunshineService() {
        super("SunshineService");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return controller;
    }

    @Override
    public void onCreate() {
        LogUtils.info(LOG_TAG, "on-create");
        super.onCreate();
        enable = true;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        LogUtils.info(LOG_TAG, "on-start: " + startId);
    }

    @Override
    protected void onHandleIntent(final Intent intent) {

        String location = intent.getExtras().getString( App.getApplication().getString( R.string.pref_location_key ) );
        if ( location != null ) {

            String json = Utils.loadDataWether(location);

            LogUtils.info( LOG_TAG, "data: " + json );
            controller.controllerSaveData.stringToJson( json );
            //controller.forecastFragment.updateAdapter( weatherDataFromJson );
        }
        //enable = false;
    }

    public class Controller extends Binder
    {
        public SunshineService getService(){ return SunshineService.this;  }
        public ControllerSaveData controllerSaveData;
    }

    public static class AlarmReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Intent sendIntent = new Intent(context, SunshineService.class);
            sendIntent.putExtra(SunshineService.LOCATION_QUERY_EXTRA, intent.getStringExtra(SunshineService.LOCATION_QUERY_EXTRA));
            context.startService(sendIntent);
        }
    }

}
