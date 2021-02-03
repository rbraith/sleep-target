package com.rbraithwaite.sleepapp.ui_tests;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.data.current_goals.SleepDurationGoalModel;
import com.rbraithwaite.sleepapp.test_utils.ui.HiltFragmentTestHelper;
import com.rbraithwaite.sleepapp.test_utils.ui.UITestNavigate;
import com.rbraithwaite.sleepapp.test_utils.ui.UITestUtils;
import com.rbraithwaite.sleepapp.ui.MainActivity;
import com.rbraithwaite.sleepapp.ui.format.DurationFormatter;
import com.rbraithwaite.sleepapp.ui.sleep_tracker.SleepTrackerFormatting;
import com.rbraithwaite.sleepapp.ui.sleep_tracker.SleepTrackerFragment;
import com.rbraithwaite.sleepapp.ui_tests.sleep_goals_fragment.SleepGoalsFragmentTestUtils;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutionException;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
public class SleepTrackerFragmentTests
{
//*********************************************************
// api
//*********************************************************

    @Test
    public void sleepDurationGoal_displaysProperly()
    {
        // GIVEN there is no sleep duration goal set
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);
        // check that the goal info is not displayed if there is no goal
        onView(withId(R.id.sleep_tracker_duration_goal_title)).check(matches(not(isDisplayed())));
        onView(withId(R.id.sleep_tracker_duration_goal_value)).check(matches(not(isDisplayed())));
        
        // WHEN the user sets a new sleep duration goal
        UITestNavigate.fromHome_toGoals();
        int testHours = 12;
        int testMinutes = 34;
        SleepGoalsFragmentTestUtils.addNewSleepDurationGoal(testHours, testMinutes);
        UITestNavigate.up();
        
        // THEN that goal is displayed on the sleep tracker screen
        onView(withId(R.id.sleep_tracker_duration_goal_title)).check(matches(isDisplayed()));
        onView(withId(R.id.sleep_tracker_duration_goal_value)).check(matches(allOf(
                isDisplayed(),
                withText(SleepTrackerFormatting.formatSleepDurationGoal(
                        new SleepDurationGoalModel(testHours, testMinutes))))));
    }
    
    @Test
    public void wakeTime_isDisplayedWhenSet()
    {
        // GIVEN the user has set a wake-time goal
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);
        // wake time info view is not displayed when there is no wake time
        onView(withId(R.id.sleep_tracker_waketime_goal_title)).check(matches(not(isDisplayed())));
        
        UITestNavigate.fromHome_toGoals();
        SleepGoalsFragmentTestUtils.addNewWakeTime(12, 34);
        
        // WHEN the user is on the sleep tracker screen
        UITestNavigate.up();
        
        // THEN the wake-time goal is displayed
        onView(withId(R.id.sleep_tracker_waketime_goal_title)).check(matches(isDisplayed()));
        onView(withId(R.id.sleep_tracker_waketime_goal_value)).check(matches(isDisplayed()));
    }
    
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
        // GIVEN the user is on the sleep tracker screen
        HiltFragmentTestHelper<SleepTrackerFragment> testHelper =
                HiltFragmentTestHelper.launchFragment(SleepTrackerFragment.class);
        
        // AND there is no current session
        onView(withId(R.id.sleep_tracker_button)).check(matches(withText(R.string.sleep_tracker_button_start)));
        
        // WHEN the user presses the sleep tracking button
        onView(withId(R.id.sleep_tracker_button)).perform(click());
        
        // THEN the text changes to indicate there is a session in progress
        onView(withId(R.id.sleep_tracker_button)).check(matches(withText(R.string.sleep_tracker_button_stop)));
        
        //-------------------------------------------------
        
        // GIVEN there is a session in progress
        // WHEN the user eventually presses the button again to stop the session
        onView(withId(R.id.sleep_tracker_button)).perform(click());
        
        // THEN the text returns to its original state
        onView(withId(R.id.sleep_tracker_button)).check(matches(withText(R.string.sleep_tracker_button_start)));
    }
    
    @Test
    public void addSessionToArchiveWithSleepTrackerButton() throws
            InterruptedException,
            ExecutionException
    {
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);
        
        // first note the current sleep sessions in the archive
        UITestNavigate.fromHome_toSessionArchive();
        int initialCount = UITestUtils.getSessionArchiveCount(scenario);
        
        // GIVEN the user records a sleep session with the sleep tracker button
        onView(withContentDescription(R.string.nav_app_bar_navigate_up_description)).perform(click()); // return to sleep tracker screen
        onView(withId(R.id.sleep_tracker_button)).check(matches(withText(R.string.sleep_tracker_button_start))); // confirm that a session is not in progress
        // record the session
        onView(withId(R.id.sleep_tracker_button)).perform(click());
        Thread.sleep(500);
        onView(withId(R.id.sleep_tracker_button)).perform(click());
        
        // WHEN the user navigates to the sleep archive screen
        UITestNavigate.fromHome_toSessionArchive();
        
        // THEN the user should see the archive updated with that new session
        int updatedCount = UITestUtils.getSessionArchiveCount(scenario);
        assertThat(updatedCount, is(greaterThan(initialCount)));
    }
    
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
