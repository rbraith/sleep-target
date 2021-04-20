package com.rbraithwaite.sleepapp.ui.sleep_tracker;

import androidx.arch.core.util.Function;
import androidx.hilt.lifecycle.ViewModelInject;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.rbraithwaite.sleepapp.core.models.CurrentSession;
import com.rbraithwaite.sleepapp.core.models.Mood;
import com.rbraithwaite.sleepapp.core.models.SleepDurationGoal;
import com.rbraithwaite.sleepapp.core.models.WakeTimeGoal;
import com.rbraithwaite.sleepapp.core.repositories.CurrentGoalsRepository;
import com.rbraithwaite.sleepapp.core.repositories.CurrentSessionRepository;
import com.rbraithwaite.sleepapp.core.repositories.SleepSessionRepository;
import com.rbraithwaite.sleepapp.di.UIDependenciesModule;
import com.rbraithwaite.sleepapp.ui.common.mood_selector.ConvertMood;
import com.rbraithwaite.sleepapp.ui.common.mood_selector.MoodUiData;
import com.rbraithwaite.sleepapp.ui.common.tag_selector.TagUiData;
import com.rbraithwaite.sleepapp.ui.format.DateTimeFormatter;
import com.rbraithwaite.sleepapp.ui.format.DurationFormatter;
import com.rbraithwaite.sleepapp.utils.LiveDataFuture;
import com.rbraithwaite.sleepapp.utils.TickingLiveData;
import com.rbraithwaite.sleepapp.utils.TimeUtils;

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
    
    private DateTimeFormatter mDateTimeFormatter;
    
    private TimeUtils mTimeUtils;
    
    private LiveData<CurrentSession> mCurrentSession;
    
    /**
     * This is used to hold comment values that the user has entered without needing to persist
     * these values to storage every time they change. These new local values take precedence over
     * older values from storage.
     */
    private LocalValue<String> mLocalAdditionalComments = new LocalValue<>();
    
    private LocalValue<Mood> mLocalMood = new LocalValue<>();
    
    private LocalValue<List<TagUiData>> mLocalSelectedTags = new LocalValue<>();

//*********************************************************
// private constants
//*********************************************************

    private static final String TAG = "SleepTrackerFragmentVie";

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
                    CurrentSession::isStarted
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
                currentSession -> mCurrentSessionRepository.setCurrentSession(new CurrentSession(
                        mTimeUtils.getNow(),
                        mLocalAdditionalComments.consumeIfValid(currentSession.getAdditionalComments()),
                        mLocalMood.consumeIfValid(currentSession.getMood()),
                        consumeLocalSelectedTagIds(currentSession.getSelectedTagIds()))));
    }
    
    public void endSleepSession()
    {
        LiveDataFuture.getValue(
                getCurrentSession(),
                null,
                currentSession -> {
                    if (currentSession.isStarted()) {
                        currentSession.setAdditionalComments(
                                mLocalAdditionalComments.consumeIfValid(currentSession.getAdditionalComments()));
                        currentSession.setMood(
                                mLocalMood.consumeIfValid(currentSession.getMood()));
                        // HACK [21-04-19 10:23PM] -- I could have converted the local TagUiData
                        //  selected tags to Tag models and set them in the SleepSession, but
                        //  that felt really wasteful, since I really only need the tag ids
                        //  anyway for a new session. This solution also feels bad though - I
                        //  need something much better here.
                        mSleepSessionRepository.addSleepSessionWithTags(
                                currentSession.toSleepSession(),
                                consumeLocalSelectedTagIds(currentSession.getSelectedTagIds()));
                        mCurrentSessionRepository.clearCurrentSession();
                    }
                });
    }
    
    public void setTimeUtils(TimeUtils timeUtils)
    {
        mTimeUtils = timeUtils;
    }
    
    // REFACTOR [21-03-31 1:05AM] -- extract everything relating to the local additional comments.
    
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
                });
        
        return mCurrentSleepSessionDuration;
    }
    
    public LiveData<String> getSessionStartTime()
    {
        return Transformations.map(
                getCurrentSession(),
                currentSession -> currentSession.isStarted() ?
                        // REFACTOR [21-02-3 3:18PM] -- move this logic to
                        //  SleepTrackerFormatting.
                        mDateTimeFormatter.formatFullDate(currentSession.getStart()) :
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
    public void persistCurrentSession()
    {
        LiveDataFuture.getValue(
                getCurrentSession(),
                null,
                currentSession -> mCurrentSessionRepository.setCurrentSession(new CurrentSession(
                        currentSession.getStart(),
                        mLocalAdditionalComments.consumeIfValid(currentSession.getAdditionalComments()),
                        mLocalMood.consumeIfValid(currentSession.getMood()),
                        consumeLocalSelectedTagIds(currentSession.getSelectedTagIds()))));
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

    private List<Integer> getIdsFromTags(List<TagUiData> tags)
    {
        return tags.stream().map(tagUiData -> tagUiData.tagId).collect(Collectors.toList());
    }

    private List<Integer> consumeLocalSelectedTagIds(List<Integer> invalidDefault)
    {
        List<TagUiData> localSelectedTags = mLocalSelectedTags.consumeIfValid(null);
        return localSelectedTags == null ? invalidDefault : getIdsFromTags(localSelectedTags);
    }

    private LiveData<CurrentSession> getCurrentSession()
    {
        if (mCurrentSession == null) {
            mCurrentSession = mCurrentSessionRepository.getCurrentSession();
        }
        return mCurrentSession;
    }

//*********************************************************
// private helpers
//*********************************************************

    // REFACTOR [21-04-3 3:09AM] -- consider extracting this.
    private static class LocalValue<T>
    {
        private T mValue;
        private boolean mIsValid = false;
        
        public void set(T value)
        {
            mValue = value;
            mIsValid = true;
        }
        
        public T consumeIfValid(T invalidDefault)
        {
            if (mIsValid) {
                mIsValid = false;
                return mValue;
            }
            return invalidDefault;
        }
    }
}
