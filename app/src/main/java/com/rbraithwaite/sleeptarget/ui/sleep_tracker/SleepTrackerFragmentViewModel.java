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

import android.view.View;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.rbraithwaite.sleeptarget.R;
import com.rbraithwaite.sleeptarget.core.models.CurrentSession;
import com.rbraithwaite.sleeptarget.core.models.Interruption;
import com.rbraithwaite.sleeptarget.core.models.SleepDurationGoal;
import com.rbraithwaite.sleeptarget.core.models.WakeTimeGoal;
import com.rbraithwaite.sleeptarget.core.repositories.CurrentGoalsRepository;
import com.rbraithwaite.sleeptarget.core.repositories.CurrentSessionRepository;
import com.rbraithwaite.sleeptarget.core.repositories.SleepSessionRepository;
import com.rbraithwaite.sleeptarget.ui.common.convert.ConvertMood;
import com.rbraithwaite.sleeptarget.ui.common.data.MoodUiData;
import com.rbraithwaite.sleeptarget.ui.common.views.tag_selector.TagUiData;
import com.rbraithwaite.sleeptarget.ui.post_sleep.PostSleepViewModel;
import com.rbraithwaite.sleeptarget.ui.sleep_tracker.data.PostSleepData;
import com.rbraithwaite.sleeptarget.ui.sleep_tracker.data.StoppedSessionData;
import com.rbraithwaite.sleeptarget.utils.CommonUtils;
import com.rbraithwaite.sleeptarget.utils.LiveDataEvent;
import com.rbraithwaite.sleeptarget.utils.LiveDataFuture;
import com.rbraithwaite.sleeptarget.utils.LiveDataSingle;
import com.rbraithwaite.sleeptarget.utils.LiveDataUtils;
import com.rbraithwaite.sleeptarget.utils.MergedLiveData;
import com.rbraithwaite.sleeptarget.utils.SimpleLiveDataEvent;
import com.rbraithwaite.sleeptarget.utils.TickingLiveData;
import com.rbraithwaite.sleeptarget.utils.TimeUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
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
    private LiveData<String> mInterruptionsTotalText;
    
    private LiveData<Boolean> mHasAnyGoal;
    private TickingLiveData<CurrentSession> mTickingCurrentSession = new TickingLiveData<>();
    
    private LiveData<Integer> mInterruptionsTotalVisibility;
    
    private MutableLiveData<LiveDataEvent<StoppedSessionData>> mNavToPostSleepEvent =
            new MutableLiveData<>();
    private MutableLiveData<SimpleLiveDataEvent> mInterruptionRecordedEvent =
            new MutableLiveData<>();
    
    /**
     * mCurrentSleepSessionDuration needs special behaviour when it is being observed for the first
     * time.
     */
    private boolean mInitializingCurrentSleepSessionDuration = true;

