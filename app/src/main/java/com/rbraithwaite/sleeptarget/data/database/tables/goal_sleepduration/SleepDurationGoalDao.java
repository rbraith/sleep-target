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

package com.rbraithwaite.sleeptarget.data.database.tables.goal_sleepduration;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.rbraithwaite.sleeptarget.data.database.tables.goal_waketime.WakeTimeGoalContract;

import java.util.Arrays;
import java.util.List;

@Dao
public abstract class SleepDurationGoalDao
{
//*********************************************************
// abstract
//*********************************************************

    @Insert
    public abstract long updateSleepDurationGoal(SleepDurationGoalEntity entity);
    
    @Query("SELECT * FROM " + SleepDurationGoalContract.TABLE_NAME +
           " WHERE " + SleepDurationGoalContract.Columns.ID + " = (" +
           "SELECT MAX(" + SleepDurationGoalContract.Columns.ID + ") " +
           "FROM " + SleepDurationGoalContract.TABLE_NAME +
           ");")
    public abstract LiveData<SleepDurationGoalEntity> getCurrentSleepDurationGoal();
    
    @Query("SELECT * FROM " + SleepDurationGoalContract.TABLE_NAME + ";")
    public abstract LiveData<List<SleepDurationGoalEntity>> getSleepDurationGoalHistory();
    
    // TEST NEEDED [21-09-30 1:24AM]
    @Query("SELECT * FROM " + SleepDurationGoalContract.TABLE_NAME +
           " WHERE " + SleepDurationGoalContract.Columns.EDIT_TIME + " <= :dateMillis" +
           " ORDER BY " + SleepDurationGoalContract.Columns.EDIT_TIME + " DESC" +
           " LIMIT 1")
    public abstract SleepDurationGoalEntity getFirstDurationTargetBefore(long dateMillis);
    
    // TEST NEEDED [21-09-30 1:24AM]
    @Query("SELECT * FROM " + SleepDurationGoalContract.TABLE_NAME +
           " WHERE " +
           SleepDurationGoalContract.Columns.EDIT_TIME + " >= :startMillis AND " +
           SleepDurationGoalContract.Columns.EDIT_TIME + " <= :endMillis")
    public abstract List<SleepDurationGoalEntity> getTargetsEditedInRange(long startMillis, long endMillis);
}
