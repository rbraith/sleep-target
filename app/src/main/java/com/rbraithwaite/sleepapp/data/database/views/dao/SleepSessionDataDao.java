package com.rbraithwaite.sleepapp.data.database.views.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import com.rbraithwaite.sleepapp.data.database.views.SleepSessionData;

import java.util.List;

@Dao
public abstract class SleepSessionDataDao {
    @Query("SELECT session_id FROM view_sleep_session_data")
    public abstract LiveData<List<Integer>> getAllSleepSessionDataIds();

    @Query("SELECT * FROM view_sleep_session_data WHERE session_id = :id")
    public abstract LiveData<SleepSessionData> getSleepSessionData(int id);
}
