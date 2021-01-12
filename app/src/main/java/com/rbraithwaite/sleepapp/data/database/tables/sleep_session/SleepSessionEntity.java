package com.rbraithwaite.sleepapp.data.database.tables.sleep_session;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.Date;

@Entity(tableName = SleepSessionContract.TABLE_NAME)
public class SleepSessionEntity
        implements Serializable
{
//*********************************************************
// public properties
//*********************************************************

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = SleepSessionContract.Columns.ID)
    public int id;
    
    @ColumnInfo(name = SleepSessionContract.Columns.START_TIME)
    public Date startTime;
    @ColumnInfo(name = SleepSessionContract.Columns.DURATION)
    public long duration;
    @ColumnInfo(name = SleepSessionContract.Columns.WAKE_TIME_GOAL)
    public Date wakeTimeGoal;
    
//*********************************************************
// public constants
//*********************************************************

    public static final long serialVersionUID = 20210105L;

//*********************************************************
// api
//*********************************************************

    // REFACTOR [21-01-5 12:12AM] -- maybe I should just give SleepSessionEntity a Builder (there's
    //  just going to be more fields later on).
    public static SleepSessionEntity create(
            Date startDateTime,
            long durationMillis,
            Date wakeTimeGoal)
    {
        SleepSessionEntity entity = new SleepSessionEntity();
        entity.startTime = startDateTime;
        entity.duration = durationMillis;
        entity.wakeTimeGoal = wakeTimeGoal;
        return entity;
    }
}
