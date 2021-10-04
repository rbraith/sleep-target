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

package com.rbraithwaite.sleeptarget.data.database.tables.goal_waketime;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.rbraithwaite.sleeptarget.core.models.WakeTimeGoal;

import java.util.List;

@Dao
public abstract class WakeTimeGoalDao
{
//*********************************************************
// abstract
//*********************************************************

    @Insert
    public abstract long updateWakeTimeGoal(WakeTimeGoalEntity wakeTimeGoalEntity);
    
    // https://stackoverflow.com/questions/9902394/how-to-get-last-record-from-sqlite
    // #comment29771764_9902506
    // https://stackoverflow.com/questions/9902394/how-to-get-last-record-from-sqlite
    // #comment112524769_9902506
    // who's right? lol
    // REFACTOR [21-09-29 1:04AM] -- semi-colon isn't needed.
    @Query("SELECT * FROM " + WakeTimeGoalContract.TABLE_NAME +
           " WHERE " + WakeTimeGoalContract.Columns.ID + " = (" +
           "SELECT MAX(" + WakeTimeGoalContract.Columns.ID + ") " +
           "FROM " + WakeTimeGoalContract.TABLE_NAME +
           ");")
    public abstract LiveData<WakeTimeGoalEntity> getCurrentWakeTimeGoal();
    
    // REFACTOR [21-09-29 1:04AM] -- semi-colon isn't needed.
    @Query("SELECT * FROM " + WakeTimeGoalContract.TABLE_NAME + ";")
    public abstract LiveData<List<WakeTimeGoalEntity>> getWakeTimeGoalHistory();
    
    // TEST NEEDED [21-09-29 3:48PM]
    @Query("SELECT * FROM " + WakeTimeGoalContract.TABLE_NAME +
           " WHERE " + WakeTimeGoalContract.Columns.EDIT_TIME + " <= :dateMillis" +
           " ORDER BY " + WakeTimeGoalContract.Columns.EDIT_TIME + " DESC" +
           " LIMIT 1")
    public abstract WakeTimeGoalEntity getFirstWakeTimeTargetBefore(long dateMillis);
    
    // TEST NEEDED [21-09-29 3:48PM]
    @Query("SELECT * FROM " + WakeTimeGoalContract.TABLE_NAME +
           " WHERE " +
           WakeTimeGoalContract.Columns.EDIT_TIME + " >= :startMillis AND " +
           WakeTimeGoalContract.Columns.EDIT_TIME + " <= :endMillis")
    public abstract List<WakeTimeGoalEntity> getWakeTimeTargetsEditedInRange(long startMillis, long endMillis);
}
