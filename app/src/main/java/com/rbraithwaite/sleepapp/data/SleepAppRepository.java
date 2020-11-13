package com.rbraithwaite.sleepapp.data;

import android.content.Context;

import androidx.lifecycle.LiveData;


import com.rbraithwaite.sleepapp.data.database.SleepAppDatabase;
import com.rbraithwaite.sleepapp.data.database.tables.SleepSessionEntity;
import com.rbraithwaite.sleepapp.data.database.views.SleepSessionData;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SleepAppRepository
{
    private SleepAppDataPrefs mDataPrefs;
    private SleepAppDatabase mDatabase;
    private Executor mExecutor;

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

    public void addSleepSession(final SleepSessionEntity sleepSession)
    {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mDatabase.getSleepSessionDao().addSleepSession(sleepSession);
            }
        });
    }

    public void setCurrentSession(Context context, Date startTime) {
        mDataPrefs.setCurrentSession(context, startTime);
    }

    public LiveData<Date> getCurrentSession(Context context) {
        return mDataPrefs.getCurrentSession(context);
    }

    public LiveData<SleepSessionData> getSleepSessionData(int id) {
        return mDatabase.getSleepSessionDataDao().getSleepSessionData(id);
    }

    public LiveData<List<Integer>> getAllSleepSessionDataIds() {
        return mDatabase.getSleepSessionDataDao().getAllSleepSessionDataIds();
    }
}
