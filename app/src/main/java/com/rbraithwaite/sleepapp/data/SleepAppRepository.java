package com.rbraithwaite.sleepapp.data;

import androidx.lifecycle.LiveData;

import com.rbraithwaite.sleepapp.data.database.SleepAppDatabase;
import com.rbraithwaite.sleepapp.data.database.tables.sleep_session.SleepSessionEntity;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Singleton;

// SMELL [20-12-22 12:31AM] -- The repository is starting to get big - sleep session, wake-time
//  goals, etc. Should I split the responsibilities for different data into different
//  repositories?
// SMELL [21-01-9 1:15AM] -- Also consider the possibility of viewmodels getting more data than
//  they need (I don't think this is a problem currently, but it likely will become one). I will
//  need to come up with a better solution to ensure that viewmodels are only receiving the data
//  that they need (I could possibly make one repository for each viewmodel? possibly also with
//  viewmodel-specific  DAOs (in addition to a common one or something)?).
@Singleton
public class SleepAppRepository
{
//*********************************************************
// private properties
//*********************************************************

    private SleepAppDataPrefs mDataPrefs;
    private SleepAppDatabase mDatabase;
    private Executor mExecutor;

//*********************************************************
// constructors
//*********************************************************

    @Inject
    public SleepAppRepository(
            SleepAppDataPrefs dataPrefs,
            SleepAppDatabase database,
            Executor executor)
    {
        mDataPrefs = dataPrefs;
        mDatabase = database;
        mExecutor = executor;
    }

//*********************************************************
// api
//*********************************************************

    public void addSleepSession(final SleepSessionEntity newSleepSession)
    {
        mExecutor.execute(new Runnable()
        {
            @Override
            public void run()
            {
                mDatabase.getSleepSessionDao().addSleepSession(newSleepSession);
            }
        });
    }
    
    public void updateSleepSession(final SleepSessionEntity sleepSession)
    {
        mExecutor.execute(new Runnable()
        {
            @Override
            public void run()
            {
                mDatabase.getSleepSessionDao().updateSleepSession(sleepSession);
            }
        });
    }
    
    public void clearCurrentSession()
    {
        setCurrentSession(null);
    }
    
    public LiveData<Date> getCurrentSession()
    {
        return mDataPrefs.getCurrentSession();
    }
    
    public void setCurrentSession(Date startTime)
    {
        mDataPrefs.setCurrentSession(startTime);
    }
    
    public LiveData<SleepSessionEntity> getSleepSession(int id)
    {
        return mDatabase.getSleepSessionDao().getSleepSession(id);
    }
    
    public LiveData<List<Integer>> getAllSleepSessionIds()
    {
        return mDatabase.getSleepSessionDao().getAllSleepSessionIds();
    }
    
    public void deleteSleepSession(final int id)
    {
        mExecutor.execute(new Runnable()
        {
            @Override
            public void run()
            {
                mDatabase.getSleepSessionDao().deleteSleepSession(id);
                // TODO [20-12-17 9:06PM] -- this will eventually need to delete the relevant
                //  data from
                //  the other tables as well - that data will need to be determined from the
                //  session data id
            }
        });
    }
    
    public LiveData<Long> getWakeTimeGoal()
    {
        return mDataPrefs.getWakeTimeGoal();
    }
    
    public void setWakeTimeGoal(long wakeTimeGoalMillis)
    {
        mDataPrefs.setWakeTimeGoal(wakeTimeGoalMillis);
    }
}
