package com.rbraithwaite.sleepapp.ui.sleep_tracker;

import androidx.hilt.lifecycle.ViewModelInject;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.rbraithwaite.sleepapp.core.models.CurrentSession;
import com.rbraithwaite.sleepapp.core.models.Mood;
import com.rbraithwaite.sleepapp.core.repositories.CurrentGoalsRepository;
import com.rbraithwaite.sleepapp.core.repositories.CurrentSessionRepository;
import com.rbraithwaite.sleepapp.core.repositories.SleepSessionRepository;
import com.rbraithwaite.sleepapp.ui.common.convert.ConvertMood;
import com.rbraithwaite.sleepapp.ui.common.data.MoodUiData;
import com.rbraithwaite.sleepapp.ui.common.views.tag_selector.TagUiData;
import com.rbraithwaite.sleepapp.ui.sleep_tracker.data.PostSleepData;
import com.rbraithwaite.sleepapp.ui.sleep_tracker.data.StoppedSessionData;
import com.rbraithwaite.sleepapp.utils.LiveDataFuture;
import com.rbraithwaite.sleepapp.utils.TickingLiveData;
import com.rbraithwaite.sleepapp.utils.TimeUtils;
import com.rbraithwaite.sleepapp.utils.interfaces.ProviderOf;

import java.util.List;
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
    
    /**
     * This is used to hold comment values that the user has entered without needing to persist
     * these values to storage every time they change. These new local values take precedence over
     * older values from storage.
     */
    private ActiveValue<String> mLocalAdditionalComments = new ActiveValue<>();
    private ActiveValue<Mood> mLocalMood = new ActiveValue<>();
    private ActiveValue<List<TagUiData>> mLocalSelectedTags = new ActiveValue<>();
    
    private MutableLiveData<Boolean> mIsCurrentSessionStopped = new MutableLiveData<>(false);
    private MutableLiveData<CurrentSession.Snapshot> mStoppedSessionSnapshot =
            new MutableLiveData<>();
    private PostSleepData mPostSleepData;
    private LiveData<CurrentSession> mCurrentSession;

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
                    CurrentSession::isStarted
            );
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
                    updateCurrentSessionWithLocalValues(currentSession);
                    currentSession.setStart(mTimeUtils.getNow());
                    mCurrentSessionRepository.setCurrentSession(currentSession);
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
                        // TODO [21-06-27 3:13AM] -- change this text to "Error" - this will
                        //  reflect the intent that the timer shouldn't be visible when not in a
                        //  session.
                        // OPTIMIZE [21-06-27 3:13AM] -- I don't need to be returning a new instance
                        //  of this every time.
                        return new MutableLiveData<>(SleepTrackerFormatting.formatDuration(0));
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
    
    // REFACTOR [21-03-31 1:05AM] -- extract everything relating to the local additional comments.
    
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
        return Transformations.map(
                getCurrentSession(),
                CurrentSession::getAdditionalComments);
    }
    
    public void setLocalAdditionalComments(String additionalComments)
    {
        if (additionalComments != null && additionalComments.equals("")) {
            additionalComments = null;
        }
        mLocalAdditionalComments.set(additionalComments);
    }
    
    /**
     * Persist the current state of the UI.
     */
    public void persistLocalValues()
    {
        if (!mIsCurrentSessionStopped.getValue()) {
            LiveDataFuture.getValue(
                    getCurrentSession(),
                    currentSession -> {
                        updateCurrentSessionWithLocalValues(currentSession);
                        mCurrentSessionRepository.setCurrentSession(currentSession);
                    });
        }
    }
    
    public LiveData<MoodUiData> getPersistedMood()
    {
        return Transformations.map(
                getCurrentSession(),
                currentSession -> ConvertMood.toUiData(currentSession.getMood()));
    }
    
    /**
     * This does not update getPersistedMood() until persist() is called.
     */
    public void setLocalMood(MoodUiData mood)
    {
        // It's easier to convert here and store as a Mood, since CurrentSession stores a Mood,
        //  so the eventual getElse() call works better.
        mLocalMood.set(ConvertMood.fromUiData(mood));
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
        mLocalSelectedTags.set(selectedTags);
    }
    
    public LiveData<InitialTagData> getInitialTagData()
    {
        return Transformations.map(
                getCurrentSession(),
                currentSession -> new InitialTagData(currentSession.getSelectedTagIds()));
    }
    
    public LiveData<List<Integer>> getPersistedSelectedTagIds()
    {
        return Transformations.map(
                getCurrentSession(),
                CurrentSession::getSelectedTagIds);
    }
    
    public StoppedSessionData getStoppedSessionData()
    {
        return mIsCurrentSessionStopped.getValue() ?
                new StoppedSessionData(mStoppedSessionSnapshot.getValue(), getPostSleepData()) :
                null;
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

    private void clearLocalValues()
    {
        mLocalMood.clear();
        mLocalAdditionalComments.clear();
        mLocalSelectedTags.clear();
    }
    
    /**
     * Sets mIsCurrentSessionStopped, and takes a current session snapshot or clears the last
     * snapshot, depending on true or false input respectively.
     */
    private void setIsCurrentSessionStopped(boolean isCurrentSessionStopped)
    {
        mIsCurrentSessionStopped.setValue(isCurrentSessionStopped);
        if (isCurrentSessionStopped) {
            mStoppedSessionSnapshot.setValue(createCurrentSessionSnapshot());
            mCurrentSessionRepository.clearCurrentSession();
            clearLocalValues();
        } else {
            mStoppedSessionSnapshot.setValue(null);
        }
    }
    
    private CurrentSession.Snapshot createCurrentSessionSnapshot()
    {
        CurrentSession currentSession = getCurrentSession().getValue();
        if (currentSession == null) {
            currentSession = new CurrentSession();
        }
        
        updateCurrentSessionWithLocalValues(currentSession);
        
        return currentSession.createSnapshot(mTimeUtils);
    }
    
    private void updateCurrentSessionWithLocalValues(CurrentSession currentSession)
    {
        currentSession.setAdditionalComments(
                mLocalAdditionalComments.getElse(currentSession::getAdditionalComments));
        currentSession.setMood(
                mLocalMood.getElse(currentSession::getMood));
        currentSession.setSelectedTagIds(
                chooseSelectedTagIds(mLocalSelectedTags, currentSession));
    }
    
    private List<Integer> chooseSelectedTagIds(
            ActiveValue<List<TagUiData>> localSelectedTags,
            CurrentSession fallback)
    {
        List<TagUiData> tagList = localSelectedTags.getElse(() -> null);
        return tagList == null ? fallback.getSelectedTagIds() : getIdsFromTags(tagList);
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
                postSleepData == null ? 0f : postSleepData.rating);
    }
    
    private List<Integer> getIdsFromTags(List<TagUiData> tags)
    {
        return tags.stream().map(tagUiData -> tagUiData.tagId).collect(Collectors.toList());
    }
    
    private LiveData<CurrentSession> getCurrentSession()
    {
        if (mCurrentSession == null) {
            mCurrentSession = mCurrentSessionRepository.getCurrentSession();
        }
        return mCurrentSession;
    }
    
    // REFACTOR [21-05-2 3:20AM] -- extract this.

//*********************************************************
// private helpers
//*********************************************************

    // SMELL [21-05-6 4:00AM] -- .
    //  This is used to distinguish between local values which are null because they are unset,
    //  and local values which are set to null. Some sort of "null object" pattern would be better.
    private static class ActiveValue<T>
    {
        private T mValue;
        private boolean mIsActive = false;
        
        public void set(T value)
        {
            mValue = value;
            mIsActive = true;
        }
        
        /**
         * If this value is active, return that value, otherwise return the fallback.
         *
         * @param fallback The value to return if this value is not active.
         *
         * @return The active value or the fallback.
         */
        public T getElse(ProviderOf<T> fallback)
        {
            if (mIsActive) {
                return mValue;
            }
            return fallback.provide();
        }
        
        /**
         * Clear this active value, resetting it to its initial state.
         */
        public void clear()
        {
            mValue = null;
            mIsActive = false;
        }
    }
}
