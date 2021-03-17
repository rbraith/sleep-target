package com.rbraithwaite.sleepapp.data.sleep_session;

import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.rbraithwaite.sleepapp.data.database.tables.sleep_session.SleepSessionDao;
import com.rbraithwaite.sleepapp.data.database.tables.sleep_session.SleepSessionEntity;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SleepSessionRepository
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
    public SleepSessionRepository(
            SleepSessionDao sleepSessionDao,
            Executor executor)
    {
        mSleepSessionDao = sleepSessionDao;
        mExecutor = executor;
    }

//*********************************************************
// api
//*********************************************************

    public void addSleepSession(final SleepSessionModel newSleepSession)
    {
        mExecutor.execute(new Runnable()
        {
            @Override
            public void run()
            {
                mSleepSessionDao.addSleepSession(
                        SleepSessionModelConverter.convertModelToEntity(newSleepSession));
            }
        });
    }
    
    public void updateSleepSession(final SleepSessionModel sleepSession)
    {
        mExecutor.execute(new Runnable()
        {
            @Override
            public void run()
            {
                mSleepSessionDao.updateSleepSession(
                        SleepSessionModelConverter.convertModelToEntity(sleepSession));
            }
        });
    }
    
    public LiveData<SleepSessionModel> getSleepSession(int id)
    {
        return Transformations.map(
                mSleepSessionDao.getSleepSession(id),
                new Function<SleepSessionEntity, SleepSessionModel>()
                {
                    @Override
                    public SleepSessionModel apply(SleepSessionEntity input)
                    {
                        return SleepSessionModelConverter.convertEntityToModel(input);
                    }
                });
    }
    
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
    
    public LiveData<List<Integer>> getAllSleepSessionIds()
    {
        return mSleepSessionDao.getAllSleepSessionIds();
    }
    
    /**
     * Returns the sleep sessions whose start OR end times fall within the provided range.
     */
    public LiveData<List<SleepSessionModel>> getSleepSessionsInRange(
            Date start,
            Date end)
    {
        // switchMap() is used so that I can have a LiveData backend to post values to
        // asynchronously while also handling the asynchronicity of the Dao call
        return Transformations.switchMap(
                mSleepSessionDao.getSleepSessionsInRange(
                        start.getTime(),
                        end.getTime()),
                new Function<List<SleepSessionEntity>, LiveData<List<SleepSessionModel>>>()
                {
                    @Override
                    public LiveData<List<SleepSessionModel>> apply(final List<SleepSessionEntity> input)
                    {
                        final MutableLiveData<List<SleepSessionModel>> liveData =
                                new MutableLiveData<>();
                        // map the input to the SleepSessionModel list asynchronously
                        mExecutor.execute(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                List<SleepSessionModel> result =
                                        SleepSessionModelConverter.convertAllEntitiesToModels(input);
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
    public List<SleepSessionModel> getSleepSessionsInRangeSynced(Date start, Date end)
    {
        return SleepSessionModelConverter.convertAllEntitiesToModels(
                mSleepSessionDao.getSleepSessionsInRangeSynced(start.getTime(), end.getTime()));
    }
    
    /**
     * Gets the first sleep session that starts before the provided time from epoch (in reverse
     * order, that is, the sleep session with the latest start time while still being before
     * dateTimeMillis.)
     * <p>
     * NOTE: This method is synchronous.
     */
    public SleepSessionModel getFirstSleepSessionStartingBefore(long dateTimeMillis)
    {
        return SleepSessionModelConverter.convertEntityToModel(
                mSleepSessionDao.getFirstSleepSessionStartingBefore(dateTimeMillis));
    }
}
