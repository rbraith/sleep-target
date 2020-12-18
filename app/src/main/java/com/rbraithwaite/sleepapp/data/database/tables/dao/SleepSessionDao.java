package com.rbraithwaite.sleepapp.data.database.tables.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.rbraithwaite.sleepapp.data.database.tables.SleepSessionContract;
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
    
    // IDEA [20-12-17 9:03PM] -- Using a query here is one option. Another option
    //  would be using @Delete w/ a POJO containing the id
    //  see: https://developer.android.com/reference/kotlin/androidx/room/Delete
    @Query("DELETE FROM " + SleepSessionContract.TABLE_NAME +
           " WHERE " + SleepSessionContract.Columns.ID + " = :sleepSessionId")
    public abstract void deleteSleepSession(int sleepSessionId);
}
