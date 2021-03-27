package com.rbraithwaite.sleepapp.ui.session_data;

import androidx.arch.core.util.Function;
import androidx.hilt.lifecycle.ViewModelInject;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.rbraithwaite.sleepapp.core.models.SleepSession;
import com.rbraithwaite.sleepapp.di.UIDependenciesModule;
import com.rbraithwaite.sleepapp.ui.format.DateTimeFormatter;
import com.rbraithwaite.sleepapp.ui.format.DurationFormatter;
import com.rbraithwaite.sleepapp.ui.session_data.data.SleepSessionWrapper;
import com.rbraithwaite.sleepapp.utils.TimeUtils;

import java.util.Calendar;
import java.util.GregorianCalendar;

// REFACTOR [21-01-5 2:30AM] -- passing around Dates and longs for times-of-day or dates is too
//  much information. Instead there should be TimeOfDay and Date which are simple POJOs and
//  contain eg hourOfDay & minute fields. Then I can use Date & Calendar in the impls to convert
//  for storage or display
public class SessionDataFragmentViewModel
        extends ViewModel
{
//*********************************************************
// private properties
//*********************************************************

    private LiveData<String> mSessionDurationText;
    
    private DateTimeFormatter mDateTimeFormatter;
    
    private MutableLiveData<SleepSession> mSleepSession = new MutableLiveData<>();

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

//*********************************************************
// constructors
//*********************************************************

    @ViewModelInject
    public SessionDataFragmentViewModel(
            // REFACTOR [21-03-24 11:06PM] -- This should be SessionDataFormatting.
            @UIDependenciesModule.SessionDataDateTimeFormatter DateTimeFormatter dateTimeFormatter)
    {
        mDateTimeFormatter = dateTimeFormatter;
    }
    
//*********************************************************
// api
//*********************************************************

    public SleepSessionWrapper getResult()
    {
        return new SleepSessionWrapper(mSleepSession.getValue());
    }
    
    public LiveData<String> getSessionDurationText()
    {
        // REFACTOR [21-03-25 12:11AM] -- This should be SessionDataFormatting.
        final DurationFormatter formatter = new DurationFormatter();
        
        if (mSessionDurationText == null) {
            final MediatorLiveData<String> mediatorLiveData = new MediatorLiveData<>();
            mediatorLiveData.addSource(getSleepSession(), new Observer<SleepSession>()
            {
                @Override
                public void onChanged(SleepSession sleepSession)
                {
                    if (sleepSession == null) {
                        mediatorLiveData.setValue(null);
                    } else {
                        mediatorLiveData.setValue(
                                formatter.formatDurationMillis(sleepSession.getDurationMillis()));
                    }
                }
            });
            mSessionDurationText = mediatorLiveData;
        }
        
        return mSessionDurationText;
    }
    
    public LiveData<String> getStartTimeText()
    {
        return Transformations.map(
                getSleepSession(),
                new Function<SleepSession, String>()
                {
                    @Override
                    public String apply(SleepSession input)
                    {
                        if (input == null) {
                            return null;
                        }
                        return mDateTimeFormatter.formatTimeOfDay(input.getStart());
                    }
                });
    }
    
    public LiveData<String> getEndTimeText()
    {
        return Transformations.map(
                getSleepSession(),
                new Function<SleepSession, String>()
                {
                    @Override
                    public String apply(SleepSession input)
                    {
                        if (input == null) {
                            return null;
                        }
                        return mDateTimeFormatter.formatTimeOfDay(input.getEnd());
                    }
                });
    }
    
    public LiveData<String> getEndDateText()
    {
        return Transformations.map(
                getSleepSession(),
                new Function<SleepSession, String>()
                {
                    @Override
                    public String apply(SleepSession input)
                    {
                        if (input == null) {
                            return null;
                        }
                        return mDateTimeFormatter.formatDate(input.getEnd());
                    }
                });
    }
    
    public LiveData<String> getStartDateText()
    {
        return Transformations.map(
                getSleepSession(),
                new Function<SleepSession, String>()
                {
                    @Override
                    public String apply(SleepSession input)
                    {
                        if (input == null) {
                            return null;
                        }
                        return mDateTimeFormatter.formatDate(input.getStart());
                    }
                });
    }
    
    public void setStartDay(int year, int month, int dayOfMonth)
    {
        // OPTIMIZE [20-11-30 11:19PM] -- consider doing nothing if the new date
        //  matches the old.
        
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(mSleepSession.getValue().getStart());
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        
        try {
            mSleepSession.getValue().setStartFixed(calendar);
            notifySessionChanged();
        } catch (SleepSession.InvalidDateError e) {
            // REFACTOR [21-03-25 12:55AM] -- Is this exception conversion necessary, or is it
            //  alright to have the view handle a domain exception?
            throw new InvalidDateTimeException((e.getMessage()));
        }
    }
    
    public void setEndDay(int year, int month, int dayOfMonth)
    {
        // OPTIMIZE [20-11-30 11:19PM] -- consider doing nothing if the new date
        //  matches the old.
        
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(mSleepSession.getValue().getEnd());
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        
        try {
            mSleepSession.getValue().setEndFixed(calendar);
            notifySessionChanged();
        } catch (SleepSession.InvalidDateError e) {
            // REFACTOR [21-03-25 12:55AM] -- Is this exception conversion necessary, or is it
            //  alright to have the view handle a domain exception?
            throw new InvalidDateTimeException((e.getMessage()));
        }
    }
    
    public void setStartTime(int hourOfDay, int minute)
    {
        // OPTIMIZE [20-12-6 8:36PM] -- consider doing nothing if the new time matches the old.
        
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(mSleepSession.getValue().getStart());
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        
        try {
            mSleepSession.getValue().setStartFixed(calendar);
            notifySessionChanged();
        } catch (SleepSession.InvalidDateError e) {
            // REFACTOR [21-03-25 12:55AM] -- Is this exception conversion necessary, or is it
            //  alright to have the view handle a domain exception?
            throw new InvalidDateTimeException((e.getMessage()));
        }
    }
    
    public void setEndTime(int hourOfDay, int minute)
    {
        // OPTIMIZE [20-12-6 8:36PM] -- consider doing nothing if the new time matches the old.
        
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(mSleepSession.getValue().getEnd());
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        
        try {
            mSleepSession.getValue().setEndFixed(calendar);
            notifySessionChanged();
        } catch (SleepSession.InvalidDateError e) {
            // REFACTOR [21-03-25 12:55AM] -- Is this exception conversion necessary, or is it
            //  alright to have the view handle a domain exception?
            throw new InvalidDateTimeException((e.getMessage()));
        }
    }
    
    public LiveData<Long> getEndDateTime()
    {
        return Transformations.map(
                getSleepSession(),
                new Function<SleepSession, Long>()
                {
                    @Override
                    public Long apply(SleepSession input)
                    {
                        if (input == null) {
                            return null;
                        }
                        // REFACTOR [21-03-25 1:07AM] -- This should be getEndMillis? (demeter)
                        return input.getEnd().getTime();
                    }
                });
    }
    
    public LiveData<Long> getStartDateTime()
    {
        return Transformations.map(
                getSleepSession(),
                new Function<SleepSession, Long>()
                {
                    @Override
                    public Long apply(SleepSession input)
                    {
                        if (input == null) {
                            return null;
                        }
                        // REFACTOR [21-03-25 1:07AM] -- This should be getStartMillis? (demeter)
                        return input.getStart().getTime();
                    }
                });
    }
    
    public void clearSessionData()
    {
        mSleepSession.setValue(null);
    }
    
    public void setSessionData(SleepSessionWrapper sessionData)
    {
        mSleepSession.setValue(sessionData.getModel());
    }
    
//*********************************************************
// protected api
//*********************************************************

    // REFACTOR [21-03-5 12:53AM] -- consider ctor injecting this instead?
    protected TimeUtils createTimeUtils()
    {
        return new TimeUtils();
    }
    
//*********************************************************
// private methods
//*********************************************************

    private LiveData<SleepSession> getSleepSession()
    {
        return mSleepSession;
    }

    private void notifySessionChanged()
    {
        mSleepSession.setValue(mSleepSession.getValue());
    }
}
