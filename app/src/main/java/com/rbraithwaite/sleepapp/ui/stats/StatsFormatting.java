package com.rbraithwaite.sleepapp.ui.stats;

import com.rbraithwaite.sleepapp.ui.stats.data.DateRange;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class StatsFormatting
{
//*********************************************************
// constructors
//*********************************************************

    private StatsFormatting() {/* No instantiation */}
    
//*********************************************************
// api
//*********************************************************

    // TODO [21-02-25 12:40AM] -- I will eventually need to account for month, year, and custom
    //  ranges here (maybe include an enum arg?).
    public static String formatIntervalsRange(DateRange range)
    {
        // REFACTOR [21-02-25 12:39AM] -- hardcoded locale.
        SimpleDateFormat
                weekFormat = new SimpleDateFormat("YY/MM/dd", Locale.CANADA);
        return String.format(
                "%s - %s",
                weekFormat.format(range.getStart()),
                weekFormat.format(range.getEnd()));
    }
}
