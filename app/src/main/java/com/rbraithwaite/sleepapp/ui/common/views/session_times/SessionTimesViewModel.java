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

package com.rbraithwaite.sleepapp.ui.common.views.session_times;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.rbraithwaite.sleepapp.core.models.session.Session;
import com.rbraithwaite.sleepapp.utils.LiveDataUtils;
import com.rbraithwaite.sleepapp.utils.TimeUtils;
import com.rbraithwaite.sleepapp.utils.time.Day;
import com.rbraithwaite.sleepapp.utils.time.TimeOfDay;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Optional;

public class SessionTimesViewModel
{
//*********************************************************
// private properties
//*********************************************************

    private MutableLiveData<Session> mSession;
    private TimeUtils mTimeUtils;
    
//*********************************************************
// public helpers
//*********************************************************

    public static class InvalidDateTimeException
            extends RuntimeException
    {
        public InvalidDateTimeException(String message)
        {
            super(message);
        }
    }
    
    public static class FutureDateTimeException
            extends RuntimeException
    {
        public FutureDateTimeException(String message)
        {
            super(message);
        }
    }
    
//*********************************************************
// constructors
//*********************************************************

    public SessionTimesViewModel(Session session, TimeUtils timeUtils)
    {
        mSession = new MutableLiveData<>(session);
        mTimeUtils = timeUtils;
    }
    
//*********************************************************
// api
//*********************************************************

    public LiveData<Date> getStart()
    {
        return Transformations.map(mSession, Session::getStart);
    }
    
    public LiveData<Date> getEnd()
    {
        return Transformations.map(mSession, Session::getEnd);
    }
    
    public GregorianCalendar getStartCalendar()
    {
        return getOptionalSession()
                .map(session -> TimeUtils.getCalendarFrom(session.getStart()))
                .orElse(null);
    }
    
    public GregorianCalendar getEndCalendar()
    {
        return getOptionalSession()
                .map(session -> TimeUtils.getCalendarFrom(session.getEnd()))
                .orElse(null);
    }
    
    public void setStartDate(Day day)
    {
        setStartDate(day.year, day.month, day.dayOfMonth);
    }
    
    public void setStartDate(int year, int month, int dayOfMonth)
    {
        updateStart(new int[][] {
                {Calendar.YEAR, year},
                {Calendar.MONTH, month},
                {Calendar.DAY_OF_MONTH, dayOfMonth}
        });
    }
    
    public void setStartTimeOfDay(int hourOfDay, int minute)
    {
        updateStart(new int[][] {
                {Calendar.HOUR_OF_DAY, hourOfDay},
                {Calendar.MINUTE, minute}
        });
    }
    
    public LiveData<String> getDurationText()
    {
        return Transformations.map(mSession,
                                   session -> SessionTimesFormatting.formatDuration(session.getDurationMillis()));
    }
    
    public void setEndDate(Day day)
    {
        setEndDate(day.year, day.month, day.dayOfMonth);
    }
    
    public void setEndDate(int year, int month, int dayOfMonth)
    {
        updateEnd(new int[][] {
                {Calendar.YEAR, year},
                {Calendar.MONTH, month},
                {Calendar.DAY_OF_MONTH, dayOfMonth}
        });
    }
    
    public void setEndTimeOfDay(TimeOfDay timeOfDay)
    {
        setEndTimeOfDay(timeOfDay.hourOfDay, timeOfDay.minute);
    }
    
    public void setEndTimeOfDay(int hourOfDay, int minute)
    {
        updateEnd(new int[][] {
                {Calendar.HOUR_OF_DAY, hourOfDay},
                {Calendar.MINUTE, minute}
        });
    }
    
    public void setStartTimeOfDay(TimeOfDay timeOfDay)
    {
        setStartTimeOfDay(timeOfDay.hourOfDay, timeOfDay.minute);
    }
    
//*********************************************************
// private methods
//*********************************************************

    private void updateStart(int[][] calFields)
    {
        Session session = mSession.getValue();
        if (session == null) {
            return;
        }
        
        GregorianCalendar cal = new GregorianCalendar();
        Date oldStart = session.getStart();
        cal.setTime(oldStart);
        for (int[] fields : calFields) {
            int field = fields[0];
            int value = fields[1];
            cal.set(field, value);
        }
        Date newStart = cal.getTime();
        
        if (newStart.equals(oldStart)) {
            return;
        }
        
        checkIfDateIsInTheFuture(newStart);
        
        try {
            session.setStartFixed(cal);
            LiveDataUtils.refresh(mSession);
        } catch (Session.InvalidDateError e) {
            // REFACTOR [21-03-25 12:55AM] -- Is this exception conversion necessary, or is it
            //  alright to have the view handle a domain exception?
            throw new InvalidDateTimeException(e.getMessage());
        }
    }
    
    private void updateEnd(int[][] calFields)
    {
        Session session = mSession.getValue();
        if (session == null) {
            return;
        }
        
        GregorianCalendar cal = new GregorianCalendar();
        Date oldEnd = session.getEnd();
        cal.setTime(oldEnd);
        for (int[] fields : calFields) {
            int field = fields[0];
            int value = fields[1];
            cal.set(field, value);
        }
        Date newEnd = cal.getTime();
        
        if (newEnd.equals(oldEnd)) {
            return;
        }
        
        checkIfDateIsInTheFuture(newEnd);
        
        try {
            session.setEndFixed(cal);
            LiveDataUtils.refresh(mSession);
        } catch (Session.InvalidDateError e) {
            // REFACTOR [21-03-25 12:55AM] -- Is this exception conversion necessary, or is it
            //  alright to have the view handle a domain exception?
            throw new InvalidDateTimeException(e.getMessage());
        }
    }
    
    private Optional<Session> getOptionalSession()
    {
        return Optional.ofNullable(mSession.getValue());
    }
    
    /**
     * Throw a FutureDateTimeException if the date is in the future.
     */
    private void checkIfDateIsInTheFuture(Date date)
    {
        if (mTimeUtils.getNow().getTime() < date.getTime()) {
            throw new FutureDateTimeException(date.toString());
        }
    }
}
