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

package com.rbraithwaite.sleeptarget.data.database.tables.goal_sleepduration;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = SleepDurationGoalContract.TABLE_NAME)
public class SleepDurationGoalEntity
{
//*********************************************************
// public properties
//*********************************************************

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = SleepDurationGoalContract.Columns.ID)
    public int id;
    
    @ColumnInfo(name = SleepDurationGoalContract.Columns.EDIT_TIME)
    public Date editTime;
    
    @ColumnInfo(name = SleepDurationGoalContract.Columns.GOAL_MINUTES)
    public int goalMinutes;

//*********************************************************
// public constants
//*********************************************************

    public static final int NO_GOAL = -1;
    
//*********************************************************
// constructors
//*********************************************************

    public SleepDurationGoalEntity(Date editTime, int goalMinutes)
    {
        this.editTime = editTime;
        this.goalMinutes = goalMinutes;
    }
    
    public SleepDurationGoalEntity()
    {
    }
}
