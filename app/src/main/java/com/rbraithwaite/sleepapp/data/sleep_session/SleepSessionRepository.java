package com.rbraithwaite.sleepapp.data.sleep_session;

import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.rbraithwaite.sleepapp.data.SleepAppDataPrefs;
import com.rbraithwaite.sleepapp.data.database.tables.sleep_session.SleepSessionDao;
import com.rbraithwaite.sleepapp.data.database.tables.sleep_session.SleepSessionEntity;

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

    private SleepAppDataPrefs mDataPrefs;
    private SleepSessionDao mSleepSessionDao;
    private Executor mExecutor;
    
//*********************************************************
// constructors
//*********************************************************

    @Inject
    public SleepSessionRepository(
            SleepAppDataPrefs dataPrefs,
            SleepSessionDao sleepSessionDao,
            Executor executor)
    {
        mDataPrefs = dataPrefs;
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
}
