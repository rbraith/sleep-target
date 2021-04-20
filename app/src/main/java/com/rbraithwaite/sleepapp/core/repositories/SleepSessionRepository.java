package com.rbraithwaite.sleepapp.core.repositories;

import androidx.lifecycle.LiveData;

import com.rbraithwaite.sleepapp.core.models.SleepSession;

import java.util.Date;
import java.util.List;

// TODO [21-03-24 10:28PM] document these methods.
public interface SleepSessionRepository
{
//*********************************************************
// abstract
//*********************************************************

    // REFACTOR [21-03-24 10:41PM] -- Right now repositories are working with domain entities,
    //  coupling these entities to the repository implementations - a more 'clean' approach would
    //  be to define simple data structures for this layer boundary.
    void addSleepSession(final SleepSession newSleepSession);
    
    // HACK [21-04-19 10:19PM] -- This is a really ugly solution to the tag data representation
    //  disparity between CurrentSession & SleepSession (see this method's usage in
    //  SleepTrackerFragmentViewModel
    //  and CurrentSession.toSleepSession())
    //  ---
    //  This is a strong argument for clearly defined simple-data domain boundaries (ie use cases).
    /**
     * @param newSleepSession The new SleepSession to add. This sleep session's id & tags are
     *                        ignored.
     * @param tagIds          The ids of the tags that belong to this new sleep session.
     */
    void addSleepSessionWithTags(SleepSession newSleepSession, List<Integer> tagIds);
    
    void updateSleepSession(final SleepSession updatedSleepSession);
    
    // REFACTOR [21-03-24 10:26PM] -- I will need to replace LiveData references in the core with
    //  RxJava - LiveData is a framework detail.
    LiveData<SleepSession> getSleepSession(final int id);
    
    void deleteSleepSession(final int id);
    
    /**
     * Returns the sleep sessions whose start OR end times fall within the provided range.
     */
    LiveData<List<SleepSession>> getSleepSessionsInRange(Date start, Date end);
    
    
    // SMELL [21-03-26 1:23AM] -- I should find a better way of managing async vs synced data
    //  access.
    /**
     * Same as getSleepSessionsInRange(), but executed on the current thread.
     */
    List<SleepSession> getSleepSessionsInRangeSynced(Date start, Date end);
    
    // REFACTOR [21-03-24 10:29PM] This should be a LiveData - the sleep session should
    //  stay synced with the db.
    /**
     * Gets the first sleep session that starts before the provided time from epoch (in reverse
     * order, that is, the sleep session with the latest start time while still being before
     * dateTimeMillis.)
     * <p>
     * NOTE: This method is synchronous.
     */
    SleepSession getFirstSleepSessionStartingBefore(long dateTimeMillis);
    
    LiveData<List<Integer>> getAllSleepSessionIds();
}
