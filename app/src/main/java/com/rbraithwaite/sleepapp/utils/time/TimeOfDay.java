package com.rbraithwaite.sleepapp.utils.time;

import com.rbraithwaite.sleepapp.utils.TimeUtils;

import java.util.Calendar;
import java.util.Date;

// REFACTOR [21-06-24 9:44PM] --
//  com/rbraithwaite/sleepapp/ui/session_details/controllers/DateTimeViewModel.java
//  also has a TimeOfDay - that should be using this.
public class TimeOfDay
{
//*********************************************************
// public constants
//*********************************************************

    public final int hourOfDay;
    public final int minute;

//*********************************************************
// constructors
//*********************************************************

    public TimeOfDay(int hourOfDay, int minute)
    {
        this.hourOfDay = hourOfDay;
        this.minute = minute;
    }

//*********************************************************
// overrides
//*********************************************************

    @Override
    public int hashCode()
    {
        int result = hourOfDay;
        result = 31 * result + minute;
        return result;
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        
        TimeOfDay timeOfDay = (TimeOfDay) o;
        
        if (hourOfDay != timeOfDay.hourOfDay) { return false; }
        return minute == timeOfDay.minute;
    }
    
//*********************************************************
// api
//*********************************************************

    public static TimeOfDay of(Calendar cal)
    {
        return new TimeOfDay(
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE));
    }
    
    public static TimeOfDay of(Date date)
    {
        return TimeOfDay.of(TimeUtils.getCalendarFrom(date));
    }
}
