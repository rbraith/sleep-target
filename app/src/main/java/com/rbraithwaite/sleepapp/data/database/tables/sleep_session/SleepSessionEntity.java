package com.rbraithwaite.sleepapp.data.database.tables.sleep_session;


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
    // both the end time and duration are recorded to facilitate db queries
    @ColumnInfo(name = SleepSessionContract.Columns.END_TIME)
    public Date endTime;
    @ColumnInfo(name = SleepSessionContract.Columns.DURATION)
    public long duration;
}
