package com.rbraithwaite.sleepapp.ui.sleep_tracker;

import android.content.Context;

import androidx.hilt.lifecycle.ViewModelInject;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.rbraithwaite.sleepapp.data.SleepAppRepository;
import com.rbraithwaite.sleepapp.data.database.tables.SleepSessionEntity;

import java.util.Date;
import java.util.GregorianCalendar;

public class SleepTrackerFragmentViewModel
        extends ViewModel
{
//*********************************************************
// private properties
//*********************************************************

    private SleepAppRepository mRepository;
    
    private LiveData<Date> mCurrentSleepSession;
    private LiveData<Boolean> mInSleepSession;
    
//*********************************************************
// constructors
//*********************************************************

    @ViewModelInject
    public SleepTrackerFragmentViewModel(SleepAppRepository repository)
    {
        mRepository = repository;
    }

    
//*********************************************************
// api
//*********************************************************

    public LiveData<Boolean> inSleepSession(Context context)
    {
        // todo
        //  should i add an empty observer to inSleepSession just to guarantee that its active?
        if (mInSleepSession == null) {
            LiveData<Date> currentSleepSessionStartDate = getCurrentSleepSession(context);
            final MediatorLiveData<Boolean> mediator = new MediatorLiveData<Boolean>();
            mediator.addSource(currentSleepSessionStartDate, new Observer<Date>()
            {
                @Override
                public void onChanged(Date date)
                {
                    mediator.setValue((date != null));
                }
            });
            mInSleepSession = mediator;
        }
        return mInSleepSession;
    }
    
    public void startSleepSession(Context context)
    {
        mRepository.setCurrentSession(context, new GregorianCalendar().getTime());
    }
    
    public void endSleepSession(Context context)
    {
        if (inSleepSession(context).getValue()) {
            Date currentSessionEnd = new GregorianCalendar().getTime();
            Date currentSessionStart = getCurrentSleepSession(context).getValue();
            
            long durationMillis = currentSessionEnd.getTime() - currentSessionStart.getTime();
            
            SleepSessionEntity newSleepSession = new SleepSessionEntity();
            newSleepSession.startTime = currentSessionStart;
            newSleepSession.duration = durationMillis;
            
            mRepository.addSleepSession(newSleepSession);
            
            // clear the current session in storage
            // todo abstract this in the repo
            mRepository.setCurrentSession(context, null);
        }
    }

    
//*********************************************************
// private methods
//*********************************************************

    private LiveData<Date> getCurrentSleepSession(Context context)
    {
        if (mCurrentSleepSession == null) {
            mCurrentSleepSession = mRepository.getCurrentSession(context);
        }
        return mCurrentSleepSession;
    }
}
