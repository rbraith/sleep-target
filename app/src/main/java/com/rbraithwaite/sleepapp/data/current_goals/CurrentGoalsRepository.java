package com.rbraithwaite.sleepapp.data.current_goals;

import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.rbraithwaite.sleepapp.data.database.tables.goal_sleepduration.SleepDurationGoalDao;
import com.rbraithwaite.sleepapp.data.database.tables.goal_sleepduration.SleepDurationGoalEntity;
import com.rbraithwaite.sleepapp.data.database.tables.goal_waketime.WakeTimeGoalDao;
import com.rbraithwaite.sleepapp.data.database.tables.goal_waketime.WakeTimeGoalEntity;
import com.rbraithwaite.sleepapp.utils.TimeUtils;

import java.util.ArrayList;
import java.util.List;
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

    private WakeTimeGoalDao mWakeTimeGoalDao;
    private SleepDurationGoalDao mSleepDurationGoalDao;
    private TimeUtils mTimeUtils;
    private Executor mExecutor;

//*********************************************************
// constructors
//*********************************************************

    @Inject
    public CurrentGoalsRepository(
            WakeTimeGoalDao wakeTimeGoalDao,
            SleepDurationGoalDao sleepDurationGoalDao,
            TimeUtils timeUtils,
            Executor executor)
    {
        mWakeTimeGoalDao = wakeTimeGoalDao;
        mSleepDurationGoalDao = sleepDurationGoalDao;
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
                mSleepDurationGoalDao.getCurrentSleepDurationGoal(),
                new Function<SleepDurationGoalEntity, SleepDurationGoalModel>()
                {
                    @Override
                    public SleepDurationGoalModel apply(SleepDurationGoalEntity input)
                    {
                        return SleepDurationGoalModelConverter.convertEntityToModel(input);
                    }
                });
    }
    
    public void setSleepDurationGoal(final SleepDurationGoalModel sleepDurationGoal)
    {
        mExecutor.execute(new Runnable()
        {
            @Override
            public void run()
            {
                mSleepDurationGoalDao.updateSleepDurationGoal(
                        SleepDurationGoalModelConverter.convertModelToEntity(sleepDurationGoal));
            }
        });
    }
    
    public void clearSleepDurationGoal()
    {
        mExecutor.execute(new Runnable()
        {
            @Override
            public void run()
            {
                SleepDurationGoalEntity entity = new SleepDurationGoalEntity();
                entity.editTime = mTimeUtils.getNow();
                entity.goalMinutes = SleepDurationGoalEntity.NO_GOAL;
                mSleepDurationGoalDao.updateSleepDurationGoal(entity);
            }
        });
    }
    
    /**
     * Returns the full history of wake-time goal edits.
     */
    public LiveData<List<WakeTimeGoalModel>> getWakeTimeGoalHistory()
    {
        return Transformations.map(
                mWakeTimeGoalDao.getWakeTimeGoalHistory(),
                new Function<List<WakeTimeGoalEntity>, List<WakeTimeGoalModel>>()
                {
                    @Override
                    public List<WakeTimeGoalModel> apply(List<WakeTimeGoalEntity> input)
                    {
                        List<WakeTimeGoalModel> result = new ArrayList<>();
                        for (WakeTimeGoalEntity entity : input) {
                            result.add(WakeTimeGoalModelConverter.convertEntityToModel(entity));
                        }
                        return result;
                    }
                });
    }
    
    public LiveData<List<SleepDurationGoalModel>> getSleepDurationGoalHistory()
    {
        return Transformations.map(
                mSleepDurationGoalDao.getSleepDurationGoalHistory(),
                new Function<List<SleepDurationGoalEntity>, List<SleepDurationGoalModel>>()
                {
                    @Override
                    public List<SleepDurationGoalModel> apply(List<SleepDurationGoalEntity> input)
                    {
                        // REFACTOR [21-03-15 8:54PM] -- duplicates getWakeTimeGoalHistory() logic.
                        List<SleepDurationGoalModel> result = new ArrayList<>();
                        for (SleepDurationGoalEntity entity : input) {
                            result.add(SleepDurationGoalModelConverter.convertEntityToModel(entity));
                        }
                        return result;
                    }
                });
    }
}
