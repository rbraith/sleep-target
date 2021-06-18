package com.rbraithwaite.sleepapp.ui.session_details;

import com.rbraithwaite.sleepapp.core.models.SleepDurationGoal;
import com.rbraithwaite.sleepapp.ui.Constants;
import com.rbraithwaite.sleepapp.ui.common.CommonFormatting;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class SessionDetailsFormatting
{
//*********************************************************
// constructors
//*********************************************************

    private SessionDetailsFormatting() {/* No instantiation */}

//*********************************************************
// api
//*********************************************************

    public static String formatSleepDurationGoal(SleepDurationGoal sleepDurationGoal)
    {
        return CommonFormatting.formatSleepDurationGoal(sleepDurationGoal);
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
