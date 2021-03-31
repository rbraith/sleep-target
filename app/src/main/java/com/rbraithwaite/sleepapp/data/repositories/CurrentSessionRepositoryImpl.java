package com.rbraithwaite.sleepapp.data.repositories;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.rbraithwaite.sleepapp.core.models.CurrentSession;
import com.rbraithwaite.sleepapp.core.repositories.CurrentSessionRepository;
import com.rbraithwaite.sleepapp.data.convert.ConvertCurrentSession;
import com.rbraithwaite.sleepapp.data.prefs.CurrentSessionPrefsData;
import com.rbraithwaite.sleepapp.data.prefs.SleepAppDataPrefs;

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
        setCurrentSession(new CurrentSession());
    }
    
    @Override
    public LiveData<CurrentSession> getCurrentSession()
    {
        return Transformations.map(
                mDataPrefs.getCurrentSession(),
                new Function<CurrentSessionPrefsData, CurrentSession>()
                {
                    @Override
                    public CurrentSession apply(CurrentSessionPrefsData input)
                    {
                        return ConvertCurrentSession.fromPrefsData(input);
                    }
                });
    }
    
    @Override
    public void setCurrentSession(@NonNull CurrentSession currentSession)
    {
        // REFACTOR [21-03-29 11:15PM] -- should the asynchronicity be here instead of down in the
        //  prefs? Or even higher in the view model?
        mDataPrefs.setCurrentSession(ConvertCurrentSession.toPrefsData(currentSession));
    }
}
