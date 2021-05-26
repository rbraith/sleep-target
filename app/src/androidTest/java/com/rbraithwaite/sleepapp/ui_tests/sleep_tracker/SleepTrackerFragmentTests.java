package com.rbraithwaite.sleepapp.ui_tests.sleep_tracker;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.core.models.SleepDurationGoal;
import com.rbraithwaite.sleepapp.test_utils.data.database.DatabaseTestDriver;
import com.rbraithwaite.sleepapp.test_utils.ui.UITestNavigate;
import com.rbraithwaite.sleepapp.test_utils.ui.UITestUtils;
import com.rbraithwaite.sleepapp.test_utils.ui.drivers.SleepTrackerTestDriver;
import com.rbraithwaite.sleepapp.test_utils.ui.fragment_helpers.HiltFragmentTestHelper;
import com.rbraithwaite.sleepapp.ui.MainActivity;
import com.rbraithwaite.sleepapp.ui.format.DurationFormatter;
import com.rbraithwaite.sleepapp.ui.sleep_tracker.SleepTrackerFormatting;
import com.rbraithwaite.sleepapp.ui.sleep_tracker.SleepTrackerFragment;
import com.rbraithwaite.sleepapp.ui_tests.sleep_goals_fragment.SleepGoalsFragmentTestUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;

// REFACTOR [21-05-1 5:00PM] -- Putting this here, but this is a general refactoring:
//  - I should hide Espresso details behind more descriptive interfaces
//  - as a general rule I should not allow any Espresso dependencies in any test classes
//  ---
//  an idea:
//  - instead of SleepTrackerFragmentTestUtils have something like SleepTrackerTestHelper, which
//      will handle launching the sleep tracker screen, performing inputs, and checking values.
@RunWith(AndroidJUnit4.class)
public class SleepTrackerFragmentTests
{
    // TODO [21-04-3 2:35AM] -- UI test for mood selection highlighting?
    
    // TODO [21-04-19 5:39PM] tag selector tests missing from more context tests below:
    //  - tag editing functionality.

    SleepTrackerTestDriver sleepTracker;
    DatabaseTestDriver database;
    
    @Before
    public void setup()
    {
        sleepTracker = new SleepTrackerTestDriver(
                HiltFragmentTestHelper.launchFragment(SleepTrackerFragment.class));
        database = new DatabaseTestDriver();
    }
    
    @After
    public void teardown()
    {
        sleepTracker = null;
        database = null;
    }
    
    @Test
    public void postSleepDialogDisplaysCorrectValuesWhenDetailsAreUnset()
    {
        // details are unset by default, so you just need to stop a session
        sleepTracker.stopSleepSession(12345); // arbitrary duration
        
        sleepTracker.assertThat.postSleepDialogCommentsAreUnset();
        sleepTracker.assertThat.postSleepDialogMoodIsUnset();
        sleepTracker.assertThat.postSleepDialogTagsAreUnset();
    }
    
    // regression for #14
    @Test
    public void detailsAreClearedWhenSessionEnds()
    {
        // SMELL [21-05-7 1:20AM] -- [big job] In general, my tests have abstraction level
        //  problems - ie they should be way more abstract, clear, and simple - I need a better
        //  "test harness".
        sleepTracker.addNewMood(2);
        List<Integer> tagIndices = sleepTracker.addTags(Arrays.asList("tag1", "tag2"));
        sleepTracker.toggleTagSelections(tagIndices);
        sleepTracker.setAdditionalComments("some comments");
        
        sleepTracker.keepSleepSession(12345); // arbitrary duration
        
        sleepTracker.assertThat.detailsAreCleared();
    }
    
//*********************************************************
// api
//*********************************************************

    @Test
    public void postSleepDialogOpensWithCorrectValues()
    {
        int expectedMoodIndex = 2;
        sleepTracker.addNewMood(2);
        
        List<Integer> expectedSelectedTagIds = sleepTracker.addTags(Arrays.asList("tag1", "tag2"));
        // hard-coded toggle of the 2 tags added above
        sleepTracker.toggleTagSelections(Arrays.asList(0, 1));
        
        String expectedComments = "some comments";
        sleepTracker.setAdditionalComments(expectedComments);
        
        int expectedDuration = 12345;
        sleepTracker.stopSleepSession(expectedDuration);
        
        sleepTracker.assertThat.postSleepDialogHasMood(expectedMoodIndex);
        sleepTracker.assertThat.postSleepDialogHasSelectedTags(expectedSelectedTagIds);
        sleepTracker.assertThat.postSleepDialogHasComments(expectedComments);
        sleepTracker.assertThat.postSleepDialogHasDuration(expectedDuration);
        sleepTracker.assertThat.postSleepDialogRatingIsUnset();
    }
    
