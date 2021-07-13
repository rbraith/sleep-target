package com.rbraithwaite.sleepapp.ui.sleep_tracker;

import androidx.hilt.lifecycle.ViewModelInject;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.rbraithwaite.sleepapp.core.models.CurrentSession;
import com.rbraithwaite.sleepapp.core.models.Interruption;
import com.rbraithwaite.sleepapp.core.repositories.CurrentGoalsRepository;
import com.rbraithwaite.sleepapp.core.repositories.CurrentSessionRepository;
import com.rbraithwaite.sleepapp.core.repositories.SleepSessionRepository;
import com.rbraithwaite.sleepapp.ui.common.convert.ConvertMood;
import com.rbraithwaite.sleepapp.ui.common.data.MoodUiData;
import com.rbraithwaite.sleepapp.ui.common.views.tag_selector.TagUiData;
import com.rbraithwaite.sleepapp.ui.sleep_tracker.data.PostSleepData;
import com.rbraithwaite.sleepapp.ui.sleep_tracker.data.StoppedSessionData;
import com.rbraithwaite.sleepapp.utils.CommonUtils;
import com.rbraithwaite.sleepapp.utils.LiveDataFuture;
import com.rbraithwaite.sleepapp.utils.LiveDataSingle;
import com.rbraithwaite.sleepapp.utils.LiveDataUtils;
import com.rbraithwaite.sleepapp.utils.TickingLiveData;
import com.rbraithwaite.sleepapp.utils.TimeUtils;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

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
    
    private TimeUtils mTimeUtils;
    
    private MutableLiveData<Boolean> mIsCurrentSessionStopped = new MutableLiveData<>(false);
    private MutableLiveData<CurrentSession.Snapshot> mStoppedSessionSnapshot =
            new MutableLiveData<>();
    private PostSleepData mPostSleepData;
    private MutableLiveData<CurrentSession> mCurrentSession;
    
    private LiveData<Boolean> mHasAnyGoal;
    
    private LiveData<CurrentSession> mRepoCurrentSession;

//*********************************************************
// public helpers
//*********************************************************

    public static class InitialTagData
    {
        public List<Integer> selectedTagIds;
        
        public InitialTagData(List<Integer> selectedTagIds)
        {
            this.selectedTagIds = selectedTagIds;
        }
    }

//*********************************************************
// constructors
//*********************************************************

    @ViewModelInject
    public SleepTrackerFragmentViewModel(
            SleepSessionRepository sleepSessionRepository,
            CurrentSessionRepository currentSessionRepository,
            CurrentGoalsRepository currentGoalsRepository)
    {
        mSleepSessionRepository = sleepSessionRepository;
        mCurrentSessionRepository = currentSessionRepository;
        mCurrentGoalsRepository = currentGoalsRepository;
        mTimeUtils = createTimeUtils();
    }

