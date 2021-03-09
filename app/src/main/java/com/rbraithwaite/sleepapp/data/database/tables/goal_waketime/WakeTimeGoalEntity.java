package com.rbraithwaite.sleepapp.data.database.tables.goal_waketime;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;


/**
 * This table acts as a record of the wake time goal's edit history.
 */
@Entity(tableName = WakeTimeGoalContract.TABLE_NAME)
public class WakeTimeGoalEntity
{
//*********************************************************
// public properties
//*********************************************************

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = WakeTimeGoalContract.Columns.ID)
    public int id;
    
    /**
     * When the goal value was edited.
     */
    @ColumnInfo(name = WakeTimeGoalContract.Columns.EDIT_TIME)
    public Date editTime;
    
    /**
     * Millis from 12am.
     */
    @ColumnInfo(name = WakeTimeGoalContract.Columns.GOAL)
    public int wakeTimeGoal;
    
//*********************************************************
// public constants
//*********************************************************

    public static final int NO_GOAL = -1;
}
