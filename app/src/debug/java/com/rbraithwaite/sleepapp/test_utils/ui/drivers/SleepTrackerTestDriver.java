package com.rbraithwaite.sleepapp.test_utils.ui.drivers;

import androidx.test.espresso.ViewInteraction;

import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.test_utils.TestUtils;
import com.rbraithwaite.sleepapp.test_utils.ui.HiltFragmentTestHelper;
import com.rbraithwaite.sleepapp.test_utils.ui.UITestUtils;
import com.rbraithwaite.sleepapp.test_utils.ui.assertion_utils.AssertOn;
import com.rbraithwaite.sleepapp.test_utils.ui.assertion_utils.AssertionFailed;
import com.rbraithwaite.sleepapp.test_utils.ui.dialog.DialogTestUtils;
import com.rbraithwaite.sleepapp.ui.common.mood_selector.MoodSelectorViewModel;
import com.rbraithwaite.sleepapp.ui.common.tag_selector.TagScrollController;
import com.rbraithwaite.sleepapp.ui.common.tag_selector.TagSelectorViewModel;
import com.rbraithwaite.sleepapp.ui.common.tag_selector.TagUiData;
import com.rbraithwaite.sleepapp.ui.sleep_tracker.PostSleepDialog;
import com.rbraithwaite.sleepapp.ui.sleep_tracker.PostSleepDialogFormatting;
import com.rbraithwaite.sleepapp.ui.sleep_tracker.SleepTrackerFragment;
import com.rbraithwaite.sleepapp.ui.sleep_tracker.SleepTrackerFragmentViewModel;
import com.rbraithwaite.sleepapp.utils.TimeUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class SleepTrackerTestDriver
{
//*********************************************************
// private properties
//*********************************************************

    private HiltFragmentTestHelper<SleepTrackerFragment> mHelper;
    private MoodSelectorDriver mMoodSelector;
    private TagSelectorDriver mTagSelectorDriver;
    private Boolean mInSession = false;

//*********************************************************
// public constants
//*********************************************************

    public final Assertions assertThat;
    
//*********************************************************
// public helpers
//*********************************************************

    public static class Assertions
    {
        private SleepTrackerTestDriver mOwningSleepTracker;
        
        public enum TrackerButtonState
        {
            NOT_STARTED,
            STARTED
        }
        
        private Assertions(SleepTrackerTestDriver owningSleepTracker)
        {
            mOwningSleepTracker = owningSleepTracker;
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
        
        public void detailsAreCleared()
        {
            moodIsUnSet();
            thereAreNoSelectedTags();
            commentsAreUnset();
        }
        
        public void moodIsUnSet()
        {
            mOwningSleepTracker.mMoodSelector.assertThat.moodIsUnset();
        }
        
        public void thereAreNoSelectedTags()
        {
            mOwningSleepTracker.mTagSelectorDriver.assertThat.thereAreNoSelectedTags();
        }
        
        public void commentsAreUnset()
        {
            onView(withId(R.id.additional_comments)).check(matches(withText("")));
        }
        
        public void postSleepDialogHasMood(int expectedMoodIndex)
        {
            assertOnPostSleepDialog(dialog -> {
                assertThat(dialog.getMoodIndex(), is(equalTo(expectedMoodIndex)));
            });
        }
        
        public void postSleepDialogHasSelectedTags(List<Integer> expectedSelectedTagIds)
        {
            assertOnPostSleepDialog(dialog -> {
                // REFACTOR [21-05-6 1:47AM] -- should this return the tag TextViews instead? should
                //  PostSleepDialog be coupled to TagUiData?
                //  Or I could just access the view model through a getter.
                List<TagUiData> tags = dialog.getTags();
                
                assertThat(tags, is(notNullValue()));
                assertThat(tags.isEmpty(), is(false));
                assertThat(tags.size(), is(equalTo(expectedSelectedTagIds.size())));
                for (int i = 0; i < tags.size(); i++) {
                    assertThat(tags.get(i).tagId, is(equalTo(expectedSelectedTagIds.get(i))));
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
                assertThat(dialog.getViewModel().getRating(), is(0f));
            });
        }
        
        public void additionalCommentsMatch(String expectedComments)
        {
            onView(withId(R.id.additional_comments)).check(matches(withText(expectedComments)));
        }
        
        public void selectedMoodMatches(int expectedMoodIndex)
        {
            mOwningSleepTracker.mMoodSelector.assertThat.selectedMoodMatches(expectedMoodIndex);
        }
        
        public void selectedTagsMatch(List<Integer> expectedSelectedTagIds)
        {
            mOwningSleepTracker.mTagSelectorDriver.assertThat.selectedTagsMatch(
                    expectedSelectedTagIds);
        }
        
        public void postSleepDialogIsDisplayed()
        {
            assertOnPostSleepDialog(dialog -> {
                assertThat(dialog, is(notNullValue()));
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
                assertThat(tagScrollController.isEmpty(), is(true));
            });
        }
        
        private void assertOnPostSleepDialog(AssertOn<PostSleepDialog> assertion)
        {
            mOwningSleepTracker.mHelper.performSyncedFragmentAction(fragment -> {
                PostSleepDialog dialog =
                        (PostSleepDialog) fragment.getDialogByTag(SleepTrackerFragment.POST_SLEEP_DIALOG);
                
                if (dialog == null) {
                    // REFACTOR [21-05-7 11:56PM] -- this could be less generic.
                    throw new AssertionFailed("post sleep dialog does not exist");
                }
                
                assertion.assertOn(dialog);
            });
        }
    }
    
//*********************************************************
// constructors
//*********************************************************

    public SleepTrackerTestDriver()
    {
        mHelper = HiltFragmentTestHelper.launchFragment(SleepTrackerFragment.class);
        mMoodSelector = new MoodSelectorDriver(
                withId(R.id.more_context_mood),
                getMoodSelectorViewModel());
        mTagSelectorDriver = new TagSelectorDriver(
                withId(R.id.more_context_tags),
                getTagSelectorViewModel());
        assertThat = new Assertions(this);
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
        assertThat.postSleepDialogIsDisplayed();
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
        // set the exact time by faking the TimeUtils
        // use the actual time for the end time, and set the start time <durationMillis> before
        // the end time
        TimeUtils fakeTimeUtils = new TimeUtils()
        {
            private int timesCalled = 0;
            private Date fakeStart;
            private Date fakeEnd;
            
            @Override
            public Date getNow()
            {
                timesCalled++;
                maybeSetupDates();
                
                if (timesCalled == 1) {
                    return fakeStart;
                } else if (timesCalled == 2) {
                    return fakeEnd;
                } else {
                    throw new RuntimeException("keepSleepSession TimeUtils called too much.");
                }
            }
            
            private void maybeSetupDates()
            {
                if (fakeStart != null) {
                    return;
                }
                
                fakeEnd = super.getNow();
                
                GregorianCalendar cal = new GregorianCalendar();
                cal.setTime(fakeEnd);
                cal.add(Calendar.MILLISECOND, durationMillis * -1);
                
                fakeStart = cal.getTime();
            }
        };
        
        TimeUtils oldTimeUtils = injectTimeUtils(fakeTimeUtils);
        
        pressSleepTrackingButton();
        pressSleepTrackingButton();
        
        injectTimeUtils(oldTimeUtils);
    }
    
    public void restartFragment()
    {
        mHelper.restartFragment();
    }
    
//*********************************************************
// private methods
//*********************************************************

    private TagSelectorViewModel getTagSelectorViewModel()
    {
        TestUtils.DoubleRef<TagSelectorViewModel> viewModel = new TestUtils.DoubleRef<>(null);
        mHelper.performSyncedFragmentAction(fragment -> {
            viewModel.ref = fragment.getTagSelectorViewModel();
        });
        return viewModel.ref;
    }
    
    private MoodSelectorViewModel getMoodSelectorViewModel()
    {
        TestUtils.DoubleRef<MoodSelectorViewModel> viewModel = new TestUtils.DoubleRef<>(null);
        mHelper.performSyncedFragmentAction(fragment -> {
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
        mHelper.performSyncedFragmentAction(fragment -> {
            SleepTrackerFragmentViewModel viewModel = fragment.getViewModel();
            oldTimeUtils.ref = viewModel.getTimeUtils();
            viewModel.setTimeUtils(newTimeUtils);
        });
        return oldTimeUtils.ref;
    }
}
