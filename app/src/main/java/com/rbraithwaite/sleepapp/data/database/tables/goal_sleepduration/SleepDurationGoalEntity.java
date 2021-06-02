package com.rbraithwaite.sleepapp.data.database.tables.goal_sleepduration;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = SleepDurationGoalContract.TABLE_NAME)
public class SleepDurationGoalEntity
{
//*********************************************************
// public properties
//*********************************************************

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = SleepDurationGoalContract.Columns.ID)
    public int id;
    
    @ColumnInfo(name = SleepDurationGoalContract.Columns.EDIT_TIME)
    public Date editTime;
    
    @ColumnInfo(name = SleepDurationGoalContract.Columns.GOAL_MINUTES)
    public int goalMinutes;
    
    public SleepDurationGoalEntity(Date editTime, int goalMinutes)
    {
        this.editTime = editTime;
        this.goalMinutes = goalMinutes;
    }
    
    public SleepDurationGoalEntity()
    {
    }
    
    //*********************************************************
// public constants
//*********************************************************

    public static final int NO_GOAL = -1;
}
