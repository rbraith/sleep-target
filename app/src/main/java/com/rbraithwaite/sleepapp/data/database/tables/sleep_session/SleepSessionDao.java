package com.rbraithwaite.sleepapp.data.database.tables.sleep_session;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

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
    
    // IDEA [20-12-17 9:03PM] -- Using a query here is one option. Another option
    //  would be using @Delete w/ a POJO containing the id
    //  see: https://developer.android.com/reference/kotlin/androidx/room/Delete
    @Query("DELETE FROM " + SleepSessionContract.TABLE_NAME +
           " WHERE " + SleepSessionContract.Columns.ID + " = :sleepSessionId")
    public abstract void deleteSleepSession(int sleepSessionId);
    
    @Query("SELECT * FROM " + SleepSessionContract.TABLE_NAME +
           " WHERE " + SleepSessionContract.Columns.ID + " = :sleepSessionId")
    public abstract LiveData<SleepSessionEntity> getSleepSession(int sleepSessionId);
    
    @Query("SELECT " + SleepSessionContract.Columns.ID +
           " FROM " + SleepSessionContract.TABLE_NAME)
    public abstract LiveData<List<Integer>> getAllSleepSessionIds();
    
    @Query("SELECT * FROM " + SleepSessionContract.TABLE_NAME +
           " WHERE " +
           "(" + SleepSessionContract.Columns.START_TIME + " BETWEEN :start AND :end)" +
           " OR " +
           "(" + SleepSessionContract.Columns.END_TIME + " BETWEEN :start AND :end)")
    public abstract LiveData<List<SleepSessionEntity>> getSleepSessionsInRange(
            long start,
            long end);
    
    // REFACTOR [21-03-16 3:47PM] -- duplicates getSleepSessionsInRange() query.
    @Query("SELECT * FROM " + SleepSessionContract.TABLE_NAME +
           " WHERE " +
           "(" + SleepSessionContract.Columns.START_TIME + " BETWEEN :start AND :end)" +
           " OR " +
           "(" + SleepSessionContract.Columns.END_TIME + " BETWEEN :start AND :end)")
    public abstract List<SleepSessionEntity> getSleepSessionsInRangeSynced(long start, long end);
    
    @Query("SELECT * FROM " + SleepSessionContract.TABLE_NAME +
           " WHERE " + SleepSessionContract.Columns.START_TIME + " <= :dateTimeMillis" +
           " ORDER BY " + SleepSessionContract.Columns.START_TIME + " DESC" +
           " LIMIT 1;")
    public abstract SleepSessionEntity getFirstSleepSessionStartingBefore(long dateTimeMillis);
}
