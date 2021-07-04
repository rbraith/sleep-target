package com.rbraithwaite.sleepapp.utils.time;

import com.rbraithwaite.sleepapp.utils.TimeUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

// REFACTOR [21-06-24 9:44PM] --
//  com/rbraithwaite/sleepapp/ui/session_details/controllers/DateTimeViewModel.java
//  also has a Date - that should be using this.
public class Day
{
//*********************************************************
// public constants
//*********************************************************

    public final int year;
    public final int month;
    public final int dayOfMonth;

//*********************************************************
// constructors
//*********************************************************

    public Day(int year, int month, int dayOfMonth)
    {
        this.year = year;
        this.month = month;
        this.dayOfMonth = dayOfMonth;
    }

//*********************************************************
// api
//*********************************************************

    // TEST NEEDED [21-06-26 9:01PM]
    public static Day of(Calendar cal)
    {
        return new Day(
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH));
    }
    
    public static Day of(Date date)
    {
        return Day.of(TimeUtils.getCalendarFrom(date));
    }
    
    public Day plus(int years, int months, int days)
    {
        GregorianCalendar cal = new GregorianCalendar(year, month, dayOfMonth);
        cal.add(Calendar.YEAR, years);
        cal.add(Calendar.MONTH, months);
        cal.add(Calendar.DAY_OF_MONTH, days);
        return Day.of(cal);
    }
    
    public Day nextDay()
    {
        return plus(0, 0, 1);
    }
}
