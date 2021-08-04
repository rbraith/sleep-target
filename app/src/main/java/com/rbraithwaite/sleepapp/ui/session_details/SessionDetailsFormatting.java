package com.rbraithwaite.sleepapp.ui.session_details;

import com.rbraithwaite.sleepapp.core.models.SleepDurationGoal;
import com.rbraithwaite.sleepapp.ui.common.CommonFormatting;

import java.util.Date;

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
        return CommonFormatting.formatTimeOfDay(hourOfDay, minute);
    }
    
    public static String formatDate(int year, int month, int dayOfMonth)
    {
        return CommonFormatting.formatDate(year, month, dayOfMonth);
    }
    
    public static String formatDuration(long durationMillis)
    {
        return CommonFormatting.formatDurationMillis(durationMillis);
    }
    
    public static String formatFullDate(Date date)
    {
        return CommonFormatting.formatFullDate(date);
    }
}
