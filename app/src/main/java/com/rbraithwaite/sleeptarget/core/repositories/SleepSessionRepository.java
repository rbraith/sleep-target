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

package com.rbraithwaite.sleeptarget.core.repositories;

import androidx.lifecycle.LiveData;

import com.rbraithwaite.sleeptarget.core.models.Interruption;
import com.rbraithwaite.sleeptarget.core.models.Mood;
import com.rbraithwaite.sleeptarget.core.models.SleepSession;
import com.rbraithwaite.sleeptarget.data.database.tables.sleep_session.SleepSessionEntity;

import java.util.Date;
import java.util.List;

// TODO [21-03-24 10:28PM] document these methods.
public interface SleepSessionRepository
{
//*********************************************************
// public helpers
//*********************************************************

    public static class NewSleepSessionData
    {
        public Date start;
        public Date end;
        public long durationMillis;
        public String additionalComments;
        // REFACTOR [21-05-10 3:45PM] -- this should be the mood index instead.
        public Mood mood;
        public List<Integer> tagIds;
        public List<Interruption> interruptions;
        
        public float rating;
        
        public NewSleepSessionData(
                Date start,
                Date end,
                long durationMillis,
                String additionalComments,
                Mood mood,
                List<Integer> tagIds,
                List<Interruption> interruptions,
                float rating)
        {
            this.start = start;
            this.end = end;
            this.durationMillis = durationMillis;
            this.additionalComments = additionalComments;
            this.mood = mood;
            this.tagIds = tagIds;
            this.interruptions = interruptions;
            this.rating = rating;
        }
        
        public SleepSessionEntity toEntity()
        {
            return new SleepSessionEntity(
                    start,
                    end,
                    durationMillis,
                    additionalComments,
                    mood == null ? null : mood.asIndex(),
                    rating);
        }
    }

//*********************************************************
// abstract
//*********************************************************

    // REFACTOR [21-03-24 10:41PM] -- Right now repositories are working with domain entities,
    //  coupling these entities to the repository implementations - a more 'clean' approach would
    //  be to define simple data structures for this layer boundary.
    void addSleepSession(final NewSleepSessionData newSleepSession);
    
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
    
    // REFACTOR [21-03-24 10:29PM] This should be a LiveData - the sleep session should
    //  stay synced with the db.
    /**
     * Gets the first sleep session that starts after the provided time from epoch (in reverse
     * order, that is, the sleep session with the earliest start time while still being after
     * dateTimeMillis.)
     * <p>
     * NOTE: This method is synchronous.
     */
    SleepSession getFirstSleepSessionStartingAfter(long dateTimeMillis);
    
    /**
     * Eh...I need to workshop the name for this. The idea is this will give you 'count' sleep
     * sessions, going back in time and starting from 'offset'. For example, if 'count' is 50 and
     * 'offset' is 0, this will give you the 50 most recent sleep sessions going back in time
     * starting from the most recent. If 'offset' is 1, these would start from the second most
     * recent, and so on.
     *
     * @param offset How many sleep sessions from the latest to ignore, before starting to collect
     *               them.
     * @param count  The number of sleep sessions to collect. If this is more than there are sleep
     *               sessions available (given the offset), all those available sleep sessions are
     *               collected. (Thus the size of the returned list isn't always guaranteed to be
     *               'count')
     *
     * @return The latest 'count' sleep sessions from 'offset'.
     */
    LiveData<List<SleepSession>> getLatestSleepSessionsFromOffset(int offset, int count);
    
    /**
     * @return The total number of sleep sessions.
     */
    LiveData<Integer> getTotalSleepSessionCount();
    LiveData<List<SleepSession>> getAllSleepSessions();
    
    /**
     * @param start The range start.
     * @param end   The range end.
     *
     * @return The sleep session with the latest start time which also ends within the provided
     * range, or null if no sleep sessions end within that range.
     */
    SleepSession getLatestSleepSessionEndingInRangeSynced(Date start, Date end);
}