    @Test
    public void detailsAreRetainedOnFragmentRestart()
    {
        int expectedMoodIndex = 2;
        sleepTracker.addNewMood(2);
        
        // REFACTOR [21-05-8 1:19AM] -- this could be addAndSelectTags.
        List<Integer> expectedSelectedTagIds = sleepTracker.addTags(Arrays.asList("tag1", "tag2"));
        // hard-coded toggle of the 2 tags added above
        sleepTracker.toggleTagSelections(Arrays.asList(0, 1));
        
        String expectedComments = "It's one banana, Michael. What could it cost, $10?";
        sleepTracker.setAdditionalComments(expectedComments);
        
        sleepTracker.restartFragment();
        
        sleepTracker.assertThat.additionalCommentsMatch(expectedComments);
        sleepTracker.assertThat.selectedMoodMatches(expectedMoodIndex);
        sleepTracker.assertThat.selectedTagsMatch(expectedSelectedTagIds);
    }
    
    // REFACTOR [21-05-8 4:06PM] -- use SleepTrackerDriver here.
    @Test
    public void moreContext_isRetainedOnAppRestart()
    {
        // GIVEN the user has input some additional comments
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);
        
        // REFACTOR [21-04-20 2:52PM] -- this same setup & verification is duplicated across
        //  several of these tests.
        String expectedText = "I don't care for GOB.";
        UITestUtils.typeOnMultilineEditText(expectedText, onView(withId(R.id.additional_comments)));
        SleepTrackerFragmentTestUtils.selectMood(1);
        String tagText = "test";
        SleepTrackerFragmentTestUtils.addTag(tagText);
        SleepTrackerFragmentTestUtils.toggleTagSelection(0);
        
        // WHEN the app is restarted
        scenario = UITestUtils.restartApp(scenario, MainActivity.class);
        
