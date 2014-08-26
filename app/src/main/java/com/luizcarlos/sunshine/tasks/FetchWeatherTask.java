package com.luizcarlos.sunshine.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.luizcarlos.sunshine.R;
import com.luizcarlos.sunshine.adapters.ListItemForecast;
import com.luizcarlos.sunshine.model.WeatherDay;
import com.luizcarlos.sunshine.utils.LogUtils;

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
import java.util.Date;
import java.util.Locale;

/**
 * Created by luizcarlos on 25/07/14.
 */
public class FetchWeatherTask extends AsyncTask<String, Void, ArrayList<WeatherDay>>
{

    private final static String TAG = FetchWeatherTask.class.getSimpleName();

    private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();
    private ListItemForecast adapter;

    public FetchWeatherTask( ListItemForecast adapter) {
        this.adapter = adapter;
    }

    @Override
    protected ArrayList<WeatherDay> doInBackground(String... params)
    {

        if ( params.length == 0 ) return null;

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
                    .appendQueryParameter(QUERY_PARAM, params[0])
                    //.appendQueryParameter(QUERY_PARAM2, params[1])
                    .appendQueryParameter(FORMAT_PARAM, format)
                    .appendQueryParameter(UNITS_PARAM, units)
                    .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                    .build();

            URL url = new URL(buildUri.toString());
            LogUtils.logInfo( LOG_TAG, "build uri: " + url.toString());

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
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
                return null;
            }
            forecastJsonStr = buffer.toString();
        } catch (IOException e) {
            LogUtils.logError(LOG_TAG, "Error " + e.getMessage());
            // If the code didn't successfully get the weather data, there's no point in attemping
            // to parse it.
            return null;
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
            return getWeatherDataFromJson( forecastJsonStr, numDays );
        }
        catch (JSONException e)
        {
            LogUtils.logError(LOG_TAG, "error: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute( ArrayList<WeatherDay> daysWeather ) {

        if ( daysWeather != null )
        {
            adapter.clear();
            adapter.addAll( daysWeather );
            /*for( String str : result )
                adapter.add( str );*/
        }
    }

    /* The date/time conversion code is going to be moved outside the asynctask later,
         * so for convenience we're breaking it out into its own method now.
         */
    private String getReadableDateString(long time){
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        Date date = new Date(time * 1000);
        // format = new SimpleDateFormat("E, MMM d");
        SimpleDateFormat format = new SimpleDateFormat("E, MMM, d", Locale.getDefault());
        return format.format(date).toString();
    }

    /**
     * Prepare the weather high/lows for presentation.
     */
    private String formatHighLows(double high, double low) {

        Context context = adapter.getContext();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences( context );

        String unitType = sharedPreferences.getString(
                context.getString(R.string.pref_units_key),
                context.getString(R.string.pref_units_metric) );

        if (unitType.equals( context.getString(R.string.pref_units_imperial) ))
        {
            low = (low * 1.8) + 32;
            high = (high * 1.8) + 32;
        }
        else if (! unitType.equals(context.getString(R.string.pref_units_metric) ))
        {
            LogUtils.logInfo(TAG, "unidade de medida não encontrada.");
        }

        // For presentation, assume the user doesn't care about tenths of a degree.
        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);

        String highLowStr = roundedHigh + "/" + roundedLow;
        return highLowStr;
    }

    private String formatTemperature( double temp )
    {
        Context context = adapter.getContext();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences( context );

        String unitType = sharedPreferences.getString(
                context.getString(R.string.pref_units_key),
                context.getString(R.string.pref_units_metric) );

        if (unitType.equals( context.getString(R.string.pref_units_imperial) ))
            temp = (temp * 1.8) + 32;
        else if (! unitType.equals(context.getString(R.string.pref_units_metric) ))
            LogUtils.logInfo(TAG, "unidade de medida não encontrada.");

        // For presentation, assume the user doesn't care about tenths of a degree.
        long roundedTemp = Math.round( temp );

        return String.format( "%d°", roundedTemp );
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
            day = getReadableDateString(dateTime);
            weatherDay.setDay( day );

            // description is in a child array called "weather", which is 1 element long.
            JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
            description = weatherObject.getString(OWM_DESCRIPTION);
            weatherDay.setDescription( description );

            // Temperatures are in a child object called "temp".  Try not to name variables
            // "temp" when working with temperature.  It confuses everybody.
            JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
            double high = temperatureObject.getDouble(OWM_MAX);
            double low = temperatureObject.getDouble(OWM_MIN);

            weatherDay.setMaxTemp( formatTemperature( high ) );
            weatherDay.setMinTemp( formatTemperature( low ) );

            resultData.add( weatherDay );
        }

        return resultData;
    }
}