//*********************************************************
// constructors
//*********************************************************

    @Inject
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
    
    public void handleAnyReturnFromPostSleep(PostSleepViewModel postSleepViewModel)
    {
        Objects.requireNonNull(postSleepViewModel);
        
        int action = postSleepViewModel.consumeAction();
        
        switch (action) {
        case PostSleepViewModel.NO_ACTION:
            break; // do nothing
        case PostSleepViewModel.KEEP:
            keepSleepSession(postSleepViewModel.consumeData());
            break;
        case PostSleepViewModel.DISCARD:
            clearCurrentSleepSession();
            postSleepViewModel.discardData();
            break;
        default:
            throw new RuntimeException("Invalid PostSleepViewModel action: " + action);
        }
    }
    
    public LiveData<LiveDataEvent<StoppedSessionData>> onNavToPostSleep()
    {
        return mNavToPostSleepEvent;
    }
    
    public LiveData<SimpleLiveDataEvent> onInterruptionRecorded()
    {
        return mInterruptionRecordedEvent;
    }
    
    // REFACTOR [21-10-21 9:55PM] -- I need to either rename StoppedSessionData or
    //  getSleepSessionSnapshot.
    public LiveData<StoppedSessionData> getSleepSessionSnapshot()
    {
        return Transformations.map(
                getCurrentSession(),
                // TODO [21-10-22 12:07AM] If the post sleep data is null, what's the
                //  point of providing a StoppedSessionData? shouldn't I just provide the snapshot?
                currentSession -> new StoppedSessionData(currentSession.createSnapshot(mTimeUtils),
                                                         null));
    }
    
    public LiveData<String> getCurrentSleepSessionDurationText()
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
    
    public LiveData<String> getInterruptionsTotalText()
    {
        mInterruptionsTotalText = CommonUtils.lazyInit(
                mInterruptionsTotalText,
                () -> Transformations.switchMap(getCurrentSession(), currentSession -> {
                    if (!currentSession.isStarted()) {
                        // no reason for an update unless the interruptions total is visible
                        return new MutableLiveData<>(null);
                    } else if (!currentSession.isInterrupted()) {
                        return new MutableLiveData<>(mInterruptionsTotalText.getValue());
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
        return mInterruptionsTotalText;
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
    
    public LiveData<String> getStartTimeText()
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
    
    public LiveData<Integer> getWakeTimeGoalVisibility()
    {
        return Transformations.map(
                mCurrentGoalsRepository.getWakeTimeGoal(),
                wakeTimeGoal -> wakeTimeGoal == null || !wakeTimeGoal.isSet() ?
                        View.GONE : View.VISIBLE);
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
    
    public LiveData<Integer> getSleepDurationGoalVisibility()
    {
        return Transformations.map(
                mCurrentGoalsRepository.getSleepDurationGoal(),
                sleepDurationGoal -> sleepDurationGoal == null || !sleepDurationGoal.isSet() ?
                        View.GONE : View.VISIBLE);
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
    
    public void setAdditionalComments(String additionalComments)
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
    public void onPause()
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
    public void setMood(MoodUiData mood)
    {
        // It's easier to convert here and store as a Mood, since CurrentSession stores a Mood,
        //  so the eventual getElse() call works better.
        LiveDataFuture.getValue(getCurrentSession(), currentSession -> {
            // TODO [21-10-19 11:27PM] -- Is this condition really nec.? I kept it around to not
            //  mess with legacy behaviour, but I should investigate removing it & just passing
            //  mood directly to the converter.
            if (mood == null || !mood.isSet()) {
                currentSession.setMood(null);
            } else {
                currentSession.setMood(ConvertMood.fromUiData(mood));
            }
            LiveDataUtils.refresh(getCurrentSession());
        });
    }
    
    public void setSelectedTags(List<TagUiData> selectedTags)
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
        mHasAnyGoal = CommonUtils.lazyInit(mHasAnyGoal, () -> Transformations.map(
                new MergedLiveData(
                        mCurrentGoalsRepository.getWakeTimeGoal(),
                        mCurrentGoalsRepository.getSleepDurationGoal()),
                update -> {
                    // wake time
                    Object value0 = update.values.get(0);
                    if (value0 != MergedLiveData.NO_VALUE) {
                        WakeTimeGoal wakeTimeGoal = (WakeTimeGoal) value0;
                        if (wakeTimeGoal != null && wakeTimeGoal.isSet()) {
                            return true;
                        }
                    }
                    
                    // sleep duration
                    Object value1 = update.values.get(1);
                    if (value1 != MergedLiveData.NO_VALUE) {
                        SleepDurationGoal sleepDurationGoal = (SleepDurationGoal) value1;
                        return sleepDurationGoal != null && sleepDurationGoal.isSet();
                    }
                    
                    return false;
                }));
        
        return mHasAnyGoal;
    }
    
    public LiveData<Integer> getNoGoalsMessageVisibility()
    {
        return Transformations.map(hasAnyGoal(), hasAnyGoal ->
                hasAnyGoal ? View.GONE : View.VISIBLE);
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
    
    public void setInterruptionReason(String interruptionReason)
    {
        LiveDataFuture.getValue(getCurrentSession(), currentSession -> {
            if (currentSession.isInterrupted()) {
                currentSession.setInterruptionReason(interruptionReason);
                LiveDataUtils.refresh(getCurrentSession());
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
    
    public void clickTrackingButton()
    {
        LiveDataFuture.getValue(
                inSleepSession(),
                inSleepSession -> {
                    if (inSleepSession) {
                        triggerPostSleepEvent();
                    } else {
                        startSleepSession();
                    }
                });
    }
    
    /**
     * The visibility of the interruptions control UI (interrupt button, reason, etc)
     */
    public LiveData<Integer> getInterruptionsVisibility()
    {
        return Transformations.map(inSleepSession(),
                                   inSleepSession -> inSleepSession ? View.VISIBLE : View.GONE);
    }
    
    public void clickInterruptionButton()
    {
        LiveDataFuture.getValue(isSleepSessionInterrupted(), isInterrupted -> {
            if (isInterrupted) {
                resumeSleepSession();
                triggerInterruptionRecordedEvent();
            } else {
                interruptSleepSession();
            }
        });
    }
    
    public LiveData<Integer> getSleepTrackingButtonText()
    {
        return Transformations.map(inSleepSession(), inSleepSession -> inSleepSession ?
                R.string.sleep_tracker_button_stop :
                R.string.sleep_tracker_button_start);
    }
    
    /**
     * The visibility of the interruptions total displayed with the session timer.
     */
    public LiveData<Integer> getInterruptionsTotalVisibility()
    {
        mInterruptionsTotalVisibility = CommonUtils.lazyInit(
                mInterruptionsTotalVisibility,
                () -> Transformations.map(getCurrentSession(), currentSession ->
                        currentSession.isStarted() && currentSession.hasAnyInterruptions() ?
                                View.VISIBLE : View.GONE));
        return mInterruptionsTotalVisibility;
    }
    
    public LiveData<Integer> getInterruptButtonText()
    {
        return Transformations.map(isSleepSessionInterrupted(), isInterrupted -> isInterrupted ?
                R.string.tracker_interrupt_btn_resume :
                R.string.tracker_interrupt_btn_interrupt);
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
     * If a new session is started while one is currently ongoing, the ongoing session is discarded.
     * If you want to retain that session instead, call
     * {@link SleepTrackerFragmentViewModel#keepSleepSession(StoppedSessionData)}
     */
    private void startSleepSession()
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
    private void keepSleepSession(StoppedSessionData stoppedSession)
    {
        mSleepSessionRepository.addSleepSession(prepareNewSleepSession(
                stoppedSession.currentSessionSnapshot,
                stoppedSession.postSleepData));
        
        clearCurrentSleepSession();
    }
    
    private void clearCurrentSleepSession()
    {
        if (mCurrentSession != null) {
            mCurrentSessionRepository.clearCurrentSession();
            mCurrentSession.setValue(new CurrentSession());
        }
    }
    
    /**
     * Stop & save the previously ongoing interruption, and resume the session. If the session was
     * not currently interrupted or there was no session, this does nothing.
     * <p>
     * This is an important state change, so the current session is persisted here (to avoid missing
     * that state change on an application crash)
     */
    private void resumeSleepSession()
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
    private void interruptSleepSession()
    {
        LiveDataFuture.getValue(getCurrentSession(), currentSession -> {
            if (currentSession.interrupt(mTimeUtils)) {
                LiveDataUtils.refresh(getCurrentSession());
                mCurrentSessionRepository.setCurrentSession(currentSession);
            }
        });
    }
    
    private void triggerPostSleepEvent()
    {
        LiveDataFuture.getValue(
                getSleepSessionSnapshot(),
                stoppedSession ->
                        mNavToPostSleepEvent.setValue(new LiveDataEvent<>(stoppedSession)));
    }

    private void triggerInterruptionRecordedEvent()
    {
        mInterruptionRecordedEvent.setValue(new SimpleLiveDataEvent());
    }

    // SMELL [21-07-14 5:38PM] -- This doesn't seem like a good way to do this - revisit this.
    private LiveData<CurrentSession> getTickingCurrentSession()
    {
        return Transformations.switchMap(getCurrentSession(), currentSession -> {
            mTickingCurrentSession.setOnTick(() -> currentSession);
            return mTickingCurrentSession;
        });
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
