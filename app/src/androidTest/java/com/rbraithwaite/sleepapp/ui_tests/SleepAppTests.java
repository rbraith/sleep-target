package com.rbraithwaite.sleepapp.ui_tests;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.test_utils.ui.UITestNavigate;
import com.rbraithwaite.sleepapp.ui.MainActivity;
import com.rbraithwaite.sleepapp.ui.format.DateTimeFormatter;
import com.rbraithwaite.sleepapp.ui_tests.sleep_goals_fragment.SleepGoalsFragmentTestUtils;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.GregorianCalendar;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;


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
        onView(withId(R.id.session_archive_list_item_card)).perform(click());
        
        GregorianCalendar calendar = new GregorianCalendar(2000, 1, 2, testHour, testMinute);
        onView(withId(R.id.session_data_goal_waketime)).check(matches(withText(
                new DateTimeFormatter().formatTimeOfDay(calendar.getTime()))));
    }
}
