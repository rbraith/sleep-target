package com.rbraithwaite.sleepapp.ui.interruption_details;

import com.rbraithwaite.sleepapp.ui.common.CommonFormatting;

import java.util.Date;

public class InterruptionDetailsFormatting
{
//*********************************************************
// constructors
//*********************************************************

    private InterruptionDetailsFormatting() {/* No instantiation */}
    
    
//*********************************************************
// api
//*********************************************************

    public static String formatFullDate(Date date)
    {
        return CommonFormatting.formatFullDate(date);
    }
    
    public static String formatReason(String reason)
    {
        return reason == null ? "" : reason;
    }
}
