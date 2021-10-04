/*
 * Copyright (c) 2020-2021 Rich Braithwaite (rbraithwaite.dev@gmail.com)
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

package com.rbraithwaite.sleeptarget.core.repositories;

import androidx.lifecycle.LiveData;

import com.rbraithwaite.sleeptarget.core.models.SleepDurationGoal;
import com.rbraithwaite.sleeptarget.core.models.WakeTimeGoal;
import com.rbraithwaite.sleeptarget.ui.stats.chart_intervals.DateRange;

import java.util.Collection;
import java.util.Date;
import java.util.List;

// REFACTOR [21-09-28 9:07PM] -- split this into DurationTargetRepository and
//  WakeTimeTargetRepository.
// TODO [21-03-24 11:04PM] document these methods.
public interface CurrentGoalsRepository
{
//*********************************************************
// abstract
//*********************************************************

    LiveData<WakeTimeGoal> getWakeTimeGoal();
    
    void setWakeTimeGoal(final WakeTimeGoal wakeTimeGoal);
    
    void clearWakeTimeGoal();
    
    LiveData<SleepDurationGoal> getSleepDurationGoal();
    
    void setSleepDurationGoal(final SleepDurationGoal sleepDurationGoal);
    
    void clearSleepDurationGoal();
    
    /**
     * Returns the full history of wake-time goal edits.
     */
    LiveData<List<WakeTimeGoal>> getWakeTimeGoalHistory();
    
    LiveData<List<SleepDurationGoal>> getSleepDurationGoalHistory();
    
    WakeTimeGoal getFirstWakeTimeTargetBefore(Date date);
    SleepDurationGoal getFirstDurationTargetBefore(Date date);
    
    List<WakeTimeGoal> getWakeTimeTargetsEditedInRange(Date rangeStart, Date rangeEnd);
    List<SleepDurationGoal> getDurationTargetsEditedInRange(Date rangeStart, Date rangeEnd);
}
