package com.rbraithwaite.sleepapp.ui_tests.sleep_goals_fragment;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.test_utils.ui.HiltFragmentTestHelper;
import com.rbraithwaite.sleepapp.test_utils.ui.UITestNavigate;
import com.rbraithwaite.sleepapp.test_utils.ui.UITestUtils;
import com.rbraithwaite.sleepapp.ui.MainActivity;
import com.rbraithwaite.sleepapp.ui.format.DateTimeFormatter;
import com.rbraithwaite.sleepapp.ui.sleep_goals.SleepGoalsFragment;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.rbraithwaite.sleepapp.test_utils.ui.EspressoMatchers.timePickerWithTime;
import static com.rbraithwaite.sleepapp.test_utils.ui.UITestUtils.onTimePicker;

@RunWith(AndroidJUnit4.class)
public class SleepGoalsFragmentTests
{
//*********************************************************
// api
//*********************************************************

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
    
    // TODO [20-12-21 10:17PM] -- add new waketime btn adds new waketime (full process test).
}
