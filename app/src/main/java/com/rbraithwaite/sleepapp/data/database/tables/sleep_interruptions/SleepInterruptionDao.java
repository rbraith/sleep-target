package com.rbraithwaite.sleepapp.data.database.tables.sleep_interruptions;

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
