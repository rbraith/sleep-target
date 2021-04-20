package com.rbraithwaite.sleepapp.data.database.junctions.sleep_session_tags;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

@Dao
public abstract class SleepSessionTagJunctionDao
{
//*********************************************************
// abstract
//*********************************************************

    @Query("SELECT * FROM " + SleepSessionTagContract.TABLE_NAME)
    public abstract List<SleepSessionTagJunction> getAll();
}
