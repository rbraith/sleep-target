package com.rbraithwaite.sleepapp.ui.sleep_tracker;

import com.rbraithwaite.sleepapp.core.models.Interruption;
import com.rbraithwaite.sleepapp.ui.Constants;
import com.rbraithwaite.sleepapp.ui.common.CommonFormatting;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

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
    
    public static String formatInterruptionsCount(List<Interruption> interruptions)
    {
        return String.valueOf(interruptions.size());
    }
    
    public static String formatInterruptionStart(Date start)
    {
        SimpleDateFormat dateFormat =
                new SimpleDateFormat("h:mm a, MMM d", Constants.STANDARD_LOCALE);
        return dateFormat.format(start);
    }
    
    public static String formatInterruptionReason(String reason)
    {
        return reason == null || reason.isEmpty() ? "- - -" : reason;
    }
}
