package com.rbraithwaite.sleepapp.ui.stats.data;

import com.rbraithwaite.sleepapp.utils.TimeUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateRange
{
//*********************************************************
// private properties
//*********************************************************

    private Date mStart;
    private Date mEnd;
    private int mDifferenceInDays;

//*********************************************************
// constructors
//*********************************************************

    public DateRange(Date start, Date end)
    {
        mStart = start;
        mEnd = end;
        mDifferenceInDays = (int) (((((end.getTime() - start.getTime()) / 1000) / 60) / 60) / 24);
    }
    
    public DateRange(DateRange other)
    {
        mStart = other.mStart;
        mEnd = other.mEnd;
        mDifferenceInDays = other.mDifferenceInDays;
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
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(date);
        // start
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        TimeUtils.setCalendarTimeOfDay(cal, offsetMillis);
        Date start = cal.getTime();
        
        // end
        cal.add(Calendar.DAY_OF_WEEK, 7);
        Date end = cal.getTime();
        
        return new DateRange(start, end);
    }
    
    /**
     * Returns the week of the date, with the start and end being at midnight.
     */
    public static DateRange asWeekOf(Date date)
    {
        return DateRange.asWeekOf(date, 0);
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
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(mStart);
        cal.add(Calendar.DAY_OF_WEEK, days);
        mStart = cal.getTime();
        
        cal.setTime(mEnd);
        cal.add(Calendar.DAY_OF_WEEK, days);
        mEnd = cal.getTime();
        
        return this;
    }
}
