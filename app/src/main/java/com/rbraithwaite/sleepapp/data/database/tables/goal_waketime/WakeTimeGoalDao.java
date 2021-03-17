package com.rbraithwaite.sleepapp.data.database.tables.goal_waketime;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public abstract class WakeTimeGoalDao
{
//*********************************************************
// abstract
//*********************************************************

    @Insert
    public abstract long updateWakeTimeGoal(WakeTimeGoalEntity wakeTimeGoalEntity);
    
    // https://stackoverflow.com/questions/9902394/how-to-get-last-record-from-sqlite
    // #comment29771764_9902506
    // https://stackoverflow.com/questions/9902394/how-to-get-last-record-from-sqlite
    // #comment112524769_9902506
    // who's right? lol
    @Query("SELECT * FROM " + WakeTimeGoalContract.TABLE_NAME +
           " WHERE " + WakeTimeGoalContract.Columns.ID + " = (" +
           "SELECT MAX(" + WakeTimeGoalContract.Columns.ID + ") " +
           "FROM " + WakeTimeGoalContract.TABLE_NAME +
           ");")
    public abstract LiveData<WakeTimeGoalEntity> getCurrentWakeTimeGoal();
    
    @Query("SELECT * FROM " + WakeTimeGoalContract.TABLE_NAME + ";")
    public abstract LiveData<List<WakeTimeGoalEntity>> getWakeTimeGoalHistory();
}
