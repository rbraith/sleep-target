package com.rbraithwaite.sleepapp.data.database.tables.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Update;

import com.rbraithwaite.sleepapp.data.database.tables.SleepSessionEntity;

@Dao
public abstract class SleepSessionDao
{
//*********************************************************
// abstract
//*********************************************************

    // atm this long retval is only useful for tests
    @Insert
    public abstract long addSleepSession(SleepSessionEntity sleepSession);
    
    @Update
    public abstract void updateSleepSession(SleepSessionEntity sleepSession);
}
