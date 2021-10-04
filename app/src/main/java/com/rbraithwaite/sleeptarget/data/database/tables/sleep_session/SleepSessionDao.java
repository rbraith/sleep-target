/*
 * Copyright (c) 2020-2021 Rich Braithwaite (rbraithwaite.dev@gmail.com)
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
package com.rbraithwaite.sleeptarget.data.database.tables.sleep_session;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.rbraithwaite.sleeptarget.data.database.junctions.sleep_session_tags.SleepSessionTagContract;
import com.rbraithwaite.sleeptarget.data.database.junctions.sleep_session_tags.SleepSessionTagJunction;
import com.rbraithwaite.sleeptarget.data.database.tables.sleep_interruptions.SleepInterruptionEntity;
import com.rbraithwaite.sleeptarget.data.database.tables.sleep_session.data.SleepSessionWithExtras;
import com.rbraithwaite.sleeptarget.data.database.tables.sleep_session.data.SleepSessionWithTags;

import java.util.ArrayList;
import java.util.List;

@Dao
public abstract class SleepSessionDao
{
//*********************************************************
// abstract
//*********************************************************

    @Insert
    public abstract long addSleepSession(SleepSessionEntity sleepSession);
    
    @Update
    public abstract void updateSleepSession(SleepSessionEntity sleepSession);
    
    // IDEA [20-12-17 9:03PM] -- Using a query here is one option. Another option
    //  would be using @Delete w/ a POJO containing the id
    //  see: https://developer.android.com/reference/kotlin/androidx/room/Delete
    @Query("DELETE FROM " + SleepSessionContract.TABLE_NAME +
           " WHERE " + SleepSessionContract.Columns.ID + " = :sleepSessionId")
    public abstract void deleteSleepSession(int sleepSessionId);
    
    @Query("SELECT * FROM " + SleepSessionContract.TABLE_NAME +
           " WHERE " + SleepSessionContract.Columns.ID + " = :sleepSessionId")
    public abstract LiveData<SleepSessionEntity> getSleepSession(int sleepSessionId);
    
    @Transaction
    @Query("SELECT * FROM " + SleepSessionContract.TABLE_NAME +
           " WHERE " + SleepSessionContract.Columns.ID + " = :sleepSessionId")
    public abstract LiveData<SleepSessionWithExtras> getSleepSessionWithExtras(int sleepSessionId);
    
    // REFACTOR [21-07-8 11:38PM] -- replace this with getSleepSessionWithExtras.
    @Deprecated
    @Transaction
    @Query("SELECT * FROM " + SleepSessionContract.TABLE_NAME +
           " WHERE " + SleepSessionContract.Columns.ID + " = :sleepSessionId")
    public abstract LiveData<SleepSessionWithTags> getSleepSessionWithTags(int sleepSessionId);
    
    @Query("SELECT * FROM " + SleepSessionContract.TABLE_NAME +
           " WHERE " +
           "(" + SleepSessionContract.Columns.START_TIME + " BETWEEN :start AND :end)" +
           " OR " +
           "(" + SleepSessionContract.Columns.END_TIME + " BETWEEN :start AND :end)")
    public abstract LiveData<List<SleepSessionEntity>> getSleepSessionsInRange(
            long start,
            long end);
    
    // REFACTOR [21-08-4 6:34PM] -- this duplicates the query for getSleepSessionsInRange.
    @Query("SELECT * FROM " + SleepSessionContract.TABLE_NAME +
           " WHERE " +
           "(" + SleepSessionContract.Columns.START_TIME + " BETWEEN :start AND :end)" +
           " OR " +
           "(" + SleepSessionContract.Columns.END_TIME + " BETWEEN :start AND :end)")
    public abstract LiveData<List<SleepSessionWithExtras>> getSleepSessionWithExtrasInRange(
            long start,
            long end);
    
    // REFACTOR [21-03-16 3:47PM] -- duplicates getSleepSessionsInRange() query.
    @Query("SELECT * FROM " + SleepSessionContract.TABLE_NAME +
           " WHERE " +
           "(" + SleepSessionContract.Columns.START_TIME + " BETWEEN :start AND :end)" +
           " OR " +
           "(" + SleepSessionContract.Columns.END_TIME + " BETWEEN :start AND :end)")
    public abstract List<SleepSessionEntity> getSleepSessionsInRangeSynced(long start, long end);
    
    @Query("SELECT * FROM " + SleepSessionContract.TABLE_NAME +
           " WHERE " + SleepSessionContract.Columns.START_TIME + " <= :dateTimeMillis" +
           " ORDER BY " + SleepSessionContract.Columns.START_TIME + " DESC" +
           " LIMIT 1;")
    public abstract SleepSessionEntity getFirstSleepSessionStartingBefore(long dateTimeMillis);
    
    // REFACTOR [21-09-29 1:07AM] -- semi-colon isn't needed here.
    @Query("SELECT * FROM " + SleepSessionContract.TABLE_NAME +
           " WHERE " + SleepSessionContract.Columns.START_TIME + " >= :dateTimeMillis" +
           " ORDER BY " + SleepSessionContract.Columns.START_TIME + " ASC" +
           " LIMIT 1;")
    public abstract SleepSessionEntity getFirstSleepSessionStartingAfter(long dateTimeMillis);
    
    // OPTIMIZE [21-05-17 4:04PM] -- The ORDER BY here seems really inefficient? Like, is every
    //  call to this sorting the entire table every time? Investigate this.
    // https://stackoverflow.com/a/8976988
    @Query("SELECT * FROM " + SleepSessionContract.TABLE_NAME +
           " ORDER BY " + SleepSessionContract.Columns.START_TIME + " DESC" +
           " LIMIT :count OFFSET :offset")
    public abstract LiveData<List<SleepSessionEntity>> getLatestSleepSessionsFromOffset(
            int offset,
            int count);
    
    @Query("SELECT COUNT(*) FROM " + SleepSessionContract.TABLE_NAME)
    public abstract LiveData<Integer> getTotalSleepSessionCount();
    
    @Query("SELECT * FROM " + SleepSessionContract.TABLE_NAME)
    public abstract LiveData<List<SleepSessionEntity>> getAllSleepSessions();
    
    // TEST NEEDED [21-06-30 6:16PM] -- .
    // TODO [21-07-1 1:44AM] -- sorting is currently hardcoded - I can parameterize this.
    // REFACTOR [21-07-20 3:34PM] -- use getAllSleepSessionsWithExtras instead.
    @Deprecated
    @Transaction
    @Query("SELECT * FROM " + SleepSessionContract.TABLE_NAME +
           " ORDER BY " + SleepSessionContract.Columns.START_TIME + " DESC")
    public abstract LiveData<List<SleepSessionWithTags>> getAllSleepSessionsWithTags();
    
    // TEST NEEDED [21-07-20 3:34PM]
    // TODO [21-07-20 3:34PM] -- sorting is currently hardcoded - I can parameterize this..
    @Transaction
    @Query("SELECT * FROM " + SleepSessionContract.TABLE_NAME +
           " ORDER BY " + SleepSessionContract.Columns.START_TIME + " DESC")
    public abstract LiveData<List<SleepSessionWithExtras>> getAllSleepSessionsWithExtras();
    
    // TEST NEEDED [21-08-7 5:46PM]
    @Query("SELECT * FROM " + SleepSessionContract.TABLE_NAME +
           " WHERE " + "(" + SleepSessionContract.Columns.END_TIME +
           " BETWEEN :rangeStart AND :rangeEnd)" +
           " ORDER BY " + SleepSessionContract.Columns.START_TIME + " DESC" +
           " LIMIT 1")
    public abstract SleepSessionEntity getLatestSleepSessionEndingInRangeSynced(
            long rangeStart,
            long rangeEnd);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract void addTagsToSleepSession(List<SleepSessionTagJunction> junctions);
    
    @Query("DELETE FROM " + SleepSessionTagContract.TABLE_NAME +
           " WHERE " + SleepSessionTagContract.Columns.SESSION_ID + " = :sleepSessionId")
    protected abstract void deleteAllTagsFromSleepSession(int sleepSessionId);
    
    @Insert
    protected abstract void addInterruptions(List<SleepInterruptionEntity> interruptions);

//*********************************************************
// api
//*********************************************************

    public void addInterruptionsToSleepSession(
            long sleepSessionId,
            List<SleepInterruptionEntity> interruptions)
    {
        for (SleepInterruptionEntity interruption : interruptions) {
            interruption.sessionId = sleepSessionId;
            interruption.id = 0;
        }
        addInterruptions(interruptions);
    }
    
    
    /**
     * Adds a new sleep session and its associated tags.
     *
     * @param sleepSession The new sleep session to add
     * @param tagIds       The ids of the tags associated with the new sleep session
     *
     * @return The id of the new sleep session
     */
    // REFACTOR [21-07-8 10:02PM] delete this.
    @Deprecated
    @Transaction
    public long addSleepSessionWithTags(SleepSessionEntity sleepSession, List<Integer> tagIds)
    {
        long newSessionId = addSleepSession(sleepSession);
        
        _addTagsToSleepSession((int) newSessionId, tagIds);
        
        return newSessionId;
    }
    
    /**
     * Adds a new sleep session with its associated tag and interruption data.
     *
     * @param sleepSession  The new sleep session to add
     * @param tagIds        The ids of the tags associated with the new sleep session
     * @param interruptions The interruptions associated with the new sleep session
     *
     * @return The id of the new sleep session
     */
    public long addSleepSessionWithExtras(
            SleepSessionEntity sleepSession,
            List<Integer> tagIds,
            List<SleepInterruptionEntity> interruptions)
    {
        long newSessionId = addSleepSession(sleepSession);
        
        // REFACTOR [21-07-8 10:17PM] -- I should switch the names of these functions -
        //  the Room function should have the leading underscore instead.
        _addTagsToSleepSession((int) newSessionId, tagIds);
        
        addInterruptionsToSleepSession(newSessionId, interruptions);
        
        return newSessionId;
    }
    
    @Transaction
    public void updateSleepSessionWithTags(
            SleepSessionEntity updatedSleepSession,
            List<Integer> tagIds)
    {
        updateSleepSession(updatedSleepSession);
        deleteAllTagsFromSleepSession(updatedSleepSession.id);
        _addTagsToSleepSession(updatedSleepSession.id, tagIds);
    }

//*********************************************************
// private methods
//*********************************************************

    // REFACTOR [21-04-22 8:51PM] -- Find a better name for this.
    private void _addTagsToSleepSession(int sleepSessionId, List<Integer> tagIds)
    {
        List<SleepSessionTagJunction> junctions = new ArrayList<>();
        for (Integer tagId : tagIds) {
            SleepSessionTagJunction junction = new SleepSessionTagJunction();
            junction.sessionId = sleepSessionId;
            junction.tagId = tagId;
            junctions.add(junction);
        }
        addTagsToSleepSession(junctions);
    }
}
