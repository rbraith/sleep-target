package com.rbraithwaite.sleepapp.data.repositories;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.rbraithwaite.sleepapp.core.models.CurrentSession;
import com.rbraithwaite.sleepapp.core.repositories.CurrentSessionRepository;
import com.rbraithwaite.sleepapp.data.convert.ConvertCurrentSession;
import com.rbraithwaite.sleepapp.data.prefs.CurrentSessionPrefs;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class CurrentSessionRepositoryImpl
        implements CurrentSessionRepository
{
//*********************************************************
// private constants
//*********************************************************

    private final CurrentSessionPrefs mCurrentSessionPrefs;

//*********************************************************
// constructors
//*********************************************************

    @Inject
    public CurrentSessionRepositoryImpl(CurrentSessionPrefs currentSessionPrefs)
    {
        mCurrentSessionPrefs = currentSessionPrefs;
    }

//*********************************************************
// overrides
//*********************************************************

    @Override
    public void clearCurrentSession()
    {
        mCurrentSessionPrefs.clearCurrentSession();
    }
    
    @Override
    public LiveData<CurrentSession> getCurrentSession()
    {
        return Transformations.map(
                mCurrentSessionPrefs.getCurrentSession(),
                ConvertCurrentSession::fromPrefsData);
    }
    
    @Override
    public void setCurrentSession(@NonNull CurrentSession currentSession)
    {
        // REFACTOR [21-03-29 11:15PM] -- should the asynchronicity be here instead of down in the
        //  prefs? Or even higher in the view model?
        mCurrentSessionPrefs.setCurrentSession(ConvertCurrentSession.toPrefsData(currentSession));
    }
}
