package com.rbraithwaite.sleepapp.ui.session_archive;

import com.rbraithwaite.sleepapp.core.models.Interruptions;
import com.rbraithwaite.sleepapp.ui.Constants;
import com.rbraithwaite.sleepapp.ui.common.CommonFormatting;
import com.rbraithwaite.sleepapp.ui.sleep_tracker.SleepTrackerFormatting;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SessionArchiveFormatting
{
//*********************************************************
// api
//*********************************************************

    public static String formatFullDate(Date date)
    {
        // REFACTOR [21-03-31 2:26AM] -- make this CommonFormatting.formatFullDate.
        if (date == null) {
            return null;
        }
        
        SimpleDateFormat fullDateFormat =
                new SimpleDateFormat(Constants.STANDARD_FORMAT_FULL_DATE,
                                     Constants.STANDARD_LOCALE);
        
        return fullDateFormat.format(date);
    }
    
    public static String formatDuration(long durationMillis)
    {
        return CommonFormatting.formatDurationMillis(durationMillis);
    }
    
    public static String formatInterruptions(Interruptions interruptions)
    {
        if (interruptions == null || interruptions.isEmpty()) {
            return null;
        }
        
        // REFACTOR [21-07-20 2:58PM] -- this should be CommonFormatting & shared between here
        //  and sleep tracker.
        return SleepTrackerFormatting.formatInterruptionsTotal(
                interruptions.getTotalDuration(),
                interruptions.getCount());
    }
}
