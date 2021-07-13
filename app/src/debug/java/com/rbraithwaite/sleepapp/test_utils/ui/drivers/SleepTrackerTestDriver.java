package com.rbraithwaite.sleepapp.test_utils.ui.drivers;

import androidx.lifecycle.LiveData;
import androidx.test.espresso.ViewInteraction;

import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.core.models.SleepDurationGoal;
import com.rbraithwaite.sleepapp.core.models.SleepSession;
import com.rbraithwaite.sleepapp.core.models.Tag;
import com.rbraithwaite.sleepapp.core.models.WakeTimeGoal;
import com.rbraithwaite.sleepapp.test_utils.TestUtils;
import com.rbraithwaite.sleepapp.test_utils.ui.UITestUtils;
import com.rbraithwaite.sleepapp.test_utils.ui.assertion_utils.AssertOn;
import com.rbraithwaite.sleepapp.test_utils.ui.assertion_utils.AssertionFailed;
import com.rbraithwaite.sleepapp.test_utils.ui.dialog.DialogTestUtils;
import com.rbraithwaite.sleepapp.test_utils.ui.fragment_helpers.FragmentTestHelper;
import com.rbraithwaite.sleepapp.ui.common.views.mood_selector.MoodSelectorViewModel;
import com.rbraithwaite.sleepapp.ui.common.views.tag_selector.TagScrollController;
import com.rbraithwaite.sleepapp.ui.common.views.tag_selector.TagSelectorViewModel;
import com.rbraithwaite.sleepapp.ui.common.views.tag_selector.TagUiData;
import com.rbraithwaite.sleepapp.ui.sleep_tracker.PostSleepDialog;
import com.rbraithwaite.sleepapp.ui.sleep_tracker.PostSleepDialogFormatting;
import com.rbraithwaite.sleepapp.ui.sleep_tracker.SleepTrackerFormatting;
import com.rbraithwaite.sleepapp.ui.sleep_tracker.SleepTrackerFragment;
import com.rbraithwaite.sleepapp.ui.sleep_tracker.SleepTrackerFragmentViewModel;
import com.rbraithwaite.sleepapp.utils.CommonUtils;
import com.rbraithwaite.sleepapp.utils.TimeUtils;
import com.rbraithwaite.sleepapp.utils.interfaces.Action;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

