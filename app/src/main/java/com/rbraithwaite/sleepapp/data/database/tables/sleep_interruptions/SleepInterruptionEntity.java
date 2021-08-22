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

package com.rbraithwaite.sleepapp.data.database.tables.sleep_interruptions;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import com.rbraithwaite.sleepapp.data.database.tables.sleep_session.SleepSessionContract;
import com.rbraithwaite.sleepapp.data.database.tables.sleep_session.SleepSessionEntity;

import java.util.Date;
import java.util.Objects;

@Entity(
        tableName = SleepInterruptionContract.TABLE_NAME,
        foreignKeys = {
                @ForeignKey(
                        entity = SleepSessionEntity.class,
                        parentColumns = SleepSessionContract.Columns.ID,
                        childColumns = SleepInterruptionContract.Columns.SESSION_ID,
                        onDelete = ForeignKey.CASCADE)
        }
)
public class SleepInterruptionEntity
{
//*********************************************************
// public properties
//*********************************************************

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = SleepInterruptionContract.Columns.ID)
    public int id;
    
    @ColumnInfo(name = SleepInterruptionContract.Columns.SESSION_ID)
    public long sessionId;
    
    @ColumnInfo(name = SleepInterruptionContract.Columns.START_TIME)
    public Date startTime;
    
    @ColumnInfo(name = SleepInterruptionContract.Columns.DURATION_MILLIS)
    public int durationMillis;
    
    @ColumnInfo(name = SleepInterruptionContract.Columns.REASON)
    public String reason;

//*********************************************************
// constructors
//*********************************************************

    public SleepInterruptionEntity()
    {
    }
    
    public SleepInterruptionEntity(Date startTime, int durationMillis, String reason)
    {
        this(0, startTime, durationMillis, reason);
    }
    
    public SleepInterruptionEntity(int id, Date startTime, int durationMillis, String reason)
    {
        this(id, 0, startTime, durationMillis, reason);
    }
    
    public SleepInterruptionEntity(
            int id,
            long sessionId,
            Date startTime,
            int durationMillis,
            String reason)
    {
        this.id = id;
        this.sessionId = sessionId;
        this.startTime = startTime;
        this.durationMillis = durationMillis;
        this.reason = reason;
    }
    
//*********************************************************
// overrides
//*********************************************************

    @Override
    public int hashCode()
    {
        int result = id;
        result = 31 * result + (int) (sessionId ^ (sessionId >>> 32));
        result = 31 * result + startTime.hashCode();
        result = 31 * result + durationMillis;
        result = 31 * result + (reason != null ? reason.hashCode() : 0);
        return result;
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        
        SleepInterruptionEntity that = (SleepInterruptionEntity) o;
        
        if (id != that.id) { return false; }
        if (sessionId != that.sessionId) { return false; }
        if (!Objects.equals(startTime, that.startTime)) { return false; }
        if (durationMillis != that.durationMillis) { return false; }
        return Objects.equals(reason, that.reason);
    }
}
