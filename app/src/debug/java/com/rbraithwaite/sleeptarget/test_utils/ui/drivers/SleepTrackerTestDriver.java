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
package com.rbraithwaite.sleeptarget.test_utils.ui.drivers;

import androidx.lifecycle.LiveData;
import androidx.test.espresso.ViewInteraction;

import com.rbraithwaite.sleeptarget.R;
import com.rbraithwaite.sleeptarget.core.models.SleepDurationGoal;
import com.rbraithwaite.sleeptarget.core.models.SleepSession;
import com.rbraithwaite.sleeptarget.core.models.Tag;
import com.rbraithwaite.sleeptarget.core.models.WakeTimeGoal;
import com.rbraithwaite.sleeptarget.test_utils.TestUtils;
import com.rbraithwaite.sleeptarget.test_utils.test_data.builders.SleepSessionBuilder;
import com.rbraithwaite.sleeptarget.test_utils.ui.UITestUtils;
import com.rbraithwaite.sleeptarget.test_utils.ui.fragment_helpers.FragmentTestHelper;
import com.rbraithwaite.sleeptarget.ui.common.views.mood_selector.MoodSelectorViewModel;
import com.rbraithwaite.sleeptarget.ui.common.views.tag_selector.TagSelectorViewModel;
import com.rbraithwaite.sleeptarget.ui.sleep_tracker.SleepTrackerFormatting;
import com.rbraithwaite.sleeptarget.ui.sleep_tracker.SleepTrackerFragment;
import com.rbraithwaite.sleeptarget.ui.sleep_tracker.SleepTrackerFragmentViewModel;
import com.rbraithwaite.sleeptarget.utils.CommonUtils;
import com.rbraithwaite.sleeptarget.utils.TimeUtils;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

