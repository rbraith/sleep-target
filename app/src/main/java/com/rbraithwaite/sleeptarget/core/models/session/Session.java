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

package com.rbraithwaite.sleeptarget.core.models.session;

import androidx.annotation.NonNull;

import com.rbraithwaite.sleeptarget.core.models.SleepSession;
import com.rbraithwaite.sleeptarget.utils.TimeUtils;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class Session
        implements Serializable
{
//*********************************************************
// private properties
//*********************************************************

    private Date mStart;
    private long mDurationMillis;

//*********************************************************
// public constants
//*********************************************************

    public static final long serialVersionUID = 20210112L;
    
//*********************************************************
// public helpers
//*********************************************************

    public static class InvalidDateError
            extends RuntimeException
    {
        public InvalidDateError(String message)
        {
            super(message);
        }
    }
    
    public static class InvalidDurationError
            extends RuntimeException
    {
        public InvalidDurationError(String message)
        {
            super(message);
        }
    }
    
//*********************************************************
// constructors
//*********************************************************

    public Session(@NonNull Date start, long durationMillis)
    {
        setStart(start);
        setDurationMillis(durationMillis);
    }
    
//*********************************************************
// overrides
//*********************************************************

    @Override
    public int hashCode()
    {
        int result = mStart.hashCode();
        result = 31 * result + (int) (mDurationMillis ^ (mDurationMillis >>> 32));
        return result;
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        
        Session session = (Session) o;
        
        if (mDurationMillis != session.mDurationMillis) { return false; }
        return mStart.equals(session.mStart);
    }
    
//*********************************************************
// api
//*********************************************************

    public Date getStart()
    {
        return mStart;
    }
    
    /**
     * Note that the duration is relative to the start, so this will offset the end value.
     */
    public void setStart(Date start)
    {
        mStart = start;
    }
    
    public long getDurationMillis()
    {
        return mDurationMillis;
    }
    
    public void setDurationMillis(long durationMillis)
    {
        // OPTIMIZE [21-03-26 2:03AM] -- It's not ideal to always & blindly be validating the inputs
        //  inside here - there are many cases where I can be confident that the input data is
        //  already valid. I need to develop a general & flexible strategy for input validation.
        if (durationMillis < 0) {
            throw new SleepSession.InvalidDurationError("durationMillis cannot be < 0.");
        }
        
        mDurationMillis = durationMillis;
    }
    
    public Date getEnd()
    {
        Date start = getStart();
        if (start == null) {
            return null;
        }
        long durationMillis = getDurationMillis();
        
        return new TimeUtils().getDateFromMillis(start.getTime() + durationMillis);
    }
    
    public void setStartFixed(Date start)
    {
        setStartFixed(TimeUtils.getCalendarFrom(start));
    }
    
    /**
     * Set the start of the session in a fixed way. In other words, change the start while keeping
     * the current end date the same (such that it is the duration that changes).
     *
     * @param start The new start date. If this comes after the current end date an {@link
     *              Session.InvalidDateError} is thrown.
     */
    public void setStartFixed(@NonNull Calendar start)
    {
        if (isInvalidStartAndEnd(start.getTime(), getEnd())) {
            throw new SleepSession.InvalidDateError(String.format(
                    "Start date (%s) cannot be after end date (%s)",
                    start.getTime().toString(), getEnd().toString()));
        }
        
        mDurationMillis = getEnd().getTime() - start.getTimeInMillis();
        mStart = start.getTime();
    }
    
    /**
     * Offset the session start by the given hours and minutes in a fixed way (The end date stays
     * the same, so the duration is changed).
     */
    public void offsetStartFixed(int hours, int minutes)
    {
        GregorianCalendar start = TimeUtils.getCalendarFrom(getStart());
        start.add(Calendar.MILLISECOND,
                  (int) TimeUtils.timeToMillis(hours, minutes, 0, 0));
        setStartFixed(start);
    }
    
    /**
     * Offset the session end by the given hours and minutes in a fixed way (The start date stays
     * the same, so the duration is changed).
     */
    public void offsetEndFixed(int hours, int minutes)
    {
        GregorianCalendar end = TimeUtils.getCalendarFrom(getEnd());
        end.add(Calendar.MILLISECOND,
                (int) TimeUtils.timeToMillis(hours, minutes, 0, 0));
        setEndFixed(end);
    }
    
    /**
     * Set the end of the sleep session in a fixed way. In other words, change the end while keeping
     * the current start date the same (such that it is the duration that changes).
     *
     * @param end The new end date. If this comes before the current start date an {@link
     *            SleepSession.InvalidDateError} is thrown.
     */
    public void setEndFixed(@NonNull Calendar end)
    {
        if (isInvalidStartAndEnd(getStart(), end.getTime())) {
            throw new InvalidDateError(String.format(
                    "Start date (%s) cannot be after end date (%s)",
                    getStart().toString(), end.getTime().toString()));
        }
        
        mDurationMillis = end.getTimeInMillis() - getStart().getTime();
    }
    
    public void setEndFixed(@NonNull Date end)
    {
        setEndFixed(TimeUtils.getCalendarFrom(end));
    }
    
//*********************************************************
// private methods
//*********************************************************

    private boolean isInvalidStartAndEnd(Date start, Date end)
    {
        return start.getTime() > end.getTime();
    }
}
