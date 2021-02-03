package com.rbraithwaite.sleepapp.ui.sleep_tracker;

import androidx.arch.core.util.Function;
import androidx.hilt.lifecycle.ViewModelInject;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.rbraithwaite.sleepapp.data.current_goals.CurrentGoalsRepository;
import com.rbraithwaite.sleepapp.data.current_goals.SleepDurationGoalModel;
import com.rbraithwaite.sleepapp.data.current_session.CurrentSessionModel;
import com.rbraithwaite.sleepapp.data.current_session.CurrentSessionRepository;
import com.rbraithwaite.sleepapp.data.sleep_session.SleepSessionModel;
import com.rbraithwaite.sleepapp.data.sleep_session.SleepSessionRepository;
import com.rbraithwaite.sleepapp.ui.UIDependenciesModule;
import com.rbraithwaite.sleepapp.ui.format.DateTimeFormatter;
import com.rbraithwaite.sleepapp.ui.format.DurationFormatter;
import com.rbraithwaite.sleepapp.utils.DateUtils;
import com.rbraithwaite.sleepapp.utils.LiveDataFuture;
import com.rbraithwaite.sleepapp.utils.TickingLiveData;

import java.util.Date;

public class SleepTrackerFragmentViewModel
        extends ViewModel
{
//*********************************************************
// private properties
//*********************************************************

    private SleepSessionRepository mSleepSessionRepository;
    private CurrentSessionRepository mCurrentSessionRepository;
    private CurrentGoalsRepository mCurrentGoalsRepository;
    
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
            SleepSessionRepository sleepSessionRepository,
            CurrentSessionRepository currentSessionRepository,
            CurrentGoalsRepository currentGoalsRepository,
            @UIDependenciesModule.SleepTrackerDateTimeFormatter DateTimeFormatter dateTimeFormatter)
    {
        mSleepSessionRepository = sleepSessionRepository;
        mCurrentSessionRepository = currentSessionRepository;
        mCurrentGoalsRepository = currentGoalsRepository;
        mDateTimeFormatter = dateTimeFormatter;
    }

//*********************************************************
// api
//*********************************************************

    public LiveData<Boolean> inSleepSession()
    {
        if (mInSleepSession == null) {
            mInSleepSession = Transformations.map(
                    mCurrentSessionRepository.getCurrentSession(),
                    new Function<CurrentSessionModel, Boolean>()
                    {
                        @Override
                        public Boolean apply(CurrentSessionModel input)
                        {
                            return (input.isSet());
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
        mCurrentSessionRepository.setCurrentSession(DateUtils.getNow());
    }
    
    public void endSleepSession()
    {
        LiveDataFuture.getValue(
                mCurrentSessionRepository.getCurrentSession(),
                null,
                new LiveDataFuture.OnValueListener<CurrentSessionModel>()
                {
                    @Override
                    public void onValue(CurrentSessionModel currentSession)
                    {
                        if (currentSession.isSet()) {
                            addCurrentSessionThenClear(currentSession);
                        }
                    }
                }
        );
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
                mCurrentSessionRepository.getCurrentSession(),
                new Function<CurrentSessionModel, androidx.lifecycle.LiveData<String>>()
                {
                    @Override
                    public androidx.lifecycle.LiveData<String> apply(final CurrentSessionModel currentSession)
                    {
                        // REFACTOR [21-01-11 10:31PM] -- this should be injected.
                        final DurationFormatter durationFormatter = new DurationFormatter();
                        
                        if (!currentSession.isSet()) {
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
                                                    currentSession.getStart(),
                                                    DateUtils.getNow()
                                            ));
                                }
                            };
                        }
                    }
                });
        
        return mCurrentSleepSessionDuration;
    }
    
    public LiveData<String> getSessionStartTime()
    {
        return Transformations.map(
                mCurrentSessionRepository.getCurrentSession(),
                new Function<CurrentSessionModel, String>()
                {
                    @Override
                    public String apply(CurrentSessionModel input)
                    {
                        return input.isSet() ?
                                // REFACTOR [21-02-3 3:18PM] -- move this logic to
                                //  SleepTrackerFormatting.
                                mDateTimeFormatter.formatFullDate(input.getStart()) :
                                null;
                    }
                });
    }
    
    public LiveData<String> getWakeTimeGoalText()
    {
        return Transformations.map(
                mCurrentGoalsRepository.getWakeTimeGoal(),
                new Function<Long, String>()
                {
                    @Override
                    public String apply(Long wakeTimeGoal)
                    {
                        return wakeTimeGoal == null ?
                                null :
                                // REFACTOR [21-02-3 3:12PM] -- move this logic to
                                //  SleepTrackerFormatting.
                                mDateTimeFormatter.formatTimeOfDay(DateUtils.getDateFromMillis(
                                        wakeTimeGoal));
                    }
                });
    }
    
    public LiveData<String> getSleepDurationGoalText()
    {
        return Transformations.map(
                mCurrentGoalsRepository.getSleepDurationGoal(),
                new Function<SleepDurationGoalModel, String>()
                {
                    @Override
                    public String apply(SleepDurationGoalModel input)
                    {
                        return input.isSet() ?
                                SleepTrackerFormatting.formatSleepDurationGoal(input) : null;
                    }
                });
    }

//*********************************************************
// private methods
//*********************************************************

    private void addCurrentSessionThenClear(final CurrentSessionModel currentSession)
    {
        LiveDataFuture.getValue(
                // REFACTOR [21-01-5 2:03AM] -- getWakeTimeGoal should return a Date.
                mCurrentGoalsRepository.getWakeTimeGoal(),
                null,
                new LiveDataFuture.OnValueListener<Long>()
                {
                    @Override
                    public void onValue(Long wakeTimeGoalMillis)
                    {
                        // REFACTOR [21-02-3 3:17PM] -- this should be CurrentSessionModel
                        //  .getDuration.
                        long durationMillis = calculateDurationMillis(currentSession.getStart(),
                                                                      DateUtils.getNow());
                        Date wakeTimeGoal = (wakeTimeGoalMillis == null) ?
                                null :
                                DateUtils.getDateFromMillis(wakeTimeGoalMillis);
                        
                        mSleepSessionRepository.addSleepSession(new SleepSessionModel(
                                currentSession.getStart(),
                                durationMillis,
                                wakeTimeGoal));
                        
                        mCurrentSessionRepository.clearCurrentSession();
                    }
                });
    }
    
    
    private long calculateDurationMillis(Date start, Date end)
    {
        return end.getTime() - start.getTime();
    }
}
