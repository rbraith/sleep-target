package com.rbraithwaite.sleepapp.data.current_goals;

import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.rbraithwaite.sleepapp.data.SleepAppDataPrefs;
import com.rbraithwaite.sleepapp.data.database.tables.goal_waketime.WakeTimeGoalDao;
import com.rbraithwaite.sleepapp.data.database.tables.goal_waketime.WakeTimeGoalEntity;
import com.rbraithwaite.sleepapp.utils.TimeUtils;

import java.util.concurrent.Executor;

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
    private WakeTimeGoalDao mWakeTimeGoalDao;
    private TimeUtils mTimeUtils;
    private Executor mExecutor;

//*********************************************************
// constructors
//*********************************************************

    @Inject
    public CurrentGoalsRepository(
            SleepAppDataPrefs dataPrefs,
            WakeTimeGoalDao wakeTimeGoalDao,
            TimeUtils timeUtils,
            Executor executor)
    {
        mDataPrefs = dataPrefs;
        mWakeTimeGoalDao = wakeTimeGoalDao;
        mTimeUtils = timeUtils;
        mExecutor = executor;
    }

//*********************************************************
// api
//*********************************************************

    public LiveData<WakeTimeGoalModel> getWakeTimeGoal()
    {
        return Transformations.map(
                mWakeTimeGoalDao.getCurrentWakeTimeGoal(),
                new Function<WakeTimeGoalEntity, WakeTimeGoalModel>()
                {
                    @Override
                    public WakeTimeGoalModel apply(WakeTimeGoalEntity input)
                    {
                        return WakeTimeGoalModelConverter.convertEntityToModel(input);
                    }
                }
        );
    }
    
    public void setWakeTimeGoal(final WakeTimeGoalModel wakeTimeGoal)
    {
        mExecutor.execute(new Runnable()
        {
            @Override
            public void run()
            {
                mWakeTimeGoalDao.updateWakeTimeGoal(
                        WakeTimeGoalModelConverter.convertModelToEntity(wakeTimeGoal));
            }
        });
    }
    
    public void clearWakeTimeGoal()
    {
        mExecutor.execute(new Runnable()
        {
            @Override
            public void run()
            {
                WakeTimeGoalEntity entity = new WakeTimeGoalEntity();
                entity.editTime = mTimeUtils.getNow();
                entity.wakeTimeGoal = WakeTimeGoalEntity.NO_GOAL;
                mWakeTimeGoalDao.updateWakeTimeGoal(entity);
            }
        });
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
