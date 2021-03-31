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
    @ColumnInfo(name = SleepSessionContract.Columns.COMMENTS)
    public String additionalComments;
    
//*********************************************************
// overrides
//*********************************************************

    @Override
    public int hashCode()
    {
        int hash = 7;
        int prime = 13;
        hash = prime * hash + id;
        hash = prime * hash + startTime.hashCode();
        hash = prime * hash + endTime.hashCode();
        hash = prime * hash + (int) duration;
        hash = prime * hash + (additionalComments == null ? 0 : additionalComments.hashCode());
        return hash;
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        SleepSessionEntity entity = (SleepSessionEntity) o;
        return id == entity.id &&
               duration == entity.duration &&
               startTime.equals(entity.startTime) &&
               endTime.equals(entity.endTime) &&
               additionalComments.equals(entity.additionalComments);
    }
}
