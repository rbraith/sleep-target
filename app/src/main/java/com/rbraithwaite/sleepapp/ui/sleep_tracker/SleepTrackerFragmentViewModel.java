package com.rbraithwaite.sleepapp.ui.sleep_tracker;

import androidx.arch.core.util.Function;
import androidx.hilt.lifecycle.ViewModelInject;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.rbraithwaite.sleepapp.core.models.CurrentSession;
import com.rbraithwaite.sleepapp.core.models.SleepDurationGoal;
import com.rbraithwaite.sleepapp.core.models.WakeTimeGoal;
import com.rbraithwaite.sleepapp.core.repositories.CurrentGoalsRepository;
import com.rbraithwaite.sleepapp.core.repositories.CurrentSessionRepository;
import com.rbraithwaite.sleepapp.core.repositories.SleepSessionRepository;
import com.rbraithwaite.sleepapp.di.UIDependenciesModule;
import com.rbraithwaite.sleepapp.ui.format.DateTimeFormatter;
import com.rbraithwaite.sleepapp.ui.format.DurationFormatter;
import com.rbraithwaite.sleepapp.utils.LiveDataFuture;
import com.rbraithwaite.sleepapp.utils.TickingLiveData;
import com.rbraithwaite.sleepapp.utils.TimeUtils;

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
    
    private TimeUtils mTimeUtils;
    
    private LiveData<CurrentSession> mCurrentSession;

    /**
     * This is used to hold comment values that the user has entered without needing to persist
     * these values to storage every time they change. These new local values take precedence over
     * older values from storage.
     */
    private String mLocalAdditionalComments;
    
    private boolean mAreLocalAdditionalCommentsValid = false;
    
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
            // REFACTOR [21-03-24 11:56PM] -- This should be SleepTrackerFormatting.
            @UIDependenciesModule.SleepTrackerDateTimeFormatter DateTimeFormatter dateTimeFormatter)
    {
        mSleepSessionRepository = sleepSessionRepository;
        mCurrentSessionRepository = currentSessionRepository;
        mCurrentGoalsRepository = currentGoalsRepository;
        mDateTimeFormatter = dateTimeFormatter;
        mTimeUtils = createTimeUtils();
    }
    
//*********************************************************
// api
//*********************************************************

    public LiveData<Boolean> inSleepSession()
    {
        if (mInSleepSession == null) {
            mInSleepSession = Transformations.map(
                    getCurrentSession(),
                    new Function<CurrentSession, Boolean>()
                    {
                        @Override
                        public Boolean apply(CurrentSession input)
                        {
                            return (input.isStarted());
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
        LiveDataFuture.getValue(
                getCurrentSession(),
                null,
                new LiveDataFuture.OnValueListener<CurrentSession>()
                {
                    @Override
                    public void onValue(CurrentSession currentSession)
                    {
                        mCurrentSessionRepository.setCurrentSession(new CurrentSession(
                                mTimeUtils.getNow(),
                                chooseAdditionalComments(currentSession)));
                    }
                });
    }
    
    public void endSleepSession()
    {
        LiveDataFuture.getValue(
                getCurrentSession(),
                null,
                new LiveDataFuture.OnValueListener<CurrentSession>()
                {
                    @Override
                    public void onValue(CurrentSession currentSession)
                    {
                        if (currentSession.isStarted()) {
                            if (mAreLocalAdditionalCommentsValid) {
                                currentSession.setAdditionalComments(consumeLocalAdditionalComments());
                            }
                            mSleepSessionRepository.addSleepSession(currentSession.toSleepSession());
                            mCurrentSessionRepository.clearCurrentSession();
                        }
                    }
                });
    }
    
    public void setTimeUtils(TimeUtils timeUtils)
    {
        mTimeUtils = timeUtils;
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
                getCurrentSession(),
                new Function<CurrentSession, androidx.lifecycle.LiveData<String>>()
                {
                    @Override
                    public androidx.lifecycle.LiveData<String> apply(final CurrentSession currentSession)
                    {
                        // REFACTOR [21-03-24 11:57PM] -- This should be SleepTrackerFormatting.
                        final DurationFormatter durationFormatter = new DurationFormatter();
                        
                        if (!currentSession.isStarted()) {
                            return new MutableLiveData<>(durationFormatter.formatDurationMillis(
                                    0));
                        } else {
                            return new TickingLiveData<String>()
                            {
                                @Override
                                public String onTick()
                                {
                                    return durationFormatter.formatDurationMillis(
                                            currentSession.getOngoingDurationMillis());
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
                getCurrentSession(),
                new Function<CurrentSession, String>()
                {
                    @Override
                    public String apply(CurrentSession input)
                    {
                        return input.isStarted() ?
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
                new Function<WakeTimeGoal, String>()
                {
                    @Override
                    public String apply(WakeTimeGoal wakeTimeGoal)
                    {
                        if (wakeTimeGoal == null || !wakeTimeGoal.isSet()) {
                            return null;
                        }
                        return SleepTrackerFormatting.formatWakeTimeGoal(wakeTimeGoal);
                    }
                });
    }
    
    // REFACTOR [21-03-31 1:05AM] -- extract everything relating to the local additional comments.
    
    public LiveData<String> getSleepDurationGoalText()
    {
        return Transformations.map(
                mCurrentGoalsRepository.getSleepDurationGoal(),
                new Function<SleepDurationGoal, String>()
                {
                    @Override
                    public String apply(SleepDurationGoal input)
                    {
                        if (input == null || !input.isSet()) {
                            return null;
                        }
                        return SleepTrackerFormatting.formatSleepDurationGoal(input);
                    }
                });
    }
    
    /**
     * The user-provided additional comments for the current sleep session. (might be null)
     */
    public LiveData<String> getAdditionalComments()
    {
        return Transformations.map(
                getCurrentSession(),
                new Function<CurrentSession, String>()
                {
                    @Override
                    public String apply(CurrentSession input)
                    {
                        return input.getAdditionalComments();
                    }
                });
    }
    
    public void setAdditionalComments(String additionalComments)
    {
        if (additionalComments != null && additionalComments.equals("")) {
            additionalComments = null;
        }
        mLocalAdditionalComments = additionalComments;
        mAreLocalAdditionalCommentsValid = true;
    }
    
    /**
     * Persist the current state of the UI.
     */
    public void persist()
    {
        LiveDataFuture.getValue(
                getCurrentSession(),
                null,
                new LiveDataFuture.OnValueListener<CurrentSession>()
                {
                    @Override
                    public void onValue(CurrentSession currentSession)
                    {
                        mCurrentSessionRepository.setCurrentSession(new CurrentSession(
                                currentSession.getStart(),
                                chooseAdditionalComments(currentSession)));
                    }
                });
    }
    
//*********************************************************
// protected api
//*********************************************************

    protected TimeUtils createTimeUtils()
    {
        return new TimeUtils();
    }

//*********************************************************
// private methods
//*********************************************************

    
    /**
     * Prioritizes the local additional comments if they have been set.
     */
    private String chooseAdditionalComments(CurrentSession currentSession)
    {
        if (mAreLocalAdditionalCommentsValid) {
            return consumeLocalAdditionalComments();
        }
        return currentSession.getAdditionalComments();
    }

    private String consumeLocalAdditionalComments()
    {
        mAreLocalAdditionalCommentsValid = false;
        return mLocalAdditionalComments;
    }

    private LiveData<CurrentSession> getCurrentSession()
    {
        if (mCurrentSession == null) {
            mCurrentSession = mCurrentSessionRepository.getCurrentSession();
        }
        return mCurrentSession;
    }
}