public class SleepTrackerTestDriver
        extends BaseFragmentTestDriver<SleepTrackerFragment, SleepTrackerTestDriver.Assertions>
{
//*********************************************************
// private properties
//*********************************************************

    private MoodSelectorDriver mMoodSelector;
    private TagSelectorDriver mTagSelectorDriver;
    private Boolean mInSession = false;
    
    
    private OnNavToPostSleepListener mOnNavToPostSleepListener;

//*********************************************************
// public helpers
//*********************************************************
    
    public interface OnNavToPostSleepListener
    {
        void onNavToPostSleep();
    }
    
    public static class Assertions extends BaseFragmentTestDriver.BaseAssertions<SleepTrackerTestDriver, SleepTrackerFragmentViewModel>
    {
        public enum TrackerButtonState
        {
            NOT_STARTED,
            STARTED
        }
        
        public enum InterruptButtonState
        {
            RESUMED,
            INTERRUPTED
        }
        
        public Assertions(SleepTrackerTestDriver owningDriver)
        {
            super(owningDriver);
        }
        
        public void sleepTrackerButtonIsInState(TrackerButtonState state)
        {
            ViewInteraction sleepTrackerButton = onView(withId(R.id.sleep_tracker_button));
            
            switch (state) {
            case NOT_STARTED:
                sleepTrackerButton.check(matches(withText(R.string.sleep_tracker_button_start)));
                break;
            case STARTED:
                sleepTrackerButton.check(matches(withText(R.string.sleep_tracker_button_stop)));
                break;
            }
        }
        
        public void interruptionButtonIsInState(InterruptButtonState state)
        {
            ViewInteraction interruptButton = onView(withId(R.id.tracker_interrupt_button));
            
            switch (state) {
            case RESUMED:
                interruptButton.check(matches(withText(R.string.tracker_interrupt_btn_interrupt)));
                break;
            case INTERRUPTED:
                interruptButton.check(matches(withText(R.string.tracker_interrupt_btn_resume)));
                break;
            }
        }
        
        public void detailsAreCleared()
        {
            moodIsUnSet();
            thereAreNoSelectedTags();
            commentsAreUnset();
        }
        
        public void moodIsUnSet()
        {
            getOwningDriver().mMoodSelector.assertThat.moodIsUnset();
        }
        
        public void thereAreNoSelectedTags()
        {
            getOwningDriver().mTagSelectorDriver.assertThat.thereAreNoSelectedTags();
        }
        
        public void commentsAreUnset()
        {
            onView(withId(R.id.additional_comments)).check(matches(withText("")));
        }
        
        public void additionalCommentsMatch(String expectedComments)
        {
            onView(withId(R.id.additional_comments)).check(matches(withText(expectedComments)));
        }
        
        public void selectedMoodMatches(int expectedMoodIndex)
        {
            getOwningDriver().mMoodSelector.assertThat.selectedMoodMatches(expectedMoodIndex);
        }
        
        public void selectedTagsMatchText(List<String> tagTexts)
        {
            tagSelector().selectedTagsMatchText(tagTexts);
        }
        
        /**
         * Assert that the tracker is not currently tracking, and that no details have been set.
         */
        public void screenIsClear()
        {
            thereIsNoCurrentSession();
            detailsAreCleared();
        }
        
        public void noGoalsAreDisplayed()
        {
            sleepDurationGoalIsNotDisplayed();
            wakeTimeGoalIsNotDisplayed();
            onView(withId(R.id.tracker_no_goals_card)).check(matches(isDisplayed()));
        }
        
        public void sleepDurationGoalIsNotDisplayed()
        {
            onView(withId(R.id.tracker_duration_goal_card)).check(matches(not(isDisplayed())));
        }
        
        public void wakeTimeGoalIsNotDisplayed()
        {
            onView(withId(R.id.tracker_waketime_goal_card)).check(matches(not(isDisplayed())));
        }
        
        public void onlyWakeTimeGoalIsDisplayed(WakeTimeGoal expectedWakeTimeGoal)
        {
            sleepDurationGoalIsNotDisplayed();
            wakeTimeGoalIsDisplayed(expectedWakeTimeGoal);
        }
        
        public void wakeTimeGoalIsDisplayed(WakeTimeGoal expectedWakeTimeGoal)
        {
            onView(withId(R.id.tracker_no_goals_card)).check(matches(not(isDisplayed())));
            onView(allOf(isDescendantOfA(withId(R.id.tracker_waketime_goal_card)),
                         withId(R.id.tracker_goal_value)))
                    .check(matches(allOf(
                            isDisplayed(),
                            withText(SleepTrackerFormatting.formatWakeTimeGoal(
                                    expectedWakeTimeGoal)))));
        }
        
        public void sessionStartTimeIsNotDisplayed()
        {
            onView(withId(R.id.sleep_tracker_start_time)).check(matches(not(isDisplayed())));
            onView(withId(R.id.sleep_tracker_started_text)).check(matches(not(isDisplayed())));
        }
        
        public void sessionTimerMatches(int expectedDuration)
        {
            onView(withId(R.id.sleep_tracker_session_time)).check(matches(withText(
                    SleepTrackerFormatting.formatDuration(expectedDuration))));
        }
        
        public void sessionTimerIsDisplayed()
        {
            onView(withId(R.id.sleep_tracker_session_time)).check(matches(isDisplayed()));
        }
        
        public void sessionStartTimeMatches(Date expectedStartTime)
        {
            onView(withId(R.id.sleep_tracker_start_time)).check(matches(withText(
                    SleepTrackerFormatting.formatSessionStartTime(expectedStartTime))));
        }
        
        public void sessionStartTimeIsDisplayed()
        {
            onView(withId(R.id.sleep_tracker_start_time)).check(matches(isDisplayed()));
        }
        
        public void detailsMatch(SleepSessionBuilder sleepSession)
        {
            detailsMatch(sleepSession.build());
        }
        
        public void detailsMatch(SleepSession sleepSession)
        {
            // TODO [21-06-25 7:55PM] -- this should handle comments being null.
            additionalCommentsMatch(sleepSession.getAdditionalComments());
            // TODO [21-06-25 7:55PM] -- this needs to handle mood being null.
            selectedMoodMatches(sleepSession.getMood().asIndex());
            selectedTagsMatchText(sleepSession.getTags().stream().map(Tag::getText).collect(Collectors.toList()));
        }
        
        public void sessionTimerIsNotDisplayed()
        {
            onView(withId(R.id.sleep_tracker_start_time)).check(matches(not(isDisplayed())));
        }
        
        public void interruptionsCardIsNotDisplayed()
        {
            onView(withId(R.id.tracker_interruptions_card)).check(matches(not(isDisplayed())));
        }
        
        public void interruptionsCardIsDisplayed()
        {
            onView(withId(R.id.tracker_interruptions_card)).check(matches(isDisplayed()));
        }
        
        public void interruptionTimerIsNotDisplayed()
        {
            onView(withId(R.id.tracker_interrupt_duration)).check(matches(not(isDisplayed())));
        }
        
        public void interruptionTimerMatches(int durationMillis)
        {
            onView(withId(R.id.tracker_interrupt_duration)).check(matches(isDisplayed()));
            onView(withId(R.id.tracker_interrupt_duration)).check(matches(withText(
                    SleepTrackerFormatting.formatDuration(durationMillis))));
        }
        
        public void interruptionsTotalIsNotDisplayed()
        {
            onView(withId(R.id.sleep_tracker_interruptions_total)).check(matches(not(isDisplayed())));
        }
        
        public void interruptionsTotalMatches(int totalDurationMillis, int count)
        {
            onView(withId(R.id.sleep_tracker_interruptions_total)).check(matches(isDisplayed()));
            onView(withId(R.id.sleep_tracker_interruptions_total)).check(matches(withText(
                    SleepTrackerFormatting.formatInterruptionsTotal(totalDurationMillis, count))));
        }
        
        public void interruptionReasonTextMatches(String expectedReason)
        {
            onView(withId(R.id.tracker_interrupt_reason)).check(matches(withText(expectedReason)));
        }
        
        public void interruptionReasonTextIsEmpty()
        {
            interruptionReasonTextMatches("");
        }
        
        public TagSelectorDriver.Assertions tagSelector()
        {
            return getOwningDriver().mTagSelectorDriver.assertThat;
        }
        
        public void isRecordingSession()
        {
            sleepTrackerButtonIsInState(TrackerButtonState.STARTED);
            sessionTimerIsDisplayed();
            sessionStartTimeIsDisplayed();
        }
        
        private void sleepDurationGoalIsDisplayed(SleepDurationGoal expectedSleepDurationGoal)
        {
            onView(withId(R.id.tracker_no_goals_card)).check(matches(not(isDisplayed())));
            onView(allOf(isDescendantOfA(withId(R.id.tracker_duration_goal_card)),
                         withId(R.id.tracker_goal_value)))
                    .check(matches(allOf(
                            isDisplayed(),
                            withText(SleepTrackerFormatting.formatSleepDurationGoal(
                                    expectedSleepDurationGoal)))));
        }
        
        public void bothGoalsAreDisplayed(
                WakeTimeGoal expectedWakeTimeGoal,
                SleepDurationGoal expectedSleepDurationGoal)
        {
            wakeTimeGoalIsDisplayed(expectedWakeTimeGoal);
            sleepDurationGoalIsDisplayed(expectedSleepDurationGoal);
        }
        
        
        private void thereIsNoCurrentSession()
        {
            // check UI
            // REFACTOR [21-07-11 3:21AM] -- hardcoded string.
            onView(withId(R.id.sleep_tracker_session_time)).check(matches(withText("Error")));
            
            // check ViewModel
            LiveData<Boolean> inSleepSession = getViewModel().inSleepSession();
            TestUtils.activateInstrumentationLiveData(inSleepSession);
            hamcrestAssertThat(inSleepSession.getValue(), is(false));
        }
    }

//*********************************************************
// constructors
//*********************************************************

    public SleepTrackerTestDriver(FragmentTestHelper<SleepTrackerFragment> helper)
    {
        init(helper, new Assertions(this));
        
        mMoodSelector = new MoodSelectorDriver(
                withId(R.id.more_context_mood),
                getMoodSelectorViewModel());
        mTagSelectorDriver = new TagSelectorDriver(
                getHelper(),
                withId(R.id.more_context_tags),
                getTagSelectorViewModel());
    }

//*********************************************************
// api
//*********************************************************

    public void setOnNavToPostSleepListener(OnNavToPostSleepListener onNavToPostSleepListener)
    {
        mOnNavToPostSleepListener = onNavToPostSleepListener;
    }
    
    public void startSessionManually()
    {
        pressSleepTrackingButton();
    }
    
    public void stopSessionManually()
    {
        if (!mInSession) {
            throw new RuntimeException(
                    "Calling this out of order. Can't stop session as it hasn't been started.");
        }
        pressSleepTrackingButton();
        onNavToPostSleep();
    }
    
    public void addNewMood(int moodIndex)
    {
        mMoodSelector.addNewMood(moodIndex);
    }
    
    /**
     * Add new tags to the tag selector.
     *
     * @param tagTexts The text values for each new tag.
     *
     * @return A list of the ids of the newly added tags.
     */
    public List<Integer> addTags(List<String> tagTexts)
    {
        return mTagSelectorDriver.addTags(tagTexts);
    }
    
    public void toggleTagSelectionsById(List<Integer> tagIds)
    {
        mTagSelectorDriver.toggleTagSelectionsById(tagIds);
    }
    
    /**
     * Set the additional comments for the current session.
     *
     * @param commentsText The value to set to the comments.
     */
    public void setAdditionalComments(String commentsText)
    {
        UITestUtils.typeOnMultilineEditText(commentsText, onView(withId(R.id.additional_comments)));
    }
    
    public void recordSpecificSession(SleepSession sleepSession)
    {
        setDetailsFrom(sleepSession);
        
        // REFACTOR [21-05-11 9:56PM] -- this duplicates keepSleepSession - I should have an
        //  overload: keepSleepSession(start, end) (although the adding of a rating is different...
        //  overload stopSleepSession instead).
        injectTimeUtils(createSessionTimeUtils(sleepSession.getStart(), sleepSession.getEnd()));
        
        pressSleepTrackingButton();
        pressSleepTrackingButton();
        
        onNavToPostSleep();
    }
    
    /**
     * Starts a session paused at a specific start time and duration. The start time is the real now
     * minus currentDuration.
     *
     * @param currentDurationMillis The duration to pause at.
     *
     * @return The start time.
     */
    public Date startPausedSession(int currentDurationMillis)
    {
        TimeUtils timeUtils = new TimeUtils();
        Date start = timeUtils.addDurationToDate(timeUtils.getNow(), currentDurationMillis * -1);
        
        // TODO [21-06-24 4:21AM] -- record the oldTimeUtils for un-pausing?
        injectTimeUtils(createPausedTimeUtils(start, currentDurationMillis));
        
        startSessionManually();
        
        return start;
    }
    
    /**
     * Unpause a session started with {@link #startPausedSession(int)}
     */
    public void unpause()
    {
        resetTimeUtils();
    }
    
    public void setDetailsFrom(SleepSession sleepSession)
    {
        onView(withId(R.id.tracker_details_card)).perform(scrollTo());
        setAdditionalComments(sleepSession.getAdditionalComments());
        addNewMood(sleepSession.getMood().asIndex());
        toggleTagSelectionsById(
                addTags(sleepSession.getTags().stream()
                                .map(Tag::getText)
                                .collect(Collectors.toList())));
    }
    
    public void startInterruptionWithReason(String reason)
    {
        pressInterruptButton();
        onInterruptionReasonText().perform(scrollTo());
        UITestUtils.typeOnMultilineEditText(reason, onInterruptionReasonText());
    }
    
    public void pressInterruptButton()
    {
        onView(withId(R.id.tracker_interrupt_button)).perform(click());
    }
    
    public void resumeSession()
    {
        pressInterruptButton();
    }
    
    public void setDetailsFrom(SleepSessionBuilder builder)
    {
        setDetailsFrom(builder.build());
    }
    
    public void recordArbitrarySession()
    {
        pressSleepTrackingButton();
        pressSleepTrackingButton();
        onNavToPostSleep();
    }
    
    public void openTagSelectorDialog()
    {
        mTagSelectorDriver.openDialog();
    }
    
    public void scrollToDetails()
    {
        onView(withId(R.id.tracker_details_card)).perform(scrollTo());
    }
    
    public void openMoodSelectorDialog()
    {
        mMoodSelector.openMoodDialog();
    }
    
    public void selectMoodInOpenDialog(int moodIndex)
    {
        mMoodSelector.selectMoodInDialog(moodIndex);
    }
    
    public void confirmMoodDialog()
    {
        mMoodSelector.confirmDialog();
    }

//*********************************************************
// private methods
//*********************************************************

    private void onNavToPostSleep()
    {
        if (mOnNavToPostSleepListener != null) {
            mOnNavToPostSleepListener.onNavToPostSleep();
        }
    }
    
    private void resetTimeUtils()
    {
        injectTimeUtils(new TimeUtils());
    }
    
    /**
     * This overrides getNow() to first return startTime, then a Date that is startTime +
     * currentDuration on every subsequent call, effectively pausing the session timer.
     */
    private TimeUtils createPausedTimeUtils(Date startTime, int currentDuration)
    {
        return new TimeUtils()
        {
            private boolean mFirstCall = true;
            private Date mPausedDate;
            
            @Override
            public Date getNow()
            {
                if (mFirstCall) {
                    mFirstCall = false;
                    return startTime;
                }
                return getPausedDate();
            }
            
            public Date getPausedDate()
            {
                mPausedDate = CommonUtils.lazyInit(mPausedDate,
                                                   () -> new TimeUtils().addDurationToDate(startTime,
                                                                                           currentDuration));
                return mPausedDate;
            }
        };
    }
    
    // REFACTOR [21-06-25 8:41PM] -- I ended up changing this TimeUtil's behaviour and now it
    //  behaves very similarly to createPausedTimeUtils()
    private TimeUtils createSessionTimeUtils(Date start, Date end)
    {
        return new TimeUtils()
        {
            private int timesCalled = 0;
            
            @Override
            public Date getNow()
            {
                timesCalled++;
                
                if (timesCalled == 1) {
                    return start == null ? super.getNow() : start;
                } else {
                    return end == null ? super.getNow() : end;
                }
            }
        };
    }
    
    private TagSelectorViewModel getTagSelectorViewModel()
    {
        TestUtils.DoubleRef<TagSelectorViewModel> viewModel = new TestUtils.DoubleRef<>(null);
        getHelper().performSyncedFragmentAction(fragment -> {
            viewModel.ref = fragment.getTagSelectorViewModel();
        });
        return viewModel.ref;
    }
    
    private MoodSelectorViewModel getMoodSelectorViewModel()
    {
        TestUtils.DoubleRef<MoodSelectorViewModel> viewModel = new TestUtils.DoubleRef<>(null);
        getHelper().performSyncedFragmentAction(fragment -> {
            viewModel.ref = fragment.getMoodSelectorViewModel();
        });
        return viewModel.ref;
    }
    
    private ViewInteraction onInterruptionReasonText()
    {
        return onView(withId(R.id.tracker_interrupt_reason));
    }
    
    private void pressSleepTrackingButton()
    {
        mInSession = !mInSession;
        onView(withId(R.id.sleep_tracker_button)).perform(scrollTo());
        onView(withId(R.id.sleep_tracker_button)).perform(click());
    }
    
    /**
     * Injects a new TimeUtils into the view model.
     *
     * @param newTimeUtils The new TimeUtils
     *
     * @return the old TimeUtils
     */
    private TimeUtils injectTimeUtils(TimeUtils newTimeUtils)
    {
        TestUtils.DoubleRef<TimeUtils> oldTimeUtils = new TestUtils.DoubleRef<>(null);
        getHelper().performSyncedFragmentAction(fragment -> {
            SleepTrackerFragmentViewModel viewModel = fragment.getViewModel();
            oldTimeUtils.ref = viewModel.getTimeUtils();
            viewModel.setTimeUtils(newTimeUtils);
        });
        return oldTimeUtils.ref;
    }
}
