package com.rbraithwaite.sleepapp.data.database.tables;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "sleep_sessions")
public class SleepSessionEntity
{
//*********************************************************
// public properties
//*********************************************************

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public int id;
    
    @ColumnInfo(name = "start_time")
    public Date startTime;
    @ColumnInfo(name = "duration")
    public long duration;
}
