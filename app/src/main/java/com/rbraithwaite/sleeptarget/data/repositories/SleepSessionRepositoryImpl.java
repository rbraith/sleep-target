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

package com.rbraithwaite.sleeptarget.data.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.rbraithwaite.sleeptarget.core.models.Interruption;
import com.rbraithwaite.sleeptarget.core.models.Interruptions;
import com.rbraithwaite.sleeptarget.core.models.SleepSession;
import com.rbraithwaite.sleeptarget.core.models.Tag;
import com.rbraithwaite.sleeptarget.core.repositories.SleepSessionRepository;
import com.rbraithwaite.sleeptarget.data.convert.ConvertInterruption;
import com.rbraithwaite.sleeptarget.data.convert.ConvertSleepSession;
import com.rbraithwaite.sleeptarget.data.database.tables.sleep_interruptions.SleepInterruptionDao;
import com.rbraithwaite.sleeptarget.data.database.tables.sleep_session.SleepSessionDao;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SleepSessionRepositoryImpl
        implements SleepSessionRepository
{
//*********************************************************
// private properties
//*********************************************************

    private SleepSessionDao mSleepSessionDao;
    private SleepInterruptionDao mInterruptionsDao;
    private Executor mExecutor;

//*********************************************************
// private constants
//*********************************************************

    private static final String TAG = "SleepSessionRepositoryI";

//*********************************************************
// constructors
//*********************************************************

    @Inject
    public SleepSessionRepositoryImpl(
            SleepSessionDao sleepSessionDao,
            SleepInterruptionDao sleepInterruptionDao,
            Executor executor)
    {
        mInterruptionsDao = sleepInterruptionDao;
        mSleepSessionDao = sleepSessionDao;
        mExecutor = executor;
    }

//*********************************************************
// overrides
//*********************************************************

    @Override
    public void addSleepSession(final NewSleepSessionData newSleepSession)
    {
        mExecutor.execute(() -> mSleepSessionDao.addSleepSessionWithExtras(
                newSleepSession.toEntity(),
                newSleepSession.tagIds,
                newSleepSession.interruptions.stream()
                        .map(ConvertInterruption::toEntity)
                        .collect(Collectors.toList())));
    }
    
    @Override
    public void updateSleepSession(final SleepSession sleepSession)
    {
        mExecutor.execute(() -> {
            mSleepSessionDao.updateSleepSessionWithTags(
                    ConvertSleepSession.toEntity(sleepSession),
                    sleepSession.getTags()
                            .stream()
                            .map(Tag::getTagId)
                            .collect(Collectors.toList()));
            
            Interruptions interruptions = sleepSession.getInterruptions();
            if (interruptions.hasUpdates()) {
                Interruptions.Updates updates = interruptions.consumeUpdates();
                
                mSleepSessionDao.addInterruptionsToSleepSession(
                        sleepSession.getId(),
                        ConvertInterruption.listToEntityList(updates.added));
                
                mInterruptionsDao.updateMany(
                        ConvertInterruption.listToEntityList(updates.updated,
                                                             sleepSession.getId()));
                
                mInterruptionsDao.deleteMany(getIdsOf(updates.deleted));
            }
        });
    }
    
    @Override
    public LiveData<SleepSession> getSleepSession(int id)
    {
        return Transformations.map(
                mSleepSessionDao.getSleepSessionWithExtras(id),
                ConvertSleepSession::fromEntityWithExtras);
    }
    
    @Override
    public void deleteSleepSession(final int id)
    {
        mExecutor.execute(() -> mSleepSessionDao.deleteSleepSession(id));
    }
    
    /**
     * Returns the sleep sessions whose start OR end times fall within the provided range.
     */
    @Override
    public LiveData<List<SleepSession>> getSleepSessionsInRange(
            Date start,
            Date end)
    {
        // switchMap() is used so that I can have a LiveData backend to post values to
        // asynchronously while also handling the asynchronicity of the Dao call
        return Transformations.switchMap(
                mSleepSessionDao.getSleepSessionWithExtrasInRange(
                        start.getTime(),
                        end.getTime()),
                sleepSessionsWithExtras -> {
                    final MutableLiveData<List<SleepSession>> liveData =
                            new MutableLiveData<>();
                    // map the input to the SleepSessionModel list asynchronously
                    mExecutor.execute(() -> {
                        List<SleepSession> result =
                                ConvertSleepSession.listFromEntityWithExtrasList(
                                        sleepSessionsWithExtras);
                        liveData.postValue(result);
                    });
                    return liveData;
                });
    }
    
    /**
     * Same as getSleepSessionsInRange(), but executed on the current thread.
     */
    @Override
    public List<SleepSession> getSleepSessionsInRangeSynced(Date start, Date end)
    {
        return ConvertSleepSession.fromEntities(
                mSleepSessionDao.getSleepSessionsInRangeSynced(start.getTime(), end.getTime()));
    }
    
    /**
     * Gets the first sleep session that starts before the provided time from epoch (in reverse
     * order, that is, the sleep session with the latest start time while still being before
     * dateTimeMillis.)
     * <p>
     * NOTE: This method is synchronous.
     */
    @Override
    public SleepSession getFirstSleepSessionStartingBefore(long dateTimeMillis)
    {
        return ConvertSleepSession.fromEntity(
                mSleepSessionDao.getFirstSleepSessionStartingBefore(dateTimeMillis));
    }
    
    /**
     * Gets the first sleep session that starts after the provided time from epoch (in reverse
     * order, that is, the sleep session with the earliest start time while still being after
     * dateTimeMillis.)
     * <p>
     * NOTE: This method is synchronous.
     */
    @Override
    public SleepSession getFirstSleepSessionStartingAfter(long dateTimeMillis)
    {
        return ConvertSleepSession.fromEntity(
                mSleepSessionDao.getFirstSleepSessionStartingAfter(dateTimeMillis));
    }
    
    @Override
    public LiveData<List<SleepSession>> getLatestSleepSessionsFromOffset(int offset, int count)
    {
        return Transformations.map(
                mSleepSessionDao.getLatestSleepSessionsFromOffset(offset, count),
                ConvertSleepSession::fromEntities);
    }
    
    @Override
    public LiveData<Integer> getTotalSleepSessionCount()
    {
        return mSleepSessionDao.getTotalSleepSessionCount();
    }
    
    @Override
    public LiveData<List<SleepSession>> getAllSleepSessions()
    {
        return Transformations.map(
                mSleepSessionDao.getAllSleepSessionsWithExtras(),
                sleepSessionsWithExtras -> sleepSessionsWithExtras.stream()
                        .map(ConvertSleepSession::fromEntityWithExtras)
                        .collect(Collectors.toList()));
    }
    
    /**
     * @param start The range start.
     * @param end   The range end.
     *
     * @return The sleep session with the latest start time which also ends within the provided
     * range, or null if no sleep sessions end within that range.
     */
    @Override
    public SleepSession getLatestSleepSessionEndingInRangeSynced(Date start, Date end)
    {
        return ConvertSleepSession.fromEntity(
                mSleepSessionDao.getLatestSleepSessionEndingInRangeSynced(start.getTime(),
                                                                          end.getTime()));
    }

//*********************************************************
// private methods
//*********************************************************

    private List<Integer> getIdsOf(List<Interruption> interruptions)
    {
        return interruptions.stream().map(Interruption::getId).collect(Collectors.toList());
    }
}
