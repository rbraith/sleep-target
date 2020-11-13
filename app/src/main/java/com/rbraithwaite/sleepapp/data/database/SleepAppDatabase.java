package com.rbraithwaite.sleepapp.data.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.rbraithwaite.sleepapp.data.database.convert.DateConverter;
import com.rbraithwaite.sleepapp.data.database.tables.SleepSessionEntity;
import com.rbraithwaite.sleepapp.data.database.tables.dao.SleepSessionDao;
import com.rbraithwaite.sleepapp.data.database.views.SleepSessionData;
import com.rbraithwaite.sleepapp.data.database.views.dao.SleepSessionDataDao;

@Database(
        version = 1,
        entities = {SleepSessionEntity.class},
        views = {SleepSessionData.class}
        )
@TypeConverters({DateConverter.class})
public abstract class SleepAppDatabase extends RoomDatabase
{
    public static String NAME = "sleepapp.db";

    public abstract SleepSessionDao getSleepSessionDao();
    public abstract SleepSessionDataDao getSleepSessionDataDao();
}
