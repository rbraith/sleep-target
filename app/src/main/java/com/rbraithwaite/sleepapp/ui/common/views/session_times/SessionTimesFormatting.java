package com.rbraithwaite.sleepapp.ui.common.views.session_times;

import com.rbraithwaite.sleepapp.ui.common.CommonFormatting;

public class SessionTimesFormatting
{
//*********************************************************
// constructors
//*********************************************************

    private SessionTimesFormatting() {/* No instantiation */}
    
    
//*********************************************************
// api
//*********************************************************

    public static String formatDuration(long durationMillis)
    {
        return CommonFormatting.formatDurationMillis(durationMillis);
    }
    
    public static String formatTimeOfDay(int hourOfDay, int minute)
    {
        return CommonFormatting.formatTimeOfDay(hourOfDay, minute);
    }
    
    public static String formatDate(int year, int month, int dayOfMonth)
    {
        return CommonFormatting.formatDate(year, month, dayOfMonth);
    }
}
