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

package com.rbraithwaite.sleeptarget.ui.stats.chart_intervals.data_set;

import com.rbraithwaite.sleeptarget.ui.stats.chart_intervals.DateRange;

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
