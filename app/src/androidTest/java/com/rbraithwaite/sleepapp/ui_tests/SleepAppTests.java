package com.rbraithwaite.sleepapp.ui_tests;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.data.current_goals.SleepDurationGoalModel;
import com.rbraithwaite.sleepapp.test_utils.ui.UITestNavigate;
import com.rbraithwaite.sleepapp.test_utils.ui.UITestUtils;
import com.rbraithwaite.sleepapp.test_utils.ui.dialog.DialogTestUtils;
import com.rbraithwaite.sleepapp.ui.MainActivity;
import com.rbraithwaite.sleepapp.ui.format.DateTimeFormatter;
import com.rbraithwaite.sleepapp.ui.session_data.SessionDataFormatting;
import com.rbraithwaite.sleepapp.ui_tests.session_data_fragment.SessionDataFragmentTestUtils;
import com.rbraithwaite.sleepapp.ui_tests.sleep_goals_fragment.SleepGoalsFragmentTestUtils;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;



/**
 * These are instrumented tests which exercise multiple components of the app in a single test.
 */
@RunWith(AndroidJUnit4.class)
public class SleepAppTests
{
//*********************************************************
// api
//*********************************************************

    @Test
    public void sessionArchiveDataSleepDurationGoal_displaysProperly()
    {
        // Record a session without a sleep duration goal
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);
        onView(withId(R.id.sleep_tracker_button)).perform(click());
        onView(withId(R.id.sleep_tracker_button)).perform(click());
        
        // Check that session's displayed goal
        UITestNavigate.fromHome_toSessionArchive();
        onView(withId(R.id.session_archive_list_item_card)).perform(click());
        onView(withId(R.id.session_data_duration_layout)).check(matches(not(isDisplayed())));
        
        // Delete that session and record a new session *with* a sleep duration goal
        SessionDataFragmentTestUtils.pressNegative();
        DialogTestUtils.pressOK();
        
        UITestNavigate.up();
        UITestUtils.waitForSnackbarToFinish();
        UITestNavigate.fromHome_toGoals();
        
        int testHours = 12;
        int testMinutes = 34;
        SleepGoalsFragmentTestUtils.addNewSleepDurationGoal(testHours, testMinutes);
        
        UITestNavigate.up();
        
        onView(withId(R.id.sleep_tracker_button)).perform(click());
        onView(withId(R.id.sleep_tracker_button)).perform(click());
        
        // Check that session's displayed goal
        UITestNavigate.fromHome_toSessionArchive();
        onView(withId(R.id.session_archive_list_item_card)).perform(click());
        onView(withId(R.id.session_data_duration_layout)).check(matches(isDisplayed()));
        onView(allOf(
                withId(R.id.session_data_duration_value),
                withParent(withId(R.id.session_data_duration_layout)))).check(matches(withText(
                SessionDataFormatting.formatSleepDurationGoal(new SleepDurationGoalModel(testHours,
                                                                                         testMinutes)))));
    }
    
    @Test
    public void addSleepDurationGoalDefault_isCurrentGoal()
    {
        // GIVEN the user has a sleep duration goal set
        int testHours = 12;
        int testMinutes = 34;
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);
        UITestNavigate.fromHome_toGoals();
        SleepGoalsFragmentTestUtils.addNewSleepDurationGoal(testHours, testMinutes);
        
        // WHEN the user is on the add session screen
        UITestNavigate.up();
        UITestNavigate.fromHome_toAddSession();
        
        // THEN the default sleep duration goal matches the current goal
        onView(withId(R.id.session_data_duration_value)).check(matches(withText(
                SessionDataFormatting.formatSleepDurationGoal(
                        new SleepDurationGoalModel(testHours, testMinutes)))));
    }
    
    @Test
    public void addSessionDefaultWakeTime_isCurrentWakeTime()
    {
        int testHour = 12;
        int testMinute = 34;
        
        // GIVEN the user has a wake-time goal set
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);
        UITestNavigate.fromHome_toGoals();
        SleepGoalsFragmentTestUtils.addNewWakeTime(testHour, testMinute);
        
        // WHEN the user is on the add session screen
        UITestNavigate.up();
        UITestNavigate.fromHome_toAddSession();
        
        // THEN the default wake-time goal is the current wake-time goal
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.set(Calendar.HOUR_OF_DAY, testHour);
        calendar.set(Calendar.MINUTE, testMinute);
        onView(withId(R.id.session_data_goal_waketime)).check(matches(withText(
                // REFACTOR [21-01-15 8:54PM] -- this should be using the
                //  SessionDataDateTimeFormatter dependency somehow.
                new DateTimeFormatter().formatTimeOfDay(calendar.getTime()))));
        // AND the "add wake-time goal" button is not displayed
        onView(withId(R.id.session_data_add_waketime_btn)).check(matches(not(isDisplayed())));
    }
    
    @Test
    public void wakeTime_isRecordedToArchive()
    {
        int testHour = 12;
        int testMinute = 34;
        
        // GIVEN the user has a wake-time goal set
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);
        UITestNavigate.fromHome_toGoals();
        SleepGoalsFragmentTestUtils.addNewWakeTime(testHour, testMinute);
        
        // AND they are recording a sleep session
        UITestNavigate.up();
        onView(withId(R.id.sleep_tracker_button)).perform(click());
        
        // WHEN the user completes the session
        onView(withId(R.id.sleep_tracker_button)).perform(click());
        
        // THEN the wake-time goal information for that session is visible from the session archive
        UITestNavigate.fromHome_toSessionArchive();
        // first check the list item card
        onView(withId(R.id.session_archive_list_item_waketime)).check(matches(isDisplayed()));
        // then check the full session info screen
        onView(withId(R.id.session_archive_list_item_card)).perform(click());
        GregorianCalendar calendar = new GregorianCalendar(2000, 1, 2, testHour, testMinute);
        onView(withId(R.id.session_data_goal_waketime)).check(matches(withText(
                new DateTimeFormatter().formatTimeOfDay(calendar.getTime()))));
    }
    
    @Test
    public void sessionArchiveListItemWakeTimeIcon_isNotDisplayedIfNoWakeTime()
    {
        // GIVEN the user has recorded a session to the archive without a wake-time goal
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);
        onView(withId(R.id.sleep_tracker_button)).perform(click());
        onView(withId(R.id.sleep_tracker_button)).perform(click());
        
        // WHEN the user is on the session archive screen
        UITestNavigate.fromHome_toSessionArchive();
        
        // THEN the list item for that session does not display a wake-time goal icon
        onView(withId(R.id.session_archive_list_item_waketime)).check(matches(not(isDisplayed())));
    }
}
