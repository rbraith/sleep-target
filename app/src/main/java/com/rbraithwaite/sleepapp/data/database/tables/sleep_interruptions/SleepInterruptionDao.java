package com.rbraithwaite.sleepapp.data.database.tables.sleep_interruptions;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

@Dao
public abstract class SleepInterruptionDao
{
//*********************************************************
// abstract
//*********************************************************

    @Query("SELECT * FROM " + SleepInterruptionContract.TABLE_NAME)
    public abstract List<SleepInterruptionEntity> getAll();
}
