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

//*********************************************************
// api
//*********************************************************

    
    /**
     * Returns a DateRange where getStart() returns Monday of the week of the provided Date, set to
     * timeOfDayOffsetMillis and getEnd() returns the Monday of the next week, set to the same time
     *
     * @param date                  the date to get the week of as a range
     * @param timeOfDayOffsetMillis Instead of
     */
    public static DateRange asWeekOf(Date date, int timeOfDayOffsetMillis)
    {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(date);
        // start
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        TimeUtils.setCalendarTimeOfDay(cal, timeOfDayOffsetMillis);
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
}