//*********************************************************
// api
//*********************************************************

    public TimeUtils getTimeUtils()
    {
        return mTimeUtils;
    }
    
    public void setTimeUtils(TimeUtils timeUtils)
    {
        mTimeUtils = timeUtils;
    }
    
    public LiveData<Boolean> inSleepSession()
    {
        if (mInSleepSession == null) {
            mInSleepSession = Transformations.map(
                    getCurrentSession(),
                    CurrentSession::isStarted);
        }
        return mInSleepSession;
    }
    
    /**
     * If a new session is started while one is currently ongoing, the ongoing session is discarded.
     * If you want to retain that session instead, call
     * {@link SleepTrackerFragmentViewModel#keepStoppedSession(StoppedSessionData)}
     */
    public void startSleepSession()
    {
        setIsCurrentSessionStopped(false);
        
        LiveDataFuture.getValue(
                getCurrentSession(),
                currentSession -> {
                    currentSession.setStart(mTimeUtils.getNow());
                    LiveDataUtils.refresh(getCurrentSession());
                });
    }
    
    /**
     * Stops the current session and creates a snapshot of it.
     */
    public void stopSleepSession()
    {
        setIsCurrentSessionStopped(true);
    }
    
    public void keepStoppedSession(StoppedSessionData stoppedSession)
    {
        mSleepSessionRepository.addSleepSession(prepareNewSleepSession(
                stoppedSession.currentSessionSnapshot,
                stoppedSession.postSleepData));
    }
    
    public void discardSleepSession()
    {
        if (mIsCurrentSessionStopped.getValue()) {
            setIsCurrentSessionStopped(false);
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
                getCurrentSession(),
                currentSession -> {
                    if (!currentSession.isStarted()) {
                        // OPTIMIZE [21-06-27 3:13AM] -- I don't need to be returning a new instance
                        //  of this every time.
                        return new MutableLiveData<>("Error");
                    } else if (currentSession.isInterrupted()) {
                        String latestDurationValue = mCurrentSleepSessionDuration.getValue();
                        return new MutableLiveData<>(latestDurationValue);
                    } else {
                        return new TickingLiveData<String>()
                        {
                            @Override
                            public String onTick()
                            {
                                return SleepTrackerFormatting.formatDuration(
                                        currentSession.getOngoingDurationMillis(mTimeUtils));
                            }
                        };
                    }
                });
        
        return mCurrentSleepSessionDuration;
    }
    
    public LiveData<String> getSessionStartTime()
    {
        return Transformations.map(
                getCurrentSession(),
                // REFACTOR [21-07-8 8:57PM] formatSessionStartTime should just return null if
                //  getStart is null.
                currentSession -> currentSession.isStarted() ?
                        SleepTrackerFormatting.formatSessionStartTime(currentSession.getStart()) :
                        null);
    }
    
    public LiveData<String> getWakeTimeGoalText()
    {
        return Transformations.map(
                mCurrentGoalsRepository.getWakeTimeGoal(),
                wakeTimeGoal -> {
                    if (wakeTimeGoal == null || !wakeTimeGoal.isSet()) {
                        return null;
                    }
                    return SleepTrackerFormatting.formatWakeTimeGoal(wakeTimeGoal);
                });
    }
    
    public LiveData<String> getSleepDurationGoalText()
    {
        return Transformations.map(
                mCurrentGoalsRepository.getSleepDurationGoal(),
                sleepDurationGoal -> {
                    if (sleepDurationGoal == null || !sleepDurationGoal.isSet()) {
                        return null;
                    }
                    return SleepTrackerFormatting.formatSleepDurationGoal(sleepDurationGoal);
                });
    }
    
    /**
     * The user-provided additional comments for the current sleep session. (might be null)
     */
    public LiveData<String> getPersistedAdditionalComments()
    {
        return LiveDataSingle.withSource(
                getCurrentSession(),
                CurrentSession::getAdditionalComments);
    }
    
    public void setLocalAdditionalComments(String additionalComments)
    {
        if (additionalComments != null && additionalComments.equals("")) {
            additionalComments = null;
        }
        String finalAdditionalComments = additionalComments;
        LiveDataFuture.getValue(getCurrentSession(), currentSession -> {
            currentSession.setAdditionalComments(finalAdditionalComments);
            LiveDataUtils.refresh(getCurrentSession());
        });
    }
    
    /**
     * Persist the current state of the UI.
     */
    public void persistLocalValues()
    {
        if (!mIsCurrentSessionStopped.getValue()) {
            LiveDataFuture.getValue(
                    getCurrentSession(),
                    mCurrentSessionRepository::setCurrentSession);
        }
    }
    
    public LiveData<MoodUiData> getPersistedMood()
    {
        return LiveDataSingle.withSource(
                getRepoCurrentSession(),
                currentSession -> ConvertMood.toUiData(currentSession.getMood()));
    }
    
    /**
     * This does not update getPersistedMood() until persist() is called.
     */
    public void setLocalMood(MoodUiData mood)
    {
        // It's easier to convert here and store as a Mood, since CurrentSession stores a Mood,
        //  so the eventual getElse() call works better.
        LiveDataFuture.getValue(getCurrentSession(), currentSession -> {
            currentSession.setMood(ConvertMood.fromUiData(mood));
            LiveDataUtils.refresh(getCurrentSession());
        });
    }
    
    /**
     * This does not update getPersistedMood() until persist() is called.
     */
    public void clearLocalMood()
    {
        setLocalMood(null);
    }
    
    public void setLocalSelectedTags(List<TagUiData> selectedTags)
    {
        LiveDataFuture.getValue(getCurrentSession(), currentSession -> {
            currentSession.setSelectedTagIds(getIdsFromTags(selectedTags));
            LiveDataUtils.refresh(getCurrentSession());
        });
    }
    
    public LiveData<List<Integer>> getPersistedSelectedTagIds()
    {
        return LiveDataSingle.withSource(
                getRepoCurrentSession(),
                CurrentSession::getSelectedTagIds);
    }
    
    public StoppedSessionData getStoppedSessionData()
    {
        return mIsCurrentSessionStopped.getValue() ?
                new StoppedSessionData(mStoppedSessionSnapshot.getValue(), getPostSleepData()) :
                null;
    }
    
    public LiveData<Boolean> hasAnyGoal()
    {
        // REFACTOR [21-07-8 9:03PM] -- I need to make a better LiveDataUtils.merge.
        // REFACTOR [21-06-27 9:29PM] -- replace TestUtils.DoubleRef w/ AtomicReference I guess lol.
        AtomicReference<Boolean> hasWakeTimeGoal = new AtomicReference<>(false);
        AtomicReference<Boolean> hasSleepDurationGoal = new AtomicReference<>(false);
        mHasAnyGoal = CommonUtils.lazyInit(mHasAnyGoal, () -> {
            MediatorLiveData<Boolean> mediator = new MediatorLiveData<>();
            mediator.addSource(mCurrentGoalsRepository.getWakeTimeGoal(), wakeTimeGoal -> {
                hasWakeTimeGoal.set(wakeTimeGoal != null && wakeTimeGoal.isSet());
                mediator.setValue(hasWakeTimeGoal.get() || hasSleepDurationGoal.get());
            });
            mediator.addSource(mCurrentGoalsRepository.getSleepDurationGoal(),
                               sleepDurationGoal -> {
                                   hasSleepDurationGoal.set(
                                           sleepDurationGoal != null && sleepDurationGoal.isSet());
                                   mediator.setValue(
                                           hasWakeTimeGoal.get() || hasSleepDurationGoal.get());
                               });
            return mediator;
        });
        
        return mHasAnyGoal;
    }
    
    /**
     * @return Whether or not the ongoing sleep session is interrupted. (This will also return false
     * if there is no ongoing sleep session)
     */
    public LiveData<Boolean> isSleepSessionInterrupted()
    {
        return Transformations.map(
                getCurrentSession(),
                CurrentSession::isInterrupted);
    }
    
    public LiveData<String> getPersistedInterruptionReason()
    {
        return LiveDataSingle.withSource(
                getRepoCurrentSession(),
                currentSession -> Optional
                        .ofNullable(currentSession.createCurrentInterruptionSnapshot(mTimeUtils))
                        .map(Interruption::getReason)
                        .orElse(null));
    }
    
    public void setLocalInterruptionReason(String interruptionReason)
    {
        LiveDataFuture.getValue(getCurrentSession(), currentSession -> {
            if (currentSession.isInterrupted()) {
                currentSession.setInterruptionReason(interruptionReason);
                LiveDataUtils.refresh(getCurrentSession());
            }
        });
    }
    
    /**
     * Stop & save the previously ongoing interruption, and resume the session. If the session was
     * not currently interrupted or there was no session, this does nothing.
     */
    public void resumeSleepSession()
    {
        LiveDataFuture.getValue(getCurrentSession(), currentSession -> {
            if (currentSession.resume(mTimeUtils)) {
                LiveDataUtils.refresh(getCurrentSession());
            }
        });
    }
    
    /**
     * Begin an interruption for an ongoing session. If there is no ongoing session, on that session
     * is already interrupted, this does nothing.
     */
    public void interruptSleepSession()
    {
        LiveDataFuture.getValue(getCurrentSession(), currentSession -> {
            if (currentSession.interrupt(mTimeUtils)) {
                LiveDataUtils.refresh(getCurrentSession());
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

    private PostSleepData getPostSleepData()
    {
        return mPostSleepData;
    }
    
    public void setPostSleepData(PostSleepData postSleepData)
    {
        mPostSleepData = postSleepData;
    }
    
    /**
     * Sets mIsCurrentSessionStopped, and takes a current session snapshot or clears the last
     * snapshot, depending on true or false input respectively.
     */
    private void setIsCurrentSessionStopped(boolean isCurrentSessionStopped)
    {
        mIsCurrentSessionStopped.setValue(isCurrentSessionStopped);
        if (isCurrentSessionStopped) {
            LiveDataFuture.getValue(createCurrentSessionSnapshot(), snapshot -> {
                mStoppedSessionSnapshot.setValue(snapshot);
                clearCurrentSession();
            });
        } else {
            mStoppedSessionSnapshot.setValue(null);
        }
    }
    
    private void clearCurrentSession()
    {
        mCurrentSessionRepository.clearCurrentSession();
        // HACK [21-07-10 3:49PM] -- This is a little bit hacky - this is setting the local current
        //  session to the same value that clearCurrentSession() above is setting to the repo
        //  session.
        //  I'm doing it this way because there was an async problem here when I tried to re-sync
        //  to the repo value (basically the re-sync happened too fast and I got the old value) and
        //  I'm lazy. I could fix that re-sync problem, or a lazy but maybe equally clean solution
        //  might be to have a factory method in CurrentSession shared between here and the repo -
        //  something like CurrentSession.createUnset() or createEmpty()
        getCurrentSession().setValue(new CurrentSession());
    }
    
    private LiveData<CurrentSession.Snapshot> createCurrentSessionSnapshot()
    {
        return Transformations.map(
                getCurrentSession(),
                currentSession -> currentSession.createSnapshot(mTimeUtils));
    }
    
    private SleepSessionRepository.NewSleepSessionData prepareNewSleepSession(
            CurrentSession.Snapshot currentSessionSnapshot,
            PostSleepData postSleepData)
    {
        return new SleepSessionRepository.NewSleepSessionData(
                currentSessionSnapshot.start,
                currentSessionSnapshot.end,
                currentSessionSnapshot.durationMillis,
                currentSessionSnapshot.additionalComments,
                currentSessionSnapshot.mood,
                currentSessionSnapshot.selectedTagIds,
                currentSessionSnapshot.interruptions,
                postSleepData == null ? 0f : postSleepData.rating);
    }
    
    private List<Integer> getIdsFromTags(List<TagUiData> tags)
    {
        return tags.stream().map(tagUiData -> tagUiData.tagId).collect(Collectors.toList());
    }
    
    private synchronized MutableLiveData<CurrentSession> getCurrentSession()
    {
        mCurrentSession = CommonUtils.lazyInit(mCurrentSession,
                                               () -> LiveDataSingle.withSource(getRepoCurrentSession()));
        return mCurrentSession;
    }
    
    private synchronized LiveData<CurrentSession> getRepoCurrentSession()
    {
        mRepoCurrentSession = CommonUtils.lazyInit(mRepoCurrentSession,
                                                   mCurrentSessionRepository::getCurrentSession);
        return mRepoCurrentSession;
    }
}
