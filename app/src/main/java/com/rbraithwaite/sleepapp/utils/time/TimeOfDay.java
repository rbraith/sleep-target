package com.rbraithwaite.sleepapp.utils.time;

import java.util.Calendar;

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
// api
//*********************************************************

    public static TimeOfDay of(Calendar cal)
    {
        return new TimeOfDay(
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE));
    }
}
