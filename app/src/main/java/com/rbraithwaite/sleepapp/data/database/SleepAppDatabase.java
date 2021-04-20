package com.rbraithwaite.sleepapp.data.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.rbraithwaite.sleepapp.data.database.convert.ConvertDate;
import com.rbraithwaite.sleepapp.data.database.junctions.sleep_session_tags.SleepSessionTagJunction;
import com.rbraithwaite.sleepapp.data.database.junctions.sleep_session_tags.SleepSessionTagJunctionDao;
import com.rbraithwaite.sleepapp.data.database.tables.goal_sleepduration.SleepDurationGoalDao;
import com.rbraithwaite.sleepapp.data.database.tables.goal_sleepduration.SleepDurationGoalEntity;
import com.rbraithwaite.sleepapp.data.database.tables.goal_waketime.WakeTimeGoalDao;
import com.rbraithwaite.sleepapp.data.database.tables.goal_waketime.WakeTimeGoalEntity;
import com.rbraithwaite.sleepapp.data.database.tables.sleep_session.SleepSessionDao;
import com.rbraithwaite.sleepapp.data.database.tables.sleep_session.SleepSessionEntity;
import com.rbraithwaite.sleepapp.data.database.tables.tag.TagDao;
import com.rbraithwaite.sleepapp.data.database.tables.tag.TagEntity;

@Database(
        version = 1,
        entities = {
                SleepSessionEntity.class,
                WakeTimeGoalEntity.class,
                SleepDurationGoalEntity.class,
                TagEntity.class,
                SleepSessionTagJunction.class
        })
@TypeConverters({ConvertDate.class})
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
    
    public abstract TagDao getTagDao();
    
    public abstract SleepSessionTagJunctionDao getSleepSessionTagsDao();
}
