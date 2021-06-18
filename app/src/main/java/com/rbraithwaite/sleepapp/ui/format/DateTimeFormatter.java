package com.rbraithwaite.sleepapp.ui.format;

import com.rbraithwaite.sleepapp.ui.Constants;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

// REFACTOR [21-06-18 1:29AM] get rid of this class.
// TODO [20-11-27 1:32AM] -- consider using android's DateUtils instead? (just discovered this)
//  https://developer.android.com/reference/android/text/format/DateUtils.
@Deprecated
public class DateTimeFormatter
{
//*********************************************************
// private properties
//*********************************************************

    private SimpleDateFormat mSimpleDateFormat;
    private SimpleDateFormat mSimpleTimeOfDayFormat;
    private SimpleDateFormat mSimpleFullDateFormat;
    
//*********************************************************
// constructors
//*********************************************************

    public DateTimeFormatter()
    {
        // TODO [20-11-27 12:53AM] -- leaving these properties with standard defaults for now.
        //  eventually its possible i will need to parameterize them.
        Locale locale = Constants.STANDARD_LOCALE;
        
        mSimpleDateFormat = new SimpleDateFormat(Constants.STANDARD_FORMAT_DATE, locale);
        mSimpleTimeOfDayFormat =
                new SimpleDateFormat(Constants.STANDARD_FORMAT_TIME_OF_DAY, locale);
        mSimpleFullDateFormat = new SimpleDateFormat(Constants.STANDARD_FORMAT_FULL_DATE, locale);
    }
    
//*********************************************************
// api
//*********************************************************

    public String formatDate(Date date)
    {
        return mSimpleDateFormat.format(date);
    }
    
    public String formatTimeOfDay(Date date)
    {
        return mSimpleTimeOfDayFormat.format(date);
    }
    
    public String formatFullDate(Date date)
    {
        return mSimpleFullDateFormat.format(date);
    }
}
