package com.rbraithwaite.sleepapp.ui_tests.sleep_goals_fragment;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.contrib.PickerActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.TestUtils;
import com.rbraithwaite.sleepapp.test_utils.ui.HiltFragmentTestHelper;
import com.rbraithwaite.sleepapp.test_utils.ui.UITestNavigate;
import com.rbraithwaite.sleepapp.test_utils.ui.UITestUtils;
import com.rbraithwaite.sleepapp.test_utils.ui.dialog.DialogTestUtils;
import com.rbraithwaite.sleepapp.ui.MainActivity;
import com.rbraithwaite.sleepapp.ui.format.DateTimeFormatter;
import com.rbraithwaite.sleepapp.ui.sleep_goals.SleepGoalsFragment;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.rbraithwaite.sleepapp.test_utils.ui.EspressoMatchers.timePickerWithTime;
import static com.rbraithwaite.sleepapp.test_utils.ui.UITestUtils.onTimePicker;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
public class SleepGoalsFragmentTests
{
//*********************************************************
// public properties
//*********************************************************

    @Rule
    // protection against potentially infinitely blocked threads
    public Timeout timeout = new Timeout(10, TimeUnit.SECONDS);

//*********************************************************
// api
//*********************************************************

    @Test
    public void addNewSleepDurationButton_isDisplayedIfNoSleepDuration()
    {
        // GIVEN the user is on the sleep goals screen
        // WHEN there is no current sleep duration goal
        HiltFragmentTestHelper<SleepGoalsFragment> helper =
                HiltFragmentTestHelper.launchFragment(SleepGoalsFragment.class);
        
        // THEN the button for adding a new sleep duration goal is displayed
        onView(withId(R.id.sleep_goals_new_duration_btn)).check(matches(isDisplayed()));
        // AND the sleep duration goal info view is not displayed
        onView(withId(R.id.sleep_goals_duration)).check(matches(not(isDisplayed())));
    }
    
    @Test
    public void deleteWakeTime_deletesWakeTime()
    {
        // REFACTOR [21-01-18 4:47PM] -- call this SleepGoalsFragmentTestUtils.launchWithWakeTime
        //  (hour, min)
        // GIVEN the user is on the sleep goals screen
        HiltFragmentTestHelper<SleepGoalsFragment> helper =
                HiltFragmentTestHelper.launchFragment(SleepGoalsFragment.class);
        // AND the user has set a wake-time goal
        int testHourOfDay = 12;
        int testMinute = 34;
        SleepGoalsFragmentTestUtils.addNewWakeTime(testHourOfDay, testMinute);
        
        // WHEN they delete that goal (confirming the dialog)
        onView(withId(R.id.waketime_delete_btn)).perform(click());
        DialogTestUtils.pressOK();
        
        // THEN that goal is deleted
        // AND the "add new wake-time" button is displayed again
        onView(withId(R.id.sleep_goals_waketime)).check(matches(not(isDisplayed())));
        onView(withId(R.id.sleep_goals_new_waketime_btn)).check(matches(isDisplayed()));
    }
    
    @Test
    public void editWakeTime_editsWakeTime()
    {
        // GIVEN the user is on the sleep goals screen
        HiltFragmentTestHelper<SleepGoalsFragment> helper =
                HiltFragmentTestHelper.launchFragment(SleepGoalsFragment.class);
        // AND the user has set a wake-time goal
        int testHourOfDay = 12;
        int testMinute = 34;
        SleepGoalsFragmentTestUtils.addNewWakeTime(testHourOfDay, testMinute);
        
        // WHEN the user edits that goal
        onView(withId(R.id.waketime_edit_btn)).perform(click());
        
        // THEN the time picker displays with the correct values (the current wake-time goal)
        onTimePicker().check(matches(timePickerWithTime(testHourOfDay, testMinute)));
        // AND the current wake-time goal is edited on positive confirmation of the dialog
        int expectedHourOfDay = 21;
        int expectedMinute = 43;
        onTimePicker().perform(PickerActions.setTime(expectedHourOfDay, expectedMinute));
        DialogTestUtils.pressOK();
        
        GregorianCalendar calendar = TestUtils.ArbitraryData.getCalendar();
        calendar.set(Calendar.HOUR_OF_DAY, expectedHourOfDay);
        calendar.set(Calendar.MINUTE, expectedMinute);
        onView(withId(R.id.waketime_value)).check(matches(withText(
                // REFACTOR [21-01-17 5:10PM] -- this should be SleepGoalFragment's
                //  DateTimeFormatter.
                new DateTimeFormatter().formatTimeOfDay(calendar.getTime()))));
    }
    
    @Test
    public void addNewWakeTime_opensTimePickerWithCorrectValues()
    {
        // GIVEN the user is on the sleep goals screen
        HiltFragmentTestHelper<SleepGoalsFragment> helper =
                HiltFragmentTestHelper.launchFragment(SleepGoalsFragment.class);
        
        // WHEN the user clicks the "add new wake-time goal" btn
        onView(withId(R.id.sleep_goals_new_waketime_btn)).perform(click());
        
        // THEN a time picker dialog is displayed with the correct values
        onTimePicker().check(matches(isDisplayed()));
        onTimePicker().check(matches(timePickerWithTime(8, 0)));
    }
    
    @Test
    public void addNewWakeTime_addsNewWakeTime()
    {
        // GIVEN the user is on the sleep goals screen
        HiltFragmentTestHelper<SleepGoalsFragment> helper =
                HiltFragmentTestHelper.launchFragment(SleepGoalsFragment.class);
        
        // WHEN the user adds a new wake-time
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 34);
        SleepGoalsFragmentTestUtils.addNewWakeTime(calendar.get(Calendar.HOUR_OF_DAY),
                                                   calendar.get(Calendar.MINUTE));
        
        // THEN the newly added wake-time is displayed to the user
        onView(withId(R.id.sleep_goals_waketime)).check(matches(isDisplayed()));
        onView(withId(R.id.waketime_value)).check(matches(withText(new DateTimeFormatter().formatTimeOfDay(
                calendar.getTime()))));
    }
    
    @Test
    public void newWakeTime_isRetainedOnFragmentRestart()
    {
        // GIVEN the user has set a new wake time goal
        ActivityScenario<MainActivity> mainActivityScenario =
                ActivityScenario.launch(MainActivity.class);
        
        UITestNavigate.fromHome_toGoals();
        
        SleepGoalsFragmentTestUtils.addNewWakeTime(12, 34);
        
        // WHEN the fragment is restarted (eg via user navigation)
        UITestNavigate.up();
        UITestNavigate.fromHome_toGoals();
        
        // THEN the new wake time display is retained
        onView(withId(R.id.sleep_goals_waketime)).check(matches(isDisplayed()));
    }
    
    // regression test for #50
    @Test
    public void newWakeTime_isRetainedOnAppRestart()
    {
        // GIVEN the user has set a new wake time goal
        ActivityScenario<MainActivity> mainActivityScenario =
                ActivityScenario.launch(MainActivity.class);
        UITestNavigate.fromHome_toGoals();
        SleepGoalsFragmentTestUtils.addNewWakeTime(12, 34);
        
        // WHEN the app is restarted
        mainActivityScenario = UITestUtils.restartApp(mainActivityScenario, MainActivity.class);
        
        // THEN the previously set wake time goal is still displayed
        UITestNavigate.fromHome_toGoals();
        onView(withId(R.id.sleep_goals_waketime)).check(matches(isDisplayed()));
    }
}
