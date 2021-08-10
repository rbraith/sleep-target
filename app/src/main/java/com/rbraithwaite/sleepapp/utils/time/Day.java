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
// overrides
//*********************************************************

    @Override
    public int hashCode()
    {
        int result = year;
        result = 31 * result + month;
        result = 31 * result + dayOfMonth;
        return result;
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        
        Day day = (Day) o;
        
        if (year != day.year) { return false; }
        if (month != day.month) { return false; }
        return dayOfMonth == day.dayOfMonth;
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
    
    // TEST NEEDED [21-08-7 5:51PM]
    public boolean isLessThan(Day other)
    {
        if (other == null) { return false; }
        
        if (year > other.year) { return false; }
        if (year < other.year) { return true; }
        
        if (month > other.month) { return false; }
        if (month < other.month) { return true; }
        
        if (dayOfMonth > other.dayOfMonth) { return false; }
        
        return dayOfMonth < other.dayOfMonth;
    }
}
