package com.rbraithwaite.sleepapp.ui.stats.chart_intervals.data_set;

import com.rbraithwaite.sleepapp.ui.stats.chart_intervals.DateRange;

public class IntervalDataPoint
{
//*********************************************************
// public properties
//*********************************************************

    public long startTime;
    public long endTime;
    
//*********************************************************
// constructors
//*********************************************************

    public IntervalDataPoint(long startTime, long endTime)
    {
        this.startTime = startTime;
        this.endTime = endTime;
    }
    
//*********************************************************
// api
//*********************************************************

    public void clipTo(DateRange dateRange)
    {
        if (this.startsBefore(dateRange)) {
            this.startTime = dateRange.getStart().getTime();
        }
        if (this.endsAfter(dateRange)) {
            this.endTime = dateRange.getEnd().getTime();
        }
    }
    
    public boolean startsBefore(DateRange dateRange)
    {
        return this.startTime < dateRange.getStart().getTime();
    }
    
    public boolean endsAfter(DateRange dateRange)
    {
        return this.endTime > dateRange.getEnd().getTime();
    }
    
    /**
     * Assumes this interval currently has absolute times. This will return this interval, but now
     * relative to the provided absolute time.
     */
    public IntervalDataPoint relativeTo(long absDatetimeMillis)
    {
        return new IntervalDataPoint(
                this.startTime - absDatetimeMillis,
                this.endTime - absDatetimeMillis);
    }
    
    public long getDuration()
    {
        return this.endTime - this.startTime;
    }
}
