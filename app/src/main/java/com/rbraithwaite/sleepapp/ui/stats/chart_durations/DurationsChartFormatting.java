package com.rbraithwaite.sleepapp.ui.stats.chart_durations;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DurationsChartFormatting
{
//*********************************************************
// api
//*********************************************************

    public static String formatDataLabel(Date start)
    {
        // REFACTOR [21-05-17 2:56PM] -- hardcoded locale.
        SimpleDateFormat labelFormat = new SimpleDateFormat("M/dd", Locale.CANADA);
        return labelFormat.format(start);
    }
}
