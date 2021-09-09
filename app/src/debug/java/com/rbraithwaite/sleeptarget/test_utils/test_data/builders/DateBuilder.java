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

package com.rbraithwaite.sleeptarget.test_utils.test_data.builders;

import com.rbraithwaite.sleeptarget.utils.TimeUtils;
import com.rbraithwaite.sleeptarget.utils.interfaces.BuilderOf;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateBuilder
        implements BuilderOf<Date>
{
//*********************************************************
// private properties
//*********************************************************

    private GregorianCalendar mCal;

//*********************************************************
// constructors
//*********************************************************

    public DateBuilder()
    {
        mCal = new GregorianCalendar(2021, 8, 3, 12, 34);
    }

//*********************************************************
// overrides
//*********************************************************

    @Override
    public Date build()
    {
        return mCal.getTime();
    }

//*********************************************************
// api
//*********************************************************

    public DateBuilder addMinutes(int minutes)
    {
        mCal.add(Calendar.MINUTE, minutes);
        return this;
    }
    
    /**
     * month starts from 1, hourOfDay is 24 hrs.
     */
    public DateBuilder withValue(int year, int month, int dayOfMonth, int hourOfDay, int minute)
    {
        mCal.set(year, month - 1, dayOfMonth, hourOfDay, minute);
        return this;
    }
    
    public DateBuilder copying(Date date)
    {
        mCal.setTime(date);
        return this;
    }
    
    public DateBuilder addDays(int days)
    {
        mCal.add(Calendar.DAY_OF_MONTH, days);
        return this;
    }
    
    public DateBuilder addTime(int years, int months, int days)
    {
        mCal.add(Calendar.YEAR, years);
        mCal.add(Calendar.MONTH, months);
        mCal.add(Calendar.DAY_OF_MONTH, days);
        return this;
    }
    
    public DateBuilder addHours(int hours)
    {
        mCal.add(Calendar.HOUR, hours);
        return this;
    }
    
    public DateBuilder withDay(int year, int month, int dayOfMonth)
    {
        mCal.set(Calendar.YEAR, year);
        mCal.set(Calendar.MONTH, month);
        mCal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        return this;
    }
    
    public DateBuilder addMillis(int millis)
    {
        mCal.add(Calendar.MILLISECOND, millis);
        return this;
    }
    
    public DateBuilder copy()
    {
        DateBuilder copyOfThis = new DateBuilder();
        copyOfThis.mCal = new GregorianCalendar();
        copyOfThis.mCal.setTime(mCal.getTime());
        return copyOfThis;
    }
    
    public DateBuilder subtractDays(int days)
    {
        addDays(-days);
        return this;
    }
    
    public DateBuilder atMidnight()
    {
        new TimeUtils().setCalendarTimeOfDay(mCal, 0);
        return this;
    }
}
