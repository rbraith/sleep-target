package com.rbraithwaite.sleepapp.ui.sleep_goals;

import androidx.arch.core.util.Function;
import androidx.hilt.lifecycle.ViewModelInject;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.rbraithwaite.sleepapp.data.SleepAppRepository;
import com.rbraithwaite.sleepapp.ui.format.DateTimeFormatter;
import com.rbraithwaite.sleepapp.utils.DateUtils;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class SleepGoalsFragmentViewModel
        extends ViewModel
{
//*********************************************************
// private properties
//*********************************************************

    private SleepAppRepository mRepository;
    private LiveData<String> mWakeTime;
    
    private boolean mHasWakeTime = false;

//*********************************************************
// public constants
//*********************************************************

    public static final int DEFAULT_WAKETIME_HOUR = 8;
    
    public static final int DEFAULT_WAKETIME_MINUTE = 0;

//*********************************************************
// constructors
//*********************************************************

    @ViewModelInject
    public SleepGoalsFragmentViewModel(SleepAppRepository repository)
    {
        mRepository = repository;
    }

//*********************************************************
// api
//*********************************************************

    public long getDefaultWakeTime()
    {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.set(Calendar.HOUR_OF_DAY, DEFAULT_WAKETIME_HOUR);
        calendar.set(Calendar.MINUTE, DEFAULT_WAKETIME_MINUTE);
        return calendar.getTimeInMillis();
    }
    
    public LiveData<String> getWakeTime()
    {
        if (mWakeTime == null) {
            mWakeTime = Transformations.map(
                    mRepository.getWakeTimeGoal(),
                    new Function<Long, String>()
                    {
                        @Override
                        public String apply(Long wakeTimeMillis)
                        {
                            if (wakeTimeMillis == null) {
                                return null;
                            }
                            return new DateTimeFormatter().formatTimeOfDay(
                                    DateUtils.getDateFromMillis(wakeTimeMillis));
                        }
                    }
            );
        }
        return mWakeTime;
    }
    
    public void setWakeTime(int hourOfDay, int minute)
    {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        mRepository.setWakeTimeGoal(calendar.getTimeInMillis());
        mHasWakeTime = true;
    }
    
    public LiveData<Boolean> hasWakeTime()
    {
        return Transformations.map(
                getWakeTime(),
                new Function<String, Boolean>()
                {
                    @Override
                    public Boolean apply(String wakeTime)
                    {
                        return (wakeTime != null);
                    }
                });
//        return mHasWakeTime;
    }
}
