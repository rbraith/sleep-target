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
package com.rbraithwaite.sleeptarget.ui.common.views.session_times;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.rbraithwaite.sleeptarget.core.models.session.Session;
import com.rbraithwaite.sleeptarget.ui.common.views.datetime.DateTimeViewModel;
import com.rbraithwaite.sleeptarget.utils.CommonUtils;
import com.rbraithwaite.sleeptarget.utils.LiveDataEvent;
import com.rbraithwaite.sleeptarget.utils.LiveDataUtils;
import com.rbraithwaite.sleeptarget.utils.TimeUtils;
import com.rbraithwaite.sleeptarget.utils.time.Day;
import com.rbraithwaite.sleeptarget.utils.time.TimeOfDay;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Optional;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class SessionTimesViewModel
        extends ViewModel
{
//*********************************************************
// private properties
//*********************************************************

    private MutableLiveData<Session> mSession;
    private TimeUtils mTimeUtils;
    
    private boolean mInitialized = false;
    private DateTimeViewModel mEndDateTimeViewModel;
    
    private DateTimeViewModel mStartDateTimeViewModel;
    
    private MutableLiveData<LiveDataEvent<Integer>> mErrorDialogEvent = new MutableLiveData<>();
    
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

    @Inject
    public SessionTimesViewModel(TimeUtils timeUtils)
    {
        mTimeUtils = timeUtils;
    }
    
//*********************************************************
// api
//*********************************************************

    public void init(Session session)
    {
        if (!mInitialized) {
            mInitialized = true;
            mSession = new MutableLiveData<>(session);
        }
    }
    
    public LiveData<Date> getStart()
    {
        return Transformations.map(mSession, Session::getStart);
    }
    
    public LiveData<Date> getEnd()
    {
        return Transformations.map(mSession, Session::getEnd);
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
    
    public DateTimeViewModel getEndDateTimeViewModel()
    {
        mEndDateTimeViewModel = CommonUtils.lazyInit(mEndDateTimeViewModel, () -> {
            return createDateTimeViewModelFrom(getEndCalendar());
        });
        return mEndDateTimeViewModel;
    }
    
    public DateTimeViewModel getStartDateTimeViewModel()
    {
        mStartDateTimeViewModel = CommonUtils.lazyInit(mStartDateTimeViewModel, () -> {
            return createDateTimeViewModelFrom(getStartCalendar());
        });
        return mStartDateTimeViewModel;
    }
    
    public LiveData<LiveDataEvent<Integer>> errorDialogEvent()
    {
        return mErrorDialogEvent;
    }
    
    public void triggerErrorDialogEvent(int errorMessageId)
    {
        mErrorDialogEvent.setValue(new LiveDataEvent<>(errorMessageId));
    }

//*********************************************************
// private methods
//*********************************************************

    private GregorianCalendar getStartCalendar()
    {
        return getOptionalSession()
                .map(session -> TimeUtils.getCalendarFrom(session.getStart()))
                .orElse(null);
    }
    
    private GregorianCalendar getEndCalendar()
    {
        return getOptionalSession()
                .map(session -> TimeUtils.getCalendarFrom(session.getEnd()))
                .orElse(null);
    }
    
    private DateTimeViewModel createDateTimeViewModelFrom(Calendar cal)
    {
        DateTimeViewModel viewModel = new DateTimeViewModel();
        viewModel.setDate(Day.of(cal));
        viewModel.setTimeOfDay(TimeOfDay.of(cal));
        return viewModel;
    }
    
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
