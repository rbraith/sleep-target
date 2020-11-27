package com.rbraithwaite.sleepapp.utils;

import java.util.Date;
import java.util.GregorianCalendar;

// TODO [20-11-27 1:16AM] -- consider redesigning to be more OOP
//  should be an instantiable obj w/ instance methods instead of
//  a collection of static methods?
public class DateUtils
{
//*********************************************************
// constructors
//*********************************************************

    private DateUtils() {/* No instantiation */}
    
//*********************************************************
// api
//*********************************************************

    // TODO [20-11-15 1:05AM] -- think about how I could test this.
    //  idk if I could? probably not a huge deal
    public static Date getNow()
    {
        return new GregorianCalendar().getTime();
    }
    
    public static Date getDateFromMillis(long dateMillis)
    {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(dateMillis);
        return calendar.getTime();
    }
}
