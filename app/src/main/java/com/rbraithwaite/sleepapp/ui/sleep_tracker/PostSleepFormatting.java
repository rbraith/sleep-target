package com.rbraithwaite.sleepapp.ui.sleep_tracker;

import com.rbraithwaite.sleepapp.core.models.Interruption;
import com.rbraithwaite.sleepapp.ui.common.CommonFormatting;
import com.rbraithwaite.sleepapp.ui.common.interruptions.InterruptionFormatting;

import java.util.Date;
import java.util.List;

public class PostSleepFormatting
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
    
    public static String formatInterruptionsCount(List<Interruption> interruptions)
    {
        return InterruptionFormatting.formatInterruptionsCount(interruptions);
    }
}
