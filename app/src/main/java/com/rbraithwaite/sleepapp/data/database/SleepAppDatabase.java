package com.rbraithwaite.sleepapp.data.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.rbraithwaite.sleepapp.data.database.convert.DateConverter;
import com.rbraithwaite.sleepapp.data.database.tables.goal_sleepduration.SleepDurationGoalDao;
import com.rbraithwaite.sleepapp.data.database.tables.goal_sleepduration.SleepDurationGoalEntity;
import com.rbraithwaite.sleepapp.data.database.tables.goal_waketime.WakeTimeGoalDao;
import com.rbraithwaite.sleepapp.data.database.tables.goal_waketime.WakeTimeGoalEntity;
import com.rbraithwaite.sleepapp.data.database.tables.sleep_session.SleepSessionDao;
import com.rbraithwaite.sleepapp.data.database.tables.sleep_session.SleepSessionEntity;

@Database(
        version = 1,
        entities = {
                SleepSessionEntity.class,
                WakeTimeGoalEntity.class,
                SleepDurationGoalEntity.class
        })
@TypeConverters({DateConverter.class})
public abstract class SleepAppDatabase
        extends RoomDatabase
{
//*********************************************************
// public properties
//*********************************************************

    public static String NAME = "sleepapp.db";

//*********************************************************
// abstract
//*********************************************************

    public abstract SleepSessionDao getSleepSessionDao();
    
    public abstract WakeTimeGoalDao getWakeTimeGoalDao();
    
    public abstract SleepDurationGoalDao getSleepDurationGoalDao();
}
