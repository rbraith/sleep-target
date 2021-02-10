package com.rbraithwaite.sleepapp.ui.session_data;

import androidx.arch.core.util.Function;
import androidx.hilt.lifecycle.ViewModelInject;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.rbraithwaite.sleepapp.data.current_goals.SleepDurationGoalModel;
import com.rbraithwaite.sleepapp.data.database.convert.DateConverter;
import com.rbraithwaite.sleepapp.data.sleep_session.SleepSessionModel;
import com.rbraithwaite.sleepapp.ui.UIDependenciesModule;
import com.rbraithwaite.sleepapp.ui.format.DateTimeFormatter;
import com.rbraithwaite.sleepapp.ui.format.DurationFormatter;
import com.rbraithwaite.sleepapp.ui.session_data.data.SleepSessionWrapper;
import com.rbraithwaite.sleepapp.utils.DateUtils;
import com.rbraithwaite.sleepapp.utils.LiveDataUtils;

import java.util.Calendar;
import java.util.Date;
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

    private int mSessionId = 0;
    
    private boolean mIsInitialized;

//*********************************************************
// package properties
//*********************************************************

    LiveData<String> mStartTime;
    LiveData<String> mStartDate;
    LiveData<String> mEndTime;
    LiveData<String> mEndDate;
    LiveData<String> mSessionDuration;
    MutableLiveData<Long> mStartDateTime;
    MutableLiveData<Long> mEndDateTime;
    DateTimeFormatter mDateTimeFormatter;
    
    MutableLiveData<Date> mWakeTimeGoal;
    MutableLiveData<SleepDurationGoalModel> mSleepDurationGoal;

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
            @UIDependenciesModule.SessionDataDateTimeFormatter DateTimeFormatter dateTimeFormatter)
    {
        mDateTimeFormatter = dateTimeFormatter;
    }

