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

package com.rbraithwaite.sleeptarget.data.database.tables.sleep_interruptions;

import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public abstract class SleepInterruptionDao
{
//*********************************************************
// abstract
//*********************************************************

    @Query("SELECT * FROM " + SleepInterruptionContract.TABLE_NAME)
    public abstract List<SleepInterruptionEntity> getAll();
    
    @Update
    public abstract void updateMany(List<SleepInterruptionEntity> entities);
    
    @Query("DELETE FROM " + SleepInterruptionContract.TABLE_NAME +
           " WHERE " + SleepInterruptionContract.Columns.ID + " IN (:ids)")
    public abstract void deleteMany(List<Integer> ids);
}
