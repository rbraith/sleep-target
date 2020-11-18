package com.rbraithwaite.sleepapp.data.database.views.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import com.rbraithwaite.sleepapp.data.database.views.SleepSessionData;
import com.rbraithwaite.sleepapp.data.database.views.SleepSessionDataContract;

import java.util.List;

@Dao
public abstract class SleepSessionDataDao
{
//*********************************************************
// abstract
//*********************************************************

    //    SELECT <session-id> FROM <view-sleep-session-data>
    @Query("SELECT " + SleepSessionDataContract.Columns.SESSION_ID +
           " FROM " + SleepSessionDataContract.VIEW_NAME)
    public abstract LiveData<List<Integer>> getAllSleepSessionDataIds();
    
    //    SELECT * FROM <view-sleep-session-data> WHERE <session-id> = :id
    @Query("SELECT * FROM " + SleepSessionDataContract.VIEW_NAME +
           " WHERE " + SleepSessionDataContract.Columns.SESSION_ID + " = :id")
    public abstract LiveData<SleepSessionData> getSleepSessionData(int id);
}
