package com.rbraithwaite.sleepapp.ui.sleep_tracker;

import android.content.Context;
import android.util.Log;

import androidx.arch.core.util.Function;
import androidx.hilt.lifecycle.ViewModelInject;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.rbraithwaite.sleepapp.data.SleepAppRepository;
import com.rbraithwaite.sleepapp.data.database.tables.SleepSessionEntity;

import java.util.Date;
import java.util.GregorianCalendar;

import kotlin.annotation.MustBeDocumented;

public class SleepTrackerFragmentViewModel extends ViewModel {
    private SleepAppRepository mRepository;

    private LiveData<Date> mCurrentSleepSession;
    private LiveData<Boolean> mInSleepSession;

    @ViewModelInject
    public SleepTrackerFragmentViewModel(SleepAppRepository repository) {
        mRepository = repository;
    }

//*********************************************************
// API
//*********************************************************

    public LiveData<Boolean> inSleepSession(Context context) {
        // todo
        //  should i add an empty observer to inSleepSession just to guarantee that its active?
        if (mInSleepSession == null) {
            LiveData<Date> currentSleepSessionStartDate = getCurrentSleepSession(context);
            final MediatorLiveData<Boolean> mediator = new MediatorLiveData<Boolean>();
            mediator.addSource(currentSleepSessionStartDate, new Observer<Date>() {
                @Override
                public void onChanged(Date date) {
                    mediator.setValue((date != null));
                }
            });
            mInSleepSession = mediator;
        }
        return mInSleepSession;
    }

    public void startSleepSession(Context context) {
        mRepository.setCurrentSession(context, new GregorianCalendar().getTime());
    }

    public void endSleepSession(Context context) {
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
// private
//*********************************************************

    private LiveData<Date> getCurrentSleepSession(Context context) {
        if (mCurrentSleepSession == null) {
            mCurrentSleepSession = mRepository.getCurrentSession(context);
        }
        return mCurrentSleepSession;
    }
}
