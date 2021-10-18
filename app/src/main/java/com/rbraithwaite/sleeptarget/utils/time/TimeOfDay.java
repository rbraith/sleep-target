/*
 * Copyright (c) 2020-2021 Rich Braithwaite (rbraithwaite.dev@gmail.com)
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
package com.rbraithwaite.sleeptarget.utils.time;

import com.rbraithwaite.sleeptarget.utils.Constants;
import com.rbraithwaite.sleeptarget.utils.TimeUtils;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

// REFACTOR [21-06-24 9:44PM] --
//  com/rbraithwaite/sleeptarget/ui/session_details/controllers/DateTimeViewModel.java
//  also has a TimeOfDay - that should be using this.
public class TimeOfDay
        implements Serializable
{
//*********************************************************
// public constants
//*********************************************************

    public static final long serialVersionUID = Constants.SERIAL_VERSION_UID;
    
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
