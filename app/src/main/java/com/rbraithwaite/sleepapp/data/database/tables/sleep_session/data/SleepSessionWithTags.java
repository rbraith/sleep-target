package com.rbraithwaite.sleepapp.data.database.tables.sleep_session.data;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import com.rbraithwaite.sleepapp.data.database.junctions.sleep_session_tags.SleepSessionTagContract;
import com.rbraithwaite.sleepapp.data.database.junctions.sleep_session_tags.SleepSessionTagJunction;
import com.rbraithwaite.sleepapp.data.database.tables.sleep_session.SleepSessionContract;
import com.rbraithwaite.sleepapp.data.database.tables.sleep_session.SleepSessionEntity;
import com.rbraithwaite.sleepapp.data.database.tables.tag.TagContract;
import com.rbraithwaite.sleepapp.data.database.tables.tag.TagEntity;

import java.util.List;

public class SleepSessionWithTags
{
//*********************************************************
// public properties
//*********************************************************

    @Embedded
    public SleepSessionEntity sleepSession;
    
    @Relation(
            parentColumn = SleepSessionContract.Columns.ID,
            entity = TagEntity.class,
            entityColumn = TagContract.Columns.ID,
            associateBy = @Junction(
                    value = SleepSessionTagJunction.class,
                    parentColumn = SleepSessionTagContract.Columns.SESSION_ID,
                    entityColumn = SleepSessionTagContract.Columns.TAG_ID
            )
    )
    public List<TagEntity> tags;

//*********************************************************
// constructors
//*********************************************************

    public SleepSessionWithTags()
    {
    }
    
    public SleepSessionWithTags(
            SleepSessionEntity sleepSession,
            List<TagEntity> tags)
    {
        this.sleepSession = sleepSession;
        this.tags = tags;
    }
}