//*********************************************************
// api
//*********************************************************

    public SleepSessionWrapper getResult()
    {
        // REFACTOR [21-12-30 3:06PM] -- like mentioned in initSessionData, there should just be
        //  an internal SleepSessionModel member, instead of needing to recompose one here.
        return new SleepSessionWrapper(new SleepSessionModel(
                mSessionId,
                DateUtils.getDateFromMillis(getStartDateTime().getValue()),
                getEndDateTime().getValue() - getStartDateTime().getValue(),
                getWakeTimeGoalMutable().getValue(),
                getSleepDurationGoalMutable().getValue()));
    }
    
    public LiveData<String> getSessionDuration()
    {
        // REFACTOR [21-01-11 3:18AM] -- inject this.
        final DurationFormatter formatter = new DurationFormatter();
        
        if (mSessionDuration == null) {
            final MediatorLiveData<String> mediatorLiveData = new MediatorLiveData<>();
            // update on start changed
            mediatorLiveData.addSource(getStartDateTimeMutable(), new Observer<Long>()
            {
                @Override
                public void onChanged(Long newStartTime)
                {
                    mediatorLiveData.setValue(formatter.formatDurationMillis(
                            computeDurationMillis(newStartTime,
                                                  getEndDateTimeMutable().getValue())));
                }
            });
            // update on end changed
            mediatorLiveData.addSource(getEndDateTimeMutable(), new Observer<Long>()
            {
                @Override
                public void onChanged(Long newEndTime)
                {
                    mediatorLiveData.setValue(formatter.formatDurationMillis(
                            computeDurationMillis(getStartDateTimeMutable().getValue(),
                                                  newEndTime)));
                }
            });
            mSessionDuration = mediatorLiveData;
        }
        
        return mSessionDuration;
    }
    
    public LiveData<String> getStartTime()
    {
        return lazyInitLiveDateTime(mStartTime,
                                    getStartDateTimeMutable(),
                                    DateTimeFormatType.TIME_OF_DAY);
    }
    
    public LiveData<String> getEndTime()
    {
        return lazyInitLiveDateTime(mEndTime,
                                    getEndDateTimeMutable(),
                                    DateTimeFormatType.TIME_OF_DAY);
    }
    
    public LiveData<String> getEndDate()
    {
        return lazyInitLiveDateTime(mEndDate, getEndDateTimeMutable(), DateTimeFormatType.DATE);
    }
    
    public LiveData<String> getStartDate()
    {
        return lazyInitLiveDateTime(mStartDate, getStartDateTimeMutable(), DateTimeFormatType.DATE);
    }
    
    public void setStartDate(int year, int month, int dayOfMonth)
    {
        // update the start date
        GregorianCalendar calendar = new GregorianCalendar();
        // REFACTOR [20-12-6 8:37PM] -- this doesn't need to be mutable - use getStartDateTime.
        calendar.setTimeInMillis(getStartDateTimeMutable().getValue());
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        
        if (isInvalidStartDateTime(calendar.getTimeInMillis())) {
            throw new InvalidDateTimeException(String.format(
                    "Invalid start date: %s (start) > %s (end)",
                    mDateTimeFormatter.formatDate(calendar.getTime()),
                    getEndDate().getValue()));
        }
        
        // OPTIMIZE [20-11-30 11:19PM] -- consider doing nothing if the new date
        //  matches the old.
        
        // set the new date
        setStartDateTime(calendar.getTimeInMillis());
    }
    
    public void setEndDate(int year, int month, int dayOfMonth)
    {
        // update the end date
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(getEndDateTime().getValue());
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        
        if (isInvalidEndDateTime(calendar.getTimeInMillis())) {
            throw new InvalidDateTimeException(String.format(
                    "Invalid end date: %s (end) < %s (start)",
                    mDateTimeFormatter.formatDate(calendar.getTime()),
                    getStartDate().getValue()));
        }
        
        // OPTIMIZE [20-11-30 11:19PM] -- consider doing nothing if the new date
        //  matches the old.
        
        // set the new date
        setEndDateTime(calendar.getTimeInMillis());
    }
    
    public void setStartTime(int hourOfDay, int minute)
    {
        // REFACTOR [20-12-6 8:35PM] -- this essentially duplicates the logic of
        //  setStartDate - consider possibilities for reducing that repetition.
        
        // OPTIMIZE [20-12-6 8:36PM] -- consider doing nothing if the new time matches the old.
        
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(getStartDateTime().getValue());
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        
        if (isInvalidStartDateTime(calendar.getTimeInMillis())) {
            throw new InvalidDateTimeException(String.format(
                    "Invalid start time: %s (start) > %s (end)",
                    mDateTimeFormatter.formatTimeOfDay(calendar.getTime()),
                    getEndTime().getValue()));
        }
        
        setStartDateTime(calendar.getTimeInMillis());
    }
    
    public void setEndTime(int hourOfDay, int minute)
    {
        // REFACTOR [20-12-6 8:35PM] -- this essentially duplicates the logic of
        //  setStartDate - consider possibilities for reducing that repetition.
        
        // OPTIMIZE [20-12-6 8:36PM] -- consider doing nothing if the new time matches the old.
        
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(getEndDateTime().getValue());
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        
        if (isInvalidEndDateTime(calendar.getTimeInMillis())) {
            throw new InvalidDateTimeException(String.format(
                    "Invalid end time: %s (end) < %s (start)",
                    mDateTimeFormatter.formatTimeOfDay(calendar.getTime()),
                    getStartTime().getValue()));
        }
        
        setEndDateTime(calendar.getTimeInMillis());
    }
    
    public LiveData<Long> getEndDateTime()
    {
        return getEndDateTimeMutable();
    }
    
    public void setEndDateTime(long endTime)
    {
        getEndDateTimeMutable().setValue(endTime);
    }
    
    public LiveData<Long> getStartDateTime()
    {
        return getStartDateTimeMutable();
    }
    
    public void setStartDateTime(long startTime)
    {
        getStartDateTimeMutable().setValue(startTime);
    }
    
    public LiveData<String> getWakeTimeGoal()
    {
        return Transformations.map(
                getWakeTimeGoalMutable(),
                new Function<Date, String>()
                {
                    @Override
                    public String apply(Date wakeTimeGoal)
                    {
                        if (wakeTimeGoal == null) {
                            return null;
                        }
                        return mDateTimeFormatter.formatTimeOfDay(wakeTimeGoal);
                    }
                });
    }
    
    public void clearSessionData()
    {
        getStartDateTimeMutable().setValue(null);
        getEndDateTimeMutable().setValue(null);
        getWakeTimeGoalMutable().setValue(null);
        
        mIsInitialized = false;
    }
    
    /**
     * Session data is only initialized once. Subsequent calls to initSessionData do nothing, unless
     * clearSessionData is called.
     */
    public void initSessionData(SleepSessionWrapper initialData)
    {
        if (!sessionDataIsInitialized()) {
            SleepSessionModel sleepSession = initialData.getValue();
            
            // REFACTOR [21-12-29 3:08AM] -- I should just store the SleepSessionModel instead of
            //  breaking it down like this (this breaking-down behaviour is legacy).
            mSessionId = sleepSession.getId();
            getStartDateTimeMutable().setValue(sleepSession.getStart().getTime());
            getEndDateTimeMutable().setValue(sleepSession.getEnd().getTime());
            getWakeTimeGoalMutable().setValue(sleepSession.getWakeTimeGoal());
            getSleepDurationGoalMutable().setValue(sleepSession.getSleepDurationGoal());
            
            mIsInitialized = true;
        }
    }
    
    public boolean sessionDataIsInitialized()
    {
        return mIsInitialized;
    }
    
    // REFACTOR [21-01-15 9:29PM] -- I need to make the wake-time goal representation consistent,
    //  right now its mixing Date & Long all over the place.
    public LiveData<Long> getWakeTimeGoalMillis()
    {
        return Transformations.map(
                getWakeTimeGoalMutable(),
                new Function<Date, Long>()
                {
                    @Override
                    public Long apply(Date input)
                    {
                        return DateConverter.convertDateToMillis(input);
                    }
                });
    }
    
    public void setWakeTimeGoal(int hourOfDay, int minute)
    {
        Date wakeTimeGoal = getWakeTimeGoalMutable().getValue();
        GregorianCalendar calendar = new GregorianCalendar();
        if (wakeTimeGoal != null) {
            calendar.setTime(wakeTimeGoal);
        }
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        getWakeTimeGoalMutable().setValue(calendar.getTime());
    }
    
    public long getDefaultWakeTimeGoalMillis()
    {
        int defaultHourOfDay = 8;
        int defaultMinute = 0;
        
        GregorianCalendar calendar = new GregorianCalendar(
                2021,
                01,
                15,
                defaultHourOfDay,
                defaultMinute);
        
        return calendar.getTimeInMillis();
    }
    
    public void clearWakeTimeGoal()
    {
        getWakeTimeGoalMutable().setValue(null);
    }
    
    public LiveData<String> getSleepDurationGoalText()
    {
        return Transformations.map(
                getSleepDurationGoalMutable(),
                new Function<SleepDurationGoalModel, String>()
                {
                    @Override
                    public String apply(SleepDurationGoalModel input)
                    {
                        // REFACTOR [21-02-7 1:56AM] -- should this null behaviour be in
                        //  SessionDataFormatting.formatSleepDurationGoal instead?
                        return input.isSet() ?
                                SessionDataFormatting.formatSleepDurationGoal(input) :
                                null;
                    }
                });
    }
    
    public void clearSleepDurationGoal()
    {
        getSleepDurationGoalMutable().setValue(new SleepDurationGoalModel());
    }

