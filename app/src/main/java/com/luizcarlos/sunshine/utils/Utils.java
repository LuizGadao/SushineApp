package com.luizcarlos.sunshine.utils;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by luizcarlos on 26/08/14.
 */
public class Utils
{
    public static Calendar dateToCalendar( Date date )
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime( date );

        return calendar;
    }

}
