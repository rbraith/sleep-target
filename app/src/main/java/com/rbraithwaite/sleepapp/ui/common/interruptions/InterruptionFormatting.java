package com.rbraithwaite.sleepapp.ui.common.interruptions;

import com.rbraithwaite.sleepapp.core.models.Interruption;
import com.rbraithwaite.sleepapp.ui.Constants;
import com.rbraithwaite.sleepapp.ui.common.CommonFormatting;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class InterruptionFormatting
{
//*********************************************************
// constructors
//*********************************************************

    private InterruptionFormatting() {/* No instantiation */}
    
//*********************************************************
// api
//*********************************************************

    public static String formatListItemStart(Date start)
    {
        SimpleDateFormat dateFormat =
                new SimpleDateFormat("h:mm a, MMM d", Constants.STANDARD_LOCALE);
        return dateFormat.format(start);
    }
    
    public static String formatInterruptionsCount(List<Interruption> interruptions)
    {
        if (interruptions == null) {
            return "0";
        }
        return String.valueOf(interruptions.size());
    }
    
    public static String formatDuration(long durationMillis)
    {
        return CommonFormatting.formatDurationMillis(durationMillis);
    }
    
    public static String formatListItemReason(String reason)
    {
        return reason == null || reason.isEmpty() ? "- - -" : reason;
    }
}
