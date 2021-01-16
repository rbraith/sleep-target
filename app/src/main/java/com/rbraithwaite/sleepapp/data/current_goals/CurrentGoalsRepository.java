package com.rbraithwaite.sleepapp.data.current_goals;

import androidx.lifecycle.LiveData;

import com.rbraithwaite.sleepapp.data.SleepAppDataPrefs;

import javax.inject.Inject;
import javax.inject.Singleton;

// REFACTOR [21-01-13 9:35PM] -- consider a model class WakeTimeGoal?
@Singleton
public class CurrentGoalsRepository
{
//*********************************************************
// private properties
//*********************************************************

    private SleepAppDataPrefs mDataPrefs;
    
//*********************************************************
// constructors
//*********************************************************

    @Inject
    public CurrentGoalsRepository(SleepAppDataPrefs dataPrefs)
    {
        mDataPrefs = dataPrefs;
    }
    
//*********************************************************
// api
//*********************************************************

    public LiveData<Long> getWakeTimeGoal()
    {
        return mDataPrefs.getWakeTimeGoal();
    }
    
    public void setWakeTimeGoal(long wakeTimeGoalMillis)
    {
        mDataPrefs.setWakeTimeGoal(wakeTimeGoalMillis);
    }
}
