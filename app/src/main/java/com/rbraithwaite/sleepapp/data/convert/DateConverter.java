package com.rbraithwaite.sleepapp.data.convert;

import androidx.room.TypeConverter;

import java.util.Date;
import java.util.GregorianCalendar;

public class DateConverter
{
//*********************************************************
// api
//*********************************************************

    // REFACTOR [20-12-16 12:34AM] -- do I prefer convertToDate(millis)?
    @TypeConverter
    public static Date convertDateFromMillis(Long millis)
    {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(millis);
        
        return calendar.getTime();
    }
    
    @TypeConverter
    public static Long convertDateToMillis(Date date)
    {
        return date.getTime();
    }
}
