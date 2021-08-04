package com.rbraithwaite.sleepapp.ui.common;

import com.rbraithwaite.sleepapp.core.models.SleepDurationGoal;
import com.rbraithwaite.sleepapp.ui.Constants;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class CommonFormatting
{
//*********************************************************
// constructors
//*********************************************************

    private CommonFormatting() {/* No instantiation */}

//*********************************************************
// api
//*********************************************************

    public static String formatSleepDurationGoal(SleepDurationGoal goalModel)
    {
        if (goalModel == null || !goalModel.isSet()) {
            return "";
        }
        
        // REFACTOR [21-02-2 8:13PM] -- hardcoded locale & format.
        return String.format(
                Locale.CANADA,
                "%dh %02dm",
                goalModel.getHours(),
                goalModel.getRemainingMinutes());
    }
    
    public static String formatDurationMillis(long durationMillis)
    {
        if (durationMillis < 0) {
            throw new IllegalArgumentException(String.format("duration must be >= 0 (was %d)",
                                                             durationMillis));
        }
        
        long durationAsSeconds = durationMillis / 1000L;
        
        long durationAsMinutes = durationAsSeconds / 60;
        long seconds = durationAsSeconds % 60;
        long minutes = durationAsMinutes % 60;
        long hours = durationAsMinutes / 60;
        
        return String.format(Constants.STANDARD_LOCALE,
                             Constants.STANDARD_FORMAT_DURATION,
                             hours,
                             minutes,
                             seconds);
    }
    
    public static String formatFullDate(Date date)
    {
        if (date == null) {
            return null;
        }
        SimpleDateFormat format = new SimpleDateFormat(
                Constants.STANDARD_FORMAT_FULL_DATE,
                Constants.STANDARD_LOCALE);
        return format.format(date);
    }
    
    public static String formatTimeOfDay(int hourOfDay, int minute)
    {
        GregorianCalendar cal = new GregorianCalendar();
        cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
        cal.set(Calendar.MINUTE, minute);
        
        SimpleDateFormat timeOfDayFormat =
                new SimpleDateFormat(Constants.STANDARD_FORMAT_TIME_OF_DAY,
                                     Constants.STANDARD_LOCALE);
        return timeOfDayFormat.format(cal.getTime());
    }
    
    public static String formatDate(int year, int month, int dayOfMonth)
    {
        GregorianCalendar cal = new GregorianCalendar(year, month, dayOfMonth);
        
        SimpleDateFormat dateFormat =
                new SimpleDateFormat(Constants.STANDARD_FORMAT_DATE, Constants.STANDARD_LOCALE);
        return dateFormat.format(cal.getTime());
    }
}
