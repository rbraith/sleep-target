/*
 * Copyright (c) 2020-2021 Richard Braithwaite (rbraithwaite.dev@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.rbraithwaite.sleepapp.data.database.tables.sleep_session.data;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import com.rbraithwaite.sleepapp.data.database.junctions.sleep_session_tags.SleepSessionTagContract;
import com.rbraithwaite.sleepapp.data.database.junctions.sleep_session_tags.SleepSessionTagJunction;
import com.rbraithwaite.sleepapp.data.database.tables.sleep_interruptions.SleepInterruptionContract;
import com.rbraithwaite.sleepapp.data.database.tables.sleep_interruptions.SleepInterruptionEntity;
import com.rbraithwaite.sleepapp.data.database.tables.sleep_session.SleepSessionContract;
import com.rbraithwaite.sleepapp.data.database.tables.sleep_session.SleepSessionEntity;
import com.rbraithwaite.sleepapp.data.database.tables.tag.TagContract;
import com.rbraithwaite.sleepapp.data.database.tables.tag.TagEntity;

import java.util.List;

public class SleepSessionWithExtras
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
    
    @Relation(
            parentColumn = SleepSessionContract.Columns.ID,
            entityColumn = SleepInterruptionContract.Columns.SESSION_ID
    )
    public List<SleepInterruptionEntity> interruptions;
    
//*********************************************************
// constructors
//*********************************************************

    public SleepSessionWithExtras()
    {
    }
    
    public SleepSessionWithExtras(
            SleepSessionEntity sleepSession,
            List<TagEntity> tags,
            List<SleepInterruptionEntity> interruptions)
    {
        this.sleepSession = sleepSession;
        this.tags = tags;
        this.interruptions = interruptions;
    }
}
