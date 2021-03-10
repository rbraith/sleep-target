package com.rbraithwaite.sleepapp.data.database.tables.goal_sleepduration;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public abstract class SleepDurationGoalDao
{
//*********************************************************
// abstract
//*********************************************************

    @Insert
    public abstract long updateSleepDurationGoal(SleepDurationGoalEntity entity);
    
    @Query("SELECT * FROM " + SleepDurationGoalContract.TABLE_NAME +
           " WHERE " + SleepDurationGoalContract.Columns.ID + " = (" +
           "SELECT MAX(" + SleepDurationGoalContract.Columns.ID + ") " +
           "FROM " + SleepDurationGoalContract.TABLE_NAME +
           ");")
    public abstract LiveData<SleepDurationGoalEntity> getCurrentSleepDurationGoal();
}
