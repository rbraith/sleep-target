package com.rbraithwaite.sleepapp.ui_tests.sleep_goals_fragment;

import androidx.test.espresso.contrib.PickerActions;

import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.test_utils.ui.HiltFragmentTestHelper;
import com.rbraithwaite.sleepapp.test_utils.ui.dialog.DialogTestUtils;
import com.rbraithwaite.sleepapp.test_utils.ui.dialog.DurationPickerTestUtils;
import com.rbraithwaite.sleepapp.ui.sleep_goals.SleepGoalsFragment;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static com.rbraithwaite.sleepapp.test_utils.ui.UITestUtils.onTimePicker;

public class SleepGoalsFragmentTestUtils
{
//*********************************************************
// constructors
//*********************************************************

    private SleepGoalsFragmentTestUtils() {/* No instantiation */}

//*********************************************************
// api
//*********************************************************

    public static HiltFragmentTestHelper<SleepGoalsFragment> launchWithSleepDurationGoal(
            int hours,
            int minutes)
    {
        HiltFragmentTestHelper<SleepGoalsFragment> helper =
                HiltFragmentTestHelper.launchFragment(SleepGoalsFragment.class);
        SleepGoalsFragmentTestUtils.addNewSleepDurationGoal(hours, minutes);
        return helper;
    }
    
    public static HiltFragmentTestHelper<SleepGoalsFragment> launchWithWakeTimeGoal(
            int hourOfDay,
            int minute)
    {
        HiltFragmentTestHelper<SleepGoalsFragment> helper =
                HiltFragmentTestHelper.launchFragment(SleepGoalsFragment.class);
        SleepGoalsFragmentTestUtils.addNewWakeTime(hourOfDay, minute);
        return helper;
    }
    
    public static void addNewSleepDurationGoal(int hours, int minutes)
    {
        onView(withId(R.id.sleep_goals_new_duration_btn)).perform(click());
        DurationPickerTestUtils.setDuration(hours, minutes);
        DialogTestUtils.pressOK();
    }
    
    
    public static void addNewWakeTime(int hour, int minute)
    {
        onView(withId(R.id.sleep_goals_new_waketime_btn)).perform(click());
        onTimePicker().perform(PickerActions.setTime(hour, minute));
        DialogTestUtils.pressOK();
    }
}
