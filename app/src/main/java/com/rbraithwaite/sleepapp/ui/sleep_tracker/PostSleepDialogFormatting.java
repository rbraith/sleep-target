package com.rbraithwaite.sleepapp.ui.sleep_tracker;

import com.rbraithwaite.sleepapp.ui.format.CommonFormatting;

import java.util.Date;

public class PostSleepDialogFormatting
{
//*********************************************************
// api
//*********************************************************

    public static String formatDate(Date date)
    {
        return CommonFormatting.formatFullDate(date);
    }
    
    public static String formatDuration(long durationMillis)
    {
        return CommonFormatting.formatDurationMillis(durationMillis);
    }
}
