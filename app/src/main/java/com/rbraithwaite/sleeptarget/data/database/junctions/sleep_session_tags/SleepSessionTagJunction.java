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

package com.rbraithwaite.sleeptarget.data.database.junctions.sleep_session_tags;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;

import com.rbraithwaite.sleeptarget.data.database.tables.sleep_session.SleepSessionContract;
import com.rbraithwaite.sleeptarget.data.database.tables.sleep_session.SleepSessionEntity;
import com.rbraithwaite.sleeptarget.data.database.tables.tag.TagContract;
import com.rbraithwaite.sleeptarget.data.database.tables.tag.TagEntity;

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
