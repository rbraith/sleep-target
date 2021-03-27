package com.rbraithwaite.sleepapp.data.database.convert;

import androidx.room.TypeConverter;

import java.util.Date;
import java.util.GregorianCalendar;

public class ConvertDate
{
//*********************************************************
// api
//*********************************************************

    @TypeConverter
    public static Date fromMillis(Long millis)
    {
        if (millis == null) {
            return null;
        }
        
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(millis);
        
        return calendar.getTime();
    }
    
    @TypeConverter
    public static Long toMillis(Date date)
    {
        if (date == null) {
            return null;
        }
        return date.getTime();
    }
}
