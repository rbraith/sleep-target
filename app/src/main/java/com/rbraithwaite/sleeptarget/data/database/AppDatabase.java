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

package com.rbraithwaite.sleeptarget.data.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.rbraithwaite.sleeptarget.data.database.convert.ConvertDate;
import com.rbraithwaite.sleeptarget.data.database.junctions.sleep_session_tags.SleepSessionTagJunction;
import com.rbraithwaite.sleeptarget.data.database.junctions.sleep_session_tags.SleepSessionTagJunctionDao;
import com.rbraithwaite.sleeptarget.data.database.tables.goal_sleepduration.SleepDurationGoalDao;
import com.rbraithwaite.sleeptarget.data.database.tables.goal_sleepduration.SleepDurationGoalEntity;
import com.rbraithwaite.sleeptarget.data.database.tables.goal_waketime.WakeTimeGoalDao;
import com.rbraithwaite.sleeptarget.data.database.tables.goal_waketime.WakeTimeGoalEntity;
import com.rbraithwaite.sleeptarget.data.database.tables.sleep_interruptions.SleepInterruptionDao;
import com.rbraithwaite.sleeptarget.data.database.tables.sleep_interruptions.SleepInterruptionEntity;
import com.rbraithwaite.sleeptarget.data.database.tables.sleep_session.SleepSessionDao;
import com.rbraithwaite.sleeptarget.data.database.tables.sleep_session.SleepSessionEntity;
import com.rbraithwaite.sleeptarget.data.database.tables.tag.TagDao;
import com.rbraithwaite.sleeptarget.data.database.tables.tag.TagEntity;

@Database(
        version = 1,
        entities = {
                SleepSessionEntity.class,
                WakeTimeGoalEntity.class,
                SleepDurationGoalEntity.class,
                TagEntity.class,
                SleepSessionTagJunction.class,
                SleepInterruptionEntity.class
        })
@TypeConverters({ConvertDate.class})
public abstract class AppDatabase
        extends RoomDatabase
{
//*********************************************************
// public properties
//*********************************************************

    public static String NAME = "app.db";

//*********************************************************
// abstract
//*********************************************************

    public abstract SleepSessionDao getSleepSessionDao();
    
    public abstract WakeTimeGoalDao getWakeTimeGoalDao();
    
    public abstract SleepDurationGoalDao getSleepDurationGoalDao();
    
    public abstract TagDao getTagDao();
    
    public abstract SleepSessionTagJunctionDao getSleepSessionTagsDao();
    
    public abstract SleepInterruptionDao getSleepInterruptionDao();
}
