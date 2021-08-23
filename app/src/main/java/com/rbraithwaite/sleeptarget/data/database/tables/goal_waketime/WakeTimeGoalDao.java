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

package com.rbraithwaite.sleeptarget.data.database.tables.goal_waketime;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

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
    @Query("SELECT * FROM " + WakeTimeGoalContract.TABLE_NAME +
           " WHERE " + WakeTimeGoalContract.Columns.ID + " = (" +
           "SELECT MAX(" + WakeTimeGoalContract.Columns.ID + ") " +
           "FROM " + WakeTimeGoalContract.TABLE_NAME +
           ");")
    public abstract LiveData<WakeTimeGoalEntity> getCurrentWakeTimeGoal();
    
    @Query("SELECT * FROM " + WakeTimeGoalContract.TABLE_NAME + ";")
    public abstract LiveData<List<WakeTimeGoalEntity>> getWakeTimeGoalHistory();
}
