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

package com.rbraithwaite.sleeptarget.ui.stats.chart_intervals;

import com.rbraithwaite.sleeptarget.utils.TimeUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

// REFACTOR [21-06-4 6:47PM] -- move this to the domain?
public class DateRange
{
//*********************************************************
// private properties
//*********************************************************

    private Date mStart;
    private Date mEnd;
    private int mDifferenceInDays;
    private TimeUtils mTimeUtils;

//*********************************************************
// constructors
//*********************************************************

    // used with the static factory methods
    private DateRange()
    {
        mTimeUtils = createTimeUtils();
    }
    
    public DateRange(Date start, Date end)
    {
        init(start, end);
        mTimeUtils = createTimeUtils();
    }
    
    public DateRange(DateRange other)
    {
        mStart = other.mStart;
        mEnd = other.mEnd;
        mDifferenceInDays = other.mDifferenceInDays;
        mTimeUtils = other.mTimeUtils;
    }

//*********************************************************
// overrides
//*********************************************************

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 13 * hash + mDifferenceInDays;
        hash = 13 * hash + mStart.hashCode();
        hash = 13 * hash + mEnd.hashCode();
        return hash;
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        DateRange dateRange = (DateRange) o;
        return mDifferenceInDays == dateRange.mDifferenceInDays &&
               mStart.equals(dateRange.mStart) &&
               mEnd.equals(dateRange.mEnd);
    }



//*********************************************************
// api
//*********************************************************

    
    /**
     * Returns a DateRange where getStart() returns Monday of the week of the provided Date, set to
     * offsetMillis and getEnd() returns the Monday of the next week, set to the same time
     *
     * @param date         the date to get the week of as a range
     * @param offsetMillis offset from 00:00:00 of the week start & end
     */
    public static DateRange asWeekOf(Date date, int offsetMillis)
    {
        DateRange dateRange = new DateRange();
        
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(date);
        // start
        // I need to account Calendar's default week starting on sunday instead of monday
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        dateRange.mTimeUtils.setCalendarTimeOfDay(cal, offsetMillis);
        Date start = cal.getTime();
        
        // end
        cal.add(Calendar.DAY_OF_WEEK, 7);
        Date end = cal.getTime();
        
        return dateRange.init(start, end);
    }
    
    /**
     * Returns the week of the date, with the start and end being at midnight.
     */
    public static DateRange asWeekOf(Date date)
    {
        return DateRange.asWeekOf(date, 0);
    }
    
    public static DateRange asMonthOf(Date date, long offsetMillis)
    {
        DateRange dateRange = new DateRange();
        
        GregorianCalendar start = new GregorianCalendar();
        start.setTime(date);
        start.set(Calendar.DAY_OF_MONTH, 1);
        GregorianCalendar end = new GregorianCalendar();
        end.setTime(start.getTime());
        end.add(Calendar.MONTH, 1);
        
        dateRange.mTimeUtils.setCalendarTimeOfDay(start, offsetMillis);
        dateRange.mTimeUtils.setCalendarTimeOfDay(end, offsetMillis);
        
        return dateRange.init(start.getTime(), end.getTime());
    }
    
    public static DateRange asMonthOf(Date date)
    {
        return DateRange.asMonthOf(date, 0);
    }
    
    public static DateRange asYearOf(Date date, long offsetMillis)
    {
        // REFACTOR [21-02-26 11:15PM] -- this duplicates the logic in asMonthOf().
        DateRange dateRange = new DateRange();
        
        GregorianCalendar start = new GregorianCalendar();
        start.setTime(date);
        start.set(Calendar.DAY_OF_YEAR, 1);
        GregorianCalendar end = new GregorianCalendar();
        end.setTime(start.getTime());
        end.add(Calendar.YEAR, 1);
        
        dateRange.mTimeUtils.setCalendarTimeOfDay(start, offsetMillis);
        dateRange.mTimeUtils.setCalendarTimeOfDay(end, offsetMillis);
        
        return dateRange.init(start.getTime(), end.getTime());
    }
    
    public Date getStart()
    {
        return mStart;
    }
    
    public Date getEnd()
    {
        return mEnd;
    }
    
    public int getDifferenceInDays()
    {
        return mDifferenceInDays;
    }
    
    public DateRange offsetDays(int days)
    {
        return offsetStartDays(days).offsetEndDays(days);
    }
    
    // TODO [21-06-25 10:02PM] -- check that start doesn't come after end.
    public DateRange offsetStartDays(int days)
    {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(mStart);
        cal.add(Calendar.DAY_OF_WEEK, days);
        mStart = cal.getTime();
        
        return this;
    }
    
    // TODO [21-06-25 10:02PM] -- check that start doesn't come after end.
    public DateRange offsetEndDays(int days)
    {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(mEnd);
        cal.add(Calendar.DAY_OF_WEEK, days);
        mEnd = cal.getTime();
        
        return this;
    }

//*********************************************************
// protected api
//*********************************************************

    protected TimeUtils createTimeUtils()
    {
        return new TimeUtils();
    }

//*********************************************************
// private methods
//*********************************************************

    private DateRange init(Date start, Date end)
    {
        mStart = start;
        mEnd = end;
        mDifferenceInDays = computeDifferenceInDays(start, end);
        return this;
    }
    
    private int computeDifferenceInDays(Date start, Date end)
    {
        return (int) (((((end.getTime() - start.getTime()) / 1000) / 60) / 60) / 24);
    }
}
