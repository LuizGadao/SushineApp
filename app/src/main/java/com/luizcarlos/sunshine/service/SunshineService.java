package com.luizcarlos.sunshine.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;

import com.luizcarlos.sunshine.App;
import com.luizcarlos.sunshine.R;
import com.luizcarlos.sunshine.fragments.ForecastFragment;
import com.luizcarlos.sunshine.model.WeatherDay;
import com.luizcarlos.sunshine.utils.LogUtils;
import com.luizcarlos.sunshine.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class SunshineService extends IntentService {
    private static final String LOG_TAG = SunshineService.class.getSimpleName();
    public ArrayList<WeatherDay> weatherDataFromJson;
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
        LogUtils.logInfo( LOG_TAG, "on-create" );
        super.onCreate();
        enable = true;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        LogUtils.logInfo( LOG_TAG, "on-start: " + startId );
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
                        LogUtils.logInfo( LOG_TAG, "loaded-data: " + weatherDataFromJson.size() );
                        controller.forecastFragment.updateAdapter( weatherDataFromJson );
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();*/

        String location = intent.getExtras().getString( App.getApplication().getString( R.string.pref_location_key ) );
        if ( location != null ) {
            weatherDataFromJson = loadUrl(location);
            LogUtils.logInfo( LOG_TAG, "loaded-data: " + weatherDataFromJson.size() );
            controller.forecastFragment.updateAdapter( weatherDataFromJson );
        }

        enable = false;
    }

    private ArrayList<WeatherDay> loadUrl( String location )
    {
        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String forecastJsonStr = null;

        String format = "json";
        String units = "metric";
        int numDays = 7;

        try {
            // Construct the URL for the OpenWeatherMap query
            // Possible parameters are avaiable at OWM's forecast API page, at
            // http://openweathermap.org/API#forecast
            final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
            final String QUERY_PARAM = "q";
            //final String QUERY_PARAM2 = "lon";
            final String FORMAT_PARAM = "mode";
            final String UNITS_PARAM = "units";
            final String DAYS_PARAM = "cnt";

            Uri buildUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                    .appendQueryParameter(QUERY_PARAM, location)
                            //.appendQueryParameter(QUERY_PARAM2, params[1])
                    .appendQueryParameter(FORMAT_PARAM, format)
                    .appendQueryParameter(UNITS_PARAM, units)
                    .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                    .build();

            URL url = new URL(buildUri.toString());
            LogUtils.logInfo(LOG_TAG, "build uri: " + url.toString());

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                LogUtils.logInfo( LOG_TAG, "inputstream is null." );
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
                LogUtils.logInfo( LOG_TAG, "date is empty." );
                return null;
            }
            forecastJsonStr = buffer.toString();
        } catch (IOException e) {
            LogUtils.logError(LOG_TAG, "Error " + e.getMessage());
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
                    LogUtils.logError(LOG_TAG, "Error closing stream " + e.getMessage());
                }
            }
        }

        try{
            return getWeatherDataFromJson(forecastJsonStr, numDays);
        }
        catch (JSONException e)
        {
            LogUtils.logError(LOG_TAG, "error: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }


    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     *
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    private ArrayList<WeatherDay> getWeatherDataFromJson(String forecastJsonStr, int numDays)
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
            day = getReadableDateDay( dateTime ); //getReadableDateString(dateTime);
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

            weatherDay.setMaxTemp( formatTemperature( high ) );
            weatherDay.setMinTemp( formatTemperature(low) );
            weatherDay.setHumidity( dayForecast.getDouble( OWN_HUMIDITY ) );
            weatherDay.setPressure( dayForecast.getDouble( OWN_PRESSURE ) );
            weatherDay.setSpeed( speed );
            weatherDay.setDegree( deg );

            weatherDay.setWindy( getFormattedWind( App.getApplication(), speedF, degF ) );

            resultData.add(weatherDay);
        }

        return resultData;
    }

    public static String getFormattedWind( Context context, float windSpeed, float degrees ) {
        int windFormat = R.string.format_wind_kmh;

        // From wind direction in degrees, determine compass direction as a string (e.g NW)
        // You know what's fun, writing really long if/else statements with tons of possible
        // conditions.  Seriously, try it!
        String direction = "Unknown";
        if (degrees >= 337.5 || degrees < 22.5) {
            direction = "N";
        } else if (degrees >= 22.5 && degrees < 67.5) {
            direction = "NE";
        } else if (degrees >= 67.5 && degrees < 112.5) {
            direction = "E";
        } else if (degrees >= 112.5 && degrees < 157.5) {
            direction = "SE";
        } else if (degrees >= 157.5 && degrees < 202.5) {
            direction = "S";
        } else if (degrees >= 202.5 && degrees < 247.5) {
            direction = "SW";
        } else if (degrees >= 247.5 && degrees < 292.5) {
            direction = "W";
        } else if (degrees >= 292.5 || degrees < 22.5) {
            direction = "NW";
        }
        return String.format(context.getString(windFormat), windSpeed, direction);
    }

    private String formatTemperature( double temp )
    {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(App.getApplication());

        String unitType = sharedPreferences.getString(
                App.getApplication().getString(R.string.pref_units_key),
                App.getApplication().getString(R.string.pref_units_metric) );

        if (unitType.equals( App.getApplication().getString(R.string.pref_units_imperial) ))
            temp = (temp * 1.8) + 32;
        else if (! unitType.equals(App.getApplication().getString(R.string.pref_units_metric) ))
            LogUtils.logInfo(LOG_TAG, "unidade de medida não encontrada.");

        // For presentation, assume the user doesn't care about tenths of a degree.
        double roundedTemp = Math.round( temp );

        return App.getApplication().getString( R.string.format_temperature, roundedTemp );
    }

    private String getReadableDateDay( long time )
    {
        Calendar calendarToday = Calendar.getInstance();
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        Date date = new Date(time * 1000);
        Calendar calendarDay = Utils.dateToCalendar(date);

        Context context = App.getApplication();
        int result = calendarDay.get( Calendar.DAY_OF_WEEK ) - calendarToday.get( Calendar.DAY_OF_WEEK );

        //verifca se os dias são iguais
        if ( calendarToday.get( Calendar.DAY_OF_WEEK ) == calendarDay.get( Calendar.DAY_OF_WEEK ) )
            return context.getString( R.string.today );
        else if ( result == 1  ) // verifica se a diferenças dos dias é de apenas uma dia.
            return context.getString( R.string.tomorrow );
        else
            return new SimpleDateFormat("EEEE").format( date ).toString();
    }

    public class Controller extends Binder
    {
        public SunshineService getService(){ return SunshineService.this;  }
        public ForecastFragment forecastFragment;
    }

}
