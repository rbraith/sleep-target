/*
 * Copyright (c) 2020-2021 Richard Braithwaite (rbraithwaite.dev@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.rbraithwaite.sleeptarget.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

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

    public static GregorianCalendar getCalendarFrom(Date date)
    {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        return calendar;
    }
    
    public static long timeToMillis(int hours, int minutes, int seconds, int millis)
    {
        long hourMillis = hours * 60 * 60 * 1000;
        long minuteMillis = minutes * 60 * 1000;
        long secondMillis = seconds * 1000;
        
        return hourMillis + minuteMillis + secondMillis + millis;
    }
    
    public static long minutesToMillis(int minutes)
    {
        return timeToMillis(0, minutes, 0, 0);
    }
    
    public static long hoursToMillis(int hours)
    {
        return timeToMillis(hours, 0, 0, 0);
    }
    
    public static GregorianCalendar copyOf(GregorianCalendar calendar)
    {
        if (calendar == null) {
            return null;
        }
        
        GregorianCalendar newCalendar = new GregorianCalendar();
        newCalendar.setTime(calendar.getTime());
        return newCalendar;
    }
    
    /**
     * Get the start (a.k.a 00:00) of the provided date, in absolute millis.
     */
    public static long startMillisOf(Date date)
    {
        GregorianCalendar cal = getCalendarFrom(date);
        // REFACTOR [21-09-29 12:36AM] -- setCalendarTimeOfDay (& the rest of this class) should
        //  be static.
        new TimeUtils().setCalendarTimeOfDay(cal, 0);
        return cal.getTimeInMillis();
    }
    
    // TODO [20-11-15 1:05AM] -- think about how I could test this.
    //  idk if I could? probably not a huge deal
    // TODO [21-06-27 3:04AM] -- replace this with a Now class.
    @Deprecated
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
    
    // REFACTOR [21-07-31 1:17AM] -- this can return an int.
    public long getTimeOfDayOf(Calendar cal)
    {
        return timeToMillis(
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                cal.get(Calendar.SECOND),
                cal.get(Calendar.MILLISECOND));
    }
    
    // REFACTOR [21-07-31 1:17AM] -- this can return an int.
    public long getTimeOfDayOf(Date date)
    {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(date);
        return getTimeOfDayOf(cal);
    }
    
    public double millisToHours(long millis)
    {
        return ((millis / 1000.0) / 60.0) / 60.0;
    }
    
    
    /**
     * Converts the date to an int yyyymmdd.
     */
    // REFACTOR [21-08-7 3:15PM] -- It would be better to just convert Dates to Days.
    @Deprecated
    public int toDayInt(Date date)
    {
        return Integer.parseInt(new SimpleDateFormat("yyyyMMdd", Locale.CANADA).format(date));
    }
    
    public Date addDurationToDate(Date date, int durationMillis)
    {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(date);
        cal.add(Calendar.MILLISECOND, durationMillis);
        return cal.getTime();
    }
    
    // TEST NEEDED [21-06-18 12:58AM] -- .
    public int monthOf(Date date)
    {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(date);
        return cal.get(Calendar.MONTH);
    }
    
    // TEST NEEDED [21-06-18 12:58AM] -- .
    public int yearOf(Date date)
    {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(date);
        return cal.get(Calendar.YEAR);
    }
}
