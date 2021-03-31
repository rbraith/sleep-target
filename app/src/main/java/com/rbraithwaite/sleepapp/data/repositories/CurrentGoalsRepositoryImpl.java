package com.rbraithwaite.sleepapp.data.repositories;

import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.rbraithwaite.sleepapp.core.models.SleepDurationGoal;
import com.rbraithwaite.sleepapp.core.models.WakeTimeGoal;
import com.rbraithwaite.sleepapp.core.repositories.CurrentGoalsRepository;
import com.rbraithwaite.sleepapp.data.convert.ConvertSleepDurationGoal;
import com.rbraithwaite.sleepapp.data.convert.ConvertWakeTimeGoal;
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

@Singleton
public class CurrentGoalsRepositoryImpl
        implements CurrentGoalsRepository
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
    public CurrentGoalsRepositoryImpl(
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
// overrides
//*********************************************************

    @Override
    public LiveData<WakeTimeGoal> getWakeTimeGoal()
    {
        return Transformations.map(
                mWakeTimeGoalDao.getCurrentWakeTimeGoal(),
                new Function<WakeTimeGoalEntity, WakeTimeGoal>()
                {
                    @Override
                    public WakeTimeGoal apply(WakeTimeGoalEntity input)
                    {
                        return ConvertWakeTimeGoal.fromEntity(input);
                    }
                }
        );
    }
    
    @Override
    public void setWakeTimeGoal(final WakeTimeGoal wakeTimeGoal)
    {
        mExecutor.execute(new Runnable()
        {
            @Override
            public void run()
            {
                mWakeTimeGoalDao.updateWakeTimeGoal(
                        ConvertWakeTimeGoal.toEntity(wakeTimeGoal));
            }
        });
    }
    
    @Override
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
    
    @Override
    public LiveData<SleepDurationGoal> getSleepDurationGoal()
    {
        return Transformations.map(
                mSleepDurationGoalDao.getCurrentSleepDurationGoal(),
                new Function<SleepDurationGoalEntity, SleepDurationGoal>()
                {
                    @Override
                    public SleepDurationGoal apply(SleepDurationGoalEntity input)
                    {
                        return ConvertSleepDurationGoal.fromEntity(input);
                    }
                });
    }
    
    @Override
    public void setSleepDurationGoal(final SleepDurationGoal sleepDurationGoal)
    {
        mExecutor.execute(new Runnable()
        {
            @Override
            public void run()
            {
                mSleepDurationGoalDao.updateSleepDurationGoal(
                        ConvertSleepDurationGoal.toEntity(sleepDurationGoal));
            }
        });
    }
    
    @Override
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
    @Override
    public LiveData<List<WakeTimeGoal>> getWakeTimeGoalHistory()
    {
        return Transformations.map(
                mWakeTimeGoalDao.getWakeTimeGoalHistory(),
                new Function<List<WakeTimeGoalEntity>, List<WakeTimeGoal>>()
                {
                    @Override
                    public List<WakeTimeGoal> apply(List<WakeTimeGoalEntity> input)
                    {
                        List<WakeTimeGoal> result = new ArrayList<>();
                        for (WakeTimeGoalEntity entity : input) {
                            result.add(ConvertWakeTimeGoal.fromEntity(entity));
                        }
                        return result;
                    }
                });
    }
    
    @Override
    public LiveData<List<SleepDurationGoal>> getSleepDurationGoalHistory()
    {
        return Transformations.map(
                mSleepDurationGoalDao.getSleepDurationGoalHistory(),
                new Function<List<SleepDurationGoalEntity>, List<SleepDurationGoal>>()
                {
                    @Override
                    public List<SleepDurationGoal> apply(List<SleepDurationGoalEntity> input)
                    {
                        // REFACTOR [21-03-15 8:54PM] -- duplicates getWakeTimeGoalHistory() logic.
                        List<SleepDurationGoal> result = new ArrayList<>();
                        for (SleepDurationGoalEntity entity : input) {
                            result.add(ConvertSleepDurationGoal.fromEntity(entity));
                        }
                        return result;
                    }
                });
    }
}
