package com.luizcarlos.sunshine.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.luizcarlos.sunshine.App;
import com.luizcarlos.sunshine.R;

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

}
