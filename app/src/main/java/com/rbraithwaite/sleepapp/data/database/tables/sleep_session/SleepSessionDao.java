package com.rbraithwaite.sleepapp.data.database.tables.sleep_session;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.rbraithwaite.sleepapp.data.database.junctions.sleep_session_tags.SleepSessionTagContract;
import com.rbraithwaite.sleepapp.data.database.junctions.sleep_session_tags.SleepSessionTagJunction;
import com.rbraithwaite.sleepapp.data.database.tables.sleep_session.data.SleepSessionWithTags;

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
    public abstract LiveData<SleepSessionWithTags> getSleepSessionWithTags(int sleepSessionId);
    
    @Query("SELECT " + SleepSessionContract.Columns.ID +
           " FROM " + SleepSessionContract.TABLE_NAME)
    public abstract LiveData<List<Integer>> getAllSleepSessionIds();
    
    @Query("SELECT * FROM " + SleepSessionContract.TABLE_NAME +
           " WHERE " +
           "(" + SleepSessionContract.Columns.START_TIME + " BETWEEN :start AND :end)" +
           " OR " +
           "(" + SleepSessionContract.Columns.END_TIME + " BETWEEN :start AND :end)")
    public abstract LiveData<List<SleepSessionEntity>> getSleepSessionsInRange(
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
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract void addTagsToSleepSession(List<SleepSessionTagJunction> junctions);
    
    @Query("DELETE FROM " + SleepSessionTagContract.TABLE_NAME +
           " WHERE " + SleepSessionTagContract.Columns.SESSION_ID + " = :sleepSessionId")
    protected abstract void deleteAllTagsFromSleepSession(int sleepSessionId);


//*********************************************************
// api
//*********************************************************

    
    /**
     * Adds a new sleep session and its associated tags.
     *
     * @param sleepSession The new sleep session to add
     * @param tagIds       The ids of the tags associated with the new sleep session
     *
     * @return The id of the new sleep session
     */
    @Transaction
    public long addSleepSessionWithTags(SleepSessionEntity sleepSession, List<Integer> tagIds)
    {
        long newSessionId = addSleepSession(sleepSession);
        
        _addTagsToSleepSession((int) newSessionId, tagIds);
        
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
