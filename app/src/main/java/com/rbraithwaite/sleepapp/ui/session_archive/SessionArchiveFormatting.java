package com.rbraithwaite.sleepapp.ui.session_archive;

import com.rbraithwaite.sleepapp.ui.Constants;
import com.rbraithwaite.sleepapp.ui.format.CommonFormatting;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SessionArchiveFormatting
{
    public static String formatFullDate(Date date)
    {
        // REFACTOR [21-03-31 2:26AM] -- make this CommonFormatting.formatFullDate.
        if (date == null) {
            return null;
        }
        
        SimpleDateFormat fullDateFormat =
                new SimpleDateFormat(Constants.STANDARD_FORMAT_FULL_DATE, Constants.STANDARD_LOCALE);
        
        return fullDateFormat.format(date);
    }
    
    public static String formatDuration(long durationMillis)
    {
        return CommonFormatting.formatDurationMillis(durationMillis);
    }
}
