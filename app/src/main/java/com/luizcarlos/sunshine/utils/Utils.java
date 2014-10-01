package com.luizcarlos.sunshine.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

import com.luizcarlos.sunshine.App;
import com.luizcarlos.sunshine.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by luizcarlos on 26/08/14.
 */
public class Utils
{
    private static final String LOG = Utils.class.getSimpleName();

    public static Calendar dateToCalendar( Date date )
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime( date );

        return calendar;
    }

    public static String getPreferenceLocation() {
        Context context = App.getApplication();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_location_key), context.getString(R.string.pref_location_default));
    }

    public static String formatTemperature( double temp )
    {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(App.getApplication());

        String unitType = sharedPreferences.getString(
                App.getApplication().getString(R.string.pref_units_key),
                App.getApplication().getString(R.string.pref_units_metric) );

        if (unitType.equals( App.getApplication().getString(R.string.pref_units_imperial) ))
            temp = (temp * 1.8) + 32;
        else if (! unitType.equals(App.getApplication().getString(R.string.pref_units_metric) ))
            LogUtils.info(LOG, "unidade de medida não encontrada.");

        // For presentation, assume the user doesn't care about tenths of a degree.
        double roundedTemp = Math.round( temp );

        return App.getApplication().getString( R.string.format_temperature, roundedTemp );
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

    public static String getReadableDateDay( long time )
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

    public static String loadDataWether(String location)
    {
        String LOG_TAG = "method-load-data-wether";

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

    private static Uri setupUri(String location) {
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

}
