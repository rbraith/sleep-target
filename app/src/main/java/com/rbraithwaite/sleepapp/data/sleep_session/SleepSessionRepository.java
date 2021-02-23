package com.rbraithwaite.sleepapp.data.sleep_session;

import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.rbraithwaite.sleepapp.data.SleepAppDataPrefs;
import com.rbraithwaite.sleepapp.data.database.tables.sleep_session.SleepSessionDao;
import com.rbraithwaite.sleepapp.data.database.tables.sleep_session.SleepSessionEntity;

import java.util.ArrayList;
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
                                // List.stream() requires api 24+ :/
                                List<SleepSessionModel> result = new ArrayList<>();
                                for (SleepSessionEntity entity : input) {
                                    result.add(SleepSessionModelConverter.convertEntityToModel(
                                            entity));
                                }
                                liveData.postValue(result);
                            }
                        });
                        return liveData;
                    }
                });
    }
}