public class SleepTrackerTestDriver
        extends BaseFragmentTestDriver<SleepTrackerFragment, SleepTrackerTestDriver.Assertions>
{
//*********************************************************
// private properties
//*********************************************************

    private MoodSelectorDriver mMoodSelector;
    private TagSelectorDriver mTagSelectorDriver;
    private Boolean mInSession = false;

//*********************************************************
// public helpers
//*********************************************************
    
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
        
        public void postSleepDialogHasMood(int expectedMoodIndex)
        {
            assertOnPostSleepDialog(dialog -> hamcrestAssertThat(dialog.getMoodIndex(), is(equalTo(expectedMoodIndex))));
        }
        
        public void postSleepDialogHasSelectedTags(List<Integer> expectedSelectedTagIds)
        {
            assertOnPostSleepDialog(dialog -> {
                // REFACTOR [21-05-6 1:47AM] -- should this return the tag TextViews instead? should
                //  PostSleepDialog be coupled to TagUiData?
                //  Or I could just access the view model through a getter.
                List<TagUiData> tags = dialog.getTags();
                
                hamcrestAssertThat(tags, is(notNullValue()));
                hamcrestAssertThat(tags.isEmpty(), is(false));
                hamcrestAssertThat(tags.size(), is(equalTo(expectedSelectedTagIds.size())));
                for (int i = 0; i < tags.size(); i++) {
                    hamcrestAssertThat(tags.get(i).tagId, is(equalTo(expectedSelectedTagIds.get(i))));
                }
            });
        }
        
        public void postSleepDialogHasComments(String expectedComments)
        {
            onView(withParent(withId(R.id.postsleep_comments_scroll))).check(matches(withText(
                    expectedComments)));
        }
        
        public void postSleepDialogHasDuration(int expectedDurationMillis)
        {
            onView(withId(R.id.postsleep_duration)).check(matches(withText(
                    PostSleepDialogFormatting.formatDuration(expectedDurationMillis))));
        }
        
        public void postSleepDialogRatingIsUnset()
        {
            assertOnPostSleepDialog(dialog -> {
                hamcrestAssertThat(dialog.getViewModel().getRating(), is(0f));
            });
        }
        
        public void additionalCommentsMatch(String expectedComments)
        {
            onView(withId(R.id.additional_comments)).check(matches(withText(expectedComments)));
        }
        
        public void selectedMoodMatches(int expectedMoodIndex)
        {
            getOwningDriver().mMoodSelector.assertThat.selectedMoodMatches(expectedMoodIndex);
        }
        
        public void selectedTagsMatch(List<Integer> expectedSelectedTagIds)
        {
            getOwningDriver().mTagSelectorDriver.assertThat.selectedTagsMatch(
                    expectedSelectedTagIds);
        }
        
        public void postSleepDialogIsDisplayed()
        {
            assertOnPostSleepDialog(dialog -> {
                hamcrestAssertThat(dialog, is(notNullValue()));
            });
        }
        
        public void postSleepDialogCommentsAreUnset()
        {
            postSleepDialogHasComments(TestUtils.getString(R.string.postsleepdialog_nocomments));
        }
        
        public void postSleepDialogMoodIsUnset()
        {
            onView(withParent(withId(R.id.postsleep_mood_frame))).check(matches(withText(R.string.postsleepdialog_nomood)));
        }
        
        public void postSleepDialogTagsAreUnset()
        {
            assertOnPostSleepDialog(dialog -> {
                TagScrollController tagScrollController = dialog.getTagScrollController();
                hamcrestAssertThat(tagScrollController.isEmpty(), is(true));
            });
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
        
        public void sessionStartTimeMatches(Date expectedStartTime)
        {
            onView(withId(R.id.sleep_tracker_start_time)).check(matches(withText(
                    SleepTrackerFormatting.formatSessionStartTime(expectedStartTime))));
        }
        
        public void detailsMatch(SleepSession sleepSession)
        {
            // TODO [21-06-25 7:55PM] -- this should handle comments being null.
            additionalCommentsMatch(sleepSession.getAdditionalComments());
            // TODO [21-06-25 7:55PM] -- this needs to handle mood being null.
            selectedMoodMatches(sleepSession.getMood().asIndex());
            selectedTagsMatch(sleepSession.getTags().stream().map(Tag::getTagId).collect(Collectors.toList()));
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
        
        private void assertOnPostSleepDialog(AssertOn<PostSleepDialog> assertion)
        {
            getOwningDriver().performOnPostSleepDialog(assertion::assertOn);
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

    public void startSessionManually()
    {
        pressSleepTrackingButton();
    }
    
    public void stopAndDiscardSessionManually()
    {
        stopSessionManually();
        discardSessionManually();
    }
    
    public void discardSessionManually()
    {
        assertThat().postSleepDialogIsDisplayed();
        DialogTestUtils.pressNegativeButton();
        DialogTestUtils.pressPositiveButton();
    }
    
    public void stopSessionManually()
    {
        if (!mInSession) {
            throw new RuntimeException(
                    "Calling this out of order. Can't stop session as it hasn't been started.");
        }
        pressSleepTrackingButton();
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
    
    /**
     * toggle the selections of the provided tags.
     *
     * @param tagIndices The indices of the tags to toggle.
     */
    public void toggleTagSelections(List<Integer> tagIndices)
    {
        mTagSelectorDriver.toggleTagSelections(tagIndices);
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
    
    /**
     * Keep a new sleep session. (Uses simulated time)
     *
     * @param durationMillis The duration of the sleep session to be kept.
     */
    public void keepSleepSession(int durationMillis)
    {
        stopSleepSession(durationMillis);
        DialogTestUtils.pressPositiveButton();
    }
    
    public void stopSleepSession(int durationMillis)
    {
        TimeUtils timeUtils = new TimeUtils();
        Date end = timeUtils.getNow();
        Date start = timeUtils.addDurationToDate(end, durationMillis * -1);
        
        TimeUtils oldTimeUtils = injectTimeUtils(createSessionTimeUtils(start, end));
        
        pressSleepTrackingButton();
        pressSleepTrackingButton();
        
        injectTimeUtils(oldTimeUtils);
    }
    
    public void recordSpecificSession(SleepSession sleepSession)
    {
        setDetailsFrom(sleepSession);
        
        // REFACTOR [21-05-11 9:56PM] -- this duplicates keepSleepSession - I should have an
        //  overload: keepSleepSession(start, end) (although the adding of a rating is different...
        //  overload stopSleepSession instead).
        TimeUtils oldTimeUtils = injectTimeUtils(
                createSessionTimeUtils(sleepSession.getStart(), sleepSession.getEnd()));
        
        pressSleepTrackingButton();
        pressSleepTrackingButton();
        
        // reset the time utils once finished the operation
        injectTimeUtils(oldTimeUtils);
        
        setPostSleepRating(sleepSession.getRating());
        
        DialogTestUtils.pressPositiveButton();
    }
    
    public void setPostSleepRating(float rating)
    {
        performOnPostSleepDialog(dialog -> dialog.getViewModel().setRating(rating));
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
        addTags(sleepSession.getTags().stream().map(Tag::getText).collect(Collectors.toList()));
        // REFACTOR [21-05-11 11:47PM] -- call this ListUtils.asIndices().
        toggleTagSelections(IntStream.range(0, sleepSession.getTags().size()).boxed().collect(
                Collectors.toList()));
    }
    
    public void startInterruptionWithReason(String reason)
    {
        pressInterruptButton();
        UITestUtils.typeOnMultilineEditText(reason, onView(withId(R.id.tracker_interrupt_reason)));
    }
    
    public void stopAndKeepSessionManually()
    {
        stopSessionManually();
        DialogTestUtils.pressPositiveButton();
    }
    
    public void pressInterruptButton()
    {
        onView(withId(R.id.tracker_interrupt_button)).perform(click());
    }
    
//*********************************************************
// private methods
//*********************************************************

    private void resetTimeUtils()
    {
        injectTimeUtils(new TimeUtils());
    }

    private void performOnPostSleepDialog(Action<PostSleepDialog> action)
    {
        getHelper().performSyncedFragmentAction(fragment -> {
            PostSleepDialog dialog =
                    (PostSleepDialog) fragment.getDialogByTag(SleepTrackerFragment.POST_SLEEP_DIALOG);
            
            if (dialog == null) {
                // REFACTOR [21-05-7 11:56PM] -- this could be less generic.
                throw new AssertionFailed("post sleep dialog does not exist");
            }
            
            action.performOn(dialog);
        });
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
    
    private void pressSleepTrackingButton()
    {
        mInSession = !mInSession;
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
