package com.rbraithwaite.sleepapp.data.database.junctions.sleep_session_tags;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;

import com.rbraithwaite.sleepapp.data.database.tables.sleep_session.SleepSessionContract;
import com.rbraithwaite.sleepapp.data.database.tables.sleep_session.SleepSessionEntity;
import com.rbraithwaite.sleepapp.data.database.tables.tag.TagContract;
import com.rbraithwaite.sleepapp.data.database.tables.tag.TagEntity;

@Entity(
        tableName = SleepSessionTagContract.TABLE_NAME,
        primaryKeys = {
                SleepSessionTagContract.Columns.SESSION_ID,
                SleepSessionTagContract.Columns.TAG_ID
        },
        foreignKeys = {
                @ForeignKey(
                        entity = SleepSessionEntity.class,
                        parentColumns = SleepSessionContract.Columns.ID,
                        childColumns = SleepSessionTagContract.Columns.SESSION_ID,
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(
                        entity = TagEntity.class,
                        parentColumns = TagContract.Columns.ID,
                        childColumns = SleepSessionTagContract.Columns.TAG_ID,
                        onDelete = ForeignKey.CASCADE)
        }
)
public class SleepSessionTagJunction
{
//*********************************************************
// public properties
//*********************************************************

    @ColumnInfo(name = SleepSessionTagContract.Columns.SESSION_ID)
    public int sessionId;
    
    @ColumnInfo(name = SleepSessionTagContract.Columns.TAG_ID)
    public int tagId;
}
