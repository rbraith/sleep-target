package com.rbraithwaite.sleepapp.data.current_session;

import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.rbraithwaite.sleepapp.data.SleepAppDataPrefs;

import java.util.Date;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class CurrentSessionRepository
{
//*********************************************************
// private properties
//*********************************************************

    private SleepAppDataPrefs mDataPrefs;
    
//*********************************************************
// constructors
//*********************************************************

    @Inject
    public CurrentSessionRepository(SleepAppDataPrefs dataPrefs)
    {
        mDataPrefs = dataPrefs;
    }
    
//*********************************************************
// api
//*********************************************************

    public void clearCurrentSession()
    {
        setCurrentSession(null);
    }
    
    public LiveData<CurrentSessionModel> getCurrentSession()
    {
        return Transformations.map(
                mDataPrefs.getCurrentSession(),
                new Function<Date, CurrentSessionModel>()
                {
                    @Override
                    public CurrentSessionModel apply(Date currentSessionStart)
                    {
                        return new CurrentSessionModel(currentSessionStart);
                    }
                });
    }
    
    public void setCurrentSession(Date start)
    {
        mDataPrefs.setCurrentSession(start);
    }
}
