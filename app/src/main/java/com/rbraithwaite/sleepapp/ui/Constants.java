package com.rbraithwaite.sleepapp.ui;

import java.util.Locale;

public class Constants
{
//*********************************************************
// public constants
//*********************************************************

    // e.g. 1:23 AM
    public static final String STANDARD_FORMAT_TIME_OF_DAY = "h:mm a";
    
    // e.g. Jan 17 2021
    public static final String STANDARD_FORMAT_DATE = "MMM d yyyy";
    
    // e.g. 1:23 AM, Jan 17 2021
    public static final String STANDARD_FORMAT_FULL_DATE = "h:mm a, MMM d yyyy";
    
    // e.g. 1h 23m 45s
    public static final String STANDARD_FORMAT_DURATION = "%dh %02dm %02ds";
    
    public static final Locale STANDARD_LOCALE = Locale.CANADA;


//*********************************************************
// constructors
//*********************************************************

    private Constants() {/* No instantiation */}
}