        // THEN the additional comment text is retained
        onView(withId(R.id.additional_comments)).check(matches(withText(expectedText)));
        onView(withId(R.id.mood_selector_mood_value)).check(matches(isDisplayed()));
        onView(withId(R.id.tag_selector_tags_scroll)).check(matches(isDisplayed()));
        onView(withId(R.id.tag_selector_tags_scroll)).check(matches(hasDescendant(withText(tagText))));
    }
    
    // REFACTOR [21-05-8 4:06PM] -- use SleepTrackerDriver here.
    @Test
    public void sleepDurationGoal_displaysProperly()
    {
        // GIVEN there is no sleep duration goal set
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);
        // check that the goal info is not displayed if there is no goal
        onView(withId(R.id.tracker_duration_goal_card)).check(matches(not(isDisplayed())));
        
        // WHEN the user sets a new sleep duration goal
        UITestNavigate.fromHome_toGoals();
        int testHours = 12;
        int testMinutes = 34;
        SleepGoalsFragmentTestUtils.addNewSleepDurationGoal(testHours, testMinutes);
        UITestNavigate.up();
        
        // THEN that goal is displayed on the sleep tracker screen
        onView(allOf(withId(R.id.tracker_goal_value),
                     isDescendantOfA(withId(R.id.tracker_duration_goal_card)))).check(matches(allOf(
                isDisplayed(),
                withText(SleepTrackerFormatting.formatSleepDurationGoal(
                        new SleepDurationGoal(testHours, testMinutes))))));
    }
    
    // REFACTOR [21-05-8 4:06PM] -- use SleepTrackerDriver here.
    @Test
    public void wakeTime_isDisplayedWhenSet()
    {
        // GIVEN the user has set a wake-time goal
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);
        // wake time info view is not displayed when there is no wake time
        onView(withId(R.id.tracker_waketime_goal_card)).check(matches(not(isDisplayed())));
        
        UITestNavigate.fromHome_toGoals();
        SleepGoalsFragmentTestUtils.addNewWakeTime(12, 34);
        
        // WHEN the user is on the sleep tracker screen
        UITestNavigate.up();
        
        // THEN the wake-time goal is displayed
        onView(withId(R.id.tracker_waketime_goal_card)).check(matches(isDisplayed()));
    }
    
    // REFACTOR [21-05-8 4:06PM] -- use SleepTrackerDriver here.
    @Test
    public void sessionStartTime_notDisplayedWhenNotInSession()
    {
        // GIVEN the user is on the sleep tracker screen
        // WHEN there is no current session
        HiltFragmentTestHelper<SleepTrackerFragment> testHelper =
                HiltFragmentTestHelper.launchFragment(SleepTrackerFragment.class);
        
        // THEN the current session start time is not displayed
        onView(withId(R.id.sleep_tracker_start_time)).check(matches(not(isDisplayed())));
        onView(withId(R.id.sleep_tracker_started_text)).check(matches(not(isDisplayed())));
    }
    
    // REFACTOR [21-05-8 4:06PM] -- use SleepTrackerDriver here.
    @Test
    public void sessionStartTime_isDisplayedWhenInSession()
    {
        // GIVEN the user is on the sleep tracker screen
        // AND there is no session
        HiltFragmentTestHelper<SleepTrackerFragment> testHelper =
                HiltFragmentTestHelper.launchFragment(SleepTrackerFragment.class);
        
        // WHEN the user starts a session
        onView(withId(R.id.sleep_tracker_button)).perform(click());
        
        // THEN the current session start time is displayed
        onView(withId(R.id.sleep_tracker_start_time)).check(matches(isDisplayed()));
        // the start time is displayed and isn't an empty string
        onView(withId(R.id.sleep_tracker_started_text)).check(matches(allOf(isDisplayed(),
                                                                            not(withText("")))));
    }
    
    @Test
    public void sleepTrackerButtonTextChangesOnSessionStatus()
    {
        sleepTracker.assertThat.sleepTrackerButtonIsInState(
                SleepTrackerTestDriver.Assertions.TrackerButtonState.NOT_STARTED);
        sleepTracker.startSessionManually();
        sleepTracker.assertThat.sleepTrackerButtonIsInState(
                SleepTrackerTestDriver.Assertions.TrackerButtonState.STARTED);
        sleepTracker.stopAndDiscardSessionManually();
        sleepTracker.assertThat.sleepTrackerButtonIsInState(
                SleepTrackerTestDriver.Assertions.TrackerButtonState.NOT_STARTED);
    }
    
    @Test
    public void keptSleepSessionIsAddedToTheDatabase()
    {
        database.assertThat.sleepSessionCountIs(0);
        sleepTracker.keepSleepSession(12345);
        database.assertThat.sleepSessionCountIs(1);
    }
    
    // REFACTOR [21-05-8 4:06PM] -- use SleepTrackerDriver here.
    @Test
    public void currentSessionTimeDisplay_isZeroWhenNoSession()
    {
        // GIVEN the user is on the sleep tracker screen
        // WHEN there is no current session
        HiltFragmentTestHelper<SleepTrackerFragment> testHelper =
                HiltFragmentTestHelper.launchFragment(SleepTrackerFragment.class);
        
        // THEN the time display is zeroed out
        DurationFormatter durationFormatter = new DurationFormatter();
        onView(withId(R.id.sleep_tracker_session_time))
                .check(matches(withText(durationFormatter.formatDurationMillis(0))));
    }
    
    // REFACTOR [21-05-8 4:06PM] -- use SleepTrackerDriver here.
    // BUG [20-12-13 4:28AM] -- is there an async problem with this test? it passes most of the time
    //  but it has failed 2 times on me now
    //  --
    //  it most recently failed as I was doing a full androidTest run.
    //  afterwards I re-ran this test in isolation and it passed
    @Test
    public void currentSessionTimeDisplay_updatesWhenInSession() throws InterruptedException
    {
        // GIVEN the user is on the sleep tracker screen
        HiltFragmentTestHelper<SleepTrackerFragment> testHelper =
                HiltFragmentTestHelper.launchFragment(SleepTrackerFragment.class);
        
        // WHEN the user is in a session
        onView(withId(R.id.sleep_tracker_button)).perform(click());
        Thread.sleep(1100); // give enough time (>1s) for display to update
        
        // THEN the time display reflects the current session duration
        // (just testing that it is not zero)
        DurationFormatter durationFormatter = new DurationFormatter();
        onView(withId(R.id.sleep_tracker_session_time))
                .check(matches(not(withText(durationFormatter.formatDurationMillis(0)))));
    }
}
