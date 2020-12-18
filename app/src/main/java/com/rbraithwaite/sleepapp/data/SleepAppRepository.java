package com.rbraithwaite.sleepapp.data;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.rbraithwaite.sleepapp.data.database.SleepAppDatabase;
import com.rbraithwaite.sleepapp.data.database.views.SleepSessionData;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Singleton;

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

    public void addSleepSessionData(final SleepSessionData sleepSessionData)
    {
        mExecutor.execute(new Runnable()
        {
            @Override
            public void run()
            {
                mDatabase.getSleepSessionDao()
                        .addSleepSession(sleepSessionData.toSleepSessionEntity());
            }
        });
    }
    
    public void updateSleepSessionData(final SleepSessionData sleepSessionData)
    {
        mExecutor.execute(new Runnable()
        {
            @Override
            public void run()
            {
                mDatabase.getSleepSessionDao()
                        .updateSleepSession(sleepSessionData.toSleepSessionEntity());
            }
        });
    }
    
    public void setCurrentSession(Context context, Date startTime)
    {
        mDataPrefs.setCurrentSession(context, startTime);
    }
    
    public void clearCurrentSession(Context context)
    {
        setCurrentSession(context, null);
    }
    
    public LiveData<Date> getCurrentSession(Context context)
    {
        return mDataPrefs.getCurrentSession(context);
    }
    
    public LiveData<SleepSessionData> getSleepSessionData(int id)
    {
        return mDatabase.getSleepSessionDataDao().getSleepSessionData(id);
    }
    
    public LiveData<List<Integer>> getAllSleepSessionDataIds()
    {
        return mDatabase.getSleepSessionDataDao().getAllSleepSessionDataIds();
    }
    
    public void deleteSleepSessionData(final int sessionDataId)
    {
        mExecutor.execute(new Runnable()
        {
            @Override
            public void run()
            {
                mDatabase.getSleepSessionDao().deleteSleepSession(sessionDataId);
                // TODO [20-12-17 9:06PM] -- this will eventually need to delete the relevant
                //  data from
                //  the other tables as well - that data will need to be determined from the
                //  session data id
            }
        });
    }
}
