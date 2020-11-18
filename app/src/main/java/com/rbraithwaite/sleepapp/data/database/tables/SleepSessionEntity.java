package com.rbraithwaite.sleepapp.data.database.tables;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = SleepSessionContract.TABLE_NAME)
public class SleepSessionEntity
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
    
//*********************************************************
// api
//*********************************************************

    public static SleepSessionEntity create(Date startTime, long duration)
    {
        SleepSessionEntity entity = new SleepSessionEntity();
        entity.startTime = startTime;
        entity.duration = duration;
        return entity;
    }
}
