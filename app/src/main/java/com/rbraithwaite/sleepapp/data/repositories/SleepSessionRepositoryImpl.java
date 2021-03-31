package com.rbraithwaite.sleepapp.data.repositories;

import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.rbraithwaite.sleepapp.core.models.SleepSession;
import com.rbraithwaite.sleepapp.core.repositories.SleepSessionRepository;
import com.rbraithwaite.sleepapp.data.convert.ConvertSleepSession;
import com.rbraithwaite.sleepapp.data.database.tables.sleep_session.SleepSessionDao;
import com.rbraithwaite.sleepapp.data.database.tables.sleep_session.SleepSessionEntity;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;

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
    private Executor mExecutor;

//*********************************************************
// constructors
//*********************************************************

    @Inject
    public SleepSessionRepositoryImpl(
            SleepSessionDao sleepSessionDao,
            Executor executor)
    {
        mSleepSessionDao = sleepSessionDao;
        mExecutor = executor;
    }

//*********************************************************
// overrides
//*********************************************************

    @Override
    public void addSleepSession(final SleepSession newSleepSession)
    {
        mExecutor.execute(new Runnable()
        {
            @Override
            public void run()
            {
                mSleepSessionDao.addSleepSession(
                        ConvertSleepSession.toEntity(newSleepSession));
            }
        });
    }
    
    @Override
    public void updateSleepSession(final SleepSession sleepSession)
    {
        mExecutor.execute(new Runnable()
        {
            @Override
            public void run()
            {
                mSleepSessionDao.updateSleepSession(
                        ConvertSleepSession.toEntity(sleepSession));
            }
        });
    }
    
    @Override
    public LiveData<SleepSession> getSleepSession(int id)
    {
        return Transformations.map(
                mSleepSessionDao.getSleepSession(id),
                new Function<SleepSessionEntity, SleepSession>()
                {
                    @Override
                    public SleepSession apply(SleepSessionEntity input)
                    {
                        return ConvertSleepSession.fromEntity(input);
                    }
                });
    }
    
    @Override
    public void deleteSleepSession(final int id)
    {
        mExecutor.execute(new Runnable()
        {
            @Override
            public void run()
            {
                mSleepSessionDao.deleteSleepSession(id);
            }
        });
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
                mSleepSessionDao.getSleepSessionsInRange(
                        start.getTime(),
                        end.getTime()),
                new Function<List<SleepSessionEntity>, LiveData<List<SleepSession>>>()
                {
                    @Override
                    public LiveData<List<SleepSession>> apply(final List<SleepSessionEntity> input)
                    {
                        final MutableLiveData<List<SleepSession>> liveData =
                                new MutableLiveData<>();
                        // map the input to the SleepSessionModel list asynchronously
                        mExecutor.execute(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                List<SleepSession> result =
                                        ConvertSleepSession.fromEntities(input);
                                liveData.postValue(result);
                            }
                        });
                        return liveData;
                    }
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
    
    public LiveData<List<Integer>> getAllSleepSessionIds()
    {
        return mSleepSessionDao.getAllSleepSessionIds();
    }
}
