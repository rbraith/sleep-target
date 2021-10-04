/*
 * Copyright (c) 2020-2021 Rich Braithwaite (rbraithwaite.dev@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.rbraithwaite.sleeptarget.ui.sleep_tracker;

import androidx.hilt.lifecycle.ViewModelInject;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.rbraithwaite.sleeptarget.core.models.CurrentSession;
import com.rbraithwaite.sleeptarget.core.models.Interruption;
import com.rbraithwaite.sleeptarget.core.repositories.CurrentGoalsRepository;
import com.rbraithwaite.sleeptarget.core.repositories.CurrentSessionRepository;
import com.rbraithwaite.sleeptarget.core.repositories.SleepSessionRepository;
import com.rbraithwaite.sleeptarget.ui.common.convert.ConvertMood;
import com.rbraithwaite.sleeptarget.ui.common.data.MoodUiData;
import com.rbraithwaite.sleeptarget.ui.common.views.tag_selector.TagUiData;
import com.rbraithwaite.sleeptarget.ui.sleep_tracker.data.PostSleepData;
import com.rbraithwaite.sleeptarget.ui.sleep_tracker.data.StoppedSessionData;
import com.rbraithwaite.sleeptarget.utils.CommonUtils;
import com.rbraithwaite.sleeptarget.utils.LiveDataFuture;
import com.rbraithwaite.sleeptarget.utils.LiveDataSingle;
import com.rbraithwaite.sleeptarget.utils.LiveDataUtils;
import com.rbraithwaite.sleeptarget.utils.TickingLiveData;
import com.rbraithwaite.sleeptarget.utils.TimeUtils;

import java.util.List;
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
    
    private TimeUtils mTimeUtils;
    
    private MutableLiveData<CurrentSession> mCurrentSession;
    private LiveData<Boolean> mInSleepSession;
    private LiveData<String> mCurrentSleepSessionDuration;
    private LiveData<String> mOngoingInterruptionDuration;
    private LiveData<String> mInterruptionsTotal;
    
    private PostSleepData mPostSleepData;
    private LiveData<Boolean> mHasAnyGoal;
    private TickingLiveData<CurrentSession> mTickingCurrentSession = new TickingLiveData<>();
    
    /**
     * mCurrentSleepSessionDuration needs special behaviour when it is being observed for the
     * first time.
     */
    private boolean mInitializingCurrentSleepSessionDuration = true;

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
     * {@link SleepTrackerFragmentViewModel#keepSleepSession(StoppedSessionData)}
     */
    public void startSleepSession()
    {
        LiveDataFuture.getValue(
                getCurrentSession(),
                currentSession -> {
                    currentSession.setStart(mTimeUtils.getNow());
                    LiveDataUtils.refresh(getCurrentSession());
                    // TODO [21-07-17 8:23PM] -- I should probably persist here.
                });
    }
    
    /**
     * This also clears the existing session.
     */
    public void keepSleepSession(StoppedSessionData stoppedSession)
    {
        mSleepSessionRepository.addSleepSession(prepareNewSleepSession(
                stoppedSession.currentSessionSnapshot,
                stoppedSession.postSleepData));
        
        clearSleepSession();
    }
    
    public void clearSleepSession()
    {
        if (mCurrentSession != null) {
            mCurrentSessionRepository.clearCurrentSession();
            mCurrentSession.setValue(new CurrentSession());
        }
    }
    
    public LiveData<StoppedSessionData> getSleepSessionSnapshot()
    {
        return Transformations.map(
                getCurrentSession(),
                currentSession -> new StoppedSessionData(
                        currentSession.createSnapshot(mTimeUtils),
                        getPostSleepData()));
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
                        // If this is the first query, use the current session for the value,
                        // otherwise use the already computed value from
                        // mCurrentSleepSessionDuration.
                        String latestDurationValue = "";
                        if (mInitializingCurrentSleepSessionDuration) {
                            mInitializingCurrentSleepSessionDuration = false;
                            latestDurationValue = SleepTrackerFormatting.formatDuration(
                                    currentSession.getDurationMinusInterruptions(mTimeUtils));
                        } else {
                            latestDurationValue = mCurrentSleepSessionDuration.getValue();
                        }
                        return new MutableLiveData<>(latestDurationValue);
                    } else {
                        return Transformations.map(getTickingCurrentSession(),
                                                   tickingCurrentSession -> {
                                                       return SleepTrackerFormatting.formatDuration(
                                                               currentSession.getDurationMinusInterruptions(
                                                                       mTimeUtils));
                                                   });
                    }
                });
        
        return mCurrentSleepSessionDuration;
    }
    
    public LiveData<String> getInterruptionsTotal()
    {
        mInterruptionsTotal = CommonUtils.lazyInit(
                mInterruptionsTotal,
                () -> Transformations.switchMap(getCurrentSession(), currentSession -> {
                    if (!currentSession.isStarted()) {
                        // no reason for an update unless the interruptions total is visible
                        return new MutableLiveData<>(null);
                    } else if (!currentSession.isInterrupted()) {
                        return new MutableLiveData<>(mInterruptionsTotal.getValue());
                    } else {
                        return Transformations.map(
                                getTickingCurrentSession(),
                                tickingCurrentSession -> {
                                    long duration =
                                            tickingCurrentSession.getInterruptionsTotalDuration(
                                                    mTimeUtils);
                                    int count = tickingCurrentSession.getInterruptions().size() +
                                                (tickingCurrentSession.isInterrupted() ? 1 : 0);
                                    
                                    return SleepTrackerFormatting.formatInterruptionsTotal(
                                            duration, count);
                                });
                    }
                }));
        return mInterruptionsTotal;
    }
    
    public LiveData<String> getOngoingInterruptionDuration()
    {
        mOngoingInterruptionDuration = CommonUtils.lazyInit(
                mOngoingInterruptionDuration,
                () -> Transformations.switchMap(getCurrentSession(), currentSession -> {
                    if (!currentSession.isInterrupted()) {
                        return new MutableLiveData<>("Error");
                    } else {
                        return Transformations.map(getTickingCurrentSession(),
                                                   tickingCurrentSession -> {
                                                       return SleepTrackerFormatting.formatDuration(
                                                               tickingCurrentSession.getOngoingInterruptionDurationMillis(
                                                                       mTimeUtils));
                                                   });
                    }
                }));
        return mOngoingInterruptionDuration;
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
     * A one-off LiveData of the user-provided additional comments for the current sleep session.
     * (might be null)
     */
    public LiveData<String> getInitialAdditionalComments()
    {
        return LiveDataSingle.withSource(
                getCurrentSession(),
                currentSession -> {
                    return currentSession.getAdditionalComments();
                });
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
        LiveDataFuture.getValue(
                getCurrentSession(),
                mCurrentSessionRepository::setCurrentSession);
    }
    
    public LiveData<MoodUiData> getInitialMood()
    {
        return LiveDataSingle.withSource(
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
    
    /**
     * @return A one-off LiveData of the initial tag ids.
     */
    public LiveData<List<Integer>> getInitialTagIds()
    {
        return LiveDataSingle.withSource(
                getCurrentSession(),
                CurrentSession::getSelectedTagIds);
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
    
    public LiveData<String> getInitialInterruptionReason()
    {
        return LiveDataSingle.withSource(
                getCurrentSession(),
                CurrentSession::getLatestInterruptionReason);
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
     * <p>
     * This is an important state change, so the current session is persisted here (to avoid missing
     * that state change on an application crash)
     */
    public void resumeSleepSession()
    {
        LiveDataFuture.getValue(getCurrentSession(), currentSession -> {
            if (currentSession.resume(mTimeUtils)) {
                LiveDataUtils.refresh(getCurrentSession());
                mCurrentSessionRepository.setCurrentSession(currentSession);
            }
        });
    }
    
    /**
     * Begin an interruption for an ongoing session. If there is no ongoing session, on that session
     * is already interrupted, this does nothing.
     * <p>
     * This is an important state change, so the current session is persisted here (to avoid missing
     * that state change on an application crash)
     */
    public void interruptSleepSession()
    {
        LiveDataFuture.getValue(getCurrentSession(), currentSession -> {
            if (currentSession.interrupt(mTimeUtils)) {
                LiveDataUtils.refresh(getCurrentSession());
                mCurrentSessionRepository.setCurrentSession(currentSession);
            }
        });
    }
    
    // TEST NEEDED [21-07-18 12:05AM] -- .
    public LiveData<String> getLastInterruptionDuration()
    {
        return Transformations.map(getCurrentSession(), currentSession -> {
            Interruption interruption = currentSession.getLastRecordedInterruption();
            return interruption == null ? null :
                    SleepTrackerFormatting.formatDuration(interruption.getDurationMillis());
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

    // SMELL [21-07-14 5:38PM] -- This doesn't seem like a good way to do this - revisit this.
    private LiveData<CurrentSession> getTickingCurrentSession()
    {
        return Transformations.switchMap(getCurrentSession(), currentSession -> {
            mTickingCurrentSession.setOnTick(() -> currentSession);
            return mTickingCurrentSession;
        });
    }
    
    private PostSleepData getPostSleepData()
    {
        return mPostSleepData;
    }
    
    public void setPostSleepData(PostSleepData postSleepData)
    {
        mPostSleepData = postSleepData;
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
    
    /**
     * On the first call, this is initialized to the value from the repo.
     */
    private synchronized MutableLiveData<CurrentSession> getCurrentSession()
    {
        // value is initialized from the repo, then this acts as a local cache updating in
        // parallel with the repo.
        mCurrentSession = CommonUtils.lazyInit(
                mCurrentSession,
                () -> {
                    return LiveDataSingle.withSource(mCurrentSessionRepository.getCurrentSession());
                });
        return mCurrentSession;
    }
}
