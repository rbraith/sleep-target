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

import java.util.Date;

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
            mediatorLiveData.addSource(getStartDateTime(), new Observer<Long>()
            {
                @Override
                public void onChanged(Long newStartTime)
                {
                    mediatorLiveData.setValue(formatter.formatDurationMillis(
                            computeDurationMillis(newStartTime, getEndDateTime().getValue())));
                }
            });
            // update on end changed
            mediatorLiveData.addSource(getEndDateTime(), new Observer<Long>()
            {
                @Override
                public void onChanged(Long newEndTime)
                {
                    mediatorLiveData.setValue(formatter.formatDurationMillis(
                            computeDurationMillis(getStartDateTime().getValue(), newEndTime)));
                }
            });
            mSessionDuration = mediatorLiveData;
        }
        
        return mSessionDuration;
    }
    
    public LiveData<String> getStartTime()
    {
        return lazyInitLiveDateTime(mStartTime, getStartDateTime(), DateTimeFormatType.TIME_OF_DAY);
    }
    
    public LiveData<String> getEndTime()
    {
        return lazyInitLiveDateTime(mEndTime, getEndDateTime(), DateTimeFormatType.TIME_OF_DAY);
    }
    
    public LiveData<String> getEndDate()
    {
        return lazyInitLiveDateTime(mEndDate, getEndDateTime(), DateTimeFormatType.DATE);
    }
    
    public LiveData<String> getStartDate()
    {
        return lazyInitLiveDateTime(mStartDate, getStartDateTime(), DateTimeFormatType.DATE);
    }
    
//*********************************************************
// private methods
//*********************************************************

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
    
    private MutableLiveData<Long> getStartDateTime()
    {
        if (mStartDateTime == null) {
            mStartDateTime = new MutableLiveData<>(null);
        }
        return mStartDateTime;
    }
    
    public void setStartDateTime(long startTime)
    {
        // TODO [20-11-23 2:52AM] -- verify startTime is before or equal to end time.
        //  (leaving this for now - this becomes relevant when I implement user input to change
        //  the values).
        getStartDateTime().setValue(startTime);
    }
    
    private MutableLiveData<Long> getEndDateTime()
    {
        if (mEndDateTime == null) {
            mEndDateTime = new MutableLiveData<>(null);
        }
        return mEndDateTime;
    }
    
    public void setEndDateTime(long endTime)
    {
        // TODO [20-11-23 3:23AM] -- verify end time is after or equal to start time.
        //  (leaving this for now - this becomes relevant when I implement user input to change
        //  the values).
        getEndDateTime().setValue(endTime);
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
