/*
 * Copyright (c) 2020-2021 Richard Braithwaite (rbraithwaite.dev@gmail.com)
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

import androidx.test.espresso.contrib.PickerActions;

import com.rbraithwaite.sleeptarget.R;
import com.rbraithwaite.sleeptarget.core.models.SleepDurationGoal;
import com.rbraithwaite.sleeptarget.core.models.WakeTimeGoal;
import com.rbraithwaite.sleeptarget.test_utils.ui.dialog.DialogTestUtils;
import com.rbraithwaite.sleeptarget.test_utils.ui.dialog.DurationPickerTestUtils;
import com.rbraithwaite.sleeptarget.test_utils.ui.fragment_helpers.FragmentTestHelper;
import com.rbraithwaite.sleeptarget.ui.sleep_goals.SleepGoalsFormatting;
import com.rbraithwaite.sleeptarget.ui.sleep_goals.SleepGoalsFragment;
import com.rbraithwaite.sleeptarget.ui.sleep_goals.SleepGoalsFragmentViewModel;
import com.rbraithwaite.sleeptarget.utils.time.TimeOfDay;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.rbraithwaite.sleeptarget.test_utils.ui.UITestUtils.onTimePicker;
import static org.hamcrest.Matchers.not;

public class SleepGoalsTestDriver
        extends BaseFragmentTestDriver<SleepGoalsFragment, SleepGoalsTestDriver.Assertions>
{
//*********************************************************
// public helpers
//*********************************************************

    public static class Assertions
            extends BaseFragmentTestDriver.BaseAssertions<SleepGoalsTestDriver,
            SleepGoalsFragmentViewModel>
    {
        public Assertions(SleepGoalsTestDriver owningDriver)
        {
            super(owningDriver);
        }
        
        public void sleepDurationGoalIsUnset()
        {
            onView(withId(R.id.sleep_goals_duration)).check(matches(not(isDisplayed())));
            onView(withId(R.id.sleep_goals_new_duration_btn)).check(matches(isDisplayed()));
            
            // TODO [21-06-24 9:21PM] -- verify the view model as well?
        }
        
        public void wakeTimeGoalIsUnset()
        {
            onView(withId(R.id.sleep_goals_waketime)).check(matches(not(isDisplayed())));
            onView(withId(R.id.sleep_goals_new_waketime_btn)).check(matches(isDisplayed()));
            
            // TODO [21-06-24 9:21PM] -- verify the view model as well?
        }
        
        public void sleepDurationGoalMatches(SleepDurationGoal expectedGoal)
        {
            onView(withId(R.id.sleep_goals_new_duration_btn)).check(matches(not(isDisplayed())));
            onView(withId(R.id.sleep_goals_duration)).check(matches(isDisplayed()));
            onView(withId(R.id.duration_value)).check(matches(withText(
                    SleepGoalsFormatting.formatSleepDurationGoal(expectedGoal))));
        }
        
        public void wakeTimeGoalMatches(WakeTimeGoal expectedGoal)
        {
            onView(withId(R.id.sleep_goals_new_waketime_btn)).check(matches(not(isDisplayed())));
            onView(withId(R.id.sleep_goals_waketime)).check(matches(isDisplayed()));
            onView(withId(R.id.waketime_value)).check(matches(withText(
                    SleepGoalsFormatting.formatWakeTimeGoal(expectedGoal))));
        }
    }
    
//*********************************************************
// constructors
//*********************************************************

    public SleepGoalsTestDriver(FragmentTestHelper<SleepGoalsFragment> helper)
    {
        init(helper, new Assertions(this));
    }
    
//*********************************************************
// api
//*********************************************************

    public void addSleepDurationGoal(SleepDurationGoal sleepDurationGoal)
    {
        onView(withId(R.id.sleep_goals_new_duration_btn)).perform(click());
        setSleepDurationGoalInPicker(sleepDurationGoal);
    }
    
    public void deleteSleepDurationGoal()
    {
        onView(withId(R.id.duration_delete_btn)).perform(click());
        DialogTestUtils.pressPositiveButton();
    }
    
    public void addWakeTimeGoal(WakeTimeGoal expectedGoal)
    {
        onView(withId(R.id.sleep_goals_new_waketime_btn)).perform(click());
        setWakeTimeGoalInPicker(expectedGoal);
    }
    
    public void editWakeTimeGoal(WakeTimeGoal newGoal)
    {
        onView(withId(R.id.waketime_edit_btn)).perform(click());
        setWakeTimeGoalInPicker(newGoal);
    }
    
    public void deleteWakeTimeGoal()
    {
        onView(withId(R.id.waketime_delete_btn)).perform(click());
        DialogTestUtils.pressPositiveButton();
    }
    
    public void editSleepDurationGoal(SleepDurationGoal newGoal)
    {
        onView(withId(R.id.duration_edit_btn)).perform(click());
        setSleepDurationGoalInPicker(newGoal);
    }
    
//*********************************************************
// private methods
//*********************************************************

    private void setWakeTimeGoalInPicker(WakeTimeGoal wakeTimeGoal)
    {
        TimeOfDay timeOfDay = wakeTimeGoal.asTimeOfDay();
        onTimePicker().perform(PickerActions.setTime(timeOfDay.hourOfDay, timeOfDay.minute));
        DialogTestUtils.pressPositiveButton();
    }
    
    private void setSleepDurationGoalInPicker(SleepDurationGoal sleepDurationGoal)
    {
        DurationPickerTestUtils.setDuration(sleepDurationGoal.getHours(),
                                            sleepDurationGoal.getRemainingMinutes());
        DialogTestUtils.pressPositiveButton();
    }
}
