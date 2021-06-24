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
    @ColumnInfo(name = SleepSessionContract.Columns.MOOD)
    public Integer moodIndex;
    @ColumnInfo(name = SleepSessionContract.Columns.RATING)
    public Float rating = 0f;

//*********************************************************
// constructors
//*********************************************************

    public SleepSessionEntity()
    {
    }
    
    public SleepSessionEntity(
            Date startTime,
            Date endTime,
            long duration,
            String additionalComments, Integer moodIndex, Float rating)
    {
        this.startTime = startTime;
        this.endTime = endTime;
        this.duration = duration;
        this.additionalComments = additionalComments;
        this.moodIndex = moodIndex;
        this.rating = rating;
    }
    
    public SleepSessionEntity(Date startTime, Date endTime)
    {
        this.startTime = startTime;
        this.endTime = endTime;
        // SMELL [21-06-1 1:16AM] -- It's questionable having any logic at all in
        //  SleepSessionEntity.
        this.duration = endTime.getTime() - startTime.getTime();
    }
    
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
        hash = prime * hash + moodIndex;
        hash = prime * hash + rating.hashCode();
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
               additionalComments.equals(entity.additionalComments) &&
               ((moodIndex == null && entity.moodIndex == null) ||
                (moodIndex != null && moodIndex.equals(entity.moodIndex))) &&
               rating.equals(entity.rating);
    }
}