//*********************************************************
// private methods
//*********************************************************

    private boolean isInvalidStartDateTime(long startDateTime)
    {
        // REFACTOR [20-12-8 12:57AM] -- this doesn't need the mutable version.
        Long endDateTime = getEndDateTimeMutable().getValue();
        // REFACTOR [20-11-30 11:59PM] -- consider making a higher order isInvalidStartEnd(start,
        //  end).
        if (endDateTime == null) {
            // if there is no end datetime then it doesn't matter what the start datetime is
            return false;
        }
        return (startDateTime > endDateTime);
    }
    
    private boolean isInvalidEndDateTime(long endDateTime)
    {
        Long startDateTime = getStartDateTime().getValue();
        if (startDateTime == null) {
            // if there is no start datetime then it doesn't matter what the end datetime is
            return false;
        }
        return (startDateTime > endDateTime);
    }
    
    // REFACTOR [20-11-27 12:14AM] -- extract this for reuse
    //  consider though: the "returning 0 on null values" is kind of a
    //  special behaviour for SessionEditFragmentViewModel (for setting the session duration
    //  text properly)
    //  in the general case, would it be better to throw an exception?
    private long computeDurationMillis(Long startMillis, Long endMillis)
    {
        if (startMillis == null || endMillis == null) {
            return 0;
        }
        return endMillis - startMillis;
    }
    
    /**
     * Transforms the target string (eg startTime, endDate) with the source date (eg startDateTime,
     * endDateTime), on the given format.
     * <p>
     * This means that when the source changes, the target is updated using that format.
     */
    private LiveData<String> lazyInitLiveDateTime(
            LiveData<String> target,
            LiveData<Long> source,
            final DateTimeFormatType formatType)
    {
        if (target == null) {
            target = Transformations.map(source, new Function<Long, String>()
            {
                @Override
                public String apply(Long dateMillis)
                {
                    if (dateMillis == null) {
                        return null;
                    } else {
                        return formatDateTimeMillisFromType(dateMillis, formatType);
                    }
                }
            });
        }
        return target;
    }
    
    private String formatDateTimeMillisFromType(long dateMillis, DateTimeFormatType formatType)
    {
        Date date = DateUtils.getDateFromMillis(dateMillis);
        switch (formatType) {
        case DATE:
            return mDateTimeFormatter.formatDate(date);
        case TIME_OF_DAY:
            return mDateTimeFormatter.formatTimeOfDay(date);
        default:
            throw new IllegalArgumentException("Invalid format type.");
        }
    }
    
    private MutableLiveData<SleepDurationGoalModel> getSleepDurationGoalMutable()
    {
        mSleepDurationGoal = LiveDataUtils.lazyInitMutable(mSleepDurationGoal);
        return mSleepDurationGoal;
    }
    
    private MutableLiveData<Date> getWakeTimeGoalMutable()
    {
        mWakeTimeGoal = LiveDataUtils.lazyInitMutable(mWakeTimeGoal, null);
        return mWakeTimeGoal;
    }
    
    private MutableLiveData<Long> getStartDateTimeMutable()
    {
        mStartDateTime = LiveDataUtils.lazyInitMutable(mStartDateTime, null);
        return mStartDateTime;
    }
    
    private MutableLiveData<Long> getEndDateTimeMutable()
    {
        mEndDateTime = LiveDataUtils.lazyInitMutable(mEndDateTime, null);
        return mEndDateTime;
    }

//*********************************************************
// private helpers
//*********************************************************

    // REFACTOR [20-11-27 1:37AM] -- consider moving this behaviour into DateTimeFormatter
    //  - would this be generally useful?
    private enum DateTimeFormatType
    {
        DATE,
        TIME_OF_DAY,
    }
}
