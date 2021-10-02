/*
 * Copyright (c) 2020-2021 Richard Braithwaite (rbraithwaite.dev@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.rbraithwaite.sleeptarget.data.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.rbraithwaite.sleeptarget.core.models.SleepDurationGoal;
import com.rbraithwaite.sleeptarget.core.models.WakeTimeGoal;
import com.rbraithwaite.sleeptarget.core.repositories.CurrentGoalsRepository;
import com.rbraithwaite.sleeptarget.data.convert.ConvertSleepDurationGoal;
import com.rbraithwaite.sleeptarget.data.convert.ConvertWakeTimeGoal;
import com.rbraithwaite.sleeptarget.data.database.tables.goal_sleepduration.SleepDurationGoalDao;
import com.rbraithwaite.sleeptarget.data.database.tables.goal_sleepduration.SleepDurationGoalEntity;
import com.rbraithwaite.sleeptarget.data.database.tables.goal_waketime.WakeTimeGoalDao;
import com.rbraithwaite.sleeptarget.data.database.tables.goal_waketime.WakeTimeGoalEntity;
import com.rbraithwaite.sleeptarget.ui.stats.chart_intervals.DateRange;
import com.rbraithwaite.sleeptarget.utils.TimeUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

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
                ConvertWakeTimeGoal::fromEntity
        );
    }
    
    @Override
    public void setWakeTimeGoal(final WakeTimeGoal wakeTimeGoal)
    {
        mExecutor.execute(() -> mWakeTimeGoalDao.updateWakeTimeGoal(
                ConvertWakeTimeGoal.toEntity(wakeTimeGoal)));
    }
    
    @Override
    public void clearWakeTimeGoal()
    {
        mExecutor.execute(() -> {
            WakeTimeGoalEntity entity = new WakeTimeGoalEntity();
            entity.editTime = mTimeUtils.getNow();
            entity.wakeTimeGoal = WakeTimeGoalEntity.NO_GOAL;
            mWakeTimeGoalDao.updateWakeTimeGoal(entity);
        });
    }
    
    @Override
    public LiveData<SleepDurationGoal> getSleepDurationGoal()
    {
        return Transformations.map(
                mSleepDurationGoalDao.getCurrentSleepDurationGoal(),
                ConvertSleepDurationGoal::fromEntity);
    }
    
    @Override
    public void setSleepDurationGoal(final SleepDurationGoal sleepDurationGoal)
    {
        mExecutor.execute(() -> mSleepDurationGoalDao.updateSleepDurationGoal(
                ConvertSleepDurationGoal.toEntity(sleepDurationGoal)));
    }
    
    @Override
    public void clearSleepDurationGoal()
    {
        mExecutor.execute(() -> {
            SleepDurationGoalEntity entity = new SleepDurationGoalEntity();
            entity.editTime = mTimeUtils.getNow();
            entity.goalMinutes = SleepDurationGoalEntity.NO_GOAL;
            mSleepDurationGoalDao.updateSleepDurationGoal(entity);
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
                input -> {
                    List<WakeTimeGoal> result = new ArrayList<>();
                    for (WakeTimeGoalEntity entity : input) {
                        result.add(ConvertWakeTimeGoal.fromEntity(entity));
                    }
                    return result;
                });
    }
    
    @Override
    public LiveData<List<SleepDurationGoal>> getSleepDurationGoalHistory()
    {
        return Transformations.map(
                mSleepDurationGoalDao.getSleepDurationGoalHistory(),
                input -> {
                    // REFACTOR [21-03-15 8:54PM] -- duplicates getWakeTimeGoalHistory() logic.
                    List<SleepDurationGoal> result = new ArrayList<>();
                    for (SleepDurationGoalEntity entity : input) {
                        result.add(ConvertSleepDurationGoal.fromEntity(entity));
                    }
                    return result;
                });
    }
    
    @Override
    public WakeTimeGoal getFirstWakeTimeTargetBefore(Date date)
    {
        return ConvertWakeTimeGoal.fromEntity(
                mWakeTimeGoalDao.getFirstWakeTimeTargetBefore(date.getTime()));
    }
    
    @Override
    public List<WakeTimeGoal> getWakeTimeTargetsEditedInRange(Date rangeStart, Date rangeEnd)
    {
        // REFACTOR [21-09-29 1:25AM] -- move this to ConvertWakeTimeGoal.
        return mWakeTimeGoalDao.getWakeTimeTargetsEditedInRange(
                rangeStart.getTime(),
                rangeEnd.getTime())
                .stream()
                .map(ConvertWakeTimeGoal::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public SleepDurationGoal getFirstDurationTargetBefore(Date date)
    {
        return ConvertSleepDurationGoal.fromEntity(
                mSleepDurationGoalDao.getFirstDurationTargetBefore(date.getTime()));
    }
    
    @Override
    public List<SleepDurationGoal> getDurationTargetsEditedInRange(
            Date rangeStart, Date rangeEnd)
    {
        return mSleepDurationGoalDao.getTargetsEditedInRange(
                rangeStart.getTime(), rangeEnd.getTime())
                .stream()
                .map(ConvertSleepDurationGoal::fromEntity)
                .collect(Collectors.toList());
    }
}
