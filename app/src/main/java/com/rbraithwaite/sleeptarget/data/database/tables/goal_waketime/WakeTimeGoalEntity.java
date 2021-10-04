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

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;


/**
 * This table acts as a record of the wake time goal's edit history.
 */
@Entity(tableName = WakeTimeGoalContract.TABLE_NAME)
public class WakeTimeGoalEntity
{
//*********************************************************
// public properties
//*********************************************************

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = WakeTimeGoalContract.Columns.ID)
    public int id;
    
    /**
     * When the goal value was edited.
     */
    @ColumnInfo(name = WakeTimeGoalContract.Columns.EDIT_TIME)
    public Date editTime;
    
    /**
     * Millis from 12am.
     */
    @ColumnInfo(name = WakeTimeGoalContract.Columns.GOAL)
    public int wakeTimeGoal;

//*********************************************************
// public constants
//*********************************************************

    public static final int NO_GOAL = -1;
    
//*********************************************************
// constructors
//*********************************************************

    public WakeTimeGoalEntity(Date editTime, int wakeTimeGoal)
    {
        this.editTime = editTime;
        this.wakeTimeGoal = wakeTimeGoal;
    }
    
    public WakeTimeGoalEntity()
    {
    }
}
