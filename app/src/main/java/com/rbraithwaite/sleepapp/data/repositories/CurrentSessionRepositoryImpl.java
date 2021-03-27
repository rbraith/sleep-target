package com.rbraithwaite.sleepapp.data.repositories;

import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.rbraithwaite.sleepapp.core.models.CurrentSession;
import com.rbraithwaite.sleepapp.core.repositories.CurrentSessionRepository;
import com.rbraithwaite.sleepapp.data.SleepAppDataPrefs;

import java.util.Date;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class CurrentSessionRepositoryImpl
        implements CurrentSessionRepository
{
//*********************************************************
// private properties
//*********************************************************

    private SleepAppDataPrefs mDataPrefs;

//*********************************************************
// constructors
//*********************************************************

    @Inject
    public CurrentSessionRepositoryImpl(SleepAppDataPrefs dataPrefs)
    {
        mDataPrefs = dataPrefs;
    }

//*********************************************************
// overrides
//*********************************************************

    @Override
    public void clearCurrentSession()
    {
        setCurrentSession(null);
    }
    
    @Override
    public LiveData<CurrentSession> getCurrentSession()
    {
        return Transformations.map(
                mDataPrefs.getCurrentSession(),
                new Function<Date, CurrentSession>()
                {
                    @Override
                    public CurrentSession apply(Date currentSessionStart)
                    {
                        return new CurrentSession(currentSessionStart);
                    }
                });
    }
    
    @Override
    public void setCurrentSession(Date start)
    {
        mDataPrefs.setCurrentSession(start);
    }
}
