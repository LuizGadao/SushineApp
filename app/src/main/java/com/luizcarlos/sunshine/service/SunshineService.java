package com.luizcarlos.sunshine.service;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;

import com.luizcarlos.sunshine.App;
import com.luizcarlos.sunshine.R;
import com.luizcarlos.sunshine.controller.ControllerSaveData;
import com.luizcarlos.sunshine.utils.LogUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SunshineService extends IntentService {
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

        /*new Thread(){
            @Override
            public void run() {

                try {
                    Thread.sleep(1000 * 5);

                    String location = intent.getExtras().getString( App.getApplication().getString( R.string.pref_location_key ) );
                    if ( location != null ) {
                        weatherDataFromJson = loadUrl(location);
                        LogUtils.info( LOG_TAG, "loaded-data: " + weatherDataFromJson.size() );
                        controller.forecastFragment.updateAdapter( weatherDataFromJson );
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();*/

        String location = intent.getExtras().getString( App.getApplication().getString( R.string.pref_location_key ) );
        if ( location != null ) {

            String json = loadData(location);

            LogUtils.info( LOG_TAG, "data: " + json );
            controller.controllerSaveData.stringToJson( json );
            //controller.forecastFragment.updateAdapter( weatherDataFromJson );
        }

        //enable = false;
    }

    private String loadData( String location )
    {
        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String forecastJsonStr = null;

        try {
            Uri buildUri = setupUri( location );
            URL url = new URL( buildUri.toString() );
            LogUtils.info(LOG_TAG, "build uri: " + url.toString());

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                LogUtils.info(LOG_TAG, "inputstream is null.");
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                LogUtils.info(LOG_TAG, "date is empty.");
                return null;
            }
            forecastJsonStr = buffer.toString();
        } catch (IOException e) {
            LogUtils.error(LOG_TAG, "Error " + e.getMessage());
            // If the code didn't successfully get the weather data, there's no point in attemping
            // to parse it.
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    LogUtils.error(LOG_TAG, "Error closing stream " + e.getMessage());
                }
            }
        }

        return forecastJsonStr;
    }

    private Uri setupUri(String location) {
        String format = "json";
        String units = "metric";
        int numDays = 7;

        // Construct the URL for the OpenWeatherMap query
        // Possible parameters are avaiable at OWM's forecast API page, at
        // http://openweathermap.org/API#forecast
        final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
        final String QUERY_PARAM = "q";
        //final String QUERY_PARAM2 = "lon";
        final String FORMAT_PARAM = "mode";
        final String UNITS_PARAM = "units";
        final String DAYS_PARAM = "cnt";

        return Uri.parse(FORECAST_BASE_URL).buildUpon()
                .appendQueryParameter(QUERY_PARAM, location)
                        //.appendQueryParameter(QUERY_PARAM2, params[1])
                .appendQueryParameter(FORMAT_PARAM, format)
                .appendQueryParameter(UNITS_PARAM, units)
                .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                .build();
    }

    public class Controller extends Binder
    {
        public SunshineService getService(){ return SunshineService.this;  }
        public ControllerSaveData controllerSaveData;
    }

}
