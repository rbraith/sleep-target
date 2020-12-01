package com.rbraithwaite.sleepapp.ui.session_edit;

import androidx.arch.core.util.Function;
import androidx.hilt.lifecycle.ViewModelInject;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.rbraithwaite.sleepapp.ui.format.DateTimeFormatter;
import com.rbraithwaite.sleepapp.ui.format.DurationFormatter;
import com.rbraithwaite.sleepapp.utils.DateUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class SessionEditFragmentViewModel
        extends ViewModel
{
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
    public SessionEditFragmentViewModel()
    {
    }

//*********************************************************
// api
//*********************************************************

    public LiveData<String> getSessionDuration()
    {
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
        calendar.setTimeInMillis(getStartDateTimeMutable().getValue());
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        
        if (isInvalidStartDateTime(calendar.getTimeInMillis())) {
            throw new InvalidDateTimeException(String.format(
                    "Invalid start date: %s",
                    new DateTimeFormatter().formatDate(calendar.getTime())));
        }
        
        // OPTIMIZE [20-11-30 11:19PM] -- consider doing nothing if the new date
        //  matches the old.
        
        // set the new date
        setStartDateTime(calendar.getTimeInMillis());
    }

    public LiveData<Long> getEndDateTime()
    {
        return getEndDateTimeMutable();
    }
    
    public void setEndDateTime(long endTime)
    {
        // TODO [20-11-23 3:23AM] -- verify end time is after or equal to start time.
        //  (leaving this for now - this becomes relevant when I implement user input to change
        //  the values).
        getEndDateTimeMutable().setValue(endTime);
    }
    
    public LiveData<Long> getStartDateTime()
    {
        return getStartDateTimeMutable();
    }
    
    public void setStartDateTime(long startTime)
    {
        // TODO [20-11-23 2:52AM] -- verify startTime is before or equal to end time.
        //  (leaving this for now - this becomes relevant when I implement user input to change
        //  the values).
        getStartDateTimeMutable().setValue(startTime);
    }
    
//*********************************************************
// private methods
//*********************************************************

    private boolean isInvalidStartDateTime(long startDateTime)
    {
        Long endDateTime = getEndDateTimeMutable().getValue();
        // REFACTOR [20-11-30 11:59PM] -- consider making a higher order isInvalidStartEnd(start,
        //  end).
        if (endDateTime == null) {
            // if there is no end datetime then it doesn't matter what the start datetime is
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
        DateTimeFormatter formatter = new DateTimeFormatter();
        Date date = DateUtils.getDateFromMillis(dateMillis);
        switch (formatType) {
        case DATE:
            return formatter.formatDate(date);
        case TIME_OF_DAY:
            return formatter.formatTimeOfDay(date);
        default:
            throw new IllegalArgumentException("Invalid format type.");
        }
    }
    
    private MutableLiveData<Long> getStartDateTimeMutable()
    {
        if (mStartDateTime == null) {
            mStartDateTime = new MutableLiveData<>(null);
        }
        return mStartDateTime;
    }
    
    private MutableLiveData<Long> getEndDateTimeMutable()
    {
        if (mEndDateTime == null) {
            mEndDateTime = new MutableLiveData<>(null);
        }
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
