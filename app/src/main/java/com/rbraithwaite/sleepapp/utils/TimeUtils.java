package com.rbraithwaite.sleepapp.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.inject.Inject;

// TODO [20-11-27 1:16AM] -- consider redesigning to be more OOP
//  should be an instantiable obj w/ instance methods instead of
//  a collection of static methods?
public class TimeUtils
{
//*********************************************************
// public constants
//*********************************************************

    public static final long MILLIS_24_HOURS = 86400000;

//*********************************************************
// constructors
//*********************************************************

    // REFACTOR [21-03-4 11:58PM] -- call this TimeService instead?
    @Inject
    public TimeUtils() {}

//*********************************************************
// api
//*********************************************************

    // TODO [20-11-15 1:05AM] -- think about how I could test this.
    //  idk if I could? probably not a huge deal
    public Date getNow()
    {
        return new GregorianCalendar().getTime();
    }
    
    public Date getDateFromMillis(long dateMillis)
    {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(dateMillis);
        return calendar.getTime();
    }
    
    public void setCalendarTimeOfDay(Calendar cal, long newTimeOfDayMillis)
    {
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        
        cal.add(Calendar.MILLISECOND, (int) newTimeOfDayMillis);
    }
    
    public long getTimeOfDay(Calendar cal)
    {
        return timeToMillis(
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                cal.get(Calendar.SECOND),
                cal.get(Calendar.MILLISECOND));
    }
    
    public long getTimeOfDay(Date date)
    {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(date);
        return getTimeOfDay(cal);
    }
    
    public long timeToMillis(int hours, int minutes, int seconds, int millis)
    {
        long hourMillis = hours * 60 * 60 * 1000;
        long minuteMillis = minutes * 60 * 1000;
        long secondMillis = seconds * 1000;
        
        return hourMillis + minuteMillis + secondMillis + millis;
    }
    
    public long hoursToMillis(int hours)
    {
        return timeToMillis(hours, 0, 0, 0);
    }
    
    public double millisToHours(long millis)
    {
        return ((millis / 1000.0) / 60.0) / 60.0;
    }
}
