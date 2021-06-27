package com.rbraithwaite.sleepapp.ui_tests.sleep_goals_fragment;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.core.models.SleepDurationGoal;
import com.rbraithwaite.sleepapp.core.models.WakeTimeGoal;
import com.rbraithwaite.sleepapp.test_utils.TestUtils;
import com.rbraithwaite.sleepapp.test_utils.ui.drivers.SleepGoalsTestDriver;
import com.rbraithwaite.sleepapp.test_utils.ui.fragment_helpers.HiltFragmentTestHelper;
import com.rbraithwaite.sleepapp.ui.sleep_goals.SleepGoalsFragment;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

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
// package properties
//*********************************************************

    SleepGoalsTestDriver sleepGoals;
    
//*********************************************************
// api
//*********************************************************

    @Before
    public void setup()
    {
        sleepGoals =
                new SleepGoalsTestDriver(HiltFragmentTestHelper.launchFragment(SleepGoalsFragment.class));
    }
    
    @After
    public void teardown()
    {
        sleepGoals = null;
    }

    // TODO [21-06-24 10:00PM] -- Tests for the goal streak calendar.
    
    @Test
    public void sleepDurationGoalCrudTest()
    {
        sleepGoals.assertThat().sleepDurationGoalIsUnset();
        
        // create
        SleepDurationGoal expectedGoal = TestUtils.ArbitraryData.getSleepDurationGoal();
        sleepGoals.addSleepDurationGoal(expectedGoal);
        sleepGoals.assertThat().sleepDurationGoalMatches(expectedGoal);
        
        // update
        SleepDurationGoal editedGoal = new SleepDurationGoal(expectedGoal.inMinutes() + 10);
        sleepGoals.editSleepDurationGoal(editedGoal);
        sleepGoals.assertThat().sleepDurationGoalMatches(editedGoal);
        
        // delete
        sleepGoals.deleteSleepDurationGoal();
        sleepGoals.assertThat().sleepDurationGoalIsUnset();
    }
    
    @Test
    public void wakeTimeGoalCrudTest()
    {
        sleepGoals.assertThat().wakeTimeGoalIsUnset();
        
        // create
        WakeTimeGoal expectedGoal = TestUtils.ArbitraryData.getWakeTimeGoal();
        sleepGoals.addWakeTimeGoal(expectedGoal);
        sleepGoals.assertThat().wakeTimeGoalMatches(expectedGoal);
        
        // update
        int fiveMinutes = 5 * 60 * 1000;
        WakeTimeGoal editedGoal = new WakeTimeGoal(
                expectedGoal.getEditTime(), expectedGoal.getGoalMillis() + fiveMinutes);
        sleepGoals.editWakeTimeGoal(editedGoal);
        sleepGoals.assertThat().wakeTimeGoalMatches(editedGoal);
        
        // delete
        sleepGoals.deleteWakeTimeGoal();
        sleepGoals.assertThat().wakeTimeGoalIsUnset();
    }
}
