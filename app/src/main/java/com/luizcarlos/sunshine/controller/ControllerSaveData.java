package com.luizcarlos.sunshine.controller;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;

import com.luizcarlos.sunshine.App;
import com.luizcarlos.sunshine.R;
import com.luizcarlos.sunshine.fragments.ForecastFragment;
import com.luizcarlos.sunshine.model.WeatherDay;
import com.luizcarlos.sunshine.service.SunshineService;
import com.luizcarlos.sunshine.utils.LogUtils;
import com.luizcarlos.sunshine.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by luizcarlos on 19/09/14.
 */
public class ControllerSaveData implements ServiceConnection {

    private static final String LOG = ControllerSaveData.class.getSimpleName();
    private static final String JSON_DATA = "JSON-DATA";
    private static final String JSON_DATE_SAVE = "JSON-DATE-SAVE";
    private static final String LOCATION = "NAME-CITY";

    private ForecastFragment forecastFragment;
    private String location;
    private SunshineService.Controller controller;
    private String dateSaved;
    private String locationSaved;
    private Boolean isRefresh;
    private String log;
    private String lat;

    public ControllerSaveData( ForecastFragment fragment, String location, Boolean isRefresh ) {
        this.forecastFragment = fragment;
        this.location = location;
        this.isRefresh = isRefresh;

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences( forecastFragment.getActivity() );
        dateSaved = sharedPreferences.getString( JSON_DATE_SAVE, null );
        String data = sharedPreferences.getString( JSON_DATA, null );
        locationSaved = sharedPreferences.getString( LOCATION, null );


        //verifica se a data que foi salva a string é igual a data do sistema.
        //se sim, busca string de json guardada nas preferencias.
        if ( isRefresh == false & dateSaved != null && data != null && dateSaved.equals( getMonthAndDay() ) && locationSaved != null && locationSaved.equals(location) )
            stringToJson( data );
        else
            loadJson();
    }

    private void loadJson() {
        Intent intent = new Intent("INTENT_SERVICE");
        intent.putExtra( App.getApplication().getString(R.string.pref_location_key), location );

        forecastFragment.getActivity().startService(intent);
        forecastFragment.getActivity().bindService(intent, this, Context.BIND_AUTO_CREATE);
    }

    public void stringToJson( String stringJson )
    {
        try {
            ArrayList<WeatherDay> weatherDays = getWeatherDataFromJson( stringJson );
            forecastFragment.updateAdapter( weatherDays );

            //date system
            Calendar calendar = Calendar.getInstance();
            LogUtils.info(
                    LOG,
                    String.format( "moth: %d day: %d", calendar.get( Calendar.MONTH ), calendar.get( Calendar.DAY_OF_MONTH ) )
            );

            //set date first item
            calendar.setTime( weatherDays.get(0).getDate() );
            LogUtils.info(
                    LOG,
                    String.format( "moth: %d day: %d", calendar.get( Calendar.MONTH ), calendar.get( Calendar.DAY_OF_MONTH ) )
            );

            String dateSave = getMonthAndDay();
            //verifica se data do dia é igual a data que foi salva o arquivo, se as datas forem diferente salva um novo dataJson.
            if( isRefresh || dateSave.equals( this.dateSaved ) == false || location.equals(locationSaved) == false ) {
                saveData(stringJson, dateSave, this.location);
            }

            if ( this.controller != null )
            {
                Intent intent = new Intent("INTENT_SERVICE");
                forecastFragment.getActivity().unbindService( this );
                forecastFragment.getActivity().stopService( intent );
                this.controller = null;
            }
        }
        catch ( JSONException e )
        {
            e.printStackTrace();
        }
    }

    public void saveData(String stringJson, String dateString, String location) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences( forecastFragment.getActivity() );
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString( JSON_DATA, stringJson );
        editor.putString( JSON_DATE_SAVE, dateString );
        editor.putString( LOCATION, location );

        if ( lat != null )
        {
            editor.putString( "lat", lat );
            editor.putString( "lon", log );
        }

        editor.commit();
    }

    private String getMonthAndDay()
    {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.MONTH) + "-" + calendar.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     *
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    private ArrayList<WeatherDay> getWeatherDataFromJson(String forecastJsonStr)
            throws JSONException {

        // These are the names of the JSON objects that need to be extracted.
        final String OWM_LIST = "list";
        final String OWM_WEATHER = "weather";
        final String OWM_TEMPERATURE = "temp";
        final String OWM_MAX = "max";
        final String OWM_MIN = "min";
        final String OWM_DATETIME = "dt";
        final String OWM_DESCRIPTION = "main";
        final String OWN_HUMIDITY = "humidity";
        final String OWN_PRESSURE = "pressure";
        final String OWN_DEGREES = "deg";
        final String OWN_SPEED = "speed";
        final String OWN_ID = "id";

        JSONObject forecastJson = new JSONObject(forecastJsonStr);

        //get coords
        JSONObject coords = forecastJson.getJSONObject( "city" ).getJSONObject( "coord" );
        this.lat = coords.getString( "lat" );
        this.log = coords.getString( "lon" );
        LogUtils.info( LOG, String.format( "lat: %s, lon:  %s", lat, log ) );

        JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

        ArrayList<WeatherDay> resultData = new ArrayList<WeatherDay>();
        for(int i = 0; i < weatherArray.length(); i++) {

            WeatherDay weatherDay = new WeatherDay();

            // For now, using the format "Day, description, hi/low"
            String day;
            String description;

            // Get the JSON object representing the day
            JSONObject dayForecast = weatherArray.getJSONObject(i);

            // The date/time is returned as a long.  We need to convert that
            // into something human-readable, since most people won't read "1400356800" as
            // "this saturday".
            long dateTime = dayForecast.getLong(OWM_DATETIME);
            day = Utils.getReadableDateDay( dateTime ); //getReadableDateString(dateTime);
            weatherDay.setDay( day );
            weatherDay.setDate( new Date( dateTime * 1000 ) );

            // description is in a child array called "weather", which is 1 element long.
            JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
            description = weatherObject.getString(OWM_DESCRIPTION);
            weatherDay.setDescription( description );
            weatherDay.setId( weatherObject.getInt( OWN_ID ) );

            // Temperatures are in a child object called "temp".  Try not to name variables
            // "temp" when working with temperature.  It confuses everybody.
            JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
            double high = temperatureObject.getDouble(OWM_MAX);
            double low = temperatureObject.getDouble(OWM_MIN);
            double speed = dayForecast.getDouble( OWN_SPEED );
            double deg = dayForecast.getDouble( OWN_DEGREES );

            float speedF = Float.parseFloat( speed + "" );
            float degF = Float.parseFloat(deg + "");

            weatherDay.setMaxTemp( Utils.formatTemperature(high) );
            weatherDay.setMinTemp( Utils.formatTemperature(low) );
            weatherDay.setHumidity( dayForecast.getDouble( OWN_HUMIDITY ) );
            weatherDay.setPressure( dayForecast.getDouble( OWN_PRESSURE ) );
            weatherDay.setSpeed( speed );
            weatherDay.setDegree( deg );

            weatherDay.setWindy( Utils.getFormattedWind( App.getApplication(), speedF, degF ) );

            resultData.add(weatherDay);
        }

        return resultData;
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        LogUtils.info( LOG, "on-service-connected" );
        this.controller = (SunshineService.Controller) iBinder;
        this.controller.controllerSaveData = this;
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {

    }

    static public class AlarmReceiver extends BroadcastReceiver
    {

        @Override
        public void onReceive(Context context, Intent intent) {

        }
    }
}
