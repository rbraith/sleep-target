package com.rbraithwaite.sleepapp.data.current_goals;

import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

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

    // REFACTOR [21-02-2 1:18AM] -- this should return a WakeTimeGoalModel.
    public LiveData<Long> getWakeTimeGoal()
    {
        return mDataPrefs.getWakeTimeGoal();
    }
    
    // REFACTOR [21-02-2 1:18AM] -- this should take a WakeTimeGoalModel.
    public void setWakeTimeGoal(long wakeTimeGoalMillis)
    {
        mDataPrefs.setWakeTimeGoal(wakeTimeGoalMillis);
    }
    
    public void clearWakeTimeGoal()
    {
        mDataPrefs.clearWakeTimeGoal();
    }
    
    public LiveData<SleepDurationGoalModel> getSleepDurationGoal()
    {
        return Transformations.map(
                mDataPrefs.getSleepDurationGoal(),
                new Function<Integer, SleepDurationGoalModel>()
                {
                    @Override
                    public SleepDurationGoalModel apply(Integer input)
                    {
                        // REFACTOR [21-02-2 1:43AM] -- move this logic into SleepDurationGoalModel?
                        //  maybe as a static factory - createWithOptionalMinutes()?
                        return (input == null) ?
                                SleepDurationGoalModel.createWithoutSettingGoal() :
                                new SleepDurationGoalModel(input);
                    }
                });
    }
    
    public void setSleepDurationGoal(SleepDurationGoalModel sleepDurationGoal)
    {
        mDataPrefs.setSleepDurationGoal(sleepDurationGoal.inMinutes());
    }
    
    public void clearSleepDurationGoal()
    {
        mDataPrefs.clearSleepDurationGoal();
    }
}
