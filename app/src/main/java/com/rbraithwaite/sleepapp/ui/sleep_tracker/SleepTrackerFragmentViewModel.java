package com.rbraithwaite.sleepapp.ui.sleep_tracker;

import androidx.arch.core.util.Function;
import androidx.hilt.lifecycle.ViewModelInject;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.rbraithwaite.sleepapp.data.SleepAppRepository;
import com.rbraithwaite.sleepapp.data.database.tables.sleep_session.SleepSessionEntity;
import com.rbraithwaite.sleepapp.ui.UIDependenciesModule;
import com.rbraithwaite.sleepapp.ui.format.DateTimeFormatter;
import com.rbraithwaite.sleepapp.ui.format.DurationFormatter;
import com.rbraithwaite.sleepapp.utils.DateUtils;
import com.rbraithwaite.sleepapp.utils.LiveDataFuture;
import com.rbraithwaite.sleepapp.utils.TickingLiveData;

import java.util.Date;

//import java.util.logging.Handler;

public class SleepTrackerFragmentViewModel
        extends ViewModel
{
//*********************************************************
// private properties
//*********************************************************

    private SleepAppRepository mRepository;
    
    private LiveData<Date> mCurrentSleepSession;
    private LiveData<Boolean> mInSleepSession;
    private LiveData<String> mCurrentSleepSessionDuration;
    
    private DateTimeFormatter mDateTimeFormatter;

//*********************************************************
// private constants
//*********************************************************

    private static final String TAG = "SleepTrackerFragmentVie";

//*********************************************************
// constructors
//*********************************************************

    @ViewModelInject
    public SleepTrackerFragmentViewModel(
            SleepAppRepository repository,
            @UIDependenciesModule.SleepTrackerDateTimeFormatter DateTimeFormatter dateTimeFormatter)
    {
        mRepository = repository;
        mDateTimeFormatter = dateTimeFormatter;
    }
    
//*********************************************************
// api
//*********************************************************

    public LiveData<Boolean> inSleepSession()
    {
        if (mInSleepSession == null) {
            mInSleepSession = Transformations.map(
                    getCurrentSleepSession(),
                    new Function<Date, Boolean>()
                    {
                        @Override
                        public Boolean apply(Date input)
                        {
                            return (input != null);
                        }
                    }
            );
        }
        return mInSleepSession;
    }
    
    /**
     * If a new session is started while one is currently ongoing, the ongoing session is discarded.
     * If you want to retain that session instead, call
     * {@link SleepTrackerFragmentViewModel#endSleepSession()}
     */
    public void startSleepSession()
    {
        mRepository.setCurrentSession(DateUtils.getNow());
    }
    
    public void endSleepSession()
    {
        // REFACTOR [21-01-5 12:05AM] -- get rid of the getValue calls in this method - think of
        //  some
        //  more reactive solution (nested LiveDataFutures don't seem ideal though)
        if (inSleepSession().getValue()) {
            LiveDataFuture.getValue(
                    // REFACTOR [21-01-5 2:03AM] -- getWakeTimeGoal should return a Date.
                    mRepository.getWakeTimeGoal(),
                    null,
                    new LiveDataFuture.OnValueListener<Long>()
                    {
                        @Override
                        public void onValue(Long wakeTimeGoalMillis)
                        {
                            Date currentSessionStart = getCurrentSleepSession().getValue();
                            long durationMillis = calculateDurationMillis(currentSessionStart,
                                                                          DateUtils.getNow());
                            
                            Date wakeTimeGoal = (wakeTimeGoalMillis == null) ? null :
                                    DateUtils.getDateFromMillis(wakeTimeGoalMillis);
                            SleepSessionEntity newSleepSession = SleepSessionEntity.create(
                                    currentSessionStart,
                                    durationMillis,
                                    wakeTimeGoal);
                            
                            mRepository.addSleepSession(newSleepSession);
                            
                            mRepository.clearCurrentSession();
                        }
                    });
        }
    }
    
    public LiveData<String> getCurrentSleepSessionDuration()
    {
        if (mCurrentSleepSessionDuration != null) {
            return mCurrentSleepSessionDuration;
        }
        
        // This works with the TickingLiveData because MediatorLiveData "correctly propagates its
        // active/inactive states down to source LiveData objects."
        // https://developer.android.com/reference/androidx/lifecycle/MediatorLiveData
        mCurrentSleepSessionDuration = Transformations.switchMap(
                getCurrentSleepSession(),
                new Function<Date, androidx.lifecycle.LiveData<String>>()
                {
                    @Override
                    public androidx.lifecycle.LiveData<String> apply(Date input)
                    {
                        final Date currentSleepSessionStart = input;
                        // REFACTOR [21-01-11 10:31PM] -- this should be injected.
                        final DurationFormatter durationFormatter = new DurationFormatter();
                        
                        if (currentSleepSessionStart == null) {
                            return new MutableLiveData<>(durationFormatter.formatDurationMillis(
                                    0));
                        } else {
                            return new TickingLiveData<String>()
                            {
                                @Override
                                public String onTick()
                                {
                                    return durationFormatter.formatDurationMillis(
                                            calculateDurationMillis(
                                                    currentSleepSessionStart,
                                                    DateUtils.getNow()
                                            ));
                                }
                            };
                        }
                    }
                }
        );
        
        return mCurrentSleepSessionDuration;
    }
    
    public String getSessionStartTime()
    {
        Date startTime = getCurrentSleepSession().getValue();
        if (startTime == null) {
            return null;
        }
        return mDateTimeFormatter.formatFullDate(startTime);
    }
    
    public LiveData<String> getWakeTime()
    {
        return Transformations.map(
                mRepository.getWakeTimeGoal(),
                new Function<Long, String>()
                {
                    @Override
                    public String apply(Long wakeTimeGoal)
                    {
                        if (wakeTimeGoal == null) {
                            return null;
                        } else {
                            return mDateTimeFormatter.formatTimeOfDay(DateUtils.getDateFromMillis(
                                    wakeTimeGoal));
                        }
                    }
                });
    }


//*********************************************************
// private methods
//*********************************************************

    private long calculateDurationMillis(Date start, Date end)
    {
        return end.getTime() - start.getTime();
    }
    
    private LiveData<Date> getCurrentSleepSession()
    {
        if (mCurrentSleepSession == null) {
            mCurrentSleepSession = mRepository.getCurrentSession();
        }
        return mCurrentSleepSession;
    }
}
